package com.hopebaytech.hcfsmgmt.main;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.AboutFragment;
import com.hopebaytech.hcfsmgmt.fragment.FileManagementFragment;
import com.hopebaytech.hcfsmgmt.fragment.HomepageFragment;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize();
	}

	private void initialize() {

		// Initialize default value set in xml/settings_preferences.xml file
		PreferenceManager.setDefaultValues(this, R.xml.settings_preferences, false);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.fragment_container, new HomepageFragment());
		ft.commit();

		Intent intent = new Intent(this, HCFSMgmtReceiver.class);
		intent.setAction(HCFSMgmtUtils.HCFS_MANAGEMENT_ALARM_INTENT_ACTION);
		boolean isNotifyUploadCompletedAlarmExist = PendingIntent.getBroadcast(this, HCFSMgmtUtils.REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED, intent,
				PendingIntent.FLAG_NO_CREATE) != null;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLAOD_COMPLETED, false);
		if (notifyUploadCompletedPref) {
			if (!isNotifyUploadCompletedAlarmExist) {
				HCFSMgmtUtils.startNotifyUploadCompletedAlarm(this);
			}
		} else {
			if (isNotifyUploadCompletedAlarmExist) {
				HCFSMgmtUtils.stopNotifyUploadCompletedAlarm(this);
			}
		}

	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.

		int id = item.getItemId();

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		if (id == R.id.nav_homepage) {
			String title = getString(R.string.nav_homepage);
			toolbar.setTitle(title);
			ft.replace(R.id.fragment_container, new HomepageFragment(), title);
			// Toast.makeText(this, getString(R.string.nav_homepage),
			// Toast.LENGTH_SHORT).show();
		} else if (id == R.id.nav_default_mountpoint) {
			String title = getString(R.string.nav_default_mountpoint);
			toolbar.setTitle(title);
			ft.replace(R.id.fragment_container, FileManagementFragment.newInstance(), title);
			// Toast.makeText(this, getString(R.string.nav_default_mountpoint),
			// Toast.LENGTH_SHORT).show();
		} else if (id == R.id.nav_add_mountpoint) {
			Intent intent = new Intent(this, AddMountPointActivity.class);
			startActivity(intent);
			
//			String title = getString(R.string.nav_default_mountpoint);
//			toolbar.setTitle(title);
//			ft.replace(R.id.fragment_container, FileManagementFragment.newInstance(), title);
			// Toast.makeText(this, getString(R.string.nav_default_mountpoint),
			// Toast.LENGTH_SHORT).show();
		} else if (id == R.id.nav_settings) {
			String title = getString(R.string.nav_settings);
			toolbar.setTitle(title);
			ft.replace(R.id.fragment_container, new SettingsFragment(this), title);
			// Toast.makeText(this, getString(R.string.nav_settings),
			// Toast.LENGTH_SHORT).show();
		} else if (id == R.id.nav_about) {
			String title = getString(R.string.nav_about);
			toolbar.setTitle(title);
			ft.replace(R.id.fragment_container, new AboutFragment(), title);
			// Toast.makeText(this, getString(R.string.nav_about),
			// Toast.LENGTH_SHORT).show();
		}
		ft.commit();

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			String tag = getString(R.string.nav_default_mountpoint);
			Fragment fragment = getFragmentManager().findFragmentByTag(tag);
			if (fragment != null && fragment.isVisible()) {
				if (fragment instanceof FileManagementFragment) {
					FileManagementFragment fileManagementFragment = (FileManagementFragment) fragment;
					if (fileManagementFragment.onBackPressed())
						return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}