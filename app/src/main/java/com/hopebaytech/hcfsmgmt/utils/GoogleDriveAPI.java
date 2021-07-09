/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;

import com.hopebaytech.hcfsmgmt.info.DeviceListInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceStatusInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final String GOOGLE_DRIVE_CONTENT_TYPE_HEADER_KEY = "Content-Type";

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

    public static DeviceListInfo buildDeviceListInfo (List<String> folders) {
        DeviceListInfo deviceListInfo = new DeviceListInfo();
        deviceListInfo.setType(DeviceListInfo.TYPE_RESTORE_FROM_GOOGLE_DRIVE);

        for (String folder : folders) {
            DeviceStatusInfo info = new DeviceStatusInfo();
            info.setImei(folder);
            deviceListInfo.addDeviceStatusInfo(info);
        }
        return deviceListInfo;
    }

    private static Map<String, String> buildAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(GOOGLE_DRIVE_AUTH_HEADER_KEY, String.format("%s %s",GOOGLE_DRIVE_AUTH_HEADER_VALUE_PREFIX, token));
        return headers;
    }

    private static Map<String, String> buildAuthWithContentTypeHeaders(String token, String type) {
        Map<String, String> headers = buildAuthHeaders(token);
        headers.put(GOOGLE_DRIVE_CONTENT_TYPE_HEADER_KEY, type);
        return headers;
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

    public static List<String> getTeraFolderItems(String accessToken)
            throws IOException, JSONException {
        List<String> teraFolder = new ArrayList<String>();
        JSONObject fileInfo = searchFile(accessToken, GOOGLE_DRIVE_TERA_FOLDER_PREFIX);
        if (fileInfo.has("items")) {
            JSONArray items = fileInfo.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = ((JSONObject) items.get(i));
                String type = item.getString("mimeType");
                if (!item.has("parents") || !type.equals("application/vnd.google-apps.folder")) {
                    continue;
                }

                JSONArray parents = item.getJSONArray("parents");
                if (parents.length() > 0) {
                    String title = item.getString("title");
                    boolean isRootFolder = ((JSONObject) parents.get(0)).getBoolean("isRoot");
                    boolean matches = title.matches(GOOGLE_DRIVE_TERA_FOLDER_PREFIX + "[0-9]{15}");
                    if (isRootFolder && matches) {
                        teraFolder.add(title.replaceAll(GOOGLE_DRIVE_TERA_FOLDER_PREFIX, ""));
                        Logs.d(">>>>> title = " + title + " type = " + type + " isRootFolder = " + isRootFolder);
                    }
                }
            }
        }

        return teraFolder;
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

    public static HttpUtil.HttpResponse renameFile(String title, String token, String fileId)
            throws IOException, JSONException {
        String url = String.format("%s/%s", GOOGLE_DRIVE_API_HOST_NAME_V2, fileId);
        Map<String, String> headers = buildAuthWithContentTypeHeaders(token, "application/json");

        JSONObject content = new JSONObject();
        content.put("title", title);

        HttpUtil.HttpRequestBody requestBody = HttpUtil.createRequestBody(
                "application/json; charset=utf-8", content.toString());
        HttpUtil.HttpRequest request = HttpUtil.buildPatchRequest(headers, url, requestBody);
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

    public static boolean resetTeraFolderOnGoogleDrive(
            Context context, String accessToken, String targetImei) {
        try {
            JSONArray items = getTeraFolderItems(accessToken, targetImei);
            String title = GOOGLE_DRIVE_TERA_FOLDER_PREFIX + HCFSMgmtUtils.getDeviceImei(context);
            HttpUtil.HttpResponse response = renameFile(title , accessToken, getTeraFolderId(items));
            return response.getCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
