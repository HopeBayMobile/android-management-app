package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
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
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.FactoryResetUtils;
import com.hopebaytech.hcfsmgmt.utils.HCFSEvent;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

import java.util.Locale;

/**
 * @author Aaron
 *         Created by Aaron on 2016/9/12.
 */
public class RestoreFailedFragment extends Fragment {

    public static final String TAG = RestoreFailedFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    public static final String KEY_ERROR_CODE = "key_error_code";

    private int mErrorCode;

    private HomeKeyEventReceiver mHomeKeyEventReceiver;
    private ScreenOffEventReceiver mScreenOffEventReceiver;

    private Context mContext;
    private TextView mMessage;
    private TextView mActionButton;

    public static RestoreFailedFragment newInstance() {
        return new RestoreFailedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mErrorCode = args.getInt(KEY_ERROR_CODE, HCFSEvent.ErrorCode.ENETDOWN);
        }

        mContext = getActivity();
        mHomeKeyEventReceiver = new HomeKeyEventReceiver();
        mScreenOffEventReceiver = new ScreenOffEventReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restore_failed_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMessage = (TextView) view.findViewById(R.id.message);
        mActionButton = (TextView) view.findViewById(R.id.action_btn);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private void init() {
        CharSequence message = getString(R.string.restore_failed_conn_failed);
        int actionButtonResId = R.string.restore_failed_conn_failed_btn;
        switch (mErrorCode) {
            case RestoreStatus.Error.OUT_OF_SPACE:
                message = mContext.getString(R.string.restore_failed_out_of_space);
                actionButtonResId = R.string.restore_failed_out_of_space_btn;
                break;
            case RestoreStatus.Error.DAMAGED_BACKUP:
                message = mContext.getString(R.string.restore_failed_damaged_backup);
                actionButtonResId = R.string.restore_failed_damaged_backup_btn;
                break;
            case RestoreStatus.Error.CONN_FAILED:
                String teraCustomService = mContext.getString(R.string.restore_failed_contact);
                String customerServiceLink = mContext.getString(R.string.tera_customer_service_link);
                String hyperlink = "<a href=\"" + customerServiceLink + "\">" + teraCustomService + "</a>";
                message = Html.fromHtml(String.format(Locale.getDefault(), mContext.getString(R.string.restore_failed_conn_failed), hyperlink));
                actionButtonResId = R.string.restore_failed_conn_failed_btn;
                mMessage.setMovementMethod(LinkMovementMethod.getInstance());
                break;
        }
        mMessage.setText(message);
        mActionButton.setText(actionButtonResId);

        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mErrorCode) {
                    case RestoreStatus.Error.OUT_OF_SPACE:
                        ((Activity) mContext).finish();
                        break;
                    case RestoreStatus.Error.DAMAGED_BACKUP:
                        FactoryResetUtils.reset(mContext);
                        break;
                    case RestoreStatus.Error.CONN_FAILED:
                        int status = HCFSMgmtUtils.checkRestoreStatus();
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        switch (status) {
                            case 0: // Not being restored
                                break;
                            case 1: // In stage 1 of restoration process
                                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_IN_PROGRESS);
                                break;
                            case 2: // In stage 2 of restoration process
                                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.FULL_RESTORE_IN_PROGRESS);
                                break;
                            default:
                        }
                        editor.apply();

                        Intent intent = new Intent(mContext, MainActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        cancelInProgressNotification();

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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int status = sharedPreferences.getInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.NONE);
        switch (status) {
            case RestoreStatus.Error.CONN_FAILED:
            case RestoreStatus.Error.DAMAGED_BACKUP:
            case RestoreStatus.Error.OUT_OF_SPACE:
                startFailedNotification(mContext, status);
                break;
        }

        mScreenOffEventReceiver.unregisterReceiver(mContext);
    }

    public static void startFailedNotification(Context context, int status) {
        showHeadsUpNotification(context, status);
        showNormalNotification(context, status);
    }

    private void cancelInProgressNotification() {
        NotificationEvent.cancel(mContext, NotificationEvent.ID_ONGOING);
    }

    /**
     * The HomeKeyEventReceiver only works when home key is pressed. As user press home key, show
     * restoration failed notification according to error code.
     */
    public class HomeKeyEventReceiver extends BroadcastReceiver {

        private boolean isRegister;
        private String SYSTEM_DIALOG_REASON_KEY = "reason";
        private String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                startFailedNotification(mContext, mErrorCode);
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
     * The ScreenOffEventReceiver only works when this RestoreFailedFragment is visible.
     * <p/>
     * If the screen is off, show normal notification instead of heads-up notification. The heads-up
     * notification will trigger the system to open Tera app continually when the screen is off.
     */
    public class ScreenOffEventReceiver extends BroadcastReceiver {

        private boolean isRegister;

        @Override
        public void onReceive(Context context, Intent intent) {
            showNormalNotification(context, mErrorCode);
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

    private static NotificationCompat.Action getNotificationAction(Context context, int status) {
        String actionText;
        NotificationCompat.Action action = null;
        PendingIntent pendingIntent;
        switch (status) {
            case RestoreStatus.Error.DAMAGED_BACKUP:
                actionText = context.getString(R.string.restore_failed_damaged_backup_btn);

                Intent factoryResetIntent = new Intent(context, TeraMgmtService.class);
                factoryResetIntent.setAction(TeraIntent.ACTION_FACTORY_RESET);
                pendingIntent = PendingIntent.getService(context, 0, factoryResetIntent, PendingIntent.FLAG_ONE_SHOT);
                action = new NotificationCompat.Action(0, actionText, pendingIntent);
                break;
            case RestoreStatus.Error.CONN_FAILED:
                actionText = context.getString(R.string.restore_failed_conn_failed_btn);

                Intent retryIntent = new Intent(context, TeraMgmtService.class);
                retryIntent.setAction(TeraIntent.ACTION_RETRY_RESTORE_WHEN_CONN_FAILED);

                pendingIntent = PendingIntent.getService(context, 0, retryIntent, PendingIntent.FLAG_ONE_SHOT);
                action = new NotificationCompat.Action(0, actionText, pendingIntent);
                break;
        }
        return action;
    }

    private static String getRestorationFailedMessage(Context context, int status) {
        String message = null;
        switch (status) {
            case RestoreStatus.Error.OUT_OF_SPACE:
                message = context.getString(R.string.restore_failed_out_of_space);
                break;
            case RestoreStatus.Error.DAMAGED_BACKUP:
                message = context.getString(R.string.restore_failed_damaged_backup);
                break;
            case RestoreStatus.Error.CONN_FAILED:
                message = String.format(Locale.getDefault(),
                        context.getString(R.string.restore_failed_conn_failed),
                        context.getString(R.string.restore_failed_contact));
                break;
        }
        return message;
    }

    private static void showHeadsUpNotification(Context context, int status) {
        // Show heads-up notification
        int flag = NotificationEvent.FLAG_ON_GOING
                | NotificationEvent.FLAG_HEADS_UP
                | NotificationEvent.FLAG_OPEN_APP;
        String title = context.getString(R.string.restore_failed_title);
        String message = getRestorationFailedMessage(context, status);
        NotificationCompat.Action action = getNotificationAction(context, status);
        NotificationEvent.notify(context, NotificationEvent.ID_ONGOING, title, message, action, flag);
    }

    private static void showNormalNotification(Context context, int status) {
        // Show normal notification (previous heads-up notification will disappear)
        int flag = NotificationEvent.FLAG_ON_GOING
                | NotificationEvent.FLAG_OPEN_APP;
        String title = context.getString(R.string.restore_failed_title);
        String message = getRestorationFailedMessage(context, status);
        NotificationCompat.Action action = getNotificationAction(context, status);
        NotificationEvent.notify(context, NotificationEvent.ID_ONGOING, title, message, action, flag);
    }

}
