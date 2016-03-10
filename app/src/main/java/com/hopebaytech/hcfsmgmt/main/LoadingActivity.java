package com.hopebaytech.hcfsmgmt.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

public class LoadingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final String CLASSNAME = getClass().getSimpleName();
    private Handler mHandler;
    private GoogleApiClient mGoogleApiClient;
//	private final String authUrl = "https://terafonnreg.hopebaytech.com/api/register/auth";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);

        HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName());
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        init();
    }

    public void init() {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", null);
        super.onStart();
//		if (NetworkUtils.isNetworkConnected(this)) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {

//                    Log.w(HCFSMgmtUtils.TAG, "HCFSApiUtils.getEncryptedIMEI()=" + HCFSApiUtils.getEncryptedIMEI());

//					final String[] server_client_id = new String[1];
//					try {
//						URL url = new URL(authUrl);
//						HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//						conn.setDoInput(true);
//						int responseCode = conn.getResponseCode();
//						if (responseCode == HttpsURLConnection.HTTP_OK) {
//							BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//							StringBuilder sb = new StringBuilder();
//							String line;
//							while ((line = bufferedReader.readLine()) != null) {
//								sb.append(line);
//							}
//
//							String jsonResponse = sb.toString();
//							HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "jsonResponse=" + jsonResponse);
//							if (!jsonResponse.isEmpty()) {
//								JSONObject jObj = new JSONObject(jsonResponse);
//								JSONObject dataObj = jObj.getJSONObject("data");
//								JSONObject authObj = dataObj.getJSONObject("google-oauth2");
//								server_client_id[0] = authObj.getString("client_id");
//								HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "server_client_id=" + server_client_id[0]);
//
//								Thread signInInitThread = new Thread(new Runnable() {
//									public void run() {
//										/**
//										 * Request only the user's ID token, which can be used to identify the
//										 * user securely to your backend. This will contain the user's basic
//										 * profile (name, profile picture URL, etc) so you should not need to
//										 * make an additional call to personalize your application.
//										 */
//										GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//												.requestIdToken(server_client_id[0]).requestEmail().build();
//
//										/** Build GoogleAPIClient with the Google Sign-In API and the above options. */
//										mGoogleApiClient = new GoogleApiClient.Builder(LoadingActivity.this)
//												.enableAutoManage(LoadingActivity.this, LoadingActivity.this)
//												.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//												.build();
////										mGoogleApiClient.connect();
//												// mGoogleApiClient[0] = new GoogleApiClient.Builder(LoadingActivity.this)
//												// .enableAutoManage(LoadingActivity.this, LoadingActivity.this).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//												// .build();
//									}
//								});
//								runOnUiThread(signInInitThread);
//								bufferedReader.close();
//								signInInitThread.join();
//							}
//						}
//						conn.disconnect();
//					} catch (Exception e) {
//						HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "init", Log.getStackTraceString(e));
//					}

                if (isActivated()) {
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "Activated");
                    if (mGoogleApiClient != null) {
                        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                        if (opr.isDone()) {
                            GoogleSignInResult googleSignInResult = opr.get();
                            handleSignInResult(googleSignInResult);
                        } else {
                            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                                @Override
                                public void onResult(GoogleSignInResult googleSignInResult) {
                                    handleSignInResult(googleSignInResult);
                                }
                            });
                        }
                    } else {
                        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "init", "NOT Activated");
                    Intent intent = new Intent(LoadingActivity.this, ActivateCloudStorageActivity.class);
//						intent.putExtra(HCFSMgmtUtils.INTENT_KEY_SERVER_CLIENT_ID, server_client_id[0]);
                    startActivity(intent);
                    finish();
                }
            }
        });
//		} else {
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle(getString(R.string.loading_alert_dialog_title));
//			builder.setMessage(getString(R.string.loading_alert_dialog_message));
//			builder.setPositiveButton(getString(R.string.loading_alert_dialog_exit), new OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					finish();
//				}
//			});
//			builder.setCancelable(false);
//			builder.show();
//		}
    }

    private boolean isActivated() {
        return !HCFSMgmtUtils.getHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT).isEmpty();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "handleSignInResult", null);
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String displayName = acct.getDisplayName();
            String email = acct.getEmail();
            Uri photoUri = acct.getPhotoUrl();

            intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME, displayName);
            intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_EMAIL, email);
            intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_PHOTO_URI, photoUri);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
        HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onConnectionFailed", connectionResult.toString());
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
