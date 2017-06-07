package org.openhab.binding.theweathercompany.internal;

import static org.openhab.binding.theweathercompany.TheWeatherCompanyBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.net.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.smarthome.config.core.Configuration;
import org.openhab.binding.theweathercompany.exception.TWCReSTException;
import org.openhab.binding.theweathercompany.internal.datatypes.DailyFormat;
import org.openhab.binding.theweathercompany.internal.datatypes.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The Weather Company API Client
 *
 * @version 1.0.0
 * @author Peter Kristensen
 */

public class TWCAPIClient {

    /*
     * Logger for this class
     */
    private static Logger logger = LoggerFactory.getLogger(TWCAPIClient.class);

    private String authKey = "da12fe5d-13fa-4f51-94d3-baf454a0468a";
    private String authToken = "pEDAFCPKMu";
    private SSLContext sslContext = null;

    private String baseUrl = null;

    private BigDecimal latitude = null;
    private BigDecimal longitude = null;

    private String locationId = null;

    private String language = null;

    private Units units = Units.METRIC;

    private boolean isSecured = true;

    public TWCAPIClient(Configuration config) throws NoSuchAlgorithmException, KeyManagementException {
        super();

        this.baseUrl = "https://" + trimedValue((String) config.get(PARAM_HOSTNAME)) + "/api/weather";

        authKey = trimedValue((String) config.get(PARAM_AUTHKEY));
        authToken = trimedValue((String) config.get(PARAM_AUTHTOKEN));

        try {
            latitude = (BigDecimal) config.get(PARAM_LATITUDE);
            longitude = (BigDecimal) config.get(PARAM_LONGITUDE);
        } catch (Exception e) {
            logger.debug("Cannot set latitude or longitude.", e);
        }

        this.isSecured = this.IsSecuredConnection(config);

        TrustManager[] trustAllCerts = null;
        boolean trustAll = false;
        /*
         * String value = opt.getProperty("Trust-All-Certificates");
         * if (value != null) {
         * trustAll = Boolean.parseBoolean(trimedValue(value));
         * }
         */
        if (trustAll) {
            trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            } };
        }

        sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, trustAllCerts, null);
    }

    protected boolean IsSecuredConnection(Configuration config) {
        boolean type = true;
        String id;
        id = (String) config.get(PARAM_SECURE);
        if (id != null) {
            type = trimedValue(id).equalsIgnoreCase("true");
        }
        return type;
    }

    private static String trimedValue(String value) {
        if (value != null) {
            return value.trim();
        }
        return value;
    }

    private HttpResponse connect(String httpOperation, String url, String jsonPacket,
            List<NameValuePair> queryParameters) throws URISyntaxException, IOException {
        final String METHOD = "connect";

        StringEntity input = null;
        if (jsonPacket != null) {
            input = new StringEntity(jsonPacket, StandardCharsets.UTF_8);
        }

        String encodedString = new String(Base64.encodeBase64(new String(authKey + ":" + authToken).getBytes()));

        switch (httpOperation) {
            case "get":
                return caseGetFromConnect(queryParameters, url, METHOD, input, encodedString);
            default:
                break;
        }
        return null;

    }

    private HttpResponse caseGetFromConnect(List<NameValuePair> queryParameters, String url, String method,
            StringEntity input, String encodedString) throws URISyntaxException, IOException {

        URIBuilder getBuilder = new URIBuilder(url);
        if (queryParameters != null) {
            getBuilder.setParameters(queryParameters);
        }
        HttpGet get = new HttpGet(getBuilder.build());
        get.addHeader("Content-Type", "application/json");
        get.addHeader("Accept", "application/json");
        get.addHeader("Authorization", "Basic " + encodedString);

        try {
            HttpClient client = HttpClientBuilder.create().useSystemProperties().setSslcontext(sslContext).build();
            return client.execute(get);
        } catch (IOException e) {
            logger.warn("{} {}", method, e.getMessage());
            throw e;
        }

    }

    private String readContent(HttpResponse response, String method) throws IllegalStateException, IOException {

        BufferedReader br = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
        String line = null;
        try {
            line = br.readLine();
        } catch (IOException e) {
            logger.warn("{} {}", method, e.getMessage());
            throw e;
        }
        logger.debug("{} {}", method, line);
        try {
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return line;
    }

    private void throwExecption(int code, String url, String method, String request, JsonElement jsonResponse)
            throws TWCReSTException {
        String reason = null;
        switch (code) {
            case 400:
                reason = TWCReSTException.HTTP_TWC_ERR_400;
                break;
            case 401:
                reason = TWCReSTException.HTTP_TWC_ERR_401;
                break;
            case 403:
                reason = TWCReSTException.HTTP_TWC_ERR_403;
                break;
            case 404:
                reason = TWCReSTException.HTTP_TWC_ERR_404;
                break;
            case 500:
                reason = TWCReSTException.HTTP_TWC_ERR_500;
                break;
            default:
                reason = "Unexpected error";
                break;
        }
        throw new TWCReSTException(method, url, request, code, reason, jsonResponse);
    }

    private JsonObject getWeatherData(String url, String METHOD) throws TWCReSTException {
        JsonElement jsonResponse = null;

        String method = "get";
        int code = 0;
        HttpResponse response = null;

        try {
            response = connect(method, url.toString(), null, null);
            code = response.getStatusLine().getStatusCode();
            String result = this.readContent(response, METHOD);
            jsonResponse = new JsonParser().parse(result);
            if (code == 200) {
                // Success
                return jsonResponse.getAsJsonObject();
            } else {
                throwExecption(code, url.toString(), method, latitude + "," + longitude, jsonResponse);
            }
        } catch (TWCReSTException e) {
            throw e;
        } catch (Exception e) {
            // This includes JsonSyntaxException
            TWCReSTException ex = new TWCReSTException(
                    "Failure in getting location service point " + "::" + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
        return null;
    }

    public JsonObject getDailyForecast(DailyFormat dailyFormat) throws TWCReSTException {
        final String METHOD = "getDailyForecast";

        StringBuilder url = new StringBuilder(baseUrl).append(getLocation()).append("/forecast/")
                .append(dailyFormat.get()).append(".json");

        url.append("?language=" + getLanguage());

        // If units are set, append it
        if (units != null) {
            url.append("&units=" + units.get());
        }

        return getWeatherData(url.toString(), METHOD);
    }

    public JsonObject getHourlyForecast() throws TWCReSTException {
        final String METHOD = "getHourlyForecast";

        StringBuilder url = new StringBuilder(baseUrl).append(getLocation()).append("/forecast/hourly/48hour.json");

        url.append("?language=" + getLanguage());

        // If units are set, append it
        if (units != null) {
            url.append("&units=" + units.get());
        }

        return getWeatherData(url.toString(), METHOD);
    }

    public JsonObject getIntradayForecast(DailyFormat dailyFormat) throws TWCReSTException {
        final String METHOD = "getIntradayForecast";

        StringBuilder url = new StringBuilder(baseUrl).append(getLocation()).append("/forecast/intraday/")
                .append(dailyFormat.get()).append(".json");

        url.append("?language=" + getLanguage());

        // If units are set, append it
        if (units != null) {
            url.append("&units=" + units.get());
        }

        return getWeatherData(url.toString(), METHOD);
    }

    public JsonObject getLocationServicesPoint(BigDecimal latitude, BigDecimal longitude) throws TWCReSTException {
        final String METHOD = "getLocationServicesPoint";

        StringBuilder url = new StringBuilder(baseUrl).append("/v3/location/point");

        url.append("?geocode=" + latitude + "," + longitude);
        url.append("&language=" + getLanguage());

        return getWeatherData(url.toString(), METHOD);
    }

    public JsonObject getCurrentConditions() throws TWCReSTException {
        final String METHOD = "getCurrentConditions";

        StringBuilder url = new StringBuilder(baseUrl).append(getLocation()).append("/observations.json");

        url.append("?language=" + getLanguage());

        // If units are set, append it
        if (units != null) {
            url.append("&units=" + units.get());
        }

        return getWeatherData(url.toString(), METHOD);
    }

    public JsonObject getHistoricalData(int hours) throws TWCReSTException {
        final String METHOD = "getHistoricalData";

        StringBuilder url = new StringBuilder(baseUrl).append(getLocation()).append("/observations/timeseries.json");

        url.append("?hours=" + hours);

        // If language is set, append it
        if (language != null) {
            url.append("&language=" + language);
        }

        // If units are set, append it
        if (units != null) {
            url.append("&units=" + units.get());
        }

        return getWeatherData(url.toString(), METHOD);
    }

    public Units getUnits() {
        return units;
    }

    public void setUnits(Units units) {
        this.units = units;
    }

    private String getLanguage() {
        if (language != null) {
            return language;
        } else {
            return "en-US";
        }
    }

    /**
     * Returns the location either as 'location' or 'geocode' depending on what is configured
     *
     * @return LOCATION URL
     */
    private String getLocation() {
        if (locationId == null) {
            return "/v1/geocode/" + latitude + "/" + longitude;
        } else {
            return "/v1/location/" + locationId;
        }
    }

    public void setLocation(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationId = null;
    }

    public void setLocation(String locationId) {
        this.locationId = locationId;
        this.latitude = null;
        this.longitude = null;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}