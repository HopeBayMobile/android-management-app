package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.customview.UsageIcon;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.misc.Threshold;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MessageDialog;

import java.text.Format;
import java.util.Locale;

public class OverviewFragment extends Fragment {

    public static final String TAG = OverviewFragment.class.getSimpleName();
    private static final String CLASSNAME = TAG;

    private Thread mUiRefreshThread;
    private ImageView mNetworkConnStatusImage;
    private TextView mNetworkConnStatusText;
    private Usage mPhysicalSpaceUsage;
    private Usage mSystemSpaceUsage;
    private Usage mCacheSpaceUsage;
    private Usage mUsedSpaceUsage;
    private Usage mPinnedSpaceUsage;
    private Usage mDataWaitToUploadUsage;
    private TextView mNetworkXferUp;
    private TextView mNetworkXferDown;
    private boolean mIsCurrentVisible;
    private Context mContext;
    private HCFSStatInfo mStatInfo;
    private Handler mUiHandler;

    private Runnable mUiRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    mStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mStatInfo == null) {
                                mPhysicalSpaceUsage.getValue().setText("-");
                                mSystemSpaceUsage.getValue().setText("-");
                                mCacheSpaceUsage.getValue().setText("-");
                                mUsedSpaceUsage.getValue().setText("-");
                                mPinnedSpaceUsage.getValue().setText("-");
                                mDataWaitToUploadUsage.getValue().setText("-");
                                mNetworkXferUp.setText("-");
                                mNetworkXferDown.setText("-");
                                return;
                            }

                            updateNetworkStatus(HCFSConnStatus.getConnStatus(mContext, mStatInfo));

                            // Set Physical Space text
                            String physicalSpaceText = String.format(Locale.getDefault(),
                                    "%s / %s",
                                    mStatInfo.getFormatPhysicalUsed(),
                                    mStatInfo.getFormatPhysicalTotal());

                            mPhysicalSpaceUsage.showUsage(mStatInfo.getPhysicalUsedPercentage(),
                                    physicalSpaceText);

                            // Set System Space text
                            String systemSpaceText = String.format(Locale.getDefault(),
                                    "%s / %s",
                                    mStatInfo.getFormatSystemUsed(),
                                    mStatInfo.getFormatSystemTotal());

                            mSystemSpaceUsage.showUsage(mStatInfo.getSystemUsedPercentage(),
                                    systemSpaceText);

                            // Set Cache Space text and
                            String cacheSpaceText = String.format(Locale.getDefault(),
                                    "%s / %s",
                                    mStatInfo.getFormatCacheUsed(),
                                    mStatInfo.getFormatCacheTotal());

                            mCacheSpaceUsage.showUsage(mStatInfo.getCacheUsedPercentage(),
                                    cacheSpaceText);

                            String usedSpaceText = String.format(Locale.getDefault(),
                                    "%s / %s",
                                    mStatInfo.getFormatCloudUsed(),
                                    mStatInfo.getFormatCloudTotal());
                            mUsedSpaceUsage.showUsage(mStatInfo.getCloudUsedPercentage(),
                                    usedSpaceText);

                            String pinnedSpaceText = String.format(Locale.getDefault(),
                                    "%s / %s",
                                    mStatInfo.getFormatPinTotal(),
                                    mStatInfo.getFormatPinMax());

                            mPinnedSpaceUsage.showUsage(mStatInfo.getPinnedUsedPercentage(),
                                    pinnedSpaceText);

                            mDataWaitToUploadUsage.showUsage(mStatInfo.getDirtyPercentage(),
                                    mStatInfo.getFormatCacheDirtyUsed());

                            String xferDownload = mStatInfo.getFormatXferDownload();
                            String xferUpload = mStatInfo.getFormatXferUpload();

                            mNetworkXferUp.setText(xferUpload);
                            mNetworkXferDown.setText(xferDownload);
                        }
                    });
                    Thread.sleep(Interval.UPDATE_OVERVIEW_INFO);
                } catch (InterruptedException e) {
                    Logs.d(CLASSNAME, "Runnable", "run", "UiRefreshThread is interrupted");
                    break;
                }
            }
        }
    };

    public static OverviewFragment newInstance() {
        return new OverviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.d(CLASSNAME, "onCreate", null);
        mContext = getActivity();
        mUiHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logs.d(CLASSNAME, "onCreateView", null);
        return inflater.inflate(R.layout.overview_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logs.d(CLASSNAME, "onViewCreated", null);

        // Initialized network status view
        mNetworkConnStatusImage = (ImageView) view.findViewById(R.id.network_conn_status_icon);
        mNetworkConnStatusText = (TextView) view.findViewById(R.id.network_conn_status);

        // Initialized physical space view
        RelativeLayout physicalSpace = (RelativeLayout) view.findViewById(R.id.physical_space);
        UsageIcon physicalSpaceIcon = (UsageIcon) physicalSpace.findViewById(R.id.physical_space_icon);
        physicalSpaceIcon.getLayoutParams();
        TextView physicalSpaceValue = (TextView) physicalSpace.findViewById(R.id.physical_space_value);
        mPhysicalSpaceUsage = new Usage(physicalSpaceIcon, physicalSpaceValue);

        // Initialized system space view
        RelativeLayout systemSpace = (RelativeLayout) view.findViewById(R.id.system_space);
        UsageIcon systemSpaceIcon = (UsageIcon) systemSpace.findViewById(R.id.system_space_icon);
        TextView systemSpaceValue = (TextView) systemSpace.findViewById(R.id.system_space_value);
        mSystemSpaceUsage = new Usage(systemSpaceIcon, systemSpaceValue);

        // Initialized cache space view
        RelativeLayout cacheSpace = (RelativeLayout) view.findViewById(R.id.cache_space);
        UsageIcon cacheSpaceIcon = (UsageIcon) cacheSpace.findViewById(R.id.cache_space_icon);
        TextView cacheSpaceValue = (TextView) cacheSpace.findViewById(R.id.cache_space_value);
        mCacheSpaceUsage = new Usage(cacheSpaceIcon, cacheSpaceValue);

        // Initialized cloud space view
        RelativeLayout usedSpace = (RelativeLayout) view.findViewById(R.id.used_space);
        UsageIcon usedSpaceIcon = (UsageIcon) usedSpace.findViewById(R.id.used_space_icon);
        TextView usedSpaceValue = (TextView) usedSpace.findViewById(R.id.used_space_value);
        mUsedSpaceUsage = new Usage(usedSpaceIcon, usedSpaceValue);

        // Initialized pinned space view
        RelativeLayout pinnedSpace = (RelativeLayout) view.findViewById(R.id.pinned_space);
        UsageIcon pinnedSpaceIcon = (UsageIcon) pinnedSpace.findViewById(R.id.pinned_space_icon);
        TextView pinnedSpaceValue = (TextView) pinnedSpace.findViewById(R.id.pinned_space_value);
        pinnedSpaceIcon.setWarningPercentage(Threshold.PINNED_SPACE);
        mPinnedSpaceUsage = new Usage(pinnedSpaceIcon, pinnedSpaceValue);

        // Initialized data wait to upload view
        RelativeLayout dataWaitToUpload = (RelativeLayout) view.findViewById(R.id.data_wait_to_upload);
        UsageIcon dataWaitToUploadIcon = (UsageIcon) dataWaitToUpload.findViewById(R.id.data_wait_to_upload_icon);
        TextView dataWaitToUploadValue = (TextView) dataWaitToUpload.findViewById(R.id.data_wait_to_upload_value);
        mDataWaitToUploadUsage = new Usage(dataWaitToUploadIcon, dataWaitToUploadValue);

        // Initialized network xfer up/down view
        mNetworkXferUp = (TextView) view.findViewById(R.id.xfer_up);
        mNetworkXferDown = (TextView) view.findViewById(R.id.xfer_down);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logs.d(CLASSNAME, "onActivityCreated", null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SettingsDAO settingsDAO = SettingsDAO.getInstance(mContext);
                String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
                int warningPercentage = Integer.valueOf(defaultValue);
                SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO);
                if (settingsInfo != null) {
                    warningPercentage = Integer.valueOf(settingsInfo.getValue());
                }

                final int percentage = warningPercentage;
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mUsedSpaceUsage.getIcon().setWarningPercentage(percentage);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logs.d(CLASSNAME, "onResume", null);

        if (mUiRefreshThread == null) {
            if (mIsCurrentVisible) {
                mUiRefreshThread = new Thread(mUiRefreshRunnable);
                mUiRefreshThread.start();
                Logs.d(CLASSNAME, "onPause", "UiRefreshThread is started");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Logs.d(CLASSNAME, "onPause", null);

        if (mUiRefreshThread != null && !mUiRefreshThread.isInterrupted()) {
            mUiRefreshThread.interrupt();
            mUiRefreshThread = null;
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        Logs.d(CLASSNAME, "setMenuVisibility", null);
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            mIsCurrentVisible = true;
            if (mUiRefreshThread == null) {
                if (mUiRefreshRunnable != null) {
                    mUiRefreshThread = new Thread(mUiRefreshRunnable);
                    mUiRefreshThread.start();
                    Logs.d(CLASSNAME, "setMenuVisibility", "UiRefreshThread is started");
                }
            }
        } else {
            mIsCurrentVisible = false;
            if (mUiRefreshThread != null && !mUiRefreshThread.isInterrupted()) {
                mUiRefreshThread.interrupt();
                mUiRefreshThread = null;
            }
        }
    }

    private void updateNetworkStatus(int connStatus) {
        switch (connStatus) {
            case HCFSConnStatus.TRANS_FAILED:
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_failed);
                mNetworkConnStatusText.setText(mContext.getString(R.string.overview_hcfs_conn_status_failed));
                break;
            case HCFSConnStatus.TRANS_NOT_ALLOWED:
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_not_allow);
                mNetworkConnStatusText.setText(mContext.getString(R.string.overview_hcfs_conn_status_not_allowed));
                break;
            case HCFSConnStatus.TRANS_NORMAL:
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_normal);
                mNetworkConnStatusText.setText(mContext.getString(R.string.overview_hcfs_conn_status_normal));
                break;
            case HCFSConnStatus.TRANS_IN_PROGRESS:
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_transmitting);
                mNetworkConnStatusText.setText(mContext.getString(R.string.overview_hcfs_conn_status_in_progress));
                break;
            case HCFSConnStatus.TRANS_SLOW:
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_slow);
                mNetworkConnStatusText.setText(mContext.getString(R.string.overview_hcfs_conn_status_slow));
                break;
        }
    }

    private class Usage {

        private UsageIcon icon;
        private TextView value;

        private Usage(UsageIcon icon, TextView value) {
            this.icon = icon;
            this.value = value;
        }

        public UsageIcon getIcon() {
            return icon;
        }

        private TextView getValue() {
            return value;
        }

        private void showUsage(int percentage, String valueText) {
            // percentage = 80;
            // valueText = "test";
            icon.showPercentage(percentage);
            if (icon.isWarning()) {
                value.setTextColor(ContextCompat.getColor(mContext, R.color.colorUserIconWarningValue));
            } else {
                value.setTextColor(ContextCompat.getColor(mContext, R.color.colorUserIconNormalValue));
            }
            value.setText(valueText);
        }

    }

}
