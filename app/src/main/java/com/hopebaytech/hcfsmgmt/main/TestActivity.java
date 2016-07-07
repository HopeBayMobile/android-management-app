package com.hopebaytech.hcfsmgmt.main;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.utils.GoogleAuthProxy;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

/**
 * Created by Aaron on 2016/7/7.
 */
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);


        new Thread(new Runnable() {
            @Override
            public void run() {
                final String serverClientId = MgmtCluster.getServerClientId();
                GoogleAuthProxy googleAuthProxy = new GoogleAuthProxy(TestActivity.this, serverClientId);
                googleAuthProxy.setOnAuthListener(new GoogleAuthProxy.OnAuthListener() {
                    @Override
                    public void onAuthSuccessful(GoogleSignInResult result) {
                        String serverAuthCode = result.getSignInAccount().getServerAuthCode();

                        final MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam();
                        authParam.setAuthCode(serverAuthCode);
                        authParam.setAuthBackend(MgmtCluster.GOOGLE_AUTH_BACKEND);
                        authParam.setImei(HCFSMgmtUtils.getEncryptedDeviceImei(HCFSMgmtUtils.getDeviceImei(TestActivity.this)));
                        authParam.setVendor(Build.BRAND);
                        authParam.setModel(Build.MODEL);
                        authParam.setAndroidVersion(Build.VERSION.RELEASE);
                        authParam.setHcfsVersion("1.0.1");

                        MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
                        authProxy.setOnAuthListener(new MgmtCluster.AuthListener() {
                            @Override
                            public void onAuthSuccessful(AuthResultInfo authResultInfo) {
                                String jwtToken = authResultInfo.getToken();
                                Logs.w("TestActivity", "onAuthSuccessful", "jwtToken=" + jwtToken);
                            }

                            @Override
                            public void onAuthFailed(AuthResultInfo authResultInfo) {
                                Logs.w("TestActivity", "onAuthFailed", null);
                            }
                        });
                        authProxy.auth();
                    }

                    @Override
                    public void onAuthFailed() {
                        Logs.w("TestActivity", "onAuthFailed", null);
                    }
                });
                googleAuthProxy.auth();
            }
        }).start();

    }

}
