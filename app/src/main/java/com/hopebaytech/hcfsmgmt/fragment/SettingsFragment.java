package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
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
	public static final String KEY_PREF_SYNC_WIFI_ONLY = "pref_sync_wifi_only";
	public static final String KEY_PREF_NOTIFY_CONN_FAILED_RECOVERY = "pref_notify_conn_failed_recovery";
	public static final String KEY_PREF_NOTIFY_UPLOAD_COMPLETED = "pref_notify_upload_completed";
	public static final String KEY_PREF_NOTIFY_LOCAL_STORAGE_USED_RATIO = "pref_notify_local_storage_used_ratio";
	public static final String KEY_PREF_NOTIFY_IS_LOCAL_STORAGE_USED_RATIO_ALREADY_NOTIFIED = "pref_notify_is_local_storage_used_ratio_already_notified";
	public static final String KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED = "pref_is_first_network_connected_received";
	public static final String KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED = "pref_is_first_network_disconnected_received";

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		ListPreference storage_used_ratio = (ListPreference) findPreference(KEY_PREF_NOTIFY_LOCAL_STORAGE_USED_RATIO);
		String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
		String ratio = sharedPreferences.getString(KEY_PREF_NOTIFY_LOCAL_STORAGE_USED_RATIO, defaultValue) + "%%";
		String summary = getString(R.string.settings_local_storage_used_ratio, ratio);
		storage_used_ratio.setSummary(summary);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout settingsLayout = (LinearLayout) inflater.inflate(R.layout.settings_fragment, container, false);
        settingsLayout.addView(super.onCreateView(inflater, container, savedInstanceState));
        return settingsLayout;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings_preferences);
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
		if (key.equals(KEY_PREF_SYNC_WIFI_ONLY)) {
			HCFSMgmtUtils.detectNetworkAndSyncDataToCloud(mContext);
		} else if (key.equals(KEY_PREF_NOTIFY_CONN_FAILED_RECOVERY)) {
			
		} else if (key.equals(KEY_PREF_NOTIFY_UPLOAD_COMPLETED)) {
			boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(key, false);
			if (notifyUploadCompletedPref) {
				HCFSMgmtUtils.startNotifyUploadCompletedAlarm(mContext);
			} else {
				HCFSMgmtUtils.stopNotifyUploadCompletedAlarm(mContext);
			}
		} else if (key.equals(KEY_PREF_NOTIFY_LOCAL_STORAGE_USED_RATIO)) {
			HCFSMgmtUtils.stopNotifyLocalStorageUsedRatioAlarm(mContext);
			HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(mContext);

			ListPreference storage_used_ratio = (ListPreference) findPreference(key);
			String ratio = storage_used_ratio.getValue() + "%%";
			String summary = getString(R.string.settings_local_storage_used_ratio, ratio);
			storage_used_ratio.setSummary(summary);
		}
		
	}

}