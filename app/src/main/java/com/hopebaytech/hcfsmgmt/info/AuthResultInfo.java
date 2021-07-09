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
 *         Created by Aaron on 2016/3/8.
 */
public class AuthResultInfo {

    private String token;
    private int responseCode;
    private String responseContent;
    private String errorCode;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", token);
            jsonObject.put("responseContent", responseContent);
            jsonObject.put("responseCode", responseCode);
            jsonObject.put("errorCode", errorCode);
        } catch (JSONException e) {
            return Log.getStackTraceString(e);
        }
        return jsonObject.toString();
    }
}
