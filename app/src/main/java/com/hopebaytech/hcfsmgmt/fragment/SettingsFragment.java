package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;

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
    public static final String PREF_SHOW_BA_LOGGING_OPTION = "pref_show_ba_logging_option";
    public static final String PREF_ASK_MOBILE_WITHOUT_WIFI_ONLY = "pref_ask_mobile_without_wifi_only";

    public static final String KEY_RATIO = "ratio";

    public static final int REQUEST_CODE_RATIO = 0;
    public static final int REQUEST_ABOUT_FRAGMENT = 1;

    private Context mContext;
    private View mView;
    private CheckBox mSyncWifiOnly;
    private CheckBox mNotifyConnFailedRecovery;
    private LinearLayout mNotifyLocalStorageUsedRatio;
    private LinearLayout mChangeAccount;
    private LinearLayout mTransferContent;
    private Snackbar mSnackbar;

    private LinearLayout mAbout;
    private LinearLayout mBa;

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
        mChangeAccount = (LinearLayout) view.findViewById(R.id.switch_account);
        mTransferContent = (LinearLayout) view.findViewById(R.id.transfer_content);
        mAbout = (LinearLayout) view.findViewById(R.id.about);
        mBa = (LinearLayout) view.findViewById(R.id.extra_log_for_ba_layout);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean isChecked = true;
        SettingsDAO mSettingsDAO = SettingsDAO.getInstance(getContext());
        SettingsInfo settingsInfo = mSettingsDAO.get(SettingsFragment.PREF_SYNC_WIFI_ONLY);
        if (settingsInfo != null) {
            isChecked = Boolean.valueOf(settingsInfo.getValue());
        }
        mSyncWifiOnly.setChecked(isChecked);
        mSyncWifiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HCFSMgmtUtils.changeCloudSyncStatus(mContext, isChecked);
                SettingsInfo settingsInfo = new SettingsInfo();
                settingsInfo.setKey(SettingsFragment.PREF_SYNC_WIFI_ONLY);
                settingsInfo.setValue(String.valueOf(isChecked));
                SettingsDAO mSettingsDAO = SettingsDAO.getInstance(getContext());
                mSettingsDAO.update(settingsInfo);
            }
        });

        isChecked = false;
        settingsInfo = mSettingsDAO.get(PREF_NOTIFY_CONN_FAILED_RECOVERY);
        if (settingsInfo != null) {
            isChecked = Boolean.valueOf(settingsInfo.getValue());
        }
        mNotifyConnFailedRecovery.setChecked(isChecked);
        mNotifyConnFailedRecovery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingsDAO mSettingsDAO = SettingsDAO.getInstance(mContext);
                SettingsInfo settingsInfo = new SettingsInfo();
                settingsInfo.setKey(PREF_NOTIFY_CONN_FAILED_RECOVERY);
                settingsInfo.setValue(String.valueOf(isChecked));
                mSettingsDAO.update(settingsInfo);
            }
        });

        String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
        String getRatio = defaultValue.concat("%");

        settingsInfo = mSettingsDAO.get(SettingsFragment.PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO);
        if (settingsInfo != null) {
            getRatio = settingsInfo.getValue().concat("%");
        }
        final String ratio = getRatio;
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

        mChangeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeAccountDialogFragment dialogFragment = ChangeAccountDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager(), ChangeAccountDialogFragment.TAG);
            }
        });

        mTransferContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransferContentDescDialogFragment dialogFragment = TransferContentDescDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager(), TransferContentDescDialogFragment.TAG);
            }
        });

        mAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutFragment fragment = AboutFragment.newInstance();
                fragment.setTargetFragment(SettingsFragment.this, REQUEST_ABOUT_FRAGMENT);
                fragment.show(getFragmentManager(), AboutFragment.TAG);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RATIO) {
            if (resultCode == Activity.RESULT_OK) {
                SettingsDAO mSettingsDAO = SettingsDAO.getInstance(getContext());
                HCFSMgmtUtils.stopNotifyLocalStorageUsedRatioAlarm(mContext);
                HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(mContext);

                String ratio = data.getStringExtra(KEY_RATIO);
                String summary = getString(R.string.settings_local_storage_used_ratio, ratio);
                TextView summaryText = (TextView) mNotifyLocalStorageUsedRatio.findViewById(R.id.notify_local_storage_used_ratio_summary);
                summaryText.setText(summary);

                SettingsInfo settingsInfo = new SettingsInfo();
                settingsInfo.setKey(PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO);
                settingsInfo.setValue(String.valueOf(ratio.replace("%", "")));
                mSettingsDAO.update(settingsInfo);
            }
        } else if (requestCode == REQUEST_ABOUT_FRAGMENT) {
            if (resultCode == Activity.RESULT_OK) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                Boolean show = sharedPreferences.getBoolean(PREF_SHOW_BA_LOGGING_OPTION, false);
                if (show)
                    mBa.setVisibility(View.VISIBLE);
                else
                    mBa.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Boolean show = sharedPreferences.getBoolean(PREF_SHOW_BA_LOGGING_OPTION, false);
        if (show)
            mBa.setVisibility(View.VISIBLE);
        else
            mBa.setVisibility(View.GONE);
    }
}
