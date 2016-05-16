package com.gooddata.qa.utils.http;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
/**
 * General REST utilities for executing request, uri or fetching json object, resource from request
 */
public final class RestUtils {

    public static final String ACCEPT_TEXT_PLAIN_WITH_VERSION = "text/plain; version=1";
    public static final String ACCEPT_HEADER_VALUE_WITH_VERSION = "application/json; version=1";

    public static final String CREATE_AND_GET_OBJ_LINK = "/gdc/md/%s/obj?createAndGet=true";

    private RestUtils() {
    }

    /**
     * Execute request with expected status code
     * 
     * @param restApiClient
     * @param request
     * @param expectedStatusCode
     */
    public static void executeRequest(final RestApiClient restApiClient, final HttpRequestBase request,
            final HttpStatus expectedStatusCode) {
        try {
            final HttpResponse response = restApiClient.execute(request, expectedStatusCode, "Invalid status code");
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            request.releaseConnection();
        }
    }

    /**
     * Execute request
     * 
     * @param restApiClient
     * @param request
     * @return status code
     */
    public static int executeRequest(final RestApiClient restApiClient, final HttpRequestBase request) {
        try {
            final HttpResponse response = restApiClient.execute(request);
            final int statusCode = response.getStatusLine().getStatusCode();
            EntityUtils.consumeQuietly(response.getEntity());
            return statusCode;
        } finally {
            request.releaseConnection();
        }
    }

    /**
     * Get resource from request with expected status code
     * 
     * @param restApiClient
     * @param request
     * @param setupRequest        setup request before executing like configure header, ...
     * @param expectedStatusCode
     * @return entity from response in String form
     */
    public static String getResource(final RestApiClient restApiClient, final HttpRequestBase request,
            final Consumer<HttpRequestBase> setupRequest, final HttpStatus expectedStatusCode)
                    throws ParseException, IOException {
        setupRequest.accept(request);

        try {
            final HttpResponse response = restApiClient.execute(request, expectedStatusCode, "Invalid status code");
            final HttpEntity entity = response.getEntity();

            final String ret = isNull(entity) ? "" : EntityUtils.toString(entity);
            EntityUtils.consumeQuietly(entity);
            return ret;

        } finally {
            request.releaseConnection();
        }
    }

    /**
     * Get resource from request with expected status code
     * 
     * @param restApiClient
     * @param request
     * @param expectedStatusCode
     * @return entity from response in json form
     */
    public static String getResource(final RestApiClient restApiClient, final HttpRequestBase request,
            final HttpStatus expectedStatusCode) throws ParseException, IOException {
        return getResource(restApiClient,
                request,
                req -> req.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType()),
                expectedStatusCode);
    }

    /**
     * Get resource from uri with expected status code
     * 
     * @param restApiClient
     * @param uri
     * @param expectedStatusCode
     * @return entity from response in json form
     */
    public static String getResource(final RestApiClient restApiClient, final String uri,
            final HttpStatus expectedStatusCode) throws ParseException, IOException {
        return getResource(restApiClient,
                restApiClient.newGetMethod(uri),
                req -> req.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType()),
                expectedStatusCode);
    }

    /**
     * Get json object from uri, expected status code is OK (200)
     * 
     * @param restApiClient
     * @param uri
     * @return
     */
    public static JSONObject getJsonObject(final RestApiClient restApiClient, final String uri)
            throws IOException, JSONException {
        return getJsonObject(restApiClient, uri, HttpStatus.OK);
    }

    /**
     * Get json object from uri with expected status code
     * 
     * @param restApiClient
     * @param uri
     * @param expectedStatusCode
     * @return
     */
    public static JSONObject getJsonObject(final RestApiClient restApiClient, final String uri,
            final HttpStatus expectedStatusCode) throws IOException, JSONException {
        return new JSONObject(getResource(restApiClient, uri, expectedStatusCode));
    }

    /**
     * Get json object from request with expected status code
     * 
     * @param restApiClient
     * @param request
     * @param expectedStatusCode
     * @return
     */
    public static JSONObject getJsonObject(final RestApiClient restApiClient, final HttpRequestBase request,
            final HttpStatus expectedStatusCode) throws ParseException, JSONException, IOException {
        return new JSONObject(getResource(restApiClient, request, expectedStatusCode));
    }

    /**
     * Get json object from request, expected status code is OK (200)
     * 
     * @param restApiClient
     * @param request
     * @return
     */
    public static JSONObject getJsonObject(final RestApiClient restApiClient, final HttpRequestBase request)
            throws ParseException, JSONException, IOException {
        return new JSONObject(getResource(restApiClient, request, HttpStatus.OK));
    }

    /**
     * Delete object from uri
     * 
     * @param restApiClient
     * @param uri
     */
    public static void deleteObject(final RestApiClient restApiClient, final String uri) {
        executeRequest(restApiClient, restApiClient.newDeleteMethod(uri));
    }
}
