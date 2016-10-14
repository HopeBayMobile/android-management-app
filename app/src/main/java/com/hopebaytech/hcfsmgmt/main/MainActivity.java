package com.hopebaytech.hcfsmgmt.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.ActivateWoCodeFragment;
import com.hopebaytech.hcfsmgmt.fragment.LoadingFragment;
import com.hopebaytech.hcfsmgmt.fragment.MainFragment;
import com.hopebaytech.hcfsmgmt.fragment.RestoreFailedFragment;
import com.hopebaytech.hcfsmgmt.fragment.RestoreMajorInstallFragment;
import com.hopebaytech.hcfsmgmt.fragment.RestorePreparingFragment;
import com.hopebaytech.hcfsmgmt.fragment.RestoreReadyFragment;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/19.
 */
public class MainActivity extends AppCompatActivity {

    private final String CLASSNAME = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_container);
        switchFragment();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        switchFragment();
    }

    private void switchFragment() {
        String TAG;
        Fragment fragment;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int status = sharedPreferences.getInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.NONE);
        switch (status) {
            case RestoreStatus.MINI_RESTORE_IN_PROGRESS:
                Logs.d(CLASSNAME, "switchFragment", "Replace with RestorePreparingFragment");
                fragment = RestorePreparingFragment.newInstance();
                TAG = RestorePreparingFragment.TAG;
                break;
            case RestoreStatus.MINI_RESTORE_COMPLETED:
                Logs.d(CLASSNAME, "switchFragment", "Replace with RestoreReadyFragment");
                fragment = RestoreReadyFragment.newInstance();
                TAG = RestoreReadyFragment.TAG;
                break;
            case RestoreStatus.FULL_RESTORE_IN_PROGRESS:
                Logs.d(CLASSNAME, "switchFragment", "Replace with RestoreMajorInstallFragment");
                fragment = RestoreMajorInstallFragment.newInstance();
                TAG = RestoreMajorInstallFragment.TAG;
                break;
            case RestoreStatus.Error.CONN_FAILED:
            case RestoreStatus.Error.OUT_OF_SPACE:
            case RestoreStatus.Error.DAMAGED_BACKUP:
                Logs.d(CLASSNAME, "switchFragment", "Replace with RestoreFailedFragment");
                Bundle args = new Bundle();
                args.putInt(RestoreFailedFragment.KEY_ERROR_CODE, status);

                fragment = RestoreFailedFragment.newInstance();
                fragment.setArguments(args);

                TAG = RestoreFailedFragment.TAG;
                break;
            default:
                if (TeraAppConfig.isTeraAppLogin(this)) {
                    Logs.d(CLASSNAME, "switchFragment", "Replace with MainFragment");
                    fragment = MainFragment.newInstance();
                    TAG = MainFragment.TAG;
                } else {
                    Logs.d(CLASSNAME, "switchFragment", "Replace with LoadingFragment");
                    fragment = LoadingFragment.newInstance();
                    TAG = LoadingFragment.TAG;
                }
                Intent intent = getIntent();
                if (intent != null) {
                    fragment.setArguments(intent.getExtras());
                }
        }
        ft.replace(R.id.fragment_container, fragment, TAG);
        ft.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(ActivateWoCodeFragment.TAG);
        Logs.d(CLASSNAME, "onActivityResult", "requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(ActivateWoCodeFragment.TAG);
        if (fragment != null && fragment.isVisible()) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        MainFragment mainFragment = (MainFragment) fm.findFragmentByTag(MainFragment.TAG);
        if (mainFragment != null && mainFragment.isVisible()) {
            mainFragment.onBackPressed();
            return;
        }
        super.onBackPressed();
    }
}
