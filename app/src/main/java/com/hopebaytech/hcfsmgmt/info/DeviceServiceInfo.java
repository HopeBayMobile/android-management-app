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
package com.hopebaytech.hcfsmgmt.info;

import android.content.Context;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.utils.HTTPErrorMessage;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/12.
 */
public class DeviceServiceInfo {

    public static class Category {
        public static final String LOCK = "pb_001";
        public static final String RESET = "pb_002";
        public static final String TX_WAITING = "pb_003";
        public static final String UNREGISTERED = "pb_004";
    }

    public static class State {
        public static final String ACTIVATED = "activated";
        public static final String DISABLED = "disabled";
        public static final String TXReady = "TXReady";
    }

    private String message;
    private int responseCode;
    private String responseContent;
    private String state;
    private Piggyback piggyback;
    private Backend backend;
    private int whiteListLatestVersion;
    private String errorCode;

    private String mImei;

    public CharSequence getErrorMessage(Context context) {
        CharSequence errorMessage;
        if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
            errorMessage = MgmtCluster.ErrorCode.getErrorMessage(context, errorCode);
        } else {
            errorMessage = context.getText(HTTPErrorMessage.getErrorMessageResId(responseCode));
        }
        return errorMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Piggyback getPiggyback() {
        return piggyback;
    }

    public void setPiggyback(Piggyback piggyback) {
        this.piggyback = piggyback;
    }

    public Backend getBackend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    public int getWhiteListLatestVersion() {
        return whiteListLatestVersion;
    }

    public void setWhiteListLatestVersion(int whiteListLatestVersion) {
        this.whiteListLatestVersion= whiteListLatestVersion;
    }
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public static class Piggyback {

        private String category;
        private String message;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("category", category);
                jsonObject.put("message", message);
            } catch (JSONException e) {
                return Log.getStackTraceString(e);
            }
            return jsonObject.toString();
        }
    }

    public static class Backend {

        private String url;
        private String account;
        private String token;
        private String backendType;
        private String bucket;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUser() {
            return account.split(":")[0];
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getBackendType() {
            if (token != null && !backendType.equals("googledrive")) {
                return backendType + "token";
            }
            return backendType;
        }

        public void setBackendType(String backendType) {
            this.backendType = backendType;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        @Override
        public String toString() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("url", url);
                jsonObject.put("account", account);
                jsonObject.put("bucket", bucket);
                jsonObject.put("token", token);
                jsonObject.put("backendType", backendType);
                jsonObject.put("bucket", bucket);
            } catch (JSONException e) {
                return Log.getStackTraceString(e);
            }
            return jsonObject.toString();
        }

    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", message);
            jsonObject.put("responseCode", responseCode);
            jsonObject.put("responseContent", responseContent);
            jsonObject.put("state", state);
            jsonObject.put("piggyback", piggyback);
            jsonObject.put("backend", backend);
            jsonObject.put("whiteListLatestVersion", whiteListLatestVersion);
            jsonObject.put("errorCode", errorCode);
        } catch (JSONException e) {
            return Log.getStackTraceString(e);
        }
        return jsonObject.toString().replace("\\", "");
    }

    public void setImei(String imei) {
        mImei = imei;
    }

    public String getImei() {
        return mImei;
    }
}
