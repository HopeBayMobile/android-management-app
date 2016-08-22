package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/19.
 */
public class ProgressDialogUtil {

    private final String CLASSNAME = ProgressDialogUtil.class.getSimpleName();

    private android.app.ProgressDialog mProgressDialog;

    public ProgressDialogUtil(Context context) {
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

    public void dismiss() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
