package com.hopebaytech.hcfsmgmt.utils;


import com.hopebaytech.hcfsmgmt.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwiftServerUtil {
    private static final String TAG = SwiftServerUtil.class.getSimpleName();

    private static final String TEST_SWIFT_INFO_IP = "172.16.11.69";
    private static final String TEST_SWIFT_INFO_PORT = "8010";
    private static final String TEST_SWIFT_INFO_ACCOUNT = "hopebay:EKGKe3W3zW9IEul6zVjr";
    private static final String TEST_SWIFT_INFO_KEY = "PZJeuN5xfIV2dQkq1MSKNQCztKgzkPpn";
    //private static final String TEST_SWIFT_INFO_BUCKET_NAME = "test1";

    public static final String SWIFT_HEADER_KEY_USER = "x-auth-user";
    public static final String SWIFT_HEADER_KEY_KEY = "x-auth-key";
    public static final String SWIFT_HEADER_KEY_TOKEN = "x-auth-token";
    public static final String SWIFT_HEADER_KEY_STORAGE_URL = "x-storage-url";

    public static final String SWIFT_TERA_BUCKET_PREFIX = "tera_";

    public static String getToken(String account, String key, String url) {
        Map<String, String> authHeaders = new HashMap<String, String>();
        authHeaders.put(SWIFT_HEADER_KEY_USER, account);
        authHeaders.put(SWIFT_HEADER_KEY_KEY, key);
        String authPath = String.format("%s%s%s", "http://", url, "/auth/v1.0");
        HttpUtil.HttpRequest  authRequest = HttpUtil.buildGetRequest(authHeaders, authPath);
        HttpUtil.HttpResponse authResponse = HttpUtil.executeSynchronousRequest(authRequest);

        if (authResponse == null || authResponse.getCode() != 200 || authResponse.getHeader(SWIFT_HEADER_KEY_TOKEN) == null) {
            Logs.e(TAG, "getToken", String.format("Code: [%s], Msg: [%s], Body: [%s]", authResponse.getCode(), authResponse.getMessage(), authResponse.getBody()));
            return null;
        }

        return authResponse.getHeader(SWIFT_HEADER_KEY_TOKEN);
    }

    public static String getStorageUrl(String account, String key, String url) {
        Map<String, String> authHeaders = new HashMap<String, String>();
        authHeaders.put(SWIFT_HEADER_KEY_USER, account);
        authHeaders.put(SWIFT_HEADER_KEY_KEY, key);
        String authPath = String.format("%s%s%s", "http://", url, "/auth/v1.0");
        HttpUtil.HttpRequest  authRequest = HttpUtil.buildGetRequest(authHeaders, authPath);
        HttpUtil.HttpResponse authResponse = HttpUtil.executeSynchronousRequest(authRequest);

        if (authResponse == null || authResponse.getCode() != 200 || authResponse.getHeader(SWIFT_HEADER_KEY_STORAGE_URL) == null) {
            Logs.e(TAG, "getStorageUrl", String.format("Code: [%s], Msg: [%s], Body: [%s]", authResponse.getCode(), authResponse.getMessage(), authResponse.getBody()));
            return null;
        }

        return authResponse.getHeader(SWIFT_HEADER_KEY_STORAGE_URL);
    }

    public static List<String> listTeraBuckets(String account, String key, String url) {
        String swiftToken = SwiftServerUtil.getToken(account, key, url);
        String swiftStorageUrl = SwiftServerUtil.getStorageUrl(account, key, url);
        if (swiftToken == null || swiftStorageUrl == null) {
            return null;
        }

        Map<String, String> listBucketHeaders = new HashMap<String, String>();
        listBucketHeaders.put(SWIFT_HEADER_KEY_TOKEN, swiftToken);
        HttpUtil.HttpResponse listBucketResponse = HttpUtil.executeSynchronousRequest(HttpUtil.buildGetRequest(listBucketHeaders, swiftStorageUrl));
        if (listBucketResponse == null || listBucketResponse.getCode() != 200) {
            Logs.e(TAG, "listTeraBuckets", String.format("Code: [%s], Msg: [%s], Body: [%s]", listBucketResponse.getCode(), listBucketResponse.getMessage(), listBucketResponse.getBody()));
            return null;
        }

        List<String> allBuckets = Arrays.asList(listBucketResponse.getBody().split("\n"));
        List<String> teraBuckets = new ArrayList<String>();
        for(String bucketName : allBuckets) {
            if(bucketName.startsWith(SWIFT_TERA_BUCKET_PREFIX)) {
                teraBuckets.add(bucketName);
            }
        }

        return teraBuckets;
    }

    public static String createBucket(String account, String key, String url, String bucketPostFix) {
        String swiftToken = SwiftServerUtil.getToken(account, key, url);
        String swiftStorageUrl = SwiftServerUtil.getStorageUrl(account, key, url);
        if (swiftToken == null || swiftStorageUrl == null) {
            return null;
        }

        String bucketName = String.format("%s%s", SWIFT_TERA_BUCKET_PREFIX, bucketPostFix);
        String createBucketUrl = String.format("%s/%s", swiftStorageUrl, bucketName);
        Map<String, String> createBucketHeaders = new HashMap<String, String>();
        createBucketHeaders.put(SWIFT_HEADER_KEY_TOKEN, swiftToken);
        HttpUtil.HttpResponse createBucketResponse = HttpUtil.executeSynchronousRequest(HttpUtil.buildPutRequest(createBucketHeaders, createBucketUrl, null));
        if (createBucketResponse == null || createBucketResponse.getCode() != 201) {
            Logs.e(TAG, "createBucket", String.format("Code: [%s], Msg: [%s], Body: [%s]", createBucketResponse.getCode(), createBucketResponse.getMessage(), createBucketResponse.getBody()));
            return null;
        }

        return bucketName;
    }
}
