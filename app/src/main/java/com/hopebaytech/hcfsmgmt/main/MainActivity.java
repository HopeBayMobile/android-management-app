package com.hopebaytech.hcfsmgmt.main;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.ActivateWoCodeFragment;
import com.hopebaytech.hcfsmgmt.fragment.MainFragment;
import com.hopebaytech.hcfsmgmt.fragment.SplashFragment;
import com.hopebaytech.hcfsmgmt.misc.TransferStatus;
import com.hopebaytech.hcfsmgmt.utils.LogServerUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/19.
 */
public class MainActivity extends AppCompatActivity {

    private final String CLASSNAME = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.d(CLASSNAME, "onCreate", null);
        setContentView(R.layout.main_container);

        init();

        sendLog();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logs.d(CLASSNAME, "onNewIntent", null);

        int transferStatus = TransferStatus.getTransferStatus(this);
        if (transferStatus == TransferStatus.NONE) {
            passIntentToFragments(intent);
        } else {
            startActivity(new Intent(this, TransferContentActivity.class));
        }
    }

    /**
     * If the activity has already started, user open Tera app from launcher or notification, the
     * intent will be passed to the onNewIntent() callback function (launchMode: singleInstance).
     * We pass the intent to the fragment which needs to update UI dynamically if it is visible.
     */
    private void passIntentToFragments(Intent intent) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment activateWoCodeFragment = fm.findFragmentByTag(ActivateWoCodeFragment.TAG);
        Fragment mainFragment = fm.findFragmentByTag(MainFragment.TAG);

        if (activateWoCodeFragment != null && activateWoCodeFragment.isVisible()) {
            ((ActivateWoCodeFragment) activateWoCodeFragment).setIntent(intent);
        } else if (mainFragment != null && mainFragment.isVisible()) {
            ((MainFragment) mainFragment).setIntent(intent);
        }
    }

    private void init() {
        Fragment fragment = SplashFragment.newInstance();
        Intent intent = getIntent();
        if (intent != null) {
            fragment.setArguments(intent.getExtras());
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment, SplashFragment.TAG);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logs.d(CLASSNAME, "onDestroy", null);
    }

    private void sendLog() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogServerUtils.sendLog(MainActivity.this);
            }
        }).start();
    }
}
