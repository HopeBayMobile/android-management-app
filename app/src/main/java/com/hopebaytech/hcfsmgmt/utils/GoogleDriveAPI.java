package com.hopebaytech.hcfsmgmt.utils;

import android.text.TextUtils;

import com.hopebaytech.hcfsmgmt.info.DeviceListInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceStatusInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by rondou.chen on 2017/8/8.
 */

public class GoogleDriveAPI {
    private static String TAG = "GoogleDriveAPI";

    private static final String GOOGLE_DRIVE_API_HOST_NAME_V2 =
            "https://www.googleapis.com/drive/v2/files";

    private static final String GOOGLE_DRIVE_API_USER_INFO_V3 =
            "https://www.googleapis.com/oauth2/v3/userinfo";

    private static final String GOOGLE_DRIVE_FOLDER_NAME = "tera";

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

    private static Request.Builder getBuilder(String token) {
        return new Request.Builder()
                .addHeader("Authorization", String.format("Bearer %s", token));
    }

    public static boolean isCanRestore(JSONArray items) throws IOException, JSONException {
        if (items.length() > 0) {
            return true;
        }
        return false;
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
        Response resp = GoogleDriveAPI.searchFile(token, GOOGLE_DRIVE_FOLDER_NAME + "." + imei);
        JSONObject body = new JSONObject(resp.body().string());
        return body.getJSONArray("items");
    }

    public static JSONObject getUserInfo(String token) throws IOException, JSONException {
        Request request = getBuilder(token)
                .url(GOOGLE_DRIVE_API_USER_INFO_V3)
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        return new JSONObject(response.body().string());
    }

    public static Response searchFile(String token, String filename) throws IOException {
        String url = String.format(
                "%s%s\'%s\'", GOOGLE_DRIVE_API_HOST_NAME_V2, "?q=title+contains+", filename);

        Request request = getBuilder(token)
                .url(url)
                .build();
        return new OkHttpClient().newCall(request).execute();
    }

    public static Response deleteFile(String token, String fileId) throws IOException {
        String url = String.format("%s/%s", GOOGLE_DRIVE_API_HOST_NAME_V2, fileId);

        Request request = getBuilder(token)
                .url(url)
                .delete()
                .build();
        //If successful, this method returns an empty response body.
        Response response = new OkHttpClient().newCall(request).execute();
        return response;
    }
}
