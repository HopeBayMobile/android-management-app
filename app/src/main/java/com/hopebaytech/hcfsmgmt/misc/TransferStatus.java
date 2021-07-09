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
package com.hopebaytech.hcfsmgmt.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hopebaytech.hcfsmgmt.main.TransferContentActivity;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/21.
 */
public class TransferStatus {

    public static final int NONE = 0;
    public static final int TRANSFERRING = 1;
    public static final int WAIT_DEVICE = 2;
    public static final int TRANSFERRED = 3;

    public static void setTransferStatus(Context context, int status) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(TransferContentActivity.PREF_TRANSFER_STATUS, status);
        editor.apply();
    }

    public static int getTransferStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(TransferContentActivity.PREF_TRANSFER_STATUS, NONE);
    }

    public static void removeTransferStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TransferContentActivity.PREF_TRANSFER_STATUS);
        editor.apply();
    }

}
