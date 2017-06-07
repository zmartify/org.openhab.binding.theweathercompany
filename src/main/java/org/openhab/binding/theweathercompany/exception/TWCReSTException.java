package org.openhab.binding.theweathercompany.exception;

import com.google.gson.JsonElement;

public class TWCReSTException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -7865938308612899576L;

    public static final String HTTP_ERR_UNEXPECTED = "Unexpected error code";
    public static final String HTTP_ERR_500 = "Internal server error";

    public static final String HTTP_TWC_ERR_400 = "Invalid request (No body, invalid JSON, unexpected key, bad value)";
    public static final String HTTP_TWC_ERR_401 = "The authentication token is empty or invalid";
    public static final String HTTP_TWC_ERR_403 = "Forbidden request. Limit reached";
    public static final String HTTP_TWC_ERR_404 = "Not found. A required parameter is missing";
    public static final String HTTP_TWC_ERR_500 = HTTP_ERR_500;

    private String method = null;
    private String url = null;
    private int httpCode;
    private JsonElement response = null;
    private String request = null;

    /**
     *
     * @param method One of the HTTP methods ("get","post", "put", "delete")
     * @param url URL of the ReST call
     * @param request Requested parameters or NULL
     * @param httpCode Returned code from Watson IoT Platform
     * @param reason Reason for the exception
     * @param response Response from Watson IoT Platform or NULL
     */
    public TWCReSTException(String method, String url, String request, int httpCode, String reason,
            JsonElement response) {
        super(reason);
        this.method = method;
        this.url = url;
        this.request = request;
        this.httpCode = httpCode;
        this.response = response;
    }

    public TWCReSTException(int httpCode, String reason, JsonElement response) {
        super(reason);
        this.httpCode = httpCode;
        this.response = response;
    }

    public TWCReSTException(int httpCode, String reason) {
        super(reason);
        this.httpCode = httpCode;
    }

    public TWCReSTException(String reason, JsonElement response) {
        super(reason);
        this.response = response;
    }

    public TWCReSTException(String reason) {
        super(reason);
    }

    public String getMethodL() {
        return method;
    }

    public String getURL() {
        return url;
    }

    public String getRequest() {
        return request;
    }

    public int getHttpCode() {
        return httpCode;
    }

    /**
     * Return the response from Watson IoT or null.
     *
     * @return JsonElement or null
     */
    public JsonElement getResponse() {
        return response;
    }

}
