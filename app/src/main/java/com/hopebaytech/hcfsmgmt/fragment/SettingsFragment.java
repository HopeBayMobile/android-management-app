package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getSimpleName();
    public static final String CLASSNAME = SettingsFragment.class.getSimpleName();

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
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings_preferences);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        ListPreference storage_used_ratio = (ListPreference) findPreference(getString(R.string.pref_notify_local_storage_used_ratio));
        String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
        String ratio = sharedPreferences.getString(getString(R.string.pref_notify_local_storage_used_ratio), defaultValue) + "%%";
        String summary = getString(R.string.settings_local_storage_used_ratio, ratio);
        storage_used_ratio.setSummary(summary);

        Preference changeGoogleAccount = findPreference(getString(R.string.pref_change_account));
        changeGoogleAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ChangeAccountDialogFragment dialogFragment = ChangeAccountDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager(), ChangeAccountDialogFragment.TAG);

                /** Unknown cause results in that dialog show twice, hence we manually dismiss one of them */
                dialogFragment.dismiss();
                return true;
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout settingsLayout = (LinearLayout) inflater.inflate(R.layout.settings_fragment, container, false);
        settingsLayout.addView(super.onCreateView(inflater, container, savedInstanceState));
        return settingsLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onSharedPreferenceChanged", "key=" + key);
        if (key.equals(getString(R.string.pref_sync_wifi_only))) {
            HCFSMgmtUtils.changeCloudSyncStatus(mContext);
        } else if (key.equals(getString(R.string.pref_notify_conn_failed_recovery))) {

        } else if (key.equals(getString(R.string.pref_notify_local_storage_used_ratio))) {
            HCFSMgmtUtils.stopNotifyLocalStorageUsedRatioAlarm(mContext);
            HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(mContext);

            ListPreference storage_used_ratio = (ListPreference) findPreference(key);
            String ratio = storage_used_ratio.getValue() + "%%";
            String summary = getString(R.string.settings_local_storage_used_ratio, ratio);
            storage_used_ratio.setSummary(summary);
        }
//      else if (key.equals(KEY_PREF_NOTIFY_UPLOAD_COMPLETED)) {
//			boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(key, false);
//			if (notifyUploadCompletedPref) {
//				HCFSMgmtUtils.startNotifyUploadCompletedAlarm(mContext);
//			} else {
//				HCFSMgmtUtils.stopNotifyUploadCompletedAlarm(mContext);
//			}
//		}
    }


}