package com.hopebaytech.hcfsmgmt.main;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.ActivateWoCodeFragment;
import com.hopebaytech.hcfsmgmt.fragment.TransferContentUploadingFragment;
import com.hopebaytech.hcfsmgmt.utils.Logs;

public class TransferContentActivity extends AppCompatActivity {

    private final String CLASSNAME = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_content_activity);

        init();
    }

    private void init() {
        TransferContentUploadingFragment fragment = TransferContentUploadingFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment, TransferContentUploadingFragment.TAG);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
