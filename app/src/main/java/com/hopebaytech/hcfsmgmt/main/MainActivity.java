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
import com.hopebaytech.hcfsmgmt.fragment.RestorePreparingFragment;
import com.hopebaytech.hcfsmgmt.fragment.RestoreReadyFragment;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;

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

        init(getIntent());
    }

    private void init(Intent intent) {
        String TAG;
        Fragment fragment;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int status = sharedPreferences.getInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_NONE);
        if (status == RestoreStatus.MINI_RESTORE_ING) {
            fragment = RestorePreparingFragment.newInstance();
            TAG = RestorePreparingFragment.TAG;
        } else if (status == RestoreStatus.MINI_RESTORE_DONE) {
            fragment = RestoreReadyFragment.newInstance();
            TAG = RestoreReadyFragment.TAG;
        } else {
            fragment = LoadingFragment.newInstance();
            TAG = LoadingFragment.TAG;
        }
        if (intent != null) {
            fragment.setArguments(intent.getExtras());
        }
        ft.replace(R.id.fragment_container, fragment, TAG);
        ft.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
        if (mainFragment != null) {
            mainFragment.changeArguments(intent.getExtras());
        }
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
