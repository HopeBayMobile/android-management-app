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

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
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
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int status = sharedPreferences.getInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_ING);
        if (status == RestoreStatus.MINI_RESTORE_DONE) {
            gotoRestoreReadyPage();
        } else {
            cancelNotifyRestore();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TeraIntent.ACTION_MINI_RESTORE_DONE);

            mReceiver.registerReceiver(mContext, intentFilter);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int status = sharedPreferences.getInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_ING);
        if (status == RestoreStatus.MINI_RESTORE_ING) {
            startNotifyRestore();
            mReceiver.unregisterReceiver(mContext);
        }
    }

    private void startNotifyRestore() {
        Intent intent = new Intent(mContext, TeraMgmtService.class);
        intent.setAction(TeraIntent.ACTION_RESTORE_NOTIFICATION);
        intent.putExtra(TeraIntent.ACTION_RESTORE_NOTIFICATION, true /* notify */);
        mContext.startService(intent);
    }

    private void cancelNotifyRestore() {
        Intent intent = new Intent(mContext, TeraMgmtService.class);
        intent.setAction(TeraIntent.ACTION_RESTORE_NOTIFICATION);
        intent.putExtra(TeraIntent.ACTION_RESTORE_NOTIFICATION, false /* notify */);
        mContext.startService(intent);
    }

    public class MiniRestoreCompletedReceiver extends BroadcastReceiver {

        private boolean isRegister;

        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_DONE);
            editor.apply();

            gotoRestoreReadyPage();
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
