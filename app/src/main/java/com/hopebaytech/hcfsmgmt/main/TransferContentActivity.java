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
package com.hopebaytech.hcfsmgmt.main;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.TransferContentDoneFragment;
import com.hopebaytech.hcfsmgmt.fragment.TransferContentUploadingFragment;
import com.hopebaytech.hcfsmgmt.fragment.TransferContentWaitingFragment;
import com.hopebaytech.hcfsmgmt.misc.TransferStatus;
import com.hopebaytech.hcfsmgmt.utils.Logs;

public class TransferContentActivity extends AppCompatActivity {

    private final String CLASSNAME = this.getClass().getSimpleName();

    public static final String PREF_TRANSFER_STATUS = "pref_transfer_status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_content_activity);

        switchFragment();
    }

    private void switchFragment() {
        int transferStatus = TransferStatus.getTransferStatus(this);
        switch (transferStatus) {
            case TransferStatus.WAIT_DEVICE:
                gotoTransferContentWaitingFragment();
            case TransferStatus.TRANSFERRED:
                gotoTransferContentDoneFragment();
                break;
            default: // TransferStatus.NONE or TransferStatus.TRANSFERRING
                gotoTransferContentUploadingFragment();
                break;
        }
    }

    private void gotoTransferContentUploadingFragment() {
        Logs.d(CLASSNAME, "gotoTransferContentUploadingFragment", "Replace with TransferContentUploadingFragment");

        TransferContentUploadingFragment fragment = TransferContentUploadingFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment, TransferContentUploadingFragment.TAG);
        ft.commit();
    }

    private void gotoTransferContentWaitingFragment() {
        Logs.d(CLASSNAME, "gotoTransferContentWaitingFragment", "Replace with TransferContentUploadingFragment");

        TransferContentWaitingFragment fragment = TransferContentWaitingFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment, TransferContentWaitingFragment.TAG);
        ft.commit();
    }

    private void gotoTransferContentDoneFragment() {
        Logs.d(CLASSNAME, "gotoTransferContentDoneFragment", "Replace with gotoTransferContentDoneFragment");

        TransferContentDoneFragment fragment = TransferContentDoneFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment, TransferContentDoneFragment.TAG);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        // Override this function and without calling super.onBackPressed() to disable back key
    }

}
