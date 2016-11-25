package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;
import com.hopebaytech.hcfsmgmt.main.HCFSMgmtReceiver;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/19.
 */
public class MainFragment extends Fragment {

    public static final String TAG = MainFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();

    private View mView;
    private Context mContext;
    private ViewPager mViewPager;
    private ImageView mUserIcon;

    private HandlerThread mWorkerThread;
    private Handler mWorkHandler;
    private Handler mUiHandler;
    private PagerAdapter mPagerAdapter;

    private boolean isExitApp = false;
    private Bundle mArguments;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mArguments = getArguments();
        mContext = getActivity();
        ((MainActivity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mUiHandler = new Handler();
        mWorkerThread = new HandlerThread(MainFragment.class.getSimpleName());
        mWorkerThread.start();
        mWorkHandler = new Handler(mWorkerThread.getLooper());
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
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mUserIcon = (ImageView) view.findViewById(R.id.user_icon);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        mWorkerThread = new HandlerThread(CLASSNAME);
        mWorkerThread.start();
        mWorkHandler = new Handler(mWorkerThread.getLooper());

        // Start ResetXferAlarm if it doesn't exist
        Intent intent = new Intent(mContext, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_RESET_DATA_XFER);
        boolean isResetXferAlarmExist = PendingIntent.getBroadcast(mContext, RequestCode.RESET_XFER,
                intent, PendingIntent.FLAG_NO_CREATE) != null;
        if (!isResetXferAlarmExist) {
            HCFSMgmtUtils.startResetXferAlarm(mContext);
        }

        // Start NotifyLocalStorageUsedRatioAlarm if it doesn't exist
        intent = new Intent(mContext, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_NOTIFY_LOCAL_STORAGE_USED_RATIO);
        boolean isNotifyLocalStorageUsedRatioAlarmExist = PendingIntent.getBroadcast(mContext,
                RequestCode.NOTIFY_LOCAL_STORAGE_USED_RATIO,
                intent, PendingIntent.FLAG_NO_CREATE) != null;
        if (!isNotifyLocalStorageUsedRatioAlarmExist) {
            HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(mContext);
        }

        // Start NotifyInsufficientPinSpaceAlarm if it doesn't exist
        intent = new Intent(mContext, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_NOTIFY_INSUFFICIENT_PIN_SPACE);
        boolean isNotifyInsufficientPinSpaceAlarmExist = PendingIntent.getBroadcast(mContext,
                RequestCode.NOTIFY_INSUFFICIENT_PIN_SPACE,
                intent, PendingIntent.FLAG_NO_CREATE) != null;
        if (!isNotifyInsufficientPinSpaceAlarmExist) {
            HCFSMgmtUtils.startNotifyInsufficientPinSpaceAlarm(mContext);
        }

        // Initialize ViewPager with CustomPagerTabStrip
        mPagerAdapter = new PagerAdapter(((MainActivity) mContext).getSupportFragmentManager());
        if (mViewPager != null) {
            mViewPager.setOffscreenPageLimit(3);
            mViewPager.setAdapter(mPagerAdapter);
        }

        // User login Tera app with mobile network, but the Wi-Fi only option is enabled by default.
        // Thus, we need to notify user to connect to Wi-Fi or disable Wi-Fi only option.
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                String key = SettingsFragment.PREF_SHOW_ACCESS_CLOUD_SETTINGS;
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                boolean showDialog = sharedPreferences.getBoolean(key, true);
                if (showDialog) {
                    boolean isWiFiOnly = true; // Default is enabled
                    SettingsDAO settingsDAO = SettingsDAO.getInstance(getContext());
                    SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_SYNC_WIFI_ONLY);
                    if (settingsInfo != null) {
                        isWiFiOnly = Boolean.valueOf(settingsInfo.getValue());
                    }

                    int type = NetworkUtils.getNetworkType(mContext);
                    if (isWiFiOnly && type == ConnectivityManager.TYPE_MOBILE) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(key, false);
                        editor.apply();

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                AccessCloudSettingsDialogFragment dialog = AccessCloudSettingsDialogFragment.newInstance();
                                dialog.setTargetFragment(MainFragment.this, RequestCode.SHOW_ACCESS_CLOUD_SETTINGS);
                                dialog.show(getFragmentManager(), AccessCloudSettingsDialogFragment.TAG);
                            }
                        });
                    }
                }
            }
        });

        mUserIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = UserInfoDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager(), UserInfoDialogFragment.TAG);
            }
        });

    }

    public class PagerAdapter extends FragmentStatePagerAdapter {

        private String[] titleArray;
        private SparseArray<Fragment> pageReference;

        private PagerAdapter(FragmentManager fm) {
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
            } else if (title.equals(getString(R.string.nav_apps))) {
                fragment = AppFileFragment.newInstance(false);

                Bundle args = new Bundle();
                args.putInt(AppFileFragment.KEY_ARGUMENT_APP_FILE, AppFileFragment.DisplayType.BY_APP);
                fragment.setArguments(args);
            } else if (title.equals(getString(R.string.nav_files))) {
                fragment = AppFileFragment.newInstance(false);

                Bundle args = new Bundle();
                args.putInt(AppFileFragment.KEY_ARGUMENT_APP_FILE, AppFileFragment.DisplayType.BY_FILE);
                fragment.setArguments(args);
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
        private Fragment getFragment(int position) {
            return pageReference.get(position);
        }

        public int getFragmentPosition(String title) {
            for (int position = 0; position < titleArray.length; position++) {
                if (title.equals(titleArray[position])) {
                    return position;
                }
            }
            return -1;
        }

    }

    public void onBackPressed() {
        int position = mViewPager.getCurrentItem();
        Fragment fragment = mPagerAdapter.getFragment(position);
        if (fragment instanceof AppFileFragment) {
            boolean isProcessed = ((AppFileFragment) fragment).onBackPressed();
            if (!isProcessed) {
                exitApp();
            }
        } else {
            exitApp();
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

    @Override
    public void onResume() {
        super.onResume();
        Intent intentService = new Intent(mContext, TeraMgmtService.class);
        intentService.setAction(TeraIntent.ACTION_ONGOING_NOTIFICATION);
        intentService.putExtra(TeraIntent.KEY_ONGOING, false);
        mContext.startService(intentService);

        if (mArguments == null) {
            return;
        }
        final int toViewPager = mArguments.getInt(HCFSMgmtUtils.BUNDLE_KEY_VIEW_PAGER_INDEX, -1);

        if (toViewPager <= -1 || toViewPager >= mPagerAdapter.getCount()) {
            return;
        }

        // Move to specific page, 0 Overview, 1 FILE/APP, 2 SETTINGS, 3 HELP.
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);

                        boolean hasFragmentVisible = false;
                        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
                            Fragment fragment = mPagerAdapter.getFragment(i);
                            if (fragment != null && fragment.isVisible()) {
                                hasFragmentVisible = true;
                                break;
                            }
                        }

                        if (hasFragmentVisible) {
                            Thread.sleep(500);
                            mViewPager.setCurrentItem(toViewPager, true);
                            mArguments = null;
                            break;
                        }
                    } catch (InterruptedException e) {
                        Logs.e(CLASSNAME, "onResume", Log.getStackTraceString(e));
                    }
                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        Intent intentService = new Intent(mContext, TeraMgmtService.class);
        intentService.setAction(TeraIntent.ACTION_ONGOING_NOTIFICATION);
        intentService.putExtra(TeraIntent.KEY_ONGOING, true);
        mContext.startService(intentService);
    }

    @Override
    public void onDestroy() {
        if (mWorkerThread != null) {
            mWorkerThread.quit();
            mWorkerThread = null;
        }
        super.onDestroy();

        if (mWorkerThread != null) {
            mWorkerThread.quit();
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.SHOW_ACCESS_CLOUD_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                // Move current page to settings page
                int position = mPagerAdapter.getFragmentPosition(getString(R.string.settings));
                if (position != -1) {
                    mViewPager.setCurrentItem(position, true);
                }
            }
        }
    }
}
