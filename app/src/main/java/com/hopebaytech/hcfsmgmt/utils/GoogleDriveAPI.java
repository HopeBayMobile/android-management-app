package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hopebaytech.hcfsmgmt.info.DeviceListInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceStatusInfo;

import net.openid.appauth.AuthState;

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

    private static final String GOOGLE_DRIVE_TERA_FOLDER_PREFIX = "tera";

    private static final String GOOGLE_DRIVE_AUTH_HEADER_KEY = "Authorization";
    private static final String GOOGLE_DRIVE_AUTH_HEADER_VALUE_PREFIX = "Bearer";

    public static DeviceServiceInfo buildDeviceServiceInfo(String url, String token,
            String backendType, String bucket, String account) {
        DeviceServiceInfo.Backend backend = new DeviceServiceInfo.Backend();
        if (TextUtils.isEmpty(url)) {
            /* You can write anything that you want, but it can not be null */
            url = "https://127.0.0.1";
        }
        backend.setUrl(url);
        backend.setToken(token);
        backend.setBackendType(backendType);
        backend.setBucket(bucket);
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
        deviceListInfo.setType(DeviceListInfo.TYPE_RESTORE_FROM_MY_TERA);

        return deviceListInfo;
    }

    private static Map<String, String> buildAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(GOOGLE_DRIVE_AUTH_HEADER_KEY, String.format("%s %s",GOOGLE_DRIVE_AUTH_HEADER_VALUE_PREFIX, token));
        return headers;
    }

    public static boolean hasTeraFolderItem(Context context, @NonNull AuthState authState) throws IOException, JSONException {
        JSONArray teraFolderItems = GoogleDriveAPI.getTeraFolderItems(authState.getAccessToken(), HCFSMgmtUtils.getDeviceImei(context));
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
        JSONObject fileInfo = GoogleDriveAPI.searchFile(token, GOOGLE_DRIVE_TERA_FOLDER_PREFIX + "." + imei);
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
                "%s%s\'%s\'", GOOGLE_DRIVE_API_HOST_NAME_V2, "?q=title+contains+", filename);
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
}
