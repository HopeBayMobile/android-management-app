package com.hopebaytech.hcfsmgmt.fragment;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.TeraIntent;
import com.hopebaytech.hcfsmgmt.main.HCFSMgmtReceiver;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.BitmapBase64Factory;
import com.hopebaytech.hcfsmgmt.utils.GoogleSilentAuthProxy;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/19.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = MainFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();
    private final int NAV_MENU_SDCARD1_ID = (int) (Math.random() * Integer.MAX_VALUE);

    // private SDCardBroadcastReceiver sdCardReceiver;

    private View mView;
    private Context mContext;
    private ViewPager mViewPager;
    private NavigationView mNavigationView;
    private PagerAdapter mPagerAdapter;
    private DrawerLayout mDrawerLayout;

    private Handler mWorkHandler;
    private Handler mUiHandler;

    private String sdcard1_path;
    private boolean isSDCard1;
    private boolean isExitApp = false;
    private String toViewPagerIndex = "ViewPagerIndex";

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mUiHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setLogo(R.drawable.icon_tera_logo_m_tab);
            toolbar.setTitle("");
            ((AppCompatActivity) mContext).setSupportActionBar(toolbar);
        }

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);

        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle((AppCompatActivity) mContext,
                    mDrawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
            mDrawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        mNavigationView = (NavigationView) view.findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            mNavigationView.findViewById(R.id.nav_dashboard).setOnClickListener(this);
            mNavigationView.findViewById(R.id.nav_app_file).setOnClickListener(this);
            mNavigationView.findViewById(R.id.nav_settings).setOnClickListener(this);
            mNavigationView.findViewById(R.id.nav_help).setOnClickListener(this);
            final AccountDAO accountDAO = AccountDAO.getInstance(mContext);
            if (accountDAO.getCount() != 0) {
                mNavigationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        mNavigationView.removeOnLayoutChangeListener(this);

                        final AccountInfo accountInfo = accountDAO.getFirst();

                        TextView displayName = (TextView) mNavigationView.findViewById(R.id.display_name);
                        displayName.setText(accountInfo.getName());

                        TextView email = (TextView) mNavigationView.findViewById(R.id.email);
                        email.setText(accountInfo.getEmail());

                        // Icon expiring time doesn't reach, use cache icon base64 instead of download
                        // the latest icon
                        final ImageView iconImage = (ImageView) mNavigationView.findViewById(R.id.icon);
                        if (System.currentTimeMillis() <= accountInfo.getImgExpiringTime()) {
                            String imgBase64 = accountInfo.getImgBase64();
                            if (imgBase64 != null) {
                                iconImage.setImageBitmap(BitmapBase64Factory.decodeBase64(imgBase64));
                            }
                            return;
                        }

                        // Show the latest Google user icon on NavigationView
                        mWorkHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String serverClientId = MgmtCluster.getServerClientId();
                                GoogleSilentAuthProxy silentAuthProxy = new GoogleSilentAuthProxy(mContext,
                                        serverClientId, new GoogleSilentAuthProxy.OnAuthListener() {
                                    @Override
                                    public void onAuthSuccessful(GoogleSignInResult result, GoogleApiClient googleApiClient) {

                                        if (result == null || !result.isSuccess()) {
                                            return;
                                        }

                                        GoogleSignInAccount acct = result.getSignInAccount();
                                        Logs.w(CLASSNAME, "onAuthSuccessful", "acct=" + acct);
                                        if (acct == null) {
                                            return;
                                        }

                                        String iconUrl = null;
                                        if (acct.getPhotoUrl() != null) {
                                            Uri iconUri = acct.getPhotoUrl();
                                            if (iconUri == null) {
                                                return;
                                            } else {
                                                iconUrl = iconUri.toString();
                                            }
                                        }

                                        final String finalIconUrl = iconUrl;
                                        mWorkHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                final Bitmap iconBitmap = downloadUserIconWithRetry(finalIconUrl);
                                                if (iconBitmap != null) {
                                                    mUiHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            iconImage.setImageBitmap(iconBitmap);
                                                        }
                                                    });

                                                    String imgBase64 = BitmapBase64Factory.encodeToBase64(
                                                            iconBitmap,
                                                            Bitmap.CompressFormat.PNG,
                                                            100);
                                                    accountInfo.setImgBase64(imgBase64);
                                                    accountInfo.setImgExpiringTime(System.currentTimeMillis() + Interval.DAY);
                                                    accountDAO.update(accountInfo);
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onAuthFailed() {
                                        Logs.e(CLASSNAME, "onAuthFailed", null);
                                    }
                                });
                                silentAuthProxy.auth();
                            }
                        });

                    }
                });
            }

        }
    }

    private Bitmap downloadUserIconWithRetry(String iconUrl) {
        Bitmap bitmap = null;
        for (int i = 0; i < 3; i++) {
            bitmap = downloadUserIcon(iconUrl);
            if (bitmap != null) {
                break;
            }
        }
        return bitmap;
    }

    private Bitmap downloadUserIcon(String iconUrl) {
        Bitmap bitmap = null;
        if (iconUrl != null) {
            try {
                URL urlConnection = new URL(iconUrl);
                HttpsURLConnection conn = (HttpsURLConnection) urlConnection.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedInputStream bInputStream = new BufferedInputStream(conn.getInputStream());
                bitmap = BitmapFactory.decodeStream(bInputStream);
            } catch (Exception e) {
                Logs.e(CLASSNAME, "run", Log.getStackTraceString(e));
            }
        }
        return bitmap;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {

        HandlerThread handlerThread = new HandlerThread(MainFragment.class.getSimpleName());
        handlerThread.start();
        mWorkHandler = new Handler(handlerThread.getLooper());

//        sdCardReceiver = new SDCardBroadcastReceiver();

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
//        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
//        filter.addDataScheme("file");
//        registerReceiver(sdCardReceiver, filter);

        // Detect whether sdcard1 exists, if exists, add to left slide menu.
//        mWorkHandler.post(new Runnable() {
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

        // Start ResetXferAlarm if it doesn't exist
        Intent intent = new Intent(mContext, HCFSMgmtReceiver.class);
//        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
        intent.setAction(TeraIntent.ACTION_RESET_XFER);
        boolean isResetXferAlarmExist = PendingIntent.getBroadcast(mContext, RequestCode.RESET_XFER,
                intent, PendingIntent.FLAG_NO_CREATE) != null;
        if (!isResetXferAlarmExist) {
            HCFSMgmtUtils.startResetXferAlarm(mContext);
        }

        // Start NotifyLocalStorageUsedRatioAlarm if it doesn't exist
        intent = new Intent(mContext, HCFSMgmtReceiver.class);
//        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
        intent.setAction(TeraIntent.ACTION_NOTIFY_LOCAL_STORAGE_USED_RATIO);
//        intent.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO);
        boolean isNotifyLocalStorageUsedRatioAlarmExist = PendingIntent.getBroadcast(mContext,
                RequestCode.NOTIFY_LOCAL_STORAGE_USED_RATIO,
                intent, PendingIntent.FLAG_NO_CREATE) != null;
        if (!isNotifyLocalStorageUsedRatioAlarmExist) {
            HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(mContext);
        }

        // Start NotifyInsufficientPinSpaceAlarm if it doesn't exist
        intent = new Intent(mContext, HCFSMgmtReceiver.class);
//        intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
        intent.setAction(TeraIntent.ACTION_NOTIFY_INSUFFICIENT_PIN_SPACE);
//        intent.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_INSUFFICIENT_PIN_SPACE);
        boolean isNotifyInsufficientPinSpaceAlarmExist = PendingIntent.getBroadcast(mContext,
                RequestCode.NOTIFY_INSUFFICIENT_PIN_SPACE,
                intent, PendingIntent.FLAG_NO_CREATE) != null;
        if (!isNotifyInsufficientPinSpaceAlarmExist) {
            HCFSMgmtUtils.startNotifyInsufficientPinSpaceAlarm(mContext);
        }

        // Initialize ViewPager with CustomPagerTabStrip
        mPagerAdapter = new PagerAdapter(((AppCompatActivity) mContext).getSupportFragmentManager());
        if (mViewPager != null) {
            mViewPager.setOffscreenPageLimit(3);
            PagerTabStrip pagerTabStrip = (PagerTabStrip) mViewPager.findViewById(R.id.pager_tab_strip);
            pagerTabStrip.setTabIndicatorColor(ContextCompat.getColor(mContext, R.color.C2));
            mViewPager.setAdapter(mPagerAdapter);
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
        } else if (id == R.id.nav_help) {
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
            } else if (title.equals(getString(R.string.nav_help))) {
                fragment = HelpFragment.newInstance();
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
            ((AppCompatActivity) mContext).finish();
        } else {
            isExitApp = true;
            String message = getString(R.string.overview_snackbar_exit_app);
            Snackbar.make(mView, message, Snackbar.LENGTH_SHORT).show();
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
            Logs.d(CLASSNAME, "onReceive", "sdcard_action=" + action);
            sdcard1_path = intent.getData().getSchemeSpecificPart().replace("///", "/");
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        Menu menu = mNavigationView.getMenu();
//                        MenuItem menuItem = menu.add(R.id.group_system, NAV_MENU_SDCARD1_ID, 2, getString(R.string.nav_sdcard1));
//                        menuItem.setIcon(R.drawable.ic_sd_storage_black);
                    }
                });
            } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                mUiHandler.post(new Runnable() {
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
    public void onStart() {
        super.onStart();
        Intent intentService = new Intent(mContext, TeraMgmtService.class);
        intentService.setAction(TeraIntent.ACTION_ONGOING_NOTIFICATION);
//        intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_ONGOING_NOTIFICATION);
//        intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_ONGOING_NOTIFICATION);
        intentService.putExtra(TeraIntent.KEY_ONGOING, false);

        // Jump to App/File page if app is launched from insufficient notification
        Bundle args = getArguments();
        if (args != null) {
            boolean insufficientPinSpace = args.getBoolean(HCFSMgmtUtils.BUNDLE_KEY_INSUFFICIENT_PIN_SPACE, false);
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
            // Jump to specific page
            final int toViewPager = args.getInt(toViewPagerIndex, -1);
            if (toViewPager > -1 && toViewPager < mPagerAdapter.getCount()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(500);
                                Fragment fragment = mPagerAdapter.getFragment(0);
                                if (fragment != null && fragment.isVisible()) {
                                    Thread.sleep(500);
                                    mViewPager.setCurrentItem(toViewPager, true);
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

        mContext.startService(intentService);
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent intentService = new Intent(mContext, TeraMgmtService.class);
        intentService.setAction(TeraIntent.ACTION_ONGOING_NOTIFICATION);
//        intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_ONGOING_NOTIFICATION);
        intentService.putExtra(TeraIntent.KEY_ONGOING, true);
        mContext.startService(intentService);
    }

    @Override
    public void onDestroy() {
//        unregisterReceiver(sdCardReceiver);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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
