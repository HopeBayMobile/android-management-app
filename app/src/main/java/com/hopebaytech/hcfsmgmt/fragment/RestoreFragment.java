package com.hopebaytech.hcfsmgmt.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceListInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceStatusInfo;
import com.hopebaytech.hcfsmgmt.utils.AppAuthUtils;
import com.hopebaytech.hcfsmgmt.utils.GoogleDriveAPI;
import com.hopebaytech.hcfsmgmt.utils.GoogleSilentAuthProxy;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;

import net.openid.appauth.AuthState;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/12.
 */
public class RestoreFragment extends RegisterFragment {

    public static final String TAG = RestoreFragment.class.getSimpleName();

    private final String CLASSNAME = TAG;

    public static final String KEY_DEVICE_LIST = "key_device_list";
    public static final String KEY_RESTORE_TYPE = "key_restore_type";

    public static final int RESTORE_TYPE_NEW_DEVICE = 1;
    public static final int RESTORE_TYPE_MY_TERA = 2;
    public static final int RESTORE_TYPE_LOCK_DEVICE = 3;
    public static final int RESTORE_TYPE_NON_LOCK_DEVICE = 4;

    private ExpandableListView mExpandableListView;
    private TextView mBackButton;
    private TextView mNextButton;

    private RestoreListAdapter mRestoreListAdapter;
    private Checkable mPrevCheckableInfo;
    private Handler mWorkHandler;
    private HandlerThread mHandlerThread;

    private AuthState mGoogleDriveAuthState;

    public static RestoreFragment newInstance() {
        return new RestoreFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandlerThread = new HandlerThread(CLASSNAME);
        mHandlerThread.start();
        mWorkHandler = new Handler(mHandlerThread.getLooper());

        Bundle extras = getArguments();
        // TODO: get info from previous view
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
        if (mGoogleDriveAuthState != null) {
            mProgressDialogUtils.show(getString(R.string.processing_msg));
            doRestoreOrRegister(mContext, mGoogleDriveAuthState,
                    mGoogleDriveAuthState.getAccessToken(), false/* check restoration */);
            return;
        }
    }

    private boolean restoreDeviceFromGoogleDrive() {
        if (mGoogleDriveAuthState != null) {
            DeviceServiceInfo deviceServiceInfo = GoogleDriveAPI.buildDeviceServiceInfo(
                    null, mGoogleDriveAuthState.getAccessToken(), "googledrive", null, null);
            new AppAuthUtils().saveAppAuthStatusToSharedPreference(mContext, mGoogleDriveAuthState);
            preRestoreSetup(deviceServiceInfo);
            return true;
        }
        return false;
    }

    private void restoreDevice(String sourceImei) {
        Logs.d(CLASSNAME, "restoreDevice", "sourceImei=" + sourceImei);
        if (restoreDeviceFromGoogleDrive()) {
            return;
        }

        //TODO: ? restoreDeviceWithoutJwtToken(sourceImei);
    }

    private void preRestoreSetup(final DeviceServiceInfo deviceServiceInfo) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                TeraCloudConfig.storeHCFSConfigWithoutReload(deviceServiceInfo, mContext);
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

                replaceWithRestorePreparingFragment();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
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

    public void setGoogleDriveAuthState(AuthState authState) {
        mGoogleDriveAuthState = authState;
    }
}
