package com.hopebaytech.hcfsmgmt.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivateCloludStorageActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

	private GoogleSignInOptions gso;
	private GoogleApiClient mGoogleApiClient;
	private Handler mHandler;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activate_cloud_storage_activity);
		init();
	}

	private void init() {
		HandlerThread handlerThread = new HandlerThread(LoadingActivity.class.getSimpleName());
		handlerThread.start();
		mHandler = new Handler(handlerThread.getLooper());

		String server_client_id = getIntent().getStringExtra(HCFSMgmtUtils.INTENT_KEY_SERVER_CLIENT_ID);
		Log.w(HCFSMgmtUtils.TAG, "server_client_id: " + server_client_id);
		if (server_client_id != null) {
			// Request only the user's ID token, which can be used to identify the
			// user securely to your backend. This will contain the user's basic
			// profile (name, profile picture URL, etc) so you should not need to
			// make an additional call to personalize your application.
			gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(server_client_id).requestEmail().build();

			// Build GoogleAPIClient with the Google Sign-In API and the above options.
			mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
		}

		LinearLayout activate = (LinearLayout) findViewById(R.id.activate);
		activate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetworkUtils.isNetworkConnected(ActivateCloludStorageActivity.this)) {
					final String username = ((EditText) findViewById(R.id.username)).getText().toString();
					final String password = ((EditText) findViewById(R.id.password)).getText().toString();
					if (username.isEmpty() || password.isEmpty()) {
						String message = getString(R.string.activate_cloud_storage_snackbar_require_username_password);
						Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
					} else {
						if (HCFSMgmtUtils.ENABLE_AUTH) {
							showProgressDialog();
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									HttpsURLConnection conn = null;
									try {
										URL url = new URL("https://terafonnreg.hopebaytech.com/api/register/login/");
										conn = (HttpsURLConnection) url.openConnection();
										conn.setDoOutput(true);
										conn.setDoInput(true);
										conn.connect();

										List<NameValuePair> params = new ArrayList<NameValuePair>();
										params.add(new BasicNameValuePair("username", username));
										params.add(new BasicNameValuePair("password", password));
										params.add(new BasicNameValuePair("imei_code", getDeviceIMEI()));

										OutputStream outputStream = conn.getOutputStream();
										BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
										bufferedWriter.write(getQuery(params));
										bufferedWriter.flush();
										bufferedWriter.close();
										outputStream.close();

										int responseCode = conn.getResponseCode();
										Log.d(HCFSMgmtUtils.TAG, "Auth response code: " + responseCode);
										if (responseCode == HttpsURLConnection.HTTP_OK) {
											// Retrieve response content
											InputStream inputStream = conn.getInputStream();
											BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
											String line;
											StringBuilder sb = new StringBuilder();
											while ((line = bufferedReader.readLine()) != null) {
												sb.append(line);
											}
											inputStream.close();
											Log.d(HCFSMgmtUtils.TAG, "response: " + sb.toString());

											JSONObject jsonObj = new JSONObject(sb.toString());
											boolean result = jsonObj.getBoolean("result");
											if (result) {
												JSONObject data = jsonObj.getJSONObject("data");
												String backend_type = data.getString("backend_type");
												String account = data.getString("account").split(":")[0];
												String user = data.getString("account").split(":")[1];
												String password = data.getString("password");
												String backend_url = data.getString("domain") + ":" + data.getInt("port");
												String bucket = data.getString("bucket");
												boolean isTLS = data.getBoolean("TLS");
												String protocol = isTLS ? "https" : "http";

												Log.d(HCFSMgmtUtils.TAG, "backend_type: " + backend_type);
												Log.d(HCFSMgmtUtils.TAG, "account: " + account);
												Log.d(HCFSMgmtUtils.TAG, "user: " + user);
												Log.d(HCFSMgmtUtils.TAG, "password: " + password);
												Log.d(HCFSMgmtUtils.TAG, "backend_url: " + backend_url);
												Log.d(HCFSMgmtUtils.TAG, "protocol: " + protocol);

												HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_CURRENT_BACKEND, backend_type);
												HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT, account);
												HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_USER, user);
												HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PASS, password);
												HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_URL, backend_url);
												HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_CONTAINER, bucket);
												HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PROTOCOL, protocol);
												HCFSMgmtUtils.reloadConfig();

												Intent intent = new Intent(ActivateCloludStorageActivity.this, MainActivity.class);
												startActivity(intent);
												finish();
											} else {
												String msg = jsonObj.getString("msg");
												Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
											}
										}
										// TODO Need to handle the situation that the number of backend account is not enough.
										// else if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
										// Snackbar.make(findViewById(android.R.id.content), getString(R.string.active_cloud_storage_auth_fail),
										// Snackbar.LENGTH_LONG).show();
										// }
									} catch (MalformedURLException e) {
										Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
									} catch (IOException e) {
										Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
									} catch (JSONException e) {
										Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
									} finally {
										if (conn != null) {
											conn.disconnect();
										}
									}
								}
							});
							hideProgressDialog();
						} else {
							HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_CURRENT_BACKEND, "swift");
							HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT, "test");
							HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_USER, "tester");
							HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PASS, "testing");
							HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_URL, "10.0.6.1:8080");
							HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_CONTAINER, "qa_terafonn_2");
							HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PROTOCOL, "http");
							HCFSMgmtUtils.reloadConfig();

							Intent intent = new Intent(ActivateCloludStorageActivity.this, MainActivity.class);
							startActivity(intent);
							finish();
						}
					}
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(ActivateCloludStorageActivity.this);
					builder.setTitle(getString(R.string.activate_cloud_storage_alert_dialog_title));
					builder.setMessage(getString(R.string.activate_cloud_storage_alert_dialog_message));			
					builder.setPositiveButton(getString(R.string.activate_cloud_storage_alert_dialog_exit), null);
					builder.show();
				}

			}
		});

		TextView forgetPassword = (TextView) findViewById(R.id.forget_password);
		forgetPassword.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Snackbar.make(findViewById(android.R.id.content), "忘記密碼", Snackbar.LENGTH_SHORT).show();
			}
		});

		final SignInButton googleActivate = (SignInButton) findViewById(R.id.google_activate);
		if (gso != null) {
			googleActivate.setScopes(gso.getScopeArray());
		}
		googleActivate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGoogleApiClient != null) {
					Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
					startActivityForResult(signInIntent, HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN);
					setGoogleSignInButtonText(googleActivate, getString(R.string.activate_cloud_storage_google_activate));
				} else {
					Snackbar.make(findViewById(android.R.id.content), "暫時無法使用", Snackbar.LENGTH_LONG).show();
				}
			}
		});
		setGoogleSignInButtonText(googleActivate, getString(R.string.activate_cloud_storage_google_activate));
	}

	private void setGoogleSignInButtonText(SignInButton signInButton, String buttonText) {
		// Find the TextView that is inside of the SignInButton and set its text
		for (int i = 0; i < signInButton.getChildCount(); i++) {
			View v = signInButton.getChildAt(i);
			if (v instanceof TextView) {
				TextView tv = (TextView) v;
				tv.setText(buttonText);
				tv.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
				return;
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
		Log.d(HCFSMgmtUtils.TAG, "onConnectionFailed:" + connectionResult);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess()) {
				showProgressDialog();
				final GoogleSignInAccount acct = result.getSignInAccount();
				final String idToken = acct.getIdToken();
				Log.d(HCFSMgmtUtils.TAG, "onActivityResult - idToken: " + idToken);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						HttpsURLConnection conn = null;
						try {
							URL url = new URL("https://terafonnreg.hopebaytech.com/api/register/login/");
							conn = (HttpsURLConnection) url.openConnection();
							conn.setDoOutput(true);
							conn.setDoInput(true);
							conn.connect();

							// Send token and imei to server and validate server-side
							List<NameValuePair> params = new ArrayList<NameValuePair>();
							params.add(new BasicNameValuePair("provider", "google-oauth2"));
							params.add(new BasicNameValuePair("token", idToken));
							params.add(new BasicNameValuePair("imei_code", getDeviceIMEI()));
							Log.d(HCFSMgmtUtils.TAG, "IMEI: " + getDeviceIMEI());
							Log.d(HCFSMgmtUtils.TAG, "idToken: " + idToken);

							OutputStream outputStream = conn.getOutputStream();
							BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							bufferedWriter.write(getQuery(params));
							bufferedWriter.flush();
							bufferedWriter.close();
							outputStream.close();

							int responseCode = conn.getResponseCode();
							Log.d(HCFSMgmtUtils.TAG, "Auth response code: " + responseCode);
							if (responseCode == HttpsURLConnection.HTTP_OK) {
								// Retrieve response content
								InputStream inputStream = conn.getInputStream();
								BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
								String line;
								StringBuilder sb = new StringBuilder();
								while ((line = bufferedReader.readLine()) != null) {
									sb.append(line);
								}
								inputStream.close();
								Log.d(HCFSMgmtUtils.TAG, "response: " + sb.toString());

								JSONObject jsonObj = new JSONObject(sb.toString());
								JSONObject data = jsonObj.getJSONObject("data");
								String backend_type = data.getString("backend_type");
								String account = data.getString("account").split(":")[0];
								String user = data.getString("account").split(":")[1];
								String password = data.getString("password");
								String backend_url = data.getString("domain") + ":" + data.getInt("port");
								String bucket = data.getString("bucket");
								boolean isTLS = data.getBoolean("TLS");
								String protocol = "http";
								if (isTLS) {
									protocol = "https";
								}

								Log.d(HCFSMgmtUtils.TAG, "backend_type: " + backend_type);
								Log.d(HCFSMgmtUtils.TAG, "account: " + account);
								Log.d(HCFSMgmtUtils.TAG, "user: " + user);
								Log.d(HCFSMgmtUtils.TAG, "password: " + password);
								Log.d(HCFSMgmtUtils.TAG, "backend_url: " + backend_url);
								Log.d(HCFSMgmtUtils.TAG, "protocol: " + protocol);

								HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_CURRENT_BACKEND, backend_type);
								HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT, account);
								HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_USER, user);
								HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PASS, password);
								HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_URL, backend_url);
								HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_CONTAINER, bucket);
								HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PROTOCOL, protocol);
								HCFSMgmtUtils.reloadConfig();

								// Intent intent = new Intent(ActivateCloludStorageActivity.this, MainActivity.class);
								// intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME, acct.getDisplayName());
								// intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_EMAIL, acct.getEmail());
								// intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_PHOTO_URI, acct.getPhotoUrl());
								// startActivity(intent);
								// finish();
							}
							// TODO Need to handle the situation that the number of backend account is not enough.
							// else if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
							// Snackbar.make(findViewById(android.R.id.content), getString(R.string.active_cloud_storage_auth_fail),
							// Snackbar.LENGTH_LONG).show();
							// }
						} catch (MalformedURLException e) {
							Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
						} catch (IOException e) {
							Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
						} catch (JSONException e) {
							Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
						} finally {
							if (conn != null) {
								conn.disconnect();
							}
						}
					}
				});
				hideProgressDialog();
			} else {
				Log.e(HCFSMgmtUtils.TAG, "Failed to sign in Google");
			}
		}

	}

	private String getQuery(List<NameValuePair> params) {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (NameValuePair pair : params) {
			if (first) {
				first = false;
			} else {
				result.append("&");
			}

			try {
				result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
				result.append("=");
				result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
			}
		}
		return result.toString();
	}

	private String getDeviceIMEI() {
		String imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		Log.d(HCFSMgmtUtils.TAG, "getDeviceIMEI(): " + imei);
		return imei == null ? "" : imei;
	}

	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
		}
		progressDialog.setMessage(getString(R.string.activate_cloud_storage_processing_msg));
		progressDialog.show();
	}

	private void hideProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.hide();
		}
	}

}
