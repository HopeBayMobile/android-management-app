package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.PreferenceManager;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.ChangeAccountActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

/**
 * Created by Aaron on 2016/4/15.
 */
public class ChangeAccountDialogFragment extends DialogFragment {

    public static final String TAG = FileMgmtAppDialogFragment.class.getSimpleName();
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
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(HCFSMgmtUtils.PREF_IS_HCFS_ACTIVATED, false);
                editor.apply();

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
