package com.hopebaytech.hcfsmgmt.info;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Aaron
 *         Created by Aaron on 2016/6/1.
 */
public class RegisterResultInfo {

    private String backend_type;
    private String account;
    private String user;
    private String password;
    private String backend_url;
    private String bucket;
    private String protocol;
    private String message;
    private String storageAccessToken;
    private int responseCode;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    @Nullable
    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    public String getBackendType() {
        return backend_type;
    }

    public void setBackendType(String backend_type) {
        this.backend_type = backend_type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBackendUrl() {
        return backend_url;
    }

    public void setBackendUrl(String backend_url) {
        this.backend_url = backend_url;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getStorageAccessToken() {
        return storageAccessToken;
    }

    public void setStorageAccessToken(String storageAccessToken) {
        this.storageAccessToken = storageAccessToken;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("backendType", backend_type);
            jsonObject.put("account", account);
            jsonObject.put("user", user);
            jsonObject.put("password", password);
            jsonObject.put("backendUrl", backend_url);
            jsonObject.put("bucket", bucket);
            jsonObject.put("protocol", protocol);
            jsonObject.put("message", message);
            jsonObject.put("storageAccessToken", storageAccessToken);
            jsonObject.put("responseCode", responseCode);
        } catch (JSONException e) {
            return Log.getStackTraceString(e);
        }
        return jsonObject.toString();
    }

}
