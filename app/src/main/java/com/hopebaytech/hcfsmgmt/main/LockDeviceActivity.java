package com.hopebaytech.hcfsmgmt.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;

public class LockDeviceActivity extends AppCompatActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_device);

        Bundle bundle =this.getIntent().getExtras();
        String lockMsg = bundle.getString("lockMsg");

        mTextView = (TextView)findViewById(R.id.lock_text);
        mTextView.setText(lockMsg);
    }
}

