/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.TransferContentActivity;
import com.hopebaytech.hcfsmgmt.misc.TransferStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/19.
 */
public class SplashFragment extends Fragment {

    public static final String TAG = SplashFragment.class.getSimpleName();
    private final String CLASSNAME = SplashFragment.class.getSimpleName();

    private final int SPLASH_TIME = 100;

    private Context mContext;

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    private Thread mSplashScreenThread;
    private Runnable mSplashScreenRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(SPLASH_TIME);
                int transferStatus = TransferStatus.getTransferStatus(mContext);
                if (transferStatus == TransferStatus.NONE) {
                    switchFragment();
                } else {
                    // Transfer process is not completed, show the transfer data page to continue the
                    // remaining process.
                    startActivity(new Intent(mContext, TransferContentActivity.class));
                }
            } catch (InterruptedException e) {
                Logs.d(CLASSNAME, "mSplashScreenRunnable", Log.getStackTraceString(e));
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.loading_activity, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mSplashScreenThread == null) {
            mSplashScreenThread = new Thread(mSplashScreenRunnable);
        }
        mSplashScreenThread.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSplashScreenThread != null) {
            mSplashScreenThread.interrupt();
            mSplashScreenThread = null;
        }
    }

    private void switchFragment() {
        String TAG;
        Bundle args;
        Fragment fragment;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
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
            case RestoreStatus.FULL_RESTORE_COMPLETED:
                Logs.d(CLASSNAME, "switchFragment", "Replace with RestoreDoneFragment");
                fragment = RestoreDoneFragment.newInstance();
                TAG = RestoreDoneFragment.TAG;
                break;
            case RestoreStatus.Error.CONN_FAILED:
            case RestoreStatus.Error.OUT_OF_SPACE:
            case RestoreStatus.Error.DAMAGED_BACKUP:
                Logs.d(CLASSNAME, "switchFragment", "Replace with RestoreFailedFragment");
                args = new Bundle();
                args.putInt(RestoreFailedFragment.KEY_ERROR_CODE, status);

                fragment = RestoreFailedFragment.newInstance();
                fragment.setArguments(args);

                TAG = RestoreFailedFragment.TAG;
                break;
            default:
                if (TeraAppConfig.isTeraAppLogin(getContext())) {
                    Logs.d(CLASSNAME, "switchFragment", "Replace with MainFragment");
                    fragment = MainFragment.newInstance();
                    TAG = MainFragment.TAG;
                } else {
                    Logs.d(CLASSNAME, "switchFragment", "Replace with SplashFragment");
                    fragment = ActivateWoCodeFragment.newInstance();
                    TAG = ActivateWoCodeFragment.TAG;
                }
                args = getArguments();
                if (args != null) {
                    fragment.setArguments(args);
                }
        }
        ft.replace(R.id.fragment_container, fragment, TAG);
        ft.commit();
    }

}
