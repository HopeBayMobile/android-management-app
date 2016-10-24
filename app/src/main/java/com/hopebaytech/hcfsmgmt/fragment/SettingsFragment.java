package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
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
    public static final String PREF_ASK_CONFIRM_TURN_OFF_WIFI_ONLY = "pref_ask_confirm_turn_off_wifi_only";
    public static final String PREF_ALLOW_PIN_UNPIN_APPS = "pref_allow_pin_unpin_apps";

    public static final String KEY_RATIO = "ratio";

    public static final int REQUEST_CODE_RATIO = 0;
    public static final int REQUEST_ABOUT_FRAGMENT = 1;
    public static final int REQUEST_CANCEL_WIFI_ONLY = 2;

    private Handler mWorkHandler;

    private View mView;
    private Context mContext;
    private Snackbar mSnackbar;
    private CheckBox mSyncWifiOnly;
    private CheckBox mNotifyConnFailedRecovery;
    private CheckBox mAllowPinUnpinApps;
    private CheckBox mAdvancedSettings;
    private LinearLayout mBa;
    private LinearLayout mAbout;
    private LinearLayout mTransferContent;
    private LinearLayout mAdvancedSettingsLayout;
    private LinearLayout mNotifyLocalStorageUsedRatio;

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

        HandlerThread handlerThread = new HandlerThread(CLASSNAME);
        handlerThread.start();
        mWorkHandler = new Handler(handlerThread.getLooper());
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
        mAdvancedSettings = (CheckBox) view.findViewById(R.id.advanced_settings);
        mAllowPinUnpinApps = (CheckBox) view.findViewById(R.id.allow_pin_unpin_apps);
        mNotifyConnFailedRecovery = (CheckBox) view.findViewById(R.id.notify_conn_failed_recovery);
        mBa = (LinearLayout) view.findViewById(R.id.extra_log_for_ba_layout);
        mAbout = (LinearLayout) view.findViewById(R.id.about);
        mTransferContent = (LinearLayout) view.findViewById(R.id.transfer_content);
        mAdvancedSettingsLayout = (LinearLayout) view.findViewById(R.id.advanced_settings_layout);
        mNotifyLocalStorageUsedRatio = (LinearLayout) view.findViewById(R.id.notify_local_storage_used_ratio);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean isChecked = true;
        SettingsDAO settingsDAO = SettingsDAO.getInstance(getContext());
        SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_SYNC_WIFI_ONLY);
        if (settingsInfo != null) {
            isChecked = Boolean.valueOf(settingsInfo.getValue());
        }
        mSyncWifiOnly.setChecked(isChecked);
        mSyncWifiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSyncWifiOnly(isChecked);

                if (!isChecked) {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                    boolean isAsked = settings.getBoolean(SettingsFragment.PREF_ASK_CONFIRM_TURN_OFF_WIFI_ONLY, false);
                    if (!isAsked) {
                        ConfirmWhenTurnOffWifiOnlyDialogFragment fragment = ConfirmWhenTurnOffWifiOnlyDialogFragment.newInstance();
                        fragment.setTargetFragment(SettingsFragment.this, REQUEST_CANCEL_WIFI_ONLY);
                        fragment.show(getFragmentManager(), ConfirmWhenTurnOffWifiOnlyDialogFragment.TAG);
                    }
                }
            }
        });

        isChecked = false;
        settingsInfo = settingsDAO.get(PREF_NOTIFY_CONN_FAILED_RECOVERY);
        if (settingsInfo != null) {
            isChecked = Boolean.valueOf(settingsInfo.getValue());
        }
        mNotifyConnFailedRecovery.setChecked(isChecked);
        mNotifyConnFailedRecovery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final SettingsInfo settingsInfo = new SettingsInfo();
                settingsInfo.setKey(PREF_NOTIFY_CONN_FAILED_RECOVERY);
                settingsInfo.setValue(String.valueOf(isChecked));

                mWorkHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SettingsDAO settingsDAO = SettingsDAO.getInstance(mContext);
                        settingsDAO.update(settingsInfo);
                    }
                });
            }
        });

        String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
        String getRatio = defaultValue.concat("%");

        settingsInfo = settingsDAO.get(SettingsFragment.PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO);
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
                AboutDialogFragment fragment = AboutDialogFragment.newInstance();
                fragment.setTargetFragment(SettingsFragment.this, REQUEST_ABOUT_FRAGMENT);
                fragment.show(getFragmentManager(), AboutDialogFragment.TAG);
            }
        });

        mAdvancedSettings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mAdvancedSettingsLayout.setVisibility(View.VISIBLE);
                } else {
                    mAdvancedSettingsLayout.setVisibility(View.GONE);
                }
            }
        });

        isChecked = false;
        settingsInfo = settingsDAO.get(PREF_ALLOW_PIN_UNPIN_APPS);
        if (settingsInfo != null) {
            isChecked = Boolean.valueOf(settingsInfo.getValue());
        }
        mAllowPinUnpinApps.setChecked(isChecked);
        mAllowPinUnpinApps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final SettingsInfo settingsInfo = new SettingsInfo();
                settingsInfo.setKey(PREF_ALLOW_PIN_UNPIN_APPS);
                settingsInfo.setValue(String.valueOf(isChecked));

                mWorkHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SettingsDAO settingsDAO = SettingsDAO.getInstance(mContext);
                        settingsDAO.update(settingsInfo);
                    }
                });
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
                HCFSMgmtUtils.stopNotifyLocalStorageUsedRatioAlarm(mContext);
                HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(mContext);

                final String ratio = data.getStringExtra(KEY_RATIO);
                String summary = getString(R.string.settings_local_storage_used_ratio, ratio);
                TextView summaryText = (TextView) mNotifyLocalStorageUsedRatio.findViewById(R.id.notify_local_storage_used_ratio_summary);
                summaryText.setText(summary);

                mWorkHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SettingsDAO settingsDAO = SettingsDAO.getInstance(getContext());
                        SettingsInfo settingsInfo = new SettingsInfo();
                        settingsInfo.setKey(PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO);
                        settingsInfo.setValue(String.valueOf(ratio.replace("%", "")));
                        settingsDAO.update(settingsInfo);
                    }
                });
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
        } else if (requestCode == REQUEST_CANCEL_WIFI_ONLY) {
            if (resultCode == Activity.RESULT_CANCELED) {
                setSyncWifiOnly(true);
            }
        }
    }

    private void setSyncWifiOnly(boolean isChecked) {
        mSyncWifiOnly.setChecked(isChecked);

        HCFSMgmtUtils.changeCloudSyncStatus(mContext, isChecked);
        final SettingsInfo settingsInfo = new SettingsInfo();
        settingsInfo.setKey(SettingsFragment.PREF_SYNC_WIFI_ONLY);
        settingsInfo.setValue(String.valueOf(isChecked));

        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                SettingsDAO settingsDAO = SettingsDAO.getInstance(getContext());
                settingsDAO.update(settingsInfo);
            }
        });
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
