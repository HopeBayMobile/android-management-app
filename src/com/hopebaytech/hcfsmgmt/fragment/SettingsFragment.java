package com.hopebaytech.hcfsmgmt.fragment;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	public static final String KEY_PREF_SYNC_WIFI_ONLY = "pref_sync_wifi_only";
	public static final String KEY_PREF_NOTIFY_CONN_FAILED_RECOVERY = "pref_notify_conn_failed_recovery";
	public static final String KEY_PREF_NOTIFY_UPLAOD_COMPLETED = "pref_notify_upload_completed";
	public static final String KEY_PREF_NOTIFY_LACK_OF_LOCAL_SPACE = "pref_notify_lack_of_local_space";
	public static final String KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED = "pref_is_first_network_connected_received";
	public static final String KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED = "pref_is_first_network_disconnected_received";
	private Context context;

	public SettingsFragment(Context context) {
		this.context = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		if (key.equals(KEY_PREF_SYNC_WIFI_ONLY)) {
//			boolean syncWifiOnlyPref = sharedPreferences.getBoolean(key, false);
//			if (syncWifiOnlyPref) {
//
//			} else {
//
//			}
		} else if (key.equals(KEY_PREF_NOTIFY_CONN_FAILED_RECOVERY)) {
//			boolean notifyConnFailedRevoeryPref = sharedPreferences.getBoolean(key, false);
//			if (notifyConnFailedRevoeryPref) {
//
//			} else {
//
//			}
		} else if (key.equals(KEY_PREF_NOTIFY_UPLAOD_COMPLETED)) {
			boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(key, false);
			if (notifyUploadCompletedPref) {
				HCFSMgmtUtils.startNotifyUploadCompletedAlarm(context);
			} else {
				HCFSMgmtUtils.stopNotifyUploadCompletedAlarm(context);
			}
		} else if (key.equals(KEY_PREF_NOTIFY_LACK_OF_LOCAL_SPACE)) {
			boolean notifyLackOfLocalSpacePref = sharedPreferences.getBoolean(key, false);
			if (notifyLackOfLocalSpacePref) {

			} else {

			}
		}

	}

}