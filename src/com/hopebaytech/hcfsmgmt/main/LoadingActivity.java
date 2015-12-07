package com.hopebaytech.hcfsmgmt.main;

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

	private GoogleApiClient mGoogleApiClient;
	private Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_activity);

		HandlerThread handlerThread = new HandlerThread(LoadingActivity.class.getSimpleName());
		handlerThread.start();
		mHandler = new Handler(handlerThread.getLooper());

		// [START configure_signin]
		// Request only the user's ID token, which can be used to identify the
		// user securely to your backend. This will contain the user's basic
		// profile (name, profile picture URL, etc) so you should not need to
		// make an additional call to personalize your application.
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.server_client_id)).requestEmail().build();
				// [END configure_signin]

		// Build GoogleAPIClient with the Google Sign-In API and the above options.
		mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

	}

	@Override
	protected void onStart() {
		super.onStart();

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Log.d(HCFSMgmtUtils.TAG, "onStart");
				if (isActivated()) {
					Log.d(HCFSMgmtUtils.TAG, "Activated");
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
					Log.d(HCFSMgmtUtils.TAG, "NOT Activated");
					Intent intent = new Intent(LoadingActivity.this, ActivateCloludStorageActivity.class);
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
		Log.d(HCFSMgmtUtils.TAG, "onConnectionFailed:" + connectionResult);
		Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

}
