package com.hopebaytech.hcfsmgmt.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSEvent;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/25.
 */
public class RestoreMajorInstallFragment extends Fragment {

    public static final String TAG = RestoreMajorInstallFragment.class.getSimpleName();
    private static final String CLASSNAME = TAG;

    private FullRestoreCompletedReceiver mRestoreReceiver;
    private HomeKeyEventReceiver mHomeKeyEventReceiver;
    private ScreenOffEventReceiver mScreenOffEventReceiver;

    private Context mContext;
    private TextView mErrorMsg;
    private pl.droidsonroids.gif.GifImageView mGifImage;

    public static RestoreMajorInstallFragment newInstance() {
        return new RestoreMajorInstallFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mRestoreReceiver = new FullRestoreCompletedReceiver();
        mHomeKeyEventReceiver = new HomeKeyEventReceiver();
        mScreenOffEventReceiver = new ScreenOffEventReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restore_major_install_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView ok = (TextView) view.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity) mContext).finish();
            }
        });

        mErrorMsg = (TextView) view.findViewById(R.id.error_msg);
        mGifImage = (pl.droidsonroids.gif.GifImageView) view.findViewById(R.id.gif_img);
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int status = sharedPreferences.getInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.NONE);
        if (status == RestoreStatus.FULL_RESTORE_IN_PROGRESS) {
            cancelInProgressNotification();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TeraIntent.ACTION_RESTORE_STAGE_2);
            mRestoreReceiver.registerReceiver(mContext, intentFilter);

            intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            mHomeKeyEventReceiver.registerReceiver(mContext, intentFilter);

            intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            mScreenOffEventReceiver.registerReceiver(mContext, intentFilter);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Cannot show on-going notification in onStop() or onPause(). If the screen is off, the
        // notification will continuously be triggered by system when user press power key to open
        // screen on.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int status = sharedPreferences.getInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.NONE);
        if (status == RestoreStatus.FULL_RESTORE_IN_PROGRESS) {
            startInProgressNotification();
            mRestoreReceiver.unregisterReceiver(mContext);
        }

        mScreenOffEventReceiver.unregisterReceiver(mContext);
    }

    private void startInProgressNotification() {
        showHeadsUpNotification();
        showNormalNotification();
    }

    private void cancelInProgressNotification() {
        NotificationEvent.cancel(mContext, HCFSMgmtUtils.NOTIFY_ID_ONGOING);
    }

    private void showHeadsUpNotification() {
        // Show heads-up notification
        int flag = NotificationEvent.FLAG_ON_GOING
                | NotificationEvent.FLAG_HEADS_UP
                | NotificationEvent.FLAG_OPEN_APP
                | NotificationEvent.FLAG_IN_PROGRESS;
        String title = getString(R.string.restore_notification_title);
        NotificationEvent.notify(mContext, HCFSMgmtUtils.NOTIFY_ID_ONGOING, title, null, flag);
    }

    private void showNormalNotification() {
        // Show normal notification (previous heads-up notification will disappear)
        int flag = NotificationEvent.FLAG_ON_GOING
                | NotificationEvent.FLAG_OPEN_APP
                | NotificationEvent.FLAG_IN_PROGRESS;
        int restoreIconId = R.drawable.restore_icon_anim;
        String title = getString(R.string.restore_notification_title);
        NotificationEvent.notify(mContext, HCFSMgmtUtils.NOTIFY_ID_ONGOING, title,
                null /* message*/, restoreIconId, null /* action */, flag, null /* extras */);
    }

    public class FullRestoreCompletedReceiver extends BroadcastReceiver {

        private boolean isRegister;

        @Override
        public void onReceive(Context context, Intent intent) {
            int errorCode = intent.getIntExtra(TeraIntent.KEY_RESTORE_ERROR_CODE, -1);
            Logs.w(CLASSNAME, "onReceive", "errorCode=" + errorCode);
            switch (errorCode) {
                case 0: // Success
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.FULL_RESTORE_COMPLETED);
                    editor.apply();

                    gotoRestoreDonePage();
                    break;
                case HCFSEvent.ErrorCode.ENOENT: // No such file or directory
                    mGifImage.setVisibility(View.GONE);
                    mErrorMsg.setVisibility(View.VISIBLE);
                    mErrorMsg.setText(R.string.restore_no_such_file_or_directory);
                    break;
                case HCFSEvent.ErrorCode.ENOSPC: // No space left on device
                    mGifImage.setVisibility(View.GONE);
                    mErrorMsg.setVisibility(View.VISIBLE);
                    mErrorMsg.setText(R.string.restore_stage_2_no_space_left);
                    break;
                case HCFSEvent.ErrorCode.ENETDOWN: // Network is down
                    mGifImage.setVisibility(View.GONE);
                    mErrorMsg.setVisibility(View.VISIBLE);
                    mErrorMsg.setText(R.string.restore_no_network_connected);
                    break;
            }
        }

        public void registerReceiver(Context context, IntentFilter intentFilter) {
            if (!isRegister) {
                if (context != null) {
                    context.registerReceiver(this, intentFilter);
                }
                isRegister = true;
            }
        }

        public void unregisterReceiver(Context context) {
            if (isRegister) {
                if (context != null) {
                    context.unregisterReceiver(this);
                    isRegister = false;
                }
            }
        }

    }

    private void gotoRestoreDonePage() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, RestoreDoneFragment.newInstance());
        ft.commit();
    }

    /**
     * The HomeKeyEventReceiver only works when home key is pressed. If the restore status is
     * FULL_RESTORE_IN_PROGRESS, start restore in progress notification.
     */
    public class HomeKeyEventReceiver extends BroadcastReceiver {

        private boolean isRegister;
        private String SYSTEM_DIALOG_REASON_KEY = "reason";
        private String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                int status = sharedPreferences.getInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_IN_PROGRESS);
                if (status == RestoreStatus.FULL_RESTORE_IN_PROGRESS) {
                    startInProgressNotification();
                    mRestoreReceiver.unregisterReceiver(mContext);
                }
                mHomeKeyEventReceiver.unregisterReceiver(mContext);
            }
        }

        public void registerReceiver(Context context, IntentFilter intentFilter) {
            if (!isRegister) {
                if (context != null) {
                    context.registerReceiver(this, intentFilter);
                }
                isRegister = true;
            }
        }

        public void unregisterReceiver(Context context) {
            if (isRegister) {
                if (context != null) {
                    context.unregisterReceiver(this);
                    isRegister = false;
                }
            }
        }

    }

    /**
     * The ScreenOffEventReceiver only works when this RestoreMajorInstallFragment is visible.
     * <p/>
     * If the screen is off, show normal notification instead of heads-up notification. The heads-up
     * notification will trigger the system to open Tera app continually when the screen is off.
     */
    public class ScreenOffEventReceiver extends BroadcastReceiver {

        private boolean isRegister;

        @Override
        public void onReceive(Context context, Intent intent) {
            showNormalNotification();
        }

        public void registerReceiver(Context context, IntentFilter intentFilter) {
            if (!isRegister) {
                if (context != null) {
                    context.registerReceiver(this, intentFilter);
                }
                isRegister = true;
            }
        }

        public void unregisterReceiver(Context context) {
            if (isRegister) {
                if (context != null) {
                    context.unregisterReceiver(this);
                    isRegister = false;
                }
            }
        }

    }

}
