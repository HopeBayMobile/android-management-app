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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSEvent;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/23.
 */
public class RestorePreparingFragment extends Fragment {

    public static final String TAG = RestorePreparingFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private MiniRestoreCompletedReceiver mReceiver;

    private Context mContext;
    private TextView mErrorMsg;
    private ProgressBar mProgressDialog;

    public static RestorePreparingFragment newInstance() {
        return new RestorePreparingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mReceiver = new MiniRestoreCompletedReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restore_preparing_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mErrorMsg = (TextView) view.findViewById(R.id.error_msg);
        mProgressDialog = (ProgressBar) view.findViewById(R.id.progress_circle);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int status = sharedPreferences.getInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_IN_PROGRESS);
        if (status == RestoreStatus.MINI_RESTORE_COMPLETED) {
            gotoRestoreReadyPage();
        } else {
            cancelInProgressNotification();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TeraIntent.ACTION_RESTORE_STAGE_1);

            mReceiver.registerReceiver(mContext, intentFilter);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int status = sharedPreferences.getInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_IN_PROGRESS);
        if (status == RestoreStatus.MINI_RESTORE_IN_PROGRESS) {
            startInProgressNotification();
            mReceiver.unregisterReceiver(mContext);
        }
    }

    private void startInProgressNotification() {
        // Show heads-up notification
        int flag = NotificationEvent.FLAG_ON_GOING
                | NotificationEvent.FLAG_HEADS_UP
                | NotificationEvent.FLAG_IN_PROGRESS;
        String title = getString(R.string.restore_notification_title);
        NotificationEvent.notify(mContext, HCFSMgmtUtils.NOTIFY_ID_ONGOING, title, null, flag);

        // Show normal notification (previous heads-up notification will disappear)
        flag = NotificationEvent.FLAG_ON_GOING | NotificationEvent.FLAG_IN_PROGRESS;
        int restoreIconId = R.drawable.restore_icon_anim;
        NotificationEvent.notify(mContext, HCFSMgmtUtils.NOTIFY_ID_ONGOING, title,
                null /* message*/, restoreIconId, null /* action */, flag, null /* extras */);
    }

    private void cancelInProgressNotification() {
        NotificationEvent.cancel(mContext, HCFSMgmtUtils.NOTIFY_ID_ONGOING);
    }

    public class MiniRestoreCompletedReceiver extends BroadcastReceiver {

        private boolean isRegister;

        @Override
        public void onReceive(Context context, Intent intent) {
            int errorCode = intent.getIntExtra(TeraIntent.KEY_RESTORE_ERROR_CODE, 0);
            switch (errorCode) {
                case 0: // Success
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_COMPLETED);
                    editor.apply();

                    gotoRestoreReadyPage();
                    break;
                case HCFSEvent.ErrorCode.ENOENT: // No such file or directory
                    mProgressDialog.setVisibility(View.GONE);
                    mErrorMsg.setVisibility(View.VISIBLE);
                    mErrorMsg.setText(R.string.restore_no_such_file_or_directory);
                    break;
                case HCFSEvent.ErrorCode.ENOSPC: // No space left on device
                    mProgressDialog.setVisibility(View.GONE);
                    mErrorMsg.setVisibility(View.VISIBLE);
                    mErrorMsg.setText(R.string.restore_stage_1_no_space_left);
                    break;
                case HCFSEvent.ErrorCode.ENETDOWN: // Network is down
                    mProgressDialog.setVisibility(View.GONE);
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

    private void gotoRestoreReadyPage() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, RestoreReadyFragment.newInstance());
        ft.commit();
    }

}
