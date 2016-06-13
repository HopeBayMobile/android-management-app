package com.hopebaytech.hcfsmgmt.main;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.fragment.AboutFragment;
import com.hopebaytech.hcfsmgmt.fragment.OverviewFragment;
import com.hopebaytech.hcfsmgmt.fragment.FileMgmtFragment;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.service.HCFSMgmtService;
import com.hopebaytech.hcfsmgmt.utils.BitmapBase64Factory;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String CLASSNAME = getClass().getSimpleName();
    private final int NAV_MENU_SDCARD1_ID = (int) (Math.random() * Integer.MAX_VALUE);
    //    private SDCardBroadcastReceiver sdCardReceiver;
    private Handler mHandler;
    private NavigationView mNavigationView;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private String sdcard1_path;
    private boolean isSDCard1;
    private boolean isExitApp = false;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        init();
    }

    private void init() {

//        /** Initialize default value set in xml/settings_preferences.xml file */
//        PreferenceManager.setDefaultValues(this, R.xml.settings_preferences, false);

        HandlerThread handlerThread = new HandlerThread(MainActivity.class.getSimpleName());
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

//        sdCardReceiver = new SDCardBroadcastReceiver();

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
//        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
//        filter.addDataScheme("file");
//        registerReceiver(sdCardReceiver, filter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setLogo(R.drawable.icon_tera_logo_m_tab);
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
            mDrawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            mNavigationView.findViewById(R.id.nav_dashboard).setOnClickListener(this);
            mNavigationView.findViewById(R.id.nav_app_file).setOnClickListener(this);
            mNavigationView.findViewById(R.id.nav_settings).setOnClickListener(this);
            mNavigationView.findViewById(R.id.nav_about).setOnClickListener(this);
            final AccountDAO accountDAO = AccountDAO.getInstance(MainActivity.this);
            final List<AccountInfo> accountInfoList = accountDAO.getAll();
            if (accountInfoList.size() != 0) {
                mNavigationView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        mNavigationView.removeOnLayoutChangeListener(this);

                        final AccountInfo accountInfo = accountInfoList.get(0);

                        TextView displayName = (TextView) mNavigationView.findViewById(R.id.display_name);
                        displayName.setText(accountInfo.getName());

                        TextView email = (TextView) mNavigationView.findViewById(R.id.email);
                        email.setText(accountInfo.getEmail());

                        final String photoUrl = accountInfo.getImgUrl();
                        if (photoUrl != null) {
                            final ImageView photo = (ImageView) mNavigationView.findViewById(R.id.photo);
                            String imgBase64 = accountInfo.getImgBase64();
                            if (imgBase64 != null) {
                                photo.setImageBitmap(BitmapBase64Factory.decodeBase64(imgBase64));
                            } else {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Bitmap bitmap = null;
                                        try {
                                            URL urlConnection = new URL(photoUrl);
                                            HttpsURLConnection conn = (HttpsURLConnection) urlConnection.openConnection();
                                            conn.setRequestMethod("GET");
                                            conn.connect();

                                            BufferedInputStream bInputStream = new BufferedInputStream(conn.getInputStream());
                                            bitmap = BitmapFactory.decodeStream(bInputStream);

                                            String imgBase64 = BitmapBase64Factory.encodeToBase64(bitmap, Bitmap.CompressFormat.PNG, 100);
                                            accountInfo.setImgBase64(imgBase64);
                                            accountDAO.update(accountInfo);
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
                    }
                });
            }

        }

        // Detect whether sdcard1 exists, if exists, add to left slide menu.
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    BufferedReader br = new BufferedReader(new FileReader(new File("/proc/mounts")));
//                    try {
//                        String line;
//                        while ((line = br.readLine()) != null) {
//                            if (line.contains("sdcard1") && line.contains("fuse")) {
//                                sdcard1_path = line.split("\\s")[1];
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Menu menu = mNavigationView.getMenu();
//                                        for (int i = 0; i < menu.size(); i++) {
//                                            if (menu.getItem(i).getTitle().equals(getString(R.string.nav_system))) {
//                                                MenuItem menuItem = menu.add(R.id.group_system, NAV_MENU_SDCARD1_ID, i + 1,
//                                                        getString(R.string.nav_sdcard1));
//                                                menuItem.setIcon(R.drawable.ic_sd_storage_black);
//                                                break;
//                                            }
//                                        }
//                                    }
//                                });
//                                break;
//                            }
//                        }
//                    } finally {
//                        br.close();
//                    }
//                } catch (Exception e) {
//                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "init", Log.getStackTraceString(e));
//                }
//            }
//        });

        // Inert default value of image, video and audio type to "dataType" table in database
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                DataTypeDAO dataTypeDAO = DataTypeDAO.getInstance(MainActivity.this);
//                if (dataTypeDAO.getCount() == 0) {
//                    DataTypeInfo dataTypeInfo = new DataTypeInfo(MainActivity.this);
//                    dataTypeInfo.setPinned(HCFSMgmtUtils.DEFAULT_PINNED_STATUS);
//
//                    dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_IMAGE);
//                    dataTypeDAO.insert(dataTypeInfo);
//
//                    dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_VIDEO);
//                    dataTypeDAO.insert(dataTypeInfo);
//
//                    dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_AUDIO);
//                    dataTypeDAO.insert(dataTypeInfo);
//                }
//            }
//        });

        // Start NotifyUploadCompletedAlarm if user enables this notification in settings. Others, stop it
//        Intent intent = new Intent(this, HCFSMgmtReceiver.class);
//        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
//        boolean isNotifyUploadCompletedAlarmExist = PendingIntent.getBroadcast(this, HCFSMgmtUtils.REQUEST_CODE_NOTIFY_UPLOAD_COMPLETED, intent,
//                PendingIntent.FLAG_NO_CREATE) != null;
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLOAD_COMPLETED, false);
//        if (notifyUploadCompletedPref) {
//            if (!isNotifyUploadCompletedAlarmExist) {
//                HCFSMgmtUtils.startNotifyUploadCompletedAlarm(this);
//            }
//        } else {
//            if (isNotifyUploadCompletedAlarmExist) {
//                HCFSMgmtUtils.stopNotifyUploadCompletedAlarm(this);
//            }
//        }

        // Start ResetXferAlarm if it doesn't exist
        Intent intent = new Intent(this, HCFSMgmtReceiver.class);
        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
        boolean isResetXferAlarmExist = PendingIntent.getBroadcast(this, RequestCode.RESET_XFER, intent,
                PendingIntent.FLAG_NO_CREATE) != null;
        if (!isResetXferAlarmExist) {
            HCFSMgmtUtils.startResetXferAlarm(this);
        }

        // Start PinDataTypeFileAlarm if it doesn't exist
//        intent = new Intent(this, HCFSMgmtReceiver.class);
//        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
//        intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE);
//        boolean isPinDataTypeAlarmExist = PendingIntent.getBroadcast(this, HCFSMgmtUtils.REQUEST_CODE_PIN_DATA_TYPE_FILE, intent,
//                PendingIntent.FLAG_NO_CREATE) != null;
//        if (!isPinDataTypeAlarmExist) {
//            HCFSMgmtUtils.startPinDataTypeFileAlarm(this);
//        }

        // Start NotifyLocalStorageUsedRatioAlarm if it doesn't exist
        intent = new Intent(this, HCFSMgmtReceiver.class);
        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO);
        boolean isNotifyLocalStorageUsedRatioAlarmExist = PendingIntent.getBroadcast(this,
                RequestCode.NOTIFY_LOCAL_STORAGE_USED_RATIO,
                intent, PendingIntent.FLAG_NO_CREATE) != null;
        if (!isNotifyLocalStorageUsedRatioAlarmExist) {
            HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(this);
        }

        // Start NotifyInsufficientPinSpaceAlarm if it doesn't exist
        intent = new Intent(this, HCFSMgmtReceiver.class);
        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_INSUFFICIENT_PIN_SPACE);
        boolean isNotifyInsufficientPinSpaceAlarmExist = PendingIntent.getBroadcast(this,
                RequestCode.NOTIFY_INSUFFICIENT_PIN_SPACE,
                intent, PendingIntent.FLAG_NO_CREATE) != null;
        if (!isNotifyInsufficientPinSpaceAlarmExist) {
            HCFSMgmtUtils.startNotifyInsufficientPinSpaceAlarm(this);
        }

        // Initialize ViewPager with CustomPagerTabStrip
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        if (mViewPager != null) {
            mViewPager.setOffscreenPageLimit(3);
            PagerTabStrip pagerTabStrip = (PagerTabStrip) mViewPager.findViewById(R.id.pager_tab_strip);
            pagerTabStrip.setTabIndicatorColor(ContextCompat.getColor(this, R.color.C2));
            mViewPager.setAdapter(mPagerAdapter);
        }

        boolean insufficientPinSpace = getIntent().getBooleanExtra(HCFSMgmtUtils.BUNDLE_KEY_INSUFFICIENT_PIN_SPACE, false);
        if (insufficientPinSpace) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(500);
                            Fragment fragment = mPagerAdapter.getFragment(0);
                            if (fragment != null && fragment.isVisible()) {
                                Thread.sleep(500);
                                mViewPager.setCurrentItem(1, true);
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

    }

    @Override
    public void onClick(View v) {
        /** Handle navigation view item clicks here. */
        int id = v.getId();
        if (id == R.id.nav_dashboard) {
            mViewPager.setCurrentItem(0, true);
        } else if (id == R.id.nav_app_file) {
            isSDCard1 = false;
            mViewPager.setCurrentItem(1, true);
        } else if (id == R.id.nav_settings) {
            mViewPager.setCurrentItem(2, true);
        } else if (id == R.id.nav_about) {
            mViewPager.setCurrentItem(3, true);
        } else if (id == NAV_MENU_SDCARD1_ID) {
            isSDCard1 = true;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {

        private String[] titleArray;
        private SparseArray<Fragment> pageReference;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            titleArray = getResources().getStringArray(R.array.nav_title);
            pageReference = new SparseArray<>();
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;
            String title = titleArray[i];
            if (title.equals(getString(R.string.nav_overview))) {
                fragment = OverviewFragment.newInstance();
            } else if (title.equals(getString(R.string.nav_app_file))) {
                fragment = FileMgmtFragment.newInstance(false);
            } else if (title.equals(getString(R.string.nav_settings))) {
                fragment = SettingsFragment.newInstance();
            } else if (title.equals(getString(R.string.nav_about))) {
                fragment = AboutFragment.newInstance();
            } else {
                fragment = new Fragment();
            }
            pageReference.put(i, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            pageReference.remove(position);
        }

        @Override
        public int getCount() {
            return titleArray.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleArray[position];
        }

        @Nullable
        public Fragment getFragment(int position) {
            return pageReference.get(position);
        }

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            int position = mViewPager.getCurrentItem();
            Fragment fragment = mPagerAdapter.getFragment(position);
            if (fragment instanceof FileMgmtFragment) {
                boolean isProcessed = ((FileMgmtFragment) fragment).onBackPressed();
                if (!isProcessed) {
                    exitApp();
                }
            } else {
                exitApp();
            }
        }
    }

    private void exitApp() {
        if (isExitApp) {
            finish();
        } else {
            isExitApp = true;
            String message = getString(R.string.dashboard_snackbar_exit_app);
            View contentView = findViewById(android.R.id.content);
            if (contentView != null) {
                Snackbar.make(contentView, message, Snackbar.LENGTH_SHORT).show();
            }
            Timer exitTimer = new Timer();
            exitTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExitApp = false;
                }
            }, 2000);
        }
    }

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
//                        Menu menu = mNavigationView.getMenu();
//                        MenuItem menuItem = menu.add(R.id.group_system, NAV_MENU_SDCARD1_ID, 2, getString(R.string.nav_sdcard1));
//                        menuItem.setIcon(R.drawable.ic_sd_storage_black);
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
//        unregisterReceiver(sdCardReceiver);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int position = mViewPager.getCurrentItem();
        Fragment fragment = mPagerAdapter.getFragment(position);
        if (fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
