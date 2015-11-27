package com.hopebaytech.hcfsmgmt.main;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ActivateCloludStorageActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

	private GoogleApiClient mGoogleApiClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activate_cloud_storage_activity);

		if (isActivated()) {
			Intent intent = new Intent(ActivateCloludStorageActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		} else {
			initialize();
		}

	}

	private boolean isActivated() {
		if (HCFSMgmtUtils.getHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT) != null)
			return true;
		return false;
	}

	private void initialize() {

		validateServerClientID();

		// [START configure_signin]
		// Request only the user's ID token, which can be used to identify the
		// user securely to your backend. This will contain the user's basic
		// profile (name, profile picture URL, etc) so you should not need to
		// make an additional call to personalize your application.
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.server_client_id)).requestEmail().build();
				// [END configure_signin]

		// Build GoogleAPIClient with the Google Sign-In API and the above options.
		mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

		Button activate = (Button) findViewById(R.id.activate);
		activate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_CURRENT_BACKEND, "swift");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT, "foxconn");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_USER, "foxconn");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PASS, "foxconnfoxconnfoxconn");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_URL, "61.219.202.66:18080");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_CONTAINER, "foxconn_test");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PROTOCOL, "http");
				HCFSMgmtUtils.reboot();

				// Intent intent = new Intent(ActivateCloludStorageActivity.this, MainActivity.class);
				// startActivity(intent);
				// finish();
			}
		});

		TextView forgetPassword = (TextView) findViewById(R.id.forget_password);
		forgetPassword.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Snackbar.make(findViewById(android.R.id.content), "忘記密碼", Snackbar.LENGTH_SHORT).show();
			}
		});

		SignInButton googleActivate = (SignInButton) findViewById(R.id.google_activate);
		setGooglePlusButtonText(googleActivate, getString(R.string.activate_cloud_storage_google_activate));
		googleActivate.setScopes(gso.getScopeArray());
		googleActivate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
				startActivityForResult(signInIntent, HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN);
			}
		});
	}

	private void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
		// Find the TextView that is inside of the SignInButton and set its text
		for (int i = 0; i < signInButton.getChildCount(); i++) {
			View v = signInButton.getChildAt(i);
			if (v instanceof TextView) {
				TextView tv = (TextView) v;
				tv.setText(buttonText);
				return;
			}
		}
	}

	/**
	 * Validates that there is a reasonable server client ID in strings.xml
	 */
	private void validateServerClientID() {
		String serverClientId = getString(R.string.server_client_id);
		String suffix = ".apps.googleusercontent.com";
		if (!serverClientId.trim().endsWith(suffix)) {
			String message = "Invalid server client ID in strings.xml, must end with " + suffix;

			Log.w(HCFSMgmtUtils.TAG, message);
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		// An unresolvable error has occurred and Google APIs (including Sign-In) will not
		// be available.
		Log.d(HCFSMgmtUtils.TAG, "onConnectionFailed:" + connectionResult);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN) {
			// [START get_id_token]
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess()) {
				GoogleSignInAccount acct = result.getSignInAccount();
				String displayName = acct.getDisplayName();
				String email = acct.getEmail();
				Uri photoUri = acct.getPhotoUrl();
				String idToken = acct.getIdToken();

				// Show signed-in UI.
				Log.d(HCFSMgmtUtils.TAG, "displayName:" + displayName);
				Log.d(HCFSMgmtUtils.TAG, "email:" + email);
				Log.d(HCFSMgmtUtils.TAG, "photoUri:" + photoUri);
				Log.d(HCFSMgmtUtils.TAG, "idToken:" + idToken);

				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_CURRENT_BACKEND, "swift");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT, "foxconn");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_USER, "foxconn");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PASS, "foxconnfoxconnfoxconn");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_URL, "61.219.202.66:18080");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_CONTAINER, "foxconn_test");
				HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PROTOCOL, "http");
				HCFSMgmtUtils.reboot();

				// Intent intent = new Intent(ActivateCloludStorageActivity.this, MainActivity.class);
				// intent.putExtra(HCFSMgmtUtils.GOOGLE_SIGN_IN_DISPLAY_NAME, displayName);
				// intent.putExtra(HCFSMgmtUtils.GOOGLE_SIGN_IN_EMAIL, email);
				// intent.putExtra(HCFSMgmtUtils.GOOGLE_SIGN_IN_PHOTO_URI, photoUri);
				// startActivity(intent);
				// finish();

				// TODO(user): send token to server and validate server-side
			} else {
				// Show signed-out UI.
				Log.e(HCFSMgmtUtils.TAG, "Failed");
			}
			// [END get_id_token]
		}

	}

}
