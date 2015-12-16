package com.hopebaytech.hcfsmgmt.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class LoadingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

	private Handler mHandler;
//	private final String authUrl = "https://terafonnreg.hopebaytech.com/register/auth";
	private final String authUrl = "https://terafonnreg.hopebaytech.com/api/register/auth";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_activity);

		HandlerThread handlerThread = new HandlerThread(LoadingActivity.class.getSimpleName());
		handlerThread.start();
		mHandler = new Handler(handlerThread.getLooper());
	}

	@Override
	protected void onStart() {
		super.onStart();

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				final String[] server_client_id = new String[1];
				final GoogleApiClient[] mGoogleApiClient = new GoogleApiClient[1];
				try {
					URL url = new URL(authUrl);
					HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
					conn.setDoInput(true);
					int responseCode = conn.getResponseCode();
					if (responseCode == HttpsURLConnection.HTTP_OK) {
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						StringBuilder sb = new StringBuilder();
						String line;
						while ((line = bufferedReader.readLine()) != null) {
							sb.append(line);
						}

						String jsonResponse = sb.toString();
						Log.i(HCFSMgmtUtils.TAG, "content: " + jsonResponse);
						if (!jsonResponse.isEmpty()) {
							JSONObject jObj = new JSONObject(jsonResponse);
							JSONObject dataObj = jObj.getJSONObject("data");
							JSONObject authObj = dataObj.getJSONObject("google-oauth2");
							server_client_id[0] = authObj.getString("client_id");
							Log.i(HCFSMgmtUtils.TAG, "server_client_id: " + server_client_id[0]);

							runOnUiThread(new Runnable() {
								public void run() {
									// Request only the user's ID token, which can be used to identify the
									// user securely to your backend. This will contain the user's basic
									// profile (name, profile picture URL, etc) so you should not need to
									// make an additional call to personalize your application.
									GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
											.requestIdToken(server_client_id[0]).requestEmail().build();

									// Build GoogleAPIClient with the Google Sign-In API and the above options.
									mGoogleApiClient[0] = new GoogleApiClient.Builder(LoadingActivity.this)
											.enableAutoManage(LoadingActivity.this, LoadingActivity.this).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
											.build();

									synchronized (LoadingActivity.this) {
										Log.d(HCFSMgmtUtils.TAG, "notify");
										LoadingActivity.this.notify();
									}
								}
							});
							bufferedReader.close();

							synchronized (LoadingActivity.this) {
								Log.d(HCFSMgmtUtils.TAG, "wait");
								LoadingActivity.this.wait();
							}
						}
					}
					conn.disconnect();
				} catch (Exception e) {
					Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
				}

				if (isActivated()) {
					Log.d(HCFSMgmtUtils.TAG, "Activated");
					if (mGoogleApiClient[0] != null) {
						OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient[0]);
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
					Log.d(HCFSMgmtUtils.TAG, "NOT Activated");
					Intent intent = new Intent(LoadingActivity.this, ActivateCloludStorageActivity.class);
					intent.putExtra(HCFSMgmtUtils.INTENT_KEY_SERVER_CLIENT_ID, server_client_id[0]);
					startActivity(intent);
					finish();
				}
			}
		});

	}

	private boolean isActivated() {
		if (!HCFSMgmtUtils.getHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT).isEmpty())
			return true;
		return false;
	}

	private void handleSignInResult(GoogleSignInResult result) {
		Log.d(HCFSMgmtUtils.TAG, "handleSignInResult");
		Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
		if (result.isSuccess()) {
			GoogleSignInAccount acct = result.getSignInAccount();
			String displayName = acct.getDisplayName();
			String email = acct.getEmail();
			Uri photoUri = acct.getPhotoUrl();

			intent.putExtra(HCFSMgmtUtils.GOOGLE_SIGN_IN_DISPLAY_NAME, displayName);
			intent.putExtra(HCFSMgmtUtils.GOOGLE_SIGN_IN_EMAIL, email);
			intent.putExtra(HCFSMgmtUtils.GOOGLE_SIGN_IN_PHOTO_URI, photoUri);
		}
		startActivity(intent);
		finish();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
		Log.d(HCFSMgmtUtils.TAG, "onConnectionFailed: " + connectionResult);
		Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

}
