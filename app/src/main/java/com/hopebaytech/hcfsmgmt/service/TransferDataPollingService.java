package com.hopebaytech.hcfsmgmt.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.GetDeviceInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.PollingServiceUtils;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

import static com.hopebaytech.hcfsmgmt.utils.PollingServiceUtils.JOB_ID_TRANSFER_DATA;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/21.
 */
public class TransferDataPollingService extends JobService {

    private final String CLASSNAME = TransferDataPollingService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Logs.d(CLASSNAME, "onStartJob", "params=" + params);
        checkDeviceServiceStatus(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Logs.d(CLASSNAME, "onStopJob", "params=" + params);
        return false;
    }

    private void checkDeviceServiceStatus(final JobParameters params) {
        if (!NetworkUtils.isNetworkConnected(this)) {
            return;
        }

        MgmtCluster.getJwtToken(this, new MgmtCluster.OnFetchJwtTokenListener() {
            @Override
            public void onFetchSuccessful(String jwt) {
                String imei = HCFSMgmtUtils.getDeviceImei(TransferDataPollingService.this);
                MgmtCluster.GetDeviceServiceInfoProxy proxy = new MgmtCluster.GetDeviceServiceInfoProxy(jwt, imei);
                proxy.setOnGetDeviceServiceInfoListener(new MgmtCluster.
                        GetDeviceServiceInfoProxy.OnGetDeviceServiceInfoListener() {
                    @Override
                    public void onGetDeviceServiceInfoSuccessful(DeviceServiceInfo deviceServiceInfo) {
                        DeviceServiceInfo.Piggyback piggyback = deviceServiceInfo.getPiggyback();
                        String category = piggyback.getCategory();
                        Logs.d(CLASSNAME, "onGetDeviceServiceInfoSuccessful", "category=" + category);
                        switch (category) {
                            case GetDeviceInfo.Category.UNREGISTERED:
                                sendBroadcast(new Intent(TeraIntent.ACTION_TRANSFER_COMPLETED));

                                // Call stopPollingService to completely stop this job service
                                PollingServiceUtils.stopPollingService(TransferDataPollingService.this, JOB_ID_TRANSFER_DATA);
                                break;
                        }

                        // Call jobFinished to inform the JobManager we've finished executing this time.
                        jobFinished(params, false);
                    }

                    @Override
                    public void onGetDeviceServiceInfoFailed(DeviceServiceInfo deviceServiceInfo) {
                        Logs.e(CLASSNAME, "onGetDeviceServiceInfoFailed", null);
                    }
                });
                proxy.get();
            }

            @Override
            public void onFetchFailed() {
                Logs.e(CLASSNAME, "onFetchFailed", "Failed to retrieve jwt token from mgmt server.");
            }

        });
    }
}
