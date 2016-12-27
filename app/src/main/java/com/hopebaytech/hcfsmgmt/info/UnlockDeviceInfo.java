package com.hopebaytech.hcfsmgmt.info;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/12.
 */
public class UnlockDeviceInfo {

    private String message;
    private int responseCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", message);
            jsonObject.put("responseCode", responseCode);
        } catch (JSONException e) {
            return Log.getStackTraceString(e);
        }
        return jsonObject.toString();
    }
}
