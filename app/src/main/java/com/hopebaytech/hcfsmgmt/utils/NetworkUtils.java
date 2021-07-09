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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    /**
     * @return true if network status is connected, false otherwise.
     */
    public static boolean isNetworkConnected(Context context) {
        boolean isConnected;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            isConnected = true;
        } else {
            isConnected = false;
        }
        return isConnected;
    }

    public static int getNetworkType(Context context) {
        int type = -1;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo != null) {
            type = netInfo.getType();
        }
        return type;
    }

}
