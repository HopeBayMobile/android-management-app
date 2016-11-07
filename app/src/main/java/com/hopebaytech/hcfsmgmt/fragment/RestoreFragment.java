package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceListInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceStatusInfo;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;
import com.hopebaytech.hcfsmgmt.utils.GoogleSilentAuthProxy;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.ProgressDialogUtils;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/12.
 */
public class RestoreFragment extends Fragment {

    public static final String TAG = RestoreFragment.class.getSimpleName();

    private final String CLASSNAME = TAG;

    public static final String KEY_DEVICE_LIST = "key_device_list";
    public static final String KEY_RESTORE_TYPE = "key_restore_type";

    public static final int RESTORE_TYPE_NEW_DEVICE = 1;
    public static final int RESTORE_TYPE_MY_TERA = 2;
    public static final int RESTORE_TYPE_LOCK_DEVICE = 3;
    public static final int RESTORE_TYPE_NON_LOCK_DEVICE = 4;

    private Context mContext;
    private ExpandableListView mExpandableListView;
    private TextView mBackButton;
    private TextView mNextButton;
    private TextView mSearchBackup;
    private TextView mErrorMessage;

    private RestoreListAdapter mRestoreListAdapter;
    private Checkable mPrevCheckableInfo;
    private ProgressDialogUtils mProgressDialogUtils;
    private Handler mUiHandler;
    private Handler mWorkHandler;
    private HandlerThread mHandlerThread;

    private String mJwtToken;
    private String mAccountEmail;
    private String mAccountName;
    private String mPhotoUrl;

    public static RestoreFragment newInstance() {
        return new RestoreFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialogUtils = new ProgressDialogUtils(mContext);
        mHandlerThread = new HandlerThread(CLASSNAME);
        mHandlerThread.start();
        mWorkHandler = new Handler(mHandlerThread.getLooper());
        mUiHandler = new Handler();

        Bundle extras = getArguments();
        mJwtToken = extras.getString(ActivateWoCodeFragment.KEY_JWT_TOKEN);
        mAccountEmail = extras.getString(ActivateWoCodeFragment.KEY_GOOGLE_EMAIL);
        mAccountName = extras.getString(ActivateWoCodeFragment.KEY_GOOGLE_NAME);
        mPhotoUrl = extras.getString(ActivateWoCodeFragment.KEY_GOOGLE_PHOTO_URL);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restore_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mExpandableListView = (ExpandableListView) view.findViewById(R.id.expanded_list);
        mBackButton = (TextView) view.findViewById(R.id.back_btn);
        mNextButton = (TextView) view.findViewById(R.id.next_btn);
        mSearchBackup = (TextView) view.findViewById(R.id.search_backup);
        mErrorMessage = (TextView) view.findViewById(R.id.error_msg);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DeviceListInfo deviceListInfo = null;
        Bundle args = getArguments();
        if (args != null) {
            deviceListInfo = args.getParcelable(KEY_DEVICE_LIST);
        }

        if (deviceListInfo == null) {
            Logs.e(CLASSNAME, "onActivityCreated", "deviceListInfo == null");
            return;
        }

        mRestoreListAdapter = new RestoreListAdapter(createRestoreList(deviceListInfo));
        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setAdapter(mRestoreListAdapter);
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ChildInfo childInfo = (ChildInfo) v.getTag();
                childInfo.setChecked(true);

                if (mPrevCheckableInfo != null) {
                    if (mPrevCheckableInfo != childInfo) {
                        boolean isChecked = mPrevCheckableInfo.isChecked();
                        mPrevCheckableInfo.setChecked(!isChecked);
                    }
                }
                mRestoreListAdapter.notifyDataSetChanged();
                mPrevCheckableInfo = childInfo;

                showDialog(childInfo.getRestoreType());
                return true;
            }
        });
        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                GroupInfo groupInfo = (GroupInfo) v.getTag();
                groupInfo.setChecked(true);
                if (groupInfo.isCheckable()) {
                    if (mPrevCheckableInfo != null) {
                        if (mPrevCheckableInfo != groupInfo) {
                            boolean isChecked = mPrevCheckableInfo.isChecked();
                            mPrevCheckableInfo.setChecked(!isChecked);
                        }
                    }
                    mRestoreListAdapter.notifyDataSetChanged();
                    mPrevCheckableInfo = groupInfo;

                    int restoreType = groupInfo.getRestoreType();
                    if (restoreType != RESTORE_TYPE_MY_TERA) {
                        showDialog(groupInfo.getRestoreType());
                    }
                }
                return false;
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPrevCheckableInfo != null) {
                    DeviceStatusInfo deviceStatusInfo = mPrevCheckableInfo.getDeviceStatusInfo();

                    // Setup as new device
                    if (mPrevCheckableInfo instanceof GroupInfo) {
                        if (deviceStatusInfo == null) {
                            registerTera();
                            return;
                        }
                    }

                    // Restore from myTera or backups
                    String sourceImei = deviceStatusInfo.getImei();
                    restoreDevice(sourceImei);
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity) mContext).onBackPressed();
            }
        });

        mSearchBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDeviceList(mJwtToken);
            }
        });
    }

    private void showDialog(int deviceType) {
        Bundle args = new Bundle();
        args.putInt(KEY_RESTORE_TYPE, deviceType);

        RestoreDialogFragment dialogFragment = RestoreDialogFragment.newInstance();
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), RestoreDialogFragment.TAG);
    }

    public class RestoreListAdapter extends BaseExpandableListAdapter {

        private List<GroupInfo> groupList;

        public RestoreListAdapter(List<GroupInfo> groupList) {
            this.groupList = groupList;
        }

        public List<GroupInfo> getGroupList() {
            return groupList;
        }

        public void setGroupList(List<GroupInfo> groupList) {
            this.groupList = groupList;
        }

        @Override
        public int getGroupCount() {
            return groupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            List<ChildInfo> childList = groupList.get(groupPosition).getChildList();
            if (childList != null) {
                return childList.size();
            }
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return groupList.get(groupPosition).getChildList().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return getGroup(groupPosition).hashCode();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getChild(groupPosition, childPosition).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupInfo groupInfo = (GroupInfo) getGroup(groupPosition);
            if (groupInfo.isCheckable()) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.restore_group_item_with_radio_btn, null);
                } else {
                    if (convertView.findViewById(R.id.radio_btn) == null) {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.restore_group_item_with_radio_btn, null);
                    }
                }
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(((GroupInfo) getGroup(groupPosition)).getTitle());

                ImageView radioBtn = (ImageView) convertView.findViewById(R.id.radio_btn);
                if (groupInfo.isChecked()) {
                    radioBtn.setImageResource(R.drawable.icon_btn_selected);
                } else {
                    radioBtn.setImageResource(R.drawable.icon_btn_unselected);
                }
            } else {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.restore_group_item_with_dropdown_arrow, null);
                } else {
                    if (convertView.findViewById(R.id.radio_btn) != null) {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.restore_group_item_with_dropdown_arrow, null);
                    }
                }
                TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(((GroupInfo) getGroup(groupPosition)).getTitle());
            }
            convertView.setTag(groupInfo);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildInfo childInfo = (ChildInfo) getChild(groupPosition, childPosition);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.restore_child_item, null);
            }

            TextView model = (TextView) convertView.findViewById(R.id.model);
            model.setText(childInfo.getModel());

            TextView imei = (TextView) convertView.findViewById(R.id.imei);
            imei.setText(childInfo.getImei());

            ImageView lockedImg = (ImageView) convertView.findViewById(R.id.locked_img);
            if (childInfo.getRestoreType() == RESTORE_TYPE_LOCK_DEVICE) {
                lockedImg.setVisibility(View.VISIBLE);
            } else {
                lockedImg.setVisibility(View.GONE);
            }

            ImageView radioBtn = (ImageView) convertView.findViewById(R.id.radio_btn);
            if (childInfo.isChecked()) {
                radioBtn.setImageResource(R.drawable.icon_btn_selected);
            } else {
                radioBtn.setImageResource(R.drawable.icon_btn_unselected);
            }

            convertView.setTag(childInfo);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private interface Checkable extends DeviceStatusGetterSetter {

        boolean isChecked();

        void setChecked(boolean isChecked);

    }

    private interface DeviceStatusGetterSetter {

        DeviceStatusInfo getDeviceStatusInfo();

        void setDeviceStatusInfo(DeviceStatusInfo deviceStatusInfo);

    }

    private class GroupInfo implements Checkable {

        private String title;

        private boolean isChecked;
        private boolean isCheckable;
        private int restoreType;

        private List<ChildInfo> childList;

        private DeviceStatusInfo deviceStatusInfo;

        public void setCheckable(boolean checkable) {
            isCheckable = checkable;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<ChildInfo> getChildList() {
            return childList;
        }

        public void setChildList(List<ChildInfo> childList) {
            this.childList = childList;
        }

        @Override
        public boolean isChecked() {
            return isChecked;
        }

        @Override
        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }

        public boolean isCheckable() {
            return isCheckable;
        }

        public int getRestoreType() {
            return restoreType;
        }

        public void setRestoreType(int restoreType) {
            this.restoreType = restoreType;
        }

        @Override
        public DeviceStatusInfo getDeviceStatusInfo() {
            return deviceStatusInfo;
        }

        @Override
        public void setDeviceStatusInfo(DeviceStatusInfo deviceStatusInfo) {
            this.deviceStatusInfo = deviceStatusInfo;
        }
    }

    public class ChildInfo implements Checkable {

        private boolean isChecked;
        private int restoreType;

        private DeviceStatusInfo deviceStatusInfo;

        public String getModel() {
            return deviceStatusInfo.getModel();
        }

        public String getImei() {
            return deviceStatusInfo.getImei();
        }

        @Override
        public boolean isChecked() {
            return isChecked;
        }

        @Override
        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        @Override
        public DeviceStatusInfo getDeviceStatusInfo() {
            return deviceStatusInfo;
        }

        @Override
        public void setDeviceStatusInfo(DeviceStatusInfo deviceStatusInfo) {
            this.deviceStatusInfo = deviceStatusInfo;
        }

        public int getRestoreType() {
            return restoreType;
        }

        public void setRestoreType(int restoreType) {
            this.restoreType = restoreType;
        }
    }

    private void registerTera() {
        if (mJwtToken != null) {
            registerWithJwtToken(mJwtToken);
        } else {
            registerWithoutJwtToken();
        }
    }

    private void registerWithJwtToken(final String jwtToken) {
        mProgressDialogUtils.show(getString(R.string.processing_msg));

        MgmtCluster.RegisterParam registerParam = new MgmtCluster.RegisterParam(mContext);
        registerParam.closeOldCloudSpace();

        MgmtCluster.RegisterProxy registerProxy = new MgmtCluster.RegisterProxy(registerParam, jwtToken);
        registerProxy.setOnRegisterListener(new MgmtCluster.RegisterListener() {
            @Override
            public void onRegisterSuccessful(final DeviceServiceInfo deviceServiceInfo) {
                mWorkHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean isSuccess = TeraCloudConfig.storeHCFSConfig(deviceServiceInfo);
                        if (!isSuccess) {
                            TeraCloudConfig.resetHCFSConfig();
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialogUtils.dismiss();
                                    mErrorMessage.setText(R.string.activate_failed);
                                }
                            });
                            return;
                        }

                        AccountInfo accountInfo = new AccountInfo();
                        accountInfo.setName(mAccountName);
                        accountInfo.setEmail(mAccountEmail);
                        if (mPhotoUrl != null) {
                            accountInfo.setImgUrl(mPhotoUrl);
                        }

                        AccountDAO accountDAO = AccountDAO.getInstance(mContext);
                        accountDAO.clear();
                        accountDAO.insert(accountInfo);

                        TeraAppConfig.enableApp(mContext);
                        TeraCloudConfig.activateTeraCloud(mContext);

                        String url = deviceServiceInfo.getBackend().getUrl();
                        String token = deviceServiceInfo.getBackend().getToken();
                        HCFSMgmtUtils.setSwiftToken(url, token);

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Bundle args = new Bundle();
                                args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_DISPLAY_NAME, mAccountName);
                                args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_EMAIL, mAccountEmail);
                                args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_PHOTO_URI, mPhotoUrl);

                                MainFragment mainFragment = MainFragment.newInstance();
                                mainFragment.setArguments(args);

                                Logs.d(CLASSNAME, "onRegisterSuccessful", "Replace with MainFragment");
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.replace(R.id.fragment_container, mainFragment, MainFragment.TAG);
                                ft.commitAllowingStateLoss();

                                mProgressDialogUtils.dismiss();
                            }
                        });

                    }
                });
            }

            @Override
            public void onRegisterFailed(DeviceServiceInfo deviceServiceInfo) {
                Logs.e(CLASSNAME, "registerWithJwtToken", "onRegisterFailed",
                        "deviceServiceInfo=" + deviceServiceInfo);

                CharSequence errorMessage = mContext.getText(R.string.activate_failed);
                if (deviceServiceInfo.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                    mProgressDialogUtils.dismiss();
                    if (deviceServiceInfo.getErrorCode().equals(MgmtCluster.ErrorCode.IMEI_NOT_FOUND)) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(ActivateWoCodeFragment.KEY_AUTH_TYPE, MgmtCluster.GOOGLE_AUTH);
                        bundle.putString(ActivateWoCodeFragment.KEY_USERNAME, mAccountEmail);
                        bundle.putString(ActivateWoCodeFragment.KEY_JWT_TOKEN, jwtToken);

                        ActivateWithCodeFragment fragment = ActivateWithCodeFragment.newInstance();
                        fragment.setArguments(bundle);

                        Logs.d(CLASSNAME, "onRegisterFailed", "Replace with ActivateWithCodeFragment");
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container, fragment);
                        ft.commitAllowingStateLoss();
                        return;
                    }
                    errorMessage = MgmtCluster.ErrorCode.getErrorMessage(mContext, deviceServiceInfo.getErrorCode());
                    mErrorMessage.setText(errorMessage);
                } else if (deviceServiceInfo.getResponseCode() == HttpsURLConnection.HTTP_FORBIDDEN) {
                    registerWithoutJwtToken();
                } else {
                    mErrorMessage.setText(errorMessage);
                }
            }

        });
        registerProxy.register();
    }

    /**
     * Without jwtToken, register Tera with jwtToken from authenticating with Google to get
     * serverAuthCode and then authenticating with mgmt server to get jwtToken.
     */
    private void registerWithoutJwtToken() {
        mProgressDialogUtils.show(getString(R.string.processing_msg));

        String serverClientId = MgmtCluster.getServerClientId();
        GoogleSilentAuthProxy googleAuthProxy = new GoogleSilentAuthProxy(mContext, serverClientId,
                new GoogleSilentAuthProxy.OnAuthListener() {
                    @Override
                    public void onAuthSuccessful(GoogleSignInResult result, GoogleApiClient googleApiClient) {
                        GoogleSignInAccount acct = result.getSignInAccount();
                        if (acct == null) {
                            registerWithoutJwtToken();
                            return;
                        }
                        String serverAuthCode = acct.getServerAuthCode();
                        MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam(serverAuthCode);
                        MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
                        authProxy.setOnAuthListener(new MgmtCluster.OnAuthListener() {
                            @Override
                            public void onAuthSuccessful(AuthResultInfo authResultInfo) {
                                String jwtToken = authResultInfo.getToken();
                                registerWithJwtToken(jwtToken);
                            }

                            @Override
                            public void onAuthFailed(AuthResultInfo authResultInfo) {
                                Logs.e(CLASSNAME, "registerWithoutJwtToken", "onAuthFailed",
                                        "authResultInfo=" + authResultInfo);
                                int responseCode = authResultInfo.getResponseCode();
                                if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                                    registerWithoutJwtToken();
                                } else {
                                    mErrorMessage.setText(R.string.activate_failed);
                                }
                            }
                        });
                        authProxy.auth();
                    }

                    @Override
                    public void onAuthFailed(GoogleSignInResult result) {
                        Logs.e(CLASSNAME, "registerWithoutJwtToken",
                                "onAuthFailed", "result=" + result);
                        if (result != null) {
                            String email = null;
                            String status = result.getStatus().toString();
                            GoogleSignInAccount account = result.getSignInAccount();
                            if (account != null) {
                                email = account.getEmail();
                            }
                            Logs.e(CLASSNAME, "registerWithoutJwtToken",
                                    "onAuthFailed",
                                    "email=" + email + ", status=" + status);
                        }
                        mProgressDialogUtils.dismiss();
                    }

                });
        googleAuthProxy.auth();
    }

    private void restoreDevice(String sourceImei) {
        Logs.d(CLASSNAME, "restoreDevice", "sourceImei=" + sourceImei);
        if (mJwtToken != null) {
            restoreDeviceWithJwtToken(mJwtToken, sourceImei);
        } else {
            restoreDeviceWithoutJwtToken(sourceImei);
        }
    }

    private void restoreDeviceWithJwtToken(final String jwtToken, final String sourceImei) {
        mProgressDialogUtils.show(getString(R.string.processing_msg));

        String currentImei = HCFSMgmtUtils.getDeviceImei(mContext);
        MgmtCluster.SwitchDeviceBackendParam param =
                new MgmtCluster.SwitchDeviceBackendParam(mContext);
        param.setSourceImei(sourceImei);
        MgmtCluster.SwitchDeviceBackendProxy proxy =
                new MgmtCluster.SwitchDeviceBackendProxy(param, currentImei, jwtToken);
        proxy.setOnSwitchDeviceBackendListener(new MgmtCluster.SwitchDeviceBackendProxy.
                OnSwitchDeviceBackendListener() {
            @Override
            public void onSwitchSuccessful(final DeviceServiceInfo deviceServiceInfo) {
                Logs.d(CLASSNAME, "restoreDeviceWithJwtToken", "onSwitchSuccessful", "deviceServiceInfo=" + deviceServiceInfo);

                mWorkHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TeraCloudConfig.storeHCFSConfigWithoutReload(deviceServiceInfo);
                        int code = HCFSMgmtUtils.triggerRestore();
                        if (code == RestoreStatus.Error.OUT_OF_SPACE) {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mErrorMessage.setText(R.string.restore_pre_check_out_of_space);
                                    mProgressDialogUtils.dismiss();
                                }
                            });
                            return;
                        }
                        TeraCloudConfig.reloadConfig();
                        HCFSMgmtUtils.setSwiftToken(deviceServiceInfo.getBackend().getUrl(),
                                deviceServiceInfo.getBackend().getToken());

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_IN_PROGRESS);
                        editor.apply();

                        // Enable Tera app so that we are able to get new token when token expired
                        TeraAppConfig.enableApp(mContext);

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialogUtils.dismiss();

                                Logs.d(CLASSNAME, "onSwitchSuccessful", "Replace with RestorePreparingFragment");
                                FragmentManager fm = getFragmentManager();
                                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                FragmentTransaction ft = fm.beginTransaction();
                                ft.replace(R.id.fragment_container, RestorePreparingFragment.newInstance());
                                ft.commitAllowingStateLoss();
                            }
                        });
                    }
                });

            }

            @Override
            public void onSwitchFailed(DeviceServiceInfo deviceServiceInfo) {
                mProgressDialogUtils.dismiss();

                if (deviceServiceInfo.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    // Jwt token is expired
                    restoreDeviceWithoutJwtToken(sourceImei);
                } else if (deviceServiceInfo.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                    // If restore failed due to out of space, user choose another backup but not allow
                    // to restore due to mapping error.
                    mErrorMessage.setText(R.string.restore_failed_device_in_use);
                } else {
                    mErrorMessage.setText(R.string.restore_failed);
                }
            }
        });
        proxy.switchBackend();
    }

    /**
     * Without jwtToken, switch device backend with jwtToken from authenticating with Google to get
     * serverAuthCode and then authenticating with mgmt server to get jwtToken.
     */
    private void restoreDeviceWithoutJwtToken(final String sourceImei) {
        mProgressDialogUtils.show(getString(R.string.processing_msg));

        String serverClientId = MgmtCluster.getServerClientId();
        GoogleSilentAuthProxy googleAuthProxy = new GoogleSilentAuthProxy(mContext, serverClientId,
                new GoogleSilentAuthProxy.OnAuthListener() {
                    @Override
                    public void onAuthSuccessful(GoogleSignInResult result, GoogleApiClient googleApiClient) {
                        GoogleSignInAccount acct = result.getSignInAccount();
                        if (acct == null) {
                            restoreDeviceWithoutJwtToken(sourceImei);
                            return;
                        }
                        String serverAuthCode = acct.getServerAuthCode();
                        MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam(serverAuthCode);
                        MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
                        authProxy.setOnAuthListener(new MgmtCluster.OnAuthListener() {
                            @Override
                            public void onAuthSuccessful(AuthResultInfo authResultInfo) {
                                mProgressDialogUtils.dismiss();

                                String jwtToken = authResultInfo.getToken();
                                restoreDeviceWithJwtToken(jwtToken, sourceImei);
                            }

                            @Override
                            public void onAuthFailed(AuthResultInfo authResultInfo) {
                                Logs.e(CLASSNAME, "restoreDeviceWithoutJwtToken", "onAuthFailed",
                                        "authResultInfo=" + authResultInfo);
                                mProgressDialogUtils.dismiss();

                                int responseCode = authResultInfo.getResponseCode();
                                if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                                    restoreDeviceWithoutJwtToken(sourceImei);
                                } else {
                                    mErrorMessage.setText(R.string.restore_failed);
                                }
                            }
                        });
                        authProxy.auth();
                    }

                    @Override
                    public void onAuthFailed(GoogleSignInResult result) {
                        Logs.e(CLASSNAME, "restoreDeviceWithoutJwtToken",
                                "onAuthFailed",
                                "result=" + result);
                        if (result != null) {
                            String email = null;
                            String status = result.getStatus().toString();
                            GoogleSignInAccount account = result.getSignInAccount();
                            if (account != null) {
                                email = account.getEmail();
                            }
                            Logs.e(CLASSNAME, "restoreDeviceWithoutJwtToken",
                                    "onAuthFailed",
                                    "email=" + email + ", status=" + status);
                        }
                        mProgressDialogUtils.dismiss();
                    }

                });
        googleAuthProxy.auth();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
    }

    private void refreshDeviceList(String jwtToken) {
        mProgressDialogUtils.show(R.string.processing_msg);
        if (jwtToken == null) {
            refreshDeviceListWithoutJwtToken();
        } else {
            refreshDeviceListWithJwtToken(jwtToken);
        }
    }

    private void refreshDeviceListWithJwtToken(final String jwtToken) {
        String imei = HCFSMgmtUtils.getDeviceImei(mContext);
        MgmtCluster.GetDeviceListProxy proxy = new MgmtCluster.GetDeviceListProxy(jwtToken, imei);
        proxy.setOnGetDeviceListListener(new MgmtCluster.GetDeviceListProxy.OnGetDeviceListListener() {
            @Override
            public void onGetDeviceListSuccessful(DeviceListInfo deviceListInfo) {
                Logs.d(CLASSNAME, "refreshDeviceListWithJwtToken", "onGetDeviceListSuccessful",
                        "deviceListInfo=" + deviceListInfo);

                // No any device backup can be restored, directly register to Tera
                if (deviceListInfo.getDeviceStatusInfoList().size() == 0) {
                    Logs.d(CLASSNAME, "refreshDeviceListWithJwtToken", "onGetDeviceListSuccessful",
                            "No any device backup can be restored, directly register to Tera");
                    deviceListInfo.setType(DeviceListInfo.TYPE_RESTORE_NONE);
                }

                mRestoreListAdapter.setGroupList(createRestoreList(deviceListInfo));
                mRestoreListAdapter.notifyDataSetChanged();

                mProgressDialogUtils.dismiss();
            }

            @Override
            public void onGetDeviceListFailed(DeviceListInfo deviceListInfo) {
                Logs.e(CLASSNAME, "refreshDeviceListWithJwtToken", "onGetDeviceListFailed",
                        "deviceListInfo=" + deviceListInfo);

                if (deviceListInfo.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                    mErrorMessage.setText(R.string.activate_failed_device_in_use);
                    mProgressDialogUtils.dismiss();
                } else if (deviceListInfo.getResponseCode() == HttpsURLConnection.HTTP_FORBIDDEN) {
                    refreshDeviceListWithoutJwtToken();
                } else {
                    mErrorMessage.setText(R.string.activate_auth_failed);
                    mProgressDialogUtils.dismiss();
                }
            }
        });
        proxy.get();
    }

    private void refreshDeviceListWithoutJwtToken() {
        String serverClientId = MgmtCluster.getServerClientId();
        GoogleSilentAuthProxy proxy = new GoogleSilentAuthProxy(mContext, serverClientId, new GoogleSilentAuthProxy.OnAuthListener() {
            @Override
            public void onAuthSuccessful(GoogleSignInResult result, GoogleApiClient googleApiClient) {
                String serverAuthCode = result.getSignInAccount().getServerAuthCode();
                MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam(serverAuthCode);
                MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
                authProxy.setOnAuthListener(new MgmtCluster.OnAuthListener() {
                    @Override
                    public void onAuthSuccessful(AuthResultInfo authResultInfo) {
                        String jwtToken = authResultInfo.getToken();
                        refreshDeviceListWithJwtToken(jwtToken);
                    }

                    @Override
                    public void onAuthFailed(AuthResultInfo authResultInfo) {
                        Logs.d(CLASSNAME, "GoogleSilentAuthProxy", "onAuthFailed", "authResultInfo=" + authResultInfo.toString());
                        if (authResultInfo.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                            refreshDeviceListWithoutJwtToken();
                        } else {
                            mErrorMessage.setText(R.string.activate_auth_failed);
                        }
                    }
                });
                authProxy.auth();
            }

            @Override
            public void onAuthFailed(GoogleSignInResult result) {
                Logs.e(CLASSNAME, "refreshDeviceListWithoutJwtToken",
                        "onAuthFailed", "result=" + result);
                if (result != null) {
                    String email = null;
                    String status = result.getStatus().toString();
                    GoogleSignInAccount account = result.getSignInAccount();
                    if (account != null) {
                        email = account.getEmail();
                    }
                    Logs.e(CLASSNAME, "refreshDeviceListWithoutJwtToken",
                            "onAuthFailed",
                            "email=" + email + ", status=" + status);
                }
            }
        });
        proxy.auth();
    }


    private List<GroupInfo> createRestoreList(DeviceListInfo deviceListInfo) {
        List<GroupInfo> groupList = new ArrayList<>();
        GroupInfo setupNewDevice = new GroupInfo();
        setupNewDevice.setCheckable(true);
        setupNewDevice.setTitle(getString(R.string.restore_item_setup_new_device));
        setupNewDevice.setRestoreType(RESTORE_TYPE_NEW_DEVICE);
        groupList.add(setupNewDevice);

        if (deviceListInfo.getType() == DeviceListInfo.TYPE_RESTORE_FROM_MY_TERA) {
            GroupInfo restoreFromMyTera = new GroupInfo();
            restoreFromMyTera.setCheckable(true);
            restoreFromMyTera.setTitle(getString(R.string.restore_item_my_tera));
            restoreFromMyTera.setRestoreType(RESTORE_TYPE_MY_TERA);
            restoreFromMyTera.setDeviceStatusInfo(deviceListInfo.getDeviceStatusInfoList().get(0));
            groupList.add(restoreFromMyTera);
        } else if (deviceListInfo.getType() == DeviceListInfo.TYPE_RESTORE_FROM_BACKUP) {
            List<ChildInfo> childList = new ArrayList<>();
            List<DeviceStatusInfo> deviceStatusInfoList = deviceListInfo.getDeviceStatusInfoList();
            for (DeviceStatusInfo info : deviceStatusInfoList) {
                ChildInfo childInfo = new ChildInfo();
                childInfo.setDeviceStatusInfo(info);
                if (info.getServiceStatus().equals(MgmtCluster.ServiceState.DISABLED)) { // locked state
                    childInfo.setRestoreType(RESTORE_TYPE_LOCK_DEVICE);
                } else {
                    childInfo.setRestoreType(RESTORE_TYPE_NON_LOCK_DEVICE);
                }
                childList.add(childInfo);
            }

            GroupInfo restoreFromBackup = new GroupInfo();
            restoreFromBackup.setCheckable(false);
            restoreFromBackup.setTitle(getString(R.string.restore_item_backup));
            restoreFromBackup.setChildList(childList);

            groupList.add(restoreFromBackup);
        }
        return groupList;
    }

}
