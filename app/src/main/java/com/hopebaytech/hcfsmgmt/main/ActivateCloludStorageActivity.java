package com.hopebaytech.hcfsmgmt.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivateCloludStorageActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

	private final String CLASSNAME = this.getClass().getSimpleName();
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
		HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "server_client_id=" + server_client_id);
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
							View view = ActivateCloludStorageActivity.this.getCurrentFocus();
							if (view != null) {
								InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
							}
							showProgressDialog();
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									boolean isFailedToSetHCFSConf = false;
									boolean isFailedToAuth = false;
									HttpsURLConnection conn = null;
									int responseCode = 0;
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

										responseCode = conn.getResponseCode();
										HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "responseCode=" + responseCode);
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
											HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "jsonResponse=" + sb.toString());

											JSONObject jsonObj = new JSONObject(sb.toString());
											boolean result = jsonObj.getBoolean("result");
											if (result) {
												isFailedToAuth = false;
												
												JSONObject data = jsonObj.getJSONObject("data");
												String backend_type = data.getString("backend_type");
												String account = data.getString("account").split(":")[0];
												String user = data.getString("account").split(":")[1];
												String password = data.getString("password");
												String backend_url = data.getString("domain") + ":" + data.getInt("port");
												String bucket = data.getString("bucket");
												boolean isTLS = data.getBoolean("TLS");
												String protocol = isTLS ? "https" : "http";

												HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "backend_type=" + backend_type);
												HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "account=" + account);
												HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "user=" + user);
												HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "password=" + password);
												HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "backend_url=" + backend_url);
												HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "bucket=" + bucket);
												HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "protocol=" + protocol);

												if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_CURRENT_BACKEND, backend_type)) {
													isFailedToSetHCFSConf = true;
												}
												if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT, account)) {
													isFailedToSetHCFSConf = true;
												}
												if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_USER, user)) {
													isFailedToSetHCFSConf = true;
												}
												if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PASS, password)) {
													isFailedToSetHCFSConf = true;
												}
												if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_URL, backend_url)) {
													isFailedToSetHCFSConf = true;
												}
												if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_CONTAINER, bucket)) {
													isFailedToSetHCFSConf = true;
												}
												if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PROTOCOL, protocol)) {
													isFailedToSetHCFSConf = true;
												}
												if (!HCFSMgmtUtils.reloadConfig()) {
													isFailedToSetHCFSConf = true;
												}
											} else {
												isFailedToAuth = true;
												
												String msg = jsonObj.getString("msg");
												Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
											}
										} else {
											isFailedToAuth = true;
										}
										// TODO Need to handle the situation that the number of backend account is not enough.
										// else if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
										// Snackbar.make(findViewById(android.R.id.content), getString(R.string.active_cloud_storage_auth_fail),
										// Snackbar.LENGTH_LONG).show();
										// }									
									} catch (Exception e) {
										isFailedToAuth = true;
										HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "init", Log.getStackTraceString(e));
									} finally {
										if (conn != null) {
											conn.disconnect();
										}
									}

									final boolean mIsFailedToSetHCFSConf = isFailedToSetHCFSConf;
									final boolean mIsFailedToAuth = isFailedToAuth;
									final int mResponseCode = responseCode;
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											hideProgressDialog();
											if (!mIsFailedToAuth) {
												if (mIsFailedToSetHCFSConf) {
													String msg = getString(R.string.activate_cloud_storage_failed_to_activate);
													Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
													mHandler.post(new Runnable() {
														@Override
														public void run() {
															HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_CURRENT_BACKEND, "NONE");
															HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT, "");
															HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_USER, "");
															HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PASS, "");
															HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_URL, "");
															HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_CONTAINER, "");
															HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PROTOCOL, "");
														}
													});
												} else {
													Intent intent = new Intent(ActivateCloludStorageActivity.this, MainActivity.class);
													startActivity(intent);
													finish();
												}
											} else {
												String msg = getString(R.string.activate_cloud_storage_failed_to_activate);
												msg += "[responseCode=" + mResponseCode + "]";
												Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
											}
										}
									});

								}
							});
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
					String message = getString(R.string.activate_cloud_failed_to_get_server_client_id);
					Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
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
		HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onConnectionFailed", "connectionResult=" + connectionResult);
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
				HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "idToken=" + idToken);

				mHandler.post(new Runnable() {
					@Override
					public void run() {
						HttpsURLConnection conn = null;
						boolean isFailedToSetHCFSConf = false;
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
							HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "IMEI=" + getDeviceIMEI());
							HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "idToken=" + idToken);

							OutputStream outputStream = conn.getOutputStream();
							BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							bufferedWriter.write(getQuery(params));
							bufferedWriter.flush();
							bufferedWriter.close();
							outputStream.close();

							int responseCode = conn.getResponseCode();
							HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "responseCode=" + responseCode);
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
								HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "response=" + sb.toString());

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

								HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "backend_type=" + backend_type);
								HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "account=" + account);
								HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "user=" + user);
								HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "password=" + password);
								HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "backend_url=" + backend_url);
								HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "protocol=" + protocol);

								if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_CURRENT_BACKEND, backend_type)) {
									isFailedToSetHCFSConf = true;
								}
								if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT, account)) {
									isFailedToSetHCFSConf = true;
								}
								if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_USER, user)) {
									isFailedToSetHCFSConf = true;
								}
								if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PASS, password)) {
									isFailedToSetHCFSConf = true;
								}
								if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_URL, backend_url)) {
									isFailedToSetHCFSConf = true;
								}
								if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_CONTAINER, bucket)) {
									isFailedToSetHCFSConf = true;
								}
								if (!HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PROTOCOL, protocol)) {
									isFailedToSetHCFSConf = true;
								}
								if (!HCFSMgmtUtils.reloadConfig()) {
									isFailedToSetHCFSConf = true;
								}

							}
							// TODO Need to handle the situation that the number of backend account is not enough.
							// else if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
							// Snackbar.make(findViewById(android.R.id.content), getString(R.string.active_cloud_storage_auth_fail),
							// Snackbar.LENGTH_LONG).show();
							// }
						} catch (Exception e) {
							Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
						} finally {
							if (conn != null) {
								conn.disconnect();
							}
						}
						final boolean mIsFailedToSetHCFSConf = isFailedToSetHCFSConf;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								hideProgressDialog();
								if (mIsFailedToSetHCFSConf) {
									Auth.GoogleSignInApi.signOut(mGoogleApiClient);
									String failureMessage = getString(R.string.activate_cloud_storage_failed_to_activate);
									Snackbar.make(findViewById(android.R.id.content), failureMessage, Snackbar.LENGTH_LONG).show();
								} else {
									Intent intent = new Intent(ActivateCloludStorageActivity.this, MainActivity.class);
									intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME, acct.getDisplayName());
									intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_EMAIL, acct.getEmail());
									intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_PHOTO_URI, acct.getPhotoUrl());
									startActivity(intent);
									finish();
								}
							}
						});
					}
				});
			} else {
				String failureMessage = getString(R.string.activate_cloud_storage_failed_to_signin_google_acount);
				Snackbar.make(findViewById(android.R.id.content), failureMessage, Snackbar.LENGTH_LONG).show();
				HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onActivityResult", "Failed to sign in Google account.");
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
		HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "getDeviceIMEI", "imei=" + imei);
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
