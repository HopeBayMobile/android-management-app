package com.hopebaytech.hcfsmgmt.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.customview.RestoreListAdapter;
import com.hopebaytech.hcfsmgmt.info.DeviceListInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceStatusInfo;
import com.hopebaytech.hcfsmgmt.info.SwiftConfigInfo;
import com.hopebaytech.hcfsmgmt.utils.GoogleDriveAPI;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.SwiftServerUtil;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/12.
 */
public class RestoreFragment extends RegisterFragment {

    public static final String TAG = RestoreFragment.class.getSimpleName();

    private final String CLASSNAME = TAG;

    public static final String KEY_DEVICE_LIST = "key_device_list";
    public static final String KEY_RESTORE_TYPE = "key_restore_type";
    public static final String KEY_GOOGLE_DRIVE_ACCESS_TOKEN= "key_access_token";

    public static final int RESTORE_TYPE_NEW_DEVICE = 1;
    public static final int RESTORE_TYPE_MY_TERA = 2;
    public static final int RESTORE_TYPE_LOCK_DEVICE = 3;
    public static final int RESTORE_TYPE_NON_LOCK_DEVICE = 4;

    private RestoreListAdapter mRestoreListAdapter;
    private ListView mRestoreListView;
    private TextView mBackButton;
    private TextView mNextButton;

    private Handler mWorkHandler;
    private HandlerThread mHandlerThread;

    private DeviceListInfo mDeviceListInfo;

    public static RestoreFragment newInstance() {
        return new RestoreFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandlerThread = new HandlerThread(CLASSNAME);
        mHandlerThread.start();
        mWorkHandler = new Handler(mHandlerThread.getLooper());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restore_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRestoreListView = (ListView) view.findViewById(R.id.restore_list);
        mBackButton = (TextView) view.findViewById(R.id.back_btn);
        mNextButton = (TextView) view.findViewById(R.id.next_btn);
        mErrorMessage = (TextView) view.findViewById(R.id.error_msg);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mDeviceListInfo = args.getParcelable(KEY_DEVICE_LIST);
            if (mDeviceListInfo == null) {
                Logs.e(CLASSNAME, "onActivityCreated", "deviceListInfo == null");
                return;
            }
        }

        mRestoreListAdapter = new RestoreListAdapter(getContext(), createRestoreList(mDeviceListInfo));
        mRestoreListView.setAdapter(mRestoreListAdapter);
        mRestoreListView.setOnItemClickListener(restoreListItemClickListener);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRestoreListAdapter.getNumOfSelectedItems() == 0) {
                    Logs.w(TAG, "onClick", "No backup is selected for restoration");
                    informErrorOccurred(R.string.restore_failed_no_backup_selected);
                    return;
                }

                if(mRestoreListAdapter.getNumOfSelectedItems() > 1) {
                    //TODO: Radio button not set up properly
                    Logs.e(TAG, "onClick", String.format("Radio button not set up properly, %d items are selected", mRestoreListAdapter.getNumOfSelectedItems()));
                    throw new RuntimeException("Radio button not set up properly");
                }

                //TODO: should only restore
                restoreDevice();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity) mContext).onBackPressed();
            }
        });
    }

    private AdapterView.OnItemClickListener restoreListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            for(RestoreListAdapter.ListItem listItem : mRestoreListAdapter.getListItems()) {
                listItem.setSelected(false);
            }

            RestoreListAdapter.ListItem selectedItem = ((RestoreListAdapter.ListItem)mRestoreListAdapter.getItem(position));
            selectedItem.setSelected(true);

            mRestoreListAdapter.notifyDataSetChanged();
        }
    };

    private void restoreDevice() {
        Logs.d(CLASSNAME, "restoreDevice", String.format("sourceImei=%s", HCFSMgmtUtils.getDeviceImei(mContext)));
        switch (mDeviceListInfo.getType()) {
            case DeviceListInfo.TYPE_RESTORE_FROM_GOOGLE_DRIVE:
                String accessToken = getArguments().getString(KEY_GOOGLE_DRIVE_ACCESS_TOKEN);

                if (TextUtils.isEmpty(accessToken)) {
                    informErrorOccurred(R.string.restore_failed);
                    return;
                }
                DeviceServiceInfo deviceServiceInfo = GoogleDriveAPI.buildDeviceServiceInfo(
                        accessToken, null);
                preRestoreSetupForGoogleDrive(deviceServiceInfo);
                break;
            case DeviceListInfo.TYPE_RESTORE_FROM_SWIFT:
                // get swift info from parcel
                Bundle bundle = getArguments();
                if (bundle == null) {
                    Logs.e(CLASSNAME, "restoreDevice", "cannot get bundle for swift info");
                    informErrorOccurred(R.string.restore_failed);
                    return;
                }
                SwiftConfigInfo swiftConfigInfo = bundle.getParcelable(SwiftConfigInfo.PARCEL_KEY);
                if (swiftConfigInfo == null) {
                    Logs.e(CLASSNAME, "restoreDevice", "swiftConfigInfo == null");
                    informErrorOccurred(R.string.restore_failed);
                    return;
                }

                String deviceImei = mRestoreListAdapter.getSelectedItem().getImei();
                String deviceModel = mRestoreListAdapter.getSelectedItem().getModel();
                String oldBucketPostfix = String.format("%s_%s", deviceImei, deviceModel);

                String swiftUrl = swiftConfigInfo.getUrl();
                String swiftAccount = swiftConfigInfo.getAccount();
                String swiftKey = swiftConfigInfo.getKey();
                String swiftBucketName = String.format("%s%s", SwiftServerUtil.SWIFT_TERA_BUCKET_PREFIX, oldBucketPostfix);

                preRestoreSetUpForSwift(swiftUrl, swiftAccount, swiftKey, swiftBucketName);
                break;
        }
    }

    private void preRestoreSetUpForSwift(final String swiftUrl,final String swiftAccount,final String swiftKey,final String swiftBucketName) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean writeToHCFSConfigSucceeded = writeSwiftInfoToHCFSConfig(swiftUrl, swiftAccount, swiftKey, swiftBucketName);
                if(!writeToHCFSConfigSucceeded) {
                    informErrorOccurred(R.string.restore_failed_upon_save_hcfs_config);
                    return;
                }
                int code = HCFSMgmtUtils.triggerRestore();
                if (code == RestoreStatus.Error.OUT_OF_SPACE) {
                    informErrorOccurred(R.string.restore_failed_out_of_space);
                    return;
                }
                if(code == RestoreStatus.Error.DAMAGED_BACKUP) {
                    informErrorOccurred(R.string.restore_failed_damaged_backup);
                    return;
                }
                if(code == RestoreStatus.Error.CONN_FAILED) {
                    informErrorOccurred(R.string.restore_failed_conn_failed);
                    return;
                }

                boolean reloadConfigSucceed = TeraCloudConfig.reloadConfig();
                if (!reloadConfigSucceed) {
                    informErrorOccurred(R.string.restore_failed_upon_reload_config);
                    return;
                }

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_IN_PROGRESS);
                editor.apply();

                TeraAppConfig.enableApp(mContext);
                replaceWithRestorePreparingFragment();
            }
        });
    }

    private void preRestoreSetupForGoogleDrive(final DeviceServiceInfo deviceServiceInfo) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                TeraCloudConfig.storeHCFSConfigWithoutReload(deviceServiceInfo, mContext);
                int code = HCFSMgmtUtils.triggerRestore();
                if (code == RestoreStatus.Error.OUT_OF_SPACE) {
                    informErrorOccurred(R.string.restore_failed_out_of_space);
                    return;
                }
                if(code == RestoreStatus.Error.DAMAGED_BACKUP) {
                    informErrorOccurred(R.string.restore_failed_damaged_backup);
                    return;
                }
                if(code == RestoreStatus.Error.CONN_FAILED) {
                    informErrorOccurred(R.string.restore_failed_conn_failed);
                    return;
                }

                TeraCloudConfig.reloadConfig();
                HCFSMgmtUtils.setSwiftToken(deviceServiceInfo.getBackend().getUrl(), deviceServiceInfo.getBackend().getToken());

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

    private void informErrorOccurred(final String errorMsg) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mErrorMessage.setText(errorMsg);
                mProgressDialogUtils.dismiss();
            }
        });
    }

    private void informErrorOccurred(final int errorMsgId) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mErrorMessage.setText(errorMsgId);
                mProgressDialogUtils.dismiss();
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

    private List<RestoreListAdapter.ListItem> createRestoreList(DeviceListInfo deviceListInfo) {
        List<RestoreListAdapter.ListItem> restoreListItems = new ArrayList<>();

        for (DeviceStatusInfo deviceStatusInfo : deviceListInfo.getDeviceStatusInfoList()) {
            restoreListItems.add(new RestoreListAdapter.ListItem(deviceStatusInfo));
        }

        return restoreListItems;
    }
}
