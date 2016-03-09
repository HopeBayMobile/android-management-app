package com.hopebaytech.hcfsmgmt.main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.fragment.AboutFragment;
import com.hopebaytech.hcfsmgmt.fragment.FileManagementFragment;
import com.hopebaytech.hcfsmgmt.fragment.HomepageFragment;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.service.HCFSMgmtService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final String CLASSNAME = getClass().getSimpleName();
    private final int NAV_MENU_SDCARD1_ID = (int) (Math.random() * Integer.MAX_VALUE);
    private SDCardBroadcastReceiver sdCardReceiver;
    private Toolbar toolbar;
    private Handler mHandler;
    private NavigationView mNavigationView;
    private String sdcard1_path;
    private boolean isExitApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        /** Initialize default value set in xml/settings_preferences.xml file */
        PreferenceManager.setDefaultValues(this, R.xml.settings_preferences, false);

        HandlerThread handlerThread = new HandlerThread(MainActivity.class.getSimpleName());
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        sdCardReceiver = new SDCardBroadcastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(sdCardReceiver, filter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.terafonn_logo);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        if (getIntent().getExtras() != null) {
            mNavigationView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mNavigationView.removeOnLayoutChangeListener(this);

                    TextView displayName = (TextView) mNavigationView.findViewById(R.id.display_name);
                    displayName.setText(getIntent().getStringExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME));

                    TextView email = (TextView) mNavigationView.findViewById(R.id.email);
                    email.setText(getIntent().getStringExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_EMAIL));

                    final Uri photoUri = (Uri) getIntent().getParcelableExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_PHOTO_URI);
                    if (photoUri != null) {
                        final ImageView photo = (ImageView) mNavigationView.findViewById(R.id.photo);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap = null;
                                try {
                                    URL urlConnection = new URL(photoUri.toString());
                                    HttpsURLConnection conn = (HttpsURLConnection) urlConnection.openConnection();
                                    conn.setRequestMethod("GET");
                                    conn.connect();
                                    BufferedInputStream bInputStream = new BufferedInputStream(conn.getInputStream());
                                    bitmap = BitmapFactory.decodeStream(bInputStream);
                                } catch (Exception e) {
                                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "init", Log.getStackTraceString(e));
                                }
                                final Bitmap photoBmp = bitmap;
                                if (photoBmp != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            photo.setImageBitmap(photoBmp);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }

        /** Detect whether sdcard1 exists, if exists, add to left slide menu. */
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(new File("/proc/mounts")));
                    try {
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (line.contains("sdcard1") && line.contains("fuse")) {
                                sdcard1_path = line.split("\\s")[1];
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Menu menu = mNavigationView.getMenu();
                                        for (int i = 0; i < menu.size(); i++) {
                                            if (menu.getItem(i).getTitle().equals(getString(R.string.nav_default_mountpoint))) {
                                                MenuItem menuItem = menu.add(R.id.group_mountpoint, NAV_MENU_SDCARD1_ID, i + 1,
                                                        getString(R.string.nav_sdcard1));
                                                menuItem.setIcon(R.drawable.ic_sd_storage_black);
                                                break;
                                            }
                                        }
                                    }
                                });
                                break;
                            }
                        }
                    } finally {
                        br.close();
                    }
                } catch (Exception e) {
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "init", Log.getStackTraceString(e));
                }
            }
        });

        /** Inert default value of image, video and audio type to "datatype" table in database */
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                DataTypeDAO dataTypeDAO = new DataTypeDAO(MainActivity.this);
                if (dataTypeDAO.getCount() == 0) {
                    DataTypeInfo dataTypeInfo = new DataTypeInfo(MainActivity.this);
                    dataTypeInfo.setPinned(HCFSMgmtUtils.DEFAULT_PINNED_STATUS);

                    dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_IMAGE);
                    dataTypeDAO.insert(dataTypeInfo);

                    dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_VIDEO);
                    dataTypeDAO.insert(dataTypeInfo);

                    dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_AUDIO);
                    dataTypeDAO.insert(dataTypeInfo);
                }
            }
        });

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, HomepageFragment.newInstance(), HomepageFragment.TAG);
        ft.commit();

        /** Start NotifyUploadCompletedAlarm if user enables this notification in settings. Others, stop it */
        Intent intent = new Intent(this, HCFSMgmtReceiver.class);
        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
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

        /** Start ResetXferAlarm if it doesn't exist */
        intent = new Intent(this, HCFSMgmtReceiver.class);
        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
        boolean isResetXferAlarmExist = PendingIntent.getBroadcast(this, HCFSMgmtUtils.REQUEST_CODE_RESET_XFER, intent,
                PendingIntent.FLAG_NO_CREATE) != null;
        if (!isResetXferAlarmExist) {
            HCFSMgmtUtils.startResetXferAlarm(this);
        }

        /** Start PinDataTypeFileAlarm if it doesn't exist */
        intent = new Intent(this, HCFSMgmtReceiver.class);
        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE);
        boolean isPinDataTypeAlarmExist = PendingIntent.getBroadcast(this, HCFSMgmtUtils.REQUEST_CODE_PIN_DATA_TYPE_FILE, intent,
                PendingIntent.FLAG_NO_CREATE) != null;
        if (!isPinDataTypeAlarmExist) {
            HCFSMgmtUtils.startPinDataTypeFileAlarm(this);
        } else {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "PinDataTypeFileAlarm already exists");
        }

        /** Start NotifyLocalStorageUsedRatioAlarm if it doesn't exist */
        intent = new Intent(this, HCFSMgmtReceiver.class);
        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO);
        boolean isNotifyLocalStorageUsedRatiolarmExist = PendingIntent.getBroadcast(this,
                HCFSMgmtUtils.REQUEST_CODE_NOTIFY_LOCAL_STORAGE_USED_RATIO,
                intent, PendingIntent.FLAG_NO_CREATE) != null;
        if (!isNotifyLocalStorageUsedRatiolarmExist) {
            HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(this);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager fm = getFragmentManager();
            HomepageFragment homepageFragment = (HomepageFragment) fm.findFragmentByTag(HomepageFragment.TAG);
            FileManagementFragment fileManagementFragment = (FileManagementFragment) fm.findFragmentByTag(FileManagementFragment.TAG);
            SettingsFragment settingsFragment = (SettingsFragment) fm.findFragmentByTag(SettingsFragment.TAG);
            AboutFragment aboutFragment = (AboutFragment) fm.findFragmentByTag(AboutFragment.TAG);
            if (homepageFragment != null && homepageFragment.isVisible()) {
                if (isExitApp) {
                    finish();
                } else {
                    isExitApp = true;
                    String message = getString(R.string.home_page_snackbar_exit_app);
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
                    Timer exitTimer = new Timer();
                    exitTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            isExitApp = false;
                        }
                    }, 2000);
                }
            } else if (fileManagementFragment != null && fileManagementFragment.isVisible()) {
                fileManagementFragment.onBackPressed();
            } else if (settingsFragment != null && settingsFragment.isVisible()) {
                settingsFragment.onBackPressed();
            } else if (aboutFragment != null && aboutFragment.isVisible()) {
                aboutFragment.onBackPressed();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (id == R.id.nav_homepage) {
            ft.replace(R.id.fragment_container, HomepageFragment.newInstance(), HomepageFragment.TAG);
        } else if (id == R.id.nav_default_mountpoint) {
            boolean isSDCard1 = false;
            ft.replace(R.id.fragment_container, FileManagementFragment.newInstance(isSDCard1), FileManagementFragment.TAG);
        } else if (id == R.id.nav_settings) {
            ft.replace(R.id.fragment_container, SettingsFragment.newInstance(), SettingsFragment.TAG);
        } else if (id == R.id.nav_about) {
            ft.replace(R.id.fragment_container, AboutFragment.newInstance(), AboutFragment.TAG);
        } else if (id == NAV_MENU_SDCARD1_ID) {
            boolean isSDCard1 = true;
            ft.replace(R.id.fragment_container, FileManagementFragment.newInstance(isSDCard1, sdcard1_path), FileManagementFragment.TAG);
        }
        ft.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		// if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
//		// Fragment fragment = getFragmentManager().findFragmentByTag(FileManagementFragment.TAG);
//		// if (fragment != null && fragment.isVisible()) {
//		// if (fragment instanceof FileManagementFragment) {
//		// FileManagementFragment fileManagementFragment = (FileManagementFragment) fragment;
//		// if (fileManagementFragment.onBackPressed())
//		// return true;
//		// }
//		//// else if (fragment instanceof InternalFileMgmtFragment) {
//		//// InternalFileMgmtFragment internalFileMgmtFragment = (InternalFileMgmtFragment) fragment;
//		//// if (internalFileMgmtFragment.onBackPressed())
//		//// return true;
//		//// }
//		// }
//		// }
//		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
//			Fragment fragment = getFragmentManager().findFragmentByTag(FileManagementFragment.TAG);
//			if (fragment != null && fragment.isVisible()) {
//				if (fragment instanceof FileManagementFragment) {
//					FileManagementFragment fileManagementFragment = (FileManagementFragment) fragment;
//					if (fileManagementFragment.onBackPressed())
//						return true;
//				}
//				// else if (fragment instanceof InternalFileMgmtFragment) {
//				// InternalFileMgmtFragment internalFileMgmtFragment = (InternalFileMgmtFragment) fragment;
//				// if (internalFileMgmtFragment.onBackPressed())
//				// return true;
//				// }
//			}
//		}
//		return super.onKeyDown(keyCode, event);
//	}

    public class SDCardBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "sdcard_action=" + action);
            sdcard1_path = intent.getData().getSchemeSpecificPart().replace("///", "/");
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Menu menu = mNavigationView.getMenu();
                        MenuItem menuItem = menu.add(R.id.group_mountpoint, NAV_MENU_SDCARD1_ID, 2, getString(R.string.nav_sdcard1));
                        menuItem.setIcon(R.drawable.ic_sd_storage_black);
                    }
                });
            } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Menu menu = mNavigationView.getMenu();
                        menu.removeItem(NAV_MENU_SDCARD1_ID);
                    }
                });
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intentService = new Intent(this, HCFSMgmtService.class);
        intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_ONGOING_NOTIFICATION);
        intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_ONGOING, false);
        startService(intentService);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intentService = new Intent(this, HCFSMgmtService.class);
        intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_ONGOING_NOTIFICATION);
        intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_ONGOING, true);
        startService(intentService);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(sdCardReceiver);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /**
         * Must override this function and comment out "super.onSaveInstanceState(outState)" in order not to save fragment state. For issue that
         * getActivity() will get null when backing to app from background (long time). In this situation, fragment is already detached from activity,
         * so that getActivity() cannot get instance.
         */
        // super.onSaveInstanceState(outState);
    }

}
