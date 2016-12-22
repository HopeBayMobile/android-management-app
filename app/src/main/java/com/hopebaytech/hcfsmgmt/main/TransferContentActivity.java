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
