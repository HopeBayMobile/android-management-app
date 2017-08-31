package com.hopebaytech.hcfsmgmt.utils;

import android.util.Log;

import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {

    private static String MESSAGE_TAG = HttpUtil.class.getName();

    public static HttpRequestBody createRequestBody(String bodyType, String bodyContent) {
        MediaType mediaType = MediaType.parse(bodyType);
        return new HttpRequestBody(mediaType, bodyContent);
    }

    public static HttpRequest buildGetRequest(Map<String, String> headers, String url) {
        return new HttpRequest(headers, url, "GET", null);
    }

    public static HttpRequest buildPostRequest(Map<String, String> headers, String url, HttpRequestBody httpRequestBody) {
        return new HttpRequest(headers, url, "POST", httpRequestBody);
    }

    public static HttpRequest buildPutRequest(Map<String, String> headers, String url, HttpRequestBody httpRequestBody) {
        return new HttpRequest(headers, url, "PUT", httpRequestBody);
    }

    public static HttpRequest buildDeleteRequest(Map<String, String> headers, String url, HttpRequestBody httpRequestBody) {
        return new HttpRequest(headers, url, "DELETE", httpRequestBody);
    }

    public static void executeAsynchronousRequest(final HttpRequest httpRequest) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                httpRequest.execute();
            }
        }).start();
    }

    public static HttpResponse executeSynchronousRequest(HttpRequest httpRequest) {
        return httpRequest.execute();
    }

    private static Request.Builder getRequestBuilderWithHeaders(Map<String, String> headers) {
        Request.Builder requestBuilder = new Request.Builder();

        if(headers != null && !headers.isEmpty()) {
            for(String key : headers.keySet()) {
                requestBuilder.addHeader(key, headers.get(key));
            }
        }

        return requestBuilder;
    }

    public static class HttpRequest {
        Request request;

        public HttpRequest(Map<String, String> headers, String url, String method, HttpRequestBody httpRequestBody) {
            this.request = getRequestBuilderWithHeaders(headers).url(url).method(method, httpRequestBody == null? null : httpRequestBody.getRequestBody()).build();
        }

        public HttpResponse execute() {
            Response response = null;
            try {
                response = new OkHttpClient().newCall(request).execute();
            } catch (IOException e) {
                Log.e(MESSAGE_TAG, String.format("Fail to execute http request: [%s]", request.toString()));
            }

            return response == null ? null : new HttpResponse(response);
        }

        public String header(String key) {
            return request.header(key);
        }
    }

    public static class HttpRequestBody {
        RequestBody requestBody;

        public HttpRequestBody(MediaType mediaType, String bodyContent) {
            requestBody = RequestBody.create(mediaType, bodyContent);
        }

        public RequestBody getRequestBody() {
            return requestBody;
        }
    }

    public static class HttpResponse {
        Response response;

        public HttpResponse(Response response) {
            this.response = response;
        }

        public int getCode() {
            return response.code();
        }

        public String getBody() {
            try {
                return response.body().string();
            } catch (IOException e) {
                return null;
            }
        }

        public String getMessage() {
            return response.message();
        }

        public String getHeader(String key) {
            return response.header(key);
        }
    }
}
