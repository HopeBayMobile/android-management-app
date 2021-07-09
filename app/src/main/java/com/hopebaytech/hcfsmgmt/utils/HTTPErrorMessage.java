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

import com.hopebaytech.hcfsmgmt.R;

import java.net.HttpURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/9/20.
 */
public class HTTPErrorMessage {

    public static int getErrorMessageResId(int errorCode) {
        int errorMsgResId = R.string.http_unknown_error;
        switch (errorCode) {
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                errorMsgResId = R.string.http_internal_error;
                break;
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                errorMsgResId = R.string.http_gateway_timeout;
                break;
        }
        return errorMsgResId;
    }

}
