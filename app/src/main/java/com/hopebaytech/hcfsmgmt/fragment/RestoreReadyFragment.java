package com.hopebaytech.hcfsmgmt.fragment;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.PowerUtils;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/25.
 */
public class RestoreReadyFragment extends Fragment {

    public static final String TAG = RestoreReadyFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private HomeKeyEventReceiver mHomeKeyEventReceiver;
    private ScreenOffEventReceiver mScreenOffEventReceiver;

    private Context mContext;

    public static RestoreReadyFragment newInstance() {
        return new RestoreReadyFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mHomeKeyEventReceiver = new HomeKeyEventReceiver();
        mScreenOffEventReceiver = new ScreenOffEventReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restore_ready_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView systemReboot = (TextView) view.findViewById(R.id.system_reboot);
        systemReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.FULL_RESTORE_IN_PROGRESS);
                editor.apply();

                PowerUtils.rebootSystem(mContext);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        NotificationEvent.cancel(mContext, HCFSMgmtUtils.NOTIFY_ID_ONGOING);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mHomeKeyEventReceiver.registerReceiver(mContext, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenOffEventReceiver.registerReceiver(mContext, intentFilter);
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

        // If the restore status is MINI_RESTORE_IN_PROGRESS, start restore in progress notification.
        // This only works when user press back key to leave app or remove app from recent app list.
        showRebootNotification(true);

        mScreenOffEventReceiver.unregisterReceiver(mContext);
    }

    /**
     * The ScreenOffEventReceiver only works when this RestoreReadyFragment is visible.
     * <p/>
     * If the screen is off, show normal notification instead of heads-up notification. The heads-up
     * notification will trigger the system to open Tera app continually when the screen is off.
     */
    public class ScreenOffEventReceiver extends BroadcastReceiver {

        private boolean isRegister;

        @Override
        public void onReceive(Context context, Intent intent) {
            showRebootNotification(false /* heads-up */);
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
     * The HomeKeyEventReceiver only works when home key is pressed. 
     */
    public class HomeKeyEventReceiver extends BroadcastReceiver {

        private boolean isRegister;
        private String SYSTEM_DIALOG_REASON_KEY = "reason";
        private String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                showRebootNotification(true /* heads-up */);
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

    private void showRebootNotification(boolean headsUp) {
        String rebootAction = getString(R.string.restore_system_reboot);
        Intent rebootIntent = new Intent(mContext, TeraMgmtService.class);
        rebootIntent.setAction(TeraIntent.ACTION_MINI_RESTORE_REBOOT_SYSTEM);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, rebootIntent, 0);
        NotificationCompat.Action action = new NotificationCompat.Action(0, rebootAction, pendingIntent);

        int notifyId = HCFSMgmtUtils.NOTIFY_ID_ONGOING;
        int flag = NotificationEvent.FLAG_ON_GOING | NotificationEvent.FLAG_OPEN_APP;
        if (headsUp) {
            flag |= NotificationEvent.FLAG_HEADS_UP;
        }
        String title = getString(R.string.restore_ready_title);
        String message = getString(R.string.restore_ready_message);
        NotificationEvent.notify(mContext, notifyId, title, message, action, flag);
    }

}
