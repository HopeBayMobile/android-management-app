package com.hopebaytech.hcfsmgmt.fragment;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	public static final String TAG = SettingsFragment.class.getSimpleName();
	public static final String KEY_PREF_SYNC_WIFI_ONLY = "pref_sync_wifi_only";
	public static final String KEY_PREF_NOTIFY_CONN_FAILED_RECOVERY = "pref_notify_conn_failed_recovery";
	public static final String KEY_PREF_NOTIFY_UPLAOD_COMPLETED = "pref_notify_upload_completed";
	public static final String KEY_PREF_NOTIFY_LOCAL_STORAGE_USED_RATIO = "pref_notify_local_storage_used_ratio";
	public static final String KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED = "pref_is_first_network_connected_received";
	public static final String KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED = "pref_is_first_network_disconnected_received";

	public static SettingsFragment newInstance() {
		SettingsFragment settingsFragment = new SettingsFragment();
		return settingsFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_preferences);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		ListPreference storage_used_ratio = (ListPreference) findPreference(KEY_PREF_NOTIFY_LOCAL_STORAGE_USED_RATIO);
		String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
		String ratio = sharedPreferences.getString(KEY_PREF_NOTIFY_LOCAL_STORAGE_USED_RATIO, defaultValue) + "%%";
		String summary = getString(R.string.settings_local_storage_used_ratio, ratio);
		storage_used_ratio.setSummary(summary);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		toolbar.setTitle(getString(R.string.nav_settings));
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

		Log.d(HCFSMgmtUtils.TAG, "key: " + key);
		if (key.equals(KEY_PREF_SYNC_WIFI_ONLY)) {
			// boolean syncWifiOnlyPref = sharedPreferences.getBoolean(key, false);
			// if (syncWifiOnlyPref) {
			//
			// } else {
			//
			// }
		} else if (key.equals(KEY_PREF_NOTIFY_CONN_FAILED_RECOVERY)) {
			// boolean notifyConnFailedRevoeryPref = sharedPreferences.getBoolean(key, false);
			// if (notifyConnFailedRevoeryPref) {
			//
			// } else {
			//
			// }
		} else if (key.equals(KEY_PREF_NOTIFY_UPLAOD_COMPLETED)) {
			boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(key, false);
			if (notifyUploadCompletedPref) {
				HCFSMgmtUtils.startNotifyUploadCompletedAlarm(getActivity());
			} else {
				HCFSMgmtUtils.stopNotifyUploadCompletedAlarm(getActivity());
			}
		} else if (key.equals(KEY_PREF_NOTIFY_LOCAL_STORAGE_USED_RATIO)) {
			ListPreference storage_used_ratio = (ListPreference) findPreference(KEY_PREF_NOTIFY_LOCAL_STORAGE_USED_RATIO);
			String ratio = storage_used_ratio.getValue() + "%%";
			String summary = getString(R.string.settings_local_storage_used_ratio, ratio);
			storage_used_ratio.setSummary(summary);

		}
	}

}