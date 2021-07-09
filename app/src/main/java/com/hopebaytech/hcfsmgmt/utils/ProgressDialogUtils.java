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
import android.support.annotation.StringRes;

import com.hopebaytech.hcfsmgmt.R;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/19.
 */
public class ProgressDialogUtils {

    private final static String CLASSNAME = ProgressDialogUtils.class.getSimpleName();

    private Context mContext;
    private android.app.ProgressDialog mProgressDialog;

    public ProgressDialogUtils(Context context) {
        mContext = context;
        mProgressDialog = new android.app.ProgressDialog(context);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    public void show(String message) {
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    public void show(@StringRes int resId) {
        if (!mProgressDialog.isShowing()) {
            String message = mContext.getString(resId);
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    public void show() {
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void setMessage(String message) {
        mProgressDialog.setMessage(message);
    }

    public void setMessage(@StringRes int resId) {
        String message = mContext.getString(resId);
        mProgressDialog.setMessage(message);
    }

    public void dismiss() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
