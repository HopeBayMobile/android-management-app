package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/24.
 */
public class SettingsFragment extends Fragment {

    private final String CLASSNAME = SettingsFragment.class.getSimpleName();

    public static final String PREF_SYNC_WIFI_ONLY= "pref_sync_wifi_only";
    public static final String PREF_NOTIFY_CONN_FAILED_RECOVERY = "pref_notify_conn_failed_recovery";
    public static final String PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO = "pref_notify_local_storage_usage_ratio";
    public static final String PREF_LOCAL_STORAGE_USAGE_RATIO_NOTIFIED = "pref_local_storage_usage_ratio_notified";

    public static final String KEY_RATIO = "ratio";

    public static final int REQUEST_CODE_RATIO = 0;

    private CheckBox mSyncWifiOnly;
    private CheckBox mNotifyConnFailedRecovery;
    private LinearLayout mNotifyLocalStorageUsedRatio;
    private LinearLayout mSwitchAccount;

    private Context mContext;

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

        mSyncWifiOnly = (CheckBox) view.findViewById(R.id.sync_wifi_only);
        mNotifyConnFailedRecovery = (CheckBox) view.findViewById(R.id.notify_conn_failed_recovery);
        mNotifyLocalStorageUsedRatio = (LinearLayout) view.findViewById(R.id.notify_local_storage_used_ratio);
        mSwitchAccount = (LinearLayout) view.findViewById(R.id.switch_account);
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

                /** Unknown cause results in that dialog show twice, thus we manually dismiss one of them */
                dialogFragment.dismiss();
            }
        });
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
}
