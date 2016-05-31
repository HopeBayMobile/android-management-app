package com.hopebaytech.hcfsmgmt.main;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.ActivateWithCodeFragment;
import com.hopebaytech.hcfsmgmt.fragment.ActivateWoCodeFragment;

public class ActivateActivity extends AppCompatActivity {

    private final String CLASSNAME = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_container_activity);

        init();
    }

    private void init() {
        ActivateWithCodeFragment fragment = ActivateWithCodeFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

}
