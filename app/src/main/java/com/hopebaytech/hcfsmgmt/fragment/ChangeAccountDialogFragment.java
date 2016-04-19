package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.ChangeAccountActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;

/**
 * Created by Aaron on 2016/4/15.
 */
public class ChangeAccountDialogFragment extends DialogFragment {

    public static final String TAG = FileMgmtDialogFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();
    private Activity mActivity;

    public static ChangeAccountDialogFragment newInstance() {
        return new ChangeAccountDialogFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(getString(R.string.alert_dialog_title_warning));
        builder.setMessage(getString(R.string.settings_section_change_account_warning_msg));
        builder.setPositiveButton(getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(mActivity, ChangeAccountActivity.class);
                mActivity.startActivity(intent);
                mActivity.finish();
            }
        });
        builder.setNegativeButton(getString(R.string.alert_dialog_no), null);
        builder.show();

        return builder.create();
    }
}
