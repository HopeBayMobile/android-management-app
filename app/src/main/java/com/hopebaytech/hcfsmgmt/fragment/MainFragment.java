package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.misc.ViewPage;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.Booster;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;
import com.hopebaytech.hcfsmgmt.utils.UiHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        // Initialize ViewPager with CustomPagerTabStrip
        if (mViewPager != null) {
            mViewPager.setOffscreenPageLimit(3);
            mViewPager.setAdapter(new ViewPagerAdapter(getFragmentManager()));

            SettingsDAO settingsDAO = SettingsDAO.getInstance(mContext);
            SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_ENABLE_BOOSTER);
            if (settingsInfo != null && Boolean.valueOf(settingsInfo.getValue())) {
                addBoosterPage(getString(R.string.nav_settings), false /* moveToAddedPage */);
            }
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

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private List<String> titleList;
        private Map<String, WeakReference<Fragment>> fragmentMap;
        private boolean isBoosterEnabled;

        @Override
        public int getItemPosition(Object object) {
            // The method is called only when data set changed
            if (isBoosterEnabled) {
                if (object instanceof HelpFragment) {
                    return PagerAdapter.POSITION_NONE;
                }
            } else {
                if (object instanceof BoosterFragment || object instanceof HelpFragment) {
                    return PagerAdapter.POSITION_NONE;
                }
            }
            return super.getItemPosition(object);
        }

        private ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            titleList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.nav_title)));
            fragmentMap = new LinkedHashMap<>();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            String title = titleList.get(position);
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
            } else if (title.equals(getString(R.string.nav_booster))) {
                fragment = BoosterFragment.newInstance();
            } else if (title.equals(getString(R.string.nav_help))) {
                fragment = HelpFragment.newInstance();
            } else {
                fragment = new Fragment();
            }
            fragmentMap.put(title, new WeakReference<>(fragment));
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            if (position < titleList.size()) {
                fragmentMap.remove(titleList.get(position));
            }
        }

        @Override
        public int getCount() {
            return titleList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }

        @Nullable
        Fragment getFragment(int position) {
            return fragmentMap.get(titleList.get(position)).get();
        }

        private List<String> getTitleList() {
            return titleList;
        }

        public void setBoosterEnabled(boolean boosterEnabled) {
            isBoosterEnabled = boosterEnabled;
        }

        public int getFragmentPosition(String title) {
            for (int position = 0; position < titleList.size(); position++) {
                if (title.equals(titleList.get(position))) {
                    return position;
                }
            }
            return -1;
        }

        /**
         * @param toViewPage the view page to move. {@link ViewPage#NONE}, {@link ViewPage#APP}
         *                   , {@link ViewPage#FILE}, {@link ViewPage#BOOSTER}, {@link ViewPage#SETTINGS}
         *                   , {@link ViewPage#HELP}
         * @return -1 if the view page not found, else the view page position in the view pager.
         */
        public int getMoveFragmentPosition(int toViewPage) {
            int titleResId;
            switch (toViewPage) {
                case ViewPage.OVERVIEW:
                    titleResId = R.string.nav_overview;
                    break;
                case ViewPage.APP:
                    titleResId = R.string.nav_apps;
                    break;
                case ViewPage.FILE:
                    titleResId = R.string.nav_files;
                    break;
                case ViewPage.BOOSTER:
                    titleResId = R.string.nav_booster;
                    break;
                case ViewPage.SETTINGS:
                    titleResId = R.string.nav_settings;
                    break;
                case ViewPage.HELP:
                    titleResId = R.string.nav_help;
                    break;
                default:
                    return -1;
            }
            return getFragmentPosition(getString(titleResId));
        }

    }

    public void onBackPressed() {
        int position = mViewPager.getCurrentItem();
        Fragment fragment = ((ViewPagerAdapter) mViewPager.getAdapter()).getFragment(position);
        if (fragment != null && fragment instanceof AppFileFragment) {
            boolean isProcessed = ((AppFileFragment) fragment).onBackPressed();
            if (!isProcessed) {
                ((Activity) mContext).finish();
            }
        } else {
            ((Activity) mContext).finish();
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

        final int toViewPage = mArguments.getInt(ViewPage.KEY, ViewPage.NONE);
        if (toViewPage == ViewPage.NONE) {
            return;
        }

        // Move to the specified page
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);

                        boolean hasFragmentVisible = false;
                        ViewPagerAdapter adapter = (ViewPagerAdapter) mViewPager.getAdapter();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            Fragment fragment = adapter.getFragment(i);
                            if (fragment != null && fragment.isVisible()) {
                                hasFragmentVisible = true;
                                break;
                            }
                        }

                        if (hasFragmentVisible) {
                            Thread.sleep(500);

                            final int position = adapter.getMoveFragmentPosition(toViewPage);
                            UiHandler.getInstance().post(new Runnable() {
                                @Override
                                public void run() {
                                    if (position != -1) {
                                        mViewPager.setCurrentItem(position, true);
                                    }
                                }
                            });

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

        int boostStatus = Booster.currentProcessBoostStatus(mContext);
        if (boostStatus != 0) {
            int notifyId = NotificationEvent.ID_BOOSTER;
            int flag = NotificationEvent.FLAG_HEADS_UP | NotificationEvent.FLAG_OPEN_APP;
            switch (boostStatus) {
                case UidInfo.BoostStatus.BOOSTING:
                    NotificationEvent.notify(
                            mContext,
                            notifyId,
                            getString(R.string.booster_notification_boosting_title),
                            getString(R.string.booster_notification_boosting_message),
                            flag
                    );
                    break;
                case UidInfo.BoostStatus.UNBOOSTING:
                    NotificationEvent.notify(
                            mContext,
                            notifyId,
                            getString(R.string.booster_notification_unboosting_title),
                            getString(R.string.booster_notification_unboosting_message),
                            flag
                    );
                    break;
            }
        }
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
        Fragment fragment = ((ViewPagerAdapter) mViewPager.getAdapter()).getFragment(position);
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
                int position = ((ViewPagerAdapter) mViewPager.getAdapter()).getFragmentPosition(getString(R.string.settings));
                if (position != -1) {
                    mViewPager.setCurrentItem(position, true);
                }
            }
        }
    }

    /**
     * Add the booster page to the right of target page and move to the added booster page.
     */
    public void addBoosterPage(String targetPageTitle, boolean moveToAddedPage) {
        List<String> titleList = ((ViewPagerAdapter) mViewPager.getAdapter()).getTitleList();
        int index = -1;
        for (int i = 0; i < titleList.size(); i++) {
            if (titleList.get(i).equals(targetPageTitle)) {
                index = i + 1;
                break;
            }
        }
        if (index == -1) {
            Logs.e(CLASSNAME, "addBoosterPage", "Target page was not found.");
            return;
        }

        String boosterTile = getString(R.string.nav_booster);
        titleList.add(index, boosterTile);
        if (boosterTile.equals(getString(R.string.nav_booster))) {
            ((ViewPagerAdapter) mViewPager.getAdapter()).setBoosterEnabled(true);
        }
        mViewPager.getAdapter().notifyDataSetChanged();

        if (moveToAddedPage) {
            final int finalIndex = index;
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(finalIndex);
                }
            }, Interval.MOVE_TO_SMART_PAGE_DELAY_TIME);
        }
    }

    public void removeBoosterPage() {
        String boosterTitle = getString(R.string.nav_booster);
        List<String> titleList = ((ViewPagerAdapter) mViewPager.getAdapter()).getTitleList();
        titleList.remove(boosterTitle);
        ((ViewPagerAdapter) mViewPager.getAdapter()).setBoosterEnabled(false);
        mViewPager.getAdapter().notifyDataSetChanged();
    }

}

