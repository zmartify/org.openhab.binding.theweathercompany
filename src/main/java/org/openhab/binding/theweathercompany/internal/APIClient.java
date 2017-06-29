package org.openhab.binding.theweathercompany.internal;

import static org.openhab.binding.theweathercompany.TheWeatherCompanyBindingConstants.*;

import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
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
 * @author Zmartify, Peter Kristensen
 */

public class APIClient {

    /*
     * Logger for this class
     */
    private Logger logger = LoggerFactory.getLogger(APIClient.class);

    // Time out for weather data api request (in milliseconds)
    private final static int TIMEOUT = 5 * 1000;

    private String authKey = "da12fe5d-13fa-4f51-94d3-baf454a0468a";
    private String authToken = "pEDAFCPKMu";

    private String basicAuthentication = null;

    private HttpClient jettyClient = null;

    private String baseUrl = null;

    private PointType location = null;

    private String locationId = null;

    private String language = null;

    private Units units = Units.METRIC;

    private boolean isSecured = true;

    public APIClient(Configuration config) throws NoSuchAlgorithmException, KeyManagementException {
        super();

        this.baseUrl = "https://" + trimedValue((String) config.get(PARAM_HOSTNAME)) + "/api/weather";

        authKey = trimedValue((String) config.get(PARAM_AUTHKEY));
        authToken = trimedValue((String) config.get(PARAM_AUTHTOKEN));
        basicAuthentication = "Basic " + B64Code.encode(authKey + ":" + authToken, StringUtil.__ISO_8859_1);

        try {
            String locString = (String) config.get(PARAM_LOCATION);
            if (locString != null) {
                location = new PointType(locString);
            } else {
                location = new PointType(new DecimalType(56.899), new DecimalType(8.899), new DecimalType(0));
            }
        } catch (Exception e) {
            logger.debug("Cannot set latitude or longitude.", e);
        }

        this.isSecured = this.IsSecuredConnection(config);
    }

    public void start() {
        if (jettyClient != null) {
            // If it is already running, then stop it first
            stop();
        }

        if (this.isSecured) {
            // Instantiate and configure the SslContextFactory
            SslContextFactory sslContextFactory = new SslContextFactory();

            // Instantiate HttpClient with the SslContextFactory
            jettyClient = new HttpClient(sslContextFactory);
        } else {
            // Instantiate non-secure HttpClient
            jettyClient = new HttpClient();
        }

        // Configure jettyClient
        jettyClient.setFollowRedirects(false);

        try {
            jettyClient.start();
        } catch (Exception e) {
            logger.error("Could not start HttpClient :: {}", e);
        }
    }

    public void stop() {
        if (jettyClient != null) {
            if (jettyClient.isStarted()) {
                jettyClient.getAuthenticationStore().clearAuthentications();
                jettyClient.getAuthenticationStore().clearAuthenticationResults();
                try {
                    jettyClient.stop();
                } catch (Exception e) {
                    logger.error("Could not stop HttpClient", e);
                }
            }
        }
        jettyClient = null;
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

    private JsonObject getWeatherData(String url) {
        JsonElement jsonResponse = null;

        String result = getRequest(url, TIMEOUT);
        jsonResponse = new JsonParser().parse(result);

        return jsonResponse.getAsJsonObject();
    }

    public JsonObject getDailyForecast(DailyFormat dailyFormat) throws TWCReSTException {
        StringBuilder url = new StringBuilder(baseUrl).append(getLocation()).append("/forecast/")
                .append(dailyFormat.get()).append(".json");

        url.append("?language=" + getLanguage());

        // If units are set, append it
        if (units != null) {
            url.append("&units=" + units.get());
        }

        return getWeatherData(url.toString());
    }

    public JsonObject getHourlyForecast() throws TWCReSTException {
        StringBuilder url = new StringBuilder(baseUrl).append(getLocation()).append("/forecast/hourly/48hour.json");

        url.append("?language=" + getLanguage());

        // If units are set, append it
        if (units != null) {
            url.append("&units=" + units.get());
        }

        return getWeatherData(url.toString());
    }

    public JsonObject getIntradayForecast(DailyFormat dailyFormat) throws TWCReSTException {
        StringBuilder url = new StringBuilder(baseUrl).append(getLocation()).append("/forecast/intraday/")
                .append(dailyFormat.get()).append(".json");

        url.append("?language=" + getLanguage());

        // If units are set, append it
        if (units != null) {
            url.append("&units=" + units.get());
        }
        return getWeatherData(url.toString());
    }

    public JsonObject getLocationServicesPoint(BigDecimal latitude, BigDecimal longitude) throws TWCReSTException {
        StringBuilder url = new StringBuilder(baseUrl).append("/v3/location/point");

        url.append("?geocode=" + latitude + "," + longitude);
        url.append("&language=" + getLanguage());

        return getWeatherData(url.toString());
    }

    public JsonObject getCurrentConditions() throws TWCReSTException {
        StringBuilder url = new StringBuilder(baseUrl).append(getLocation()).append("/observations.json");

        url.append("?language=" + getLanguage());

        // If units are set, append it
        if (units != null) {
            url.append("&units=" + units.get());
        }

        return getWeatherData(url.toString());
    }

    public JsonObject getHistoricalData(int hours) throws TWCReSTException {
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

        return getWeatherData(url.toString());
    }

    /**
     * Simple logic to perform a authenticated GET request
     *
     * @param url
     * @param timeout
     * @return
     */
    private String getRequest(String url, int timeout) {
        // url += (url.contains("?") ? "&" : "?") + "timestamp=" + System.currentTimeMillis();

        if (!jettyClient.isStarted()) {
            logger.error("Jetty Client is not running");
            return "";
        }

        Request request = jettyClient.newRequest(url).timeout(timeout, TimeUnit.MILLISECONDS);

        request.header(HttpHeader.AUTHORIZATION, basicAuthentication);

        try {
            ContentResponse response = request.send();
            int statusCode = response.getStatus();
            if (statusCode != HttpStatus.OK_200) {
                logger.debug("Method failed: {}", response.getStatus() + " " + response.getReason());
                return null;
            }
            return response.getContentAsString();
        } catch (Exception e) {
            logger.debug("Could not make http connection", e);
        }

        return null;
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
            return "/v1/geocode/" + location.getLatitude() + "/" + location.getLongitude();
        } else {
            return "/v1/location/" + locationId;
        }
    }

    public void setLocation(DecimalType latitude, DecimalType longitude) {
        location = new PointType(latitude, longitude, location.getAltitude());
        this.locationId = null;
    }

    public void setLocation(String locationId) {
        this.locationId = locationId;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}