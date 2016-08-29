package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;
import com.hopebaytech.hcfsmgmt.utils.ZipUtils;

import java.io.File;
import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/24.
 */
public class SettingsFragment extends Fragment {

    private final String CLASSNAME = SettingsFragment.class.getSimpleName();

    public static final String PREF_SYNC_WIFI_ONLY = "pref_sync_wifi_only";
    public static final String PREF_NOTIFY_CONN_FAILED_RECOVERY = "pref_notify_conn_failed_recovery";
    public static final String PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO = "pref_notify_local_storage_usage_ratio";
    public static final String PREF_LOCAL_STORAGE_USAGE_RATIO_NOTIFIED = "pref_local_storage_usage_ratio_notified";
    public static final String PREF_INSUFFICIENT_PIN_SPACE_NOTIFIED = "pref_insufficient_pin_space_notified";

    public static final String KEY_RATIO = "ratio";

    public static final int REQUEST_CODE_RATIO = 0;

    private Context mContext;
    private View mView;
    private CheckBox mSyncWifiOnly;
    private CheckBox mNotifyConnFailedRecovery;
    private LinearLayout mNotifyLocalStorageUsedRatio;
    private LinearLayout mSwitchAccount;
    private LinearLayout mTransferContent;
    private LinearLayout mFeedback;
    private Snackbar mSnackbar;

    private boolean cancelAttachLog = false;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logs.d(CLASSNAME, "onViewCreated", null);
        mView = view;

        mSyncWifiOnly = (CheckBox) view.findViewById(R.id.sync_wifi_only);
        mNotifyConnFailedRecovery = (CheckBox) view.findViewById(R.id.notify_conn_failed_recovery);
        mNotifyLocalStorageUsedRatio = (LinearLayout) view.findViewById(R.id.notify_local_storage_used_ratio);
        mSwitchAccount = (LinearLayout) view.findViewById(R.id.switch_account);
        mTransferContent = (LinearLayout) view.findViewById(R.id.transfer_content);
        mFeedback = (LinearLayout) view.findViewById(R.id.feedback);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isChecked = sharedPreferences.getBoolean(PREF_SYNC_WIFI_ONLY, true);
        mSyncWifiOnly.setChecked(isChecked);
        mSyncWifiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HCFSMgmtUtils.changeCloudSyncStatus(mContext, isChecked);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PREF_SYNC_WIFI_ONLY, isChecked);
                editor.apply();
            }
        });

        isChecked = sharedPreferences.getBoolean(PREF_NOTIFY_CONN_FAILED_RECOVERY, false);
        mNotifyConnFailedRecovery.setChecked(isChecked);
        mNotifyConnFailedRecovery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PREF_NOTIFY_CONN_FAILED_RECOVERY, isChecked);
                editor.apply();
            }
        });

        String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
        final String ratio = sharedPreferences.getString(PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO, defaultValue).concat("%");
        String summary = getString(R.string.settings_local_storage_used_ratio, ratio);
        TextView summaryText = (TextView) mNotifyLocalStorageUsedRatio.findViewById(R.id.notify_local_storage_used_ratio_summary);
        summaryText.setText(summary);
        mNotifyLocalStorageUsedRatio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalSpaceUsageRatioDialogFragment fragment = LocalSpaceUsageRatioDialogFragment.newInstance();
                fragment.setTargetFragment(SettingsFragment.this, REQUEST_CODE_RATIO);
                fragment.show(getFragmentManager(), LocalSpaceUsageRatioDialogFragment.TAG);
            }
        });

        mSwitchAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwitchAccountDialogFragment dialogFragment = SwitchAccountDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager(), SwitchAccountDialogFragment.TAG);

                // Unknown cause results in that dialog show twice, thus we manually dismiss one of them
                dialogFragment.dismiss();
            }
        });

        mTransferContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransferContentDescDialogFragment dialogFragment = TransferContentDescDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager(), TransferContentDescDialogFragment.TAG);
            }
        });

        mFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(getString(R.string.alert_dialog_title_warning));
                        builder.setMessage(getString(R.string.require_write_external_storage_permission));
                        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        RequestCode.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    } else {
                        attachLogInMail();
                    }
                } else {
                    attachLogInMail();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAttachLog = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_RATIO) {
            if (resultCode == Activity.RESULT_OK) {
                HCFSMgmtUtils.stopNotifyLocalStorageUsedRatioAlarm(mContext);
                HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(mContext);

                String ratio = data.getStringExtra(KEY_RATIO);
                String summary = getString(R.string.settings_local_storage_used_ratio, ratio);
                TextView summaryText = (TextView) mNotifyLocalStorageUsedRatio.findViewById(R.id.notify_local_storage_used_ratio_summary);
                summaryText.setText(summary);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO, ratio.replace("%", ""));
                editor.apply();
            }
        }

    }

    private void attachLogInMail() {
        cancelAttachLog = false;
        final ProgressDialog mProgressDialog;
        mProgressDialog = getProgressDialog(
                getString(R.string.settings_feedback_log_collecting_title),
                getString(R.string.settings_feedback_log_collecting_message));
        mProgressDialog.show();

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new StringBuilder()
                            .append("<p><b>" + getString(R.string.settings_feedback_content_mail_time) + "</b></p>")
                            .append("<p><b>" + getString(R.string.settings_feedback_content_mail_opinions) + "</b></p>")
                            .append("<p></p>")
                            .append("<p><b>" + getString(R.string.settings_feedback_content_mail_description) + "</b></p>")
                            .append("<p><b>" + getString(R.string.settings_feedback_content_mail_contact) + "</b></p>")
                            .append("<p><b>" + getString(R.string.settings_feedback_content_mail_phone) + "</b></p>")
                            .toString())
                    );
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cs@hbmobile.com"});

                    PackageManager manager = mContext.getPackageManager();
                    List<ResolveInfo> infoList = manager.queryIntentActivities(intent, 0);
                    if (infoList.size() != 0) {
                        final String source = getString(R.string.zip_source_path);
                        final String target = getString(R.string.zip_target_path);
                        boolean isSuccess = ZipUtils.zip(source, target);
                        File file = new File(target);
                        if (file.exists() && isSuccess) {
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                            intent.setType("application/zip");
                        }
                        if (!cancelAttachLog) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    mProgressDialog.hide();
                                }
                            });
                            startActivity(intent);
                        }
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mProgressDialog.hide();
                                Snackbar snackbar = Snackbar.make(
                                        mView,
                                        R.string.settings_snackbar_no_available_email_app_found,
                                        Snackbar.LENGTH_LONG);
                                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setMaxLines(10);
                                snackbar.show();
                            }
                        });
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private ProgressDialog getProgressDialog(String title, String msg) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(title);
        progressDialog.setMessage(msg);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelAttachLog = true;
                dialog.dismiss();
            }
        });
        return progressDialog;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        attachLogInMail();
    }

}
