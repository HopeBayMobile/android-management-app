package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;

import com.hopebaytech.hcfsmgmt.info.DeviceListInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceStatusInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rondou.chen on 2017/8/8.
 */

public class GoogleDriveAPI {
    private static String TAG = GoogleDriveAPI.class.getSimpleName();

    private static final String GOOGLE_DRIVE_API_HOST_NAME_V2 =
            "https://www.googleapis.com/drive/v2/files";

    private static final String GOOGLE_DRIVE_API_USER_INFO_V3 =
            "https://www.googleapis.com/oauth2/v3/userinfo";

    private static final String GOOGLE_DRIVE_TERA_FOLDER_PREFIX = "tera.";

    private static final String GOOGLE_DRIVE_AUTH_HEADER_KEY = "Authorization";
    private static final String GOOGLE_DRIVE_AUTH_HEADER_VALUE_PREFIX = "Bearer";

    public static final String GOOGLE_ENDPOINT_AUTH = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String GOOGLE_ENDPOINT_TOKEN = "https://www.googleapis.com/oauth2/v4/token";
    public static final String GOOGLE_CLIENT_ID = "795577377875-k5blp9vlffpe9s13sp6t4vqav0t6siss.apps.googleusercontent.com";

    public static final String ACTION_AUTHORIZATION_RESPONSE = "com.hopebaytech.hcfsmgmt.HANDLE_AUTHORIZATION_RESPONSE";

    public static DeviceServiceInfo buildDeviceServiceInfo(String token, String account) {
        DeviceServiceInfo.Backend backend = new DeviceServiceInfo.Backend();
        backend.setUrl("https://127.0.0.1"/* You can write anything that you want, but it can not be null */);
        backend.setToken(token);
        backend.setBackendType("googledrive");
        backend.setAccount(account);

        DeviceServiceInfo deviceServiceInfo = new DeviceServiceInfo();
        deviceServiceInfo.setBackend(backend);

        return deviceServiceInfo;
    }

    public static DeviceListInfo buildDeviceListInfo(String imei) {
        DeviceStatusInfo info = new DeviceStatusInfo();
        info.setImei(imei);

        DeviceListInfo deviceListInfo = new DeviceListInfo();
        deviceListInfo.addDeviceStatusInfo(info);
        deviceListInfo.setType(DeviceListInfo.TYPE_RESTORE_FROM_GOOGLE_DRIVE);

        return deviceListInfo;
    }

    private static Map<String, String> buildAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(GOOGLE_DRIVE_AUTH_HEADER_KEY, String.format("%s %s",GOOGLE_DRIVE_AUTH_HEADER_VALUE_PREFIX, token));
        return headers;
    }

    public static boolean hasTeraFolderItem(Context context, String accessToken) throws IOException, JSONException {
        JSONArray teraFolderItems = GoogleDriveAPI.getTeraFolderItems(accessToken, HCFSMgmtUtils.getDeviceImei(context));
        return teraFolderItems.length() > 0;
    }

    public static String getTeraFolderId(JSONArray items) throws IOException, JSONException {
        String id = null;
        if (items.length() > 0) {
            id = ((JSONObject) items.get(0)).getString("id");
        }
        return id;
    }

    public static JSONArray getTeraFolderItems(String token, String imei)
            throws IOException, JSONException {
        JSONObject fileInfo = searchFile(token, GOOGLE_DRIVE_TERA_FOLDER_PREFIX + imei);
        return fileInfo.has("items") ? fileInfo.getJSONArray("items") : new JSONArray();
    }

    public static JSONObject getUserInfo(String token) throws IOException, JSONException {
        Map<String, String> headers = buildAuthHeaders(token);

        HttpUtil.HttpRequest request = HttpUtil.buildGetRequest(headers, GOOGLE_DRIVE_API_USER_INFO_V3);
        HttpUtil.HttpResponse response = HttpUtil.executeSynchronousRequest(request);
        return response == null ? new JSONObject() : new JSONObject(response.getBody());
    }

    public static JSONObject searchFile(String token, String filename) throws IOException, JSONException {
        String url = String.format(
                "%s%s\'%s\'", GOOGLE_DRIVE_API_HOST_NAME_V2, "?q=title contains ", filename);
        Map<String, String> headers = buildAuthHeaders(token);

        HttpUtil.HttpRequest request = HttpUtil.buildGetRequest(headers, url);
        HttpUtil.HttpResponse response = HttpUtil.executeSynchronousRequest(request);
        return response == null ? new JSONObject() : new JSONObject(response.getBody());
    }

    public static HttpUtil.HttpResponse deleteFile(String token, String fileId) throws IOException, JSONException {
        String url = String.format("%s/%s", GOOGLE_DRIVE_API_HOST_NAME_V2, fileId);
        Map<String, String> headers = buildAuthHeaders(token);

        HttpUtil.HttpRequest request = HttpUtil.buildDeleteRequest(headers, url, null);
        HttpUtil.HttpResponse response = HttpUtil.executeSynchronousRequest(request);
        return response;
    }

    public static boolean deleteTeraFolderOnGoogleDrive(Context context, String accessToken) {
        String imei = HCFSMgmtUtils.getDeviceImei(context);
        try {
            JSONArray items = getTeraFolderItems(accessToken, imei);
            HttpUtil.HttpResponse response = deleteFile(accessToken, getTeraFolderId(items));
            return response.getCode() == 204 || response.getCode() == 404;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
