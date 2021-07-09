/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hopebaytech.hcfsmgmt.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.GetDeviceInfo;
import com.hopebaytech.hcfsmgmt.misc.JobServiceId;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.PeriodicServiceUtils;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/21.
 */
public class CheckDeviceTransferredPeriodicService extends JobService {

    private final String CLASSNAME = CheckDeviceTransferredPeriodicService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Logs.d(CLASSNAME, "onStartJob", "jobId=" + params.getJobId());
        checkDeviceServiceStatus(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Logs.d(CLASSNAME, "onStopJob", "jobId=" + params.getJobId());
        return false;
    }

    private void checkDeviceServiceStatus(final JobParameters params) {
        if (!NetworkUtils.isNetworkConnected(this)) {
            return;
        }

        MgmtCluster.getJwtToken(this, new MgmtCluster.OnFetchJwtTokenListener() {
            @Override
            public void onFetchSuccessful(String jwt) {
                String imei = HCFSMgmtUtils.getDeviceImei(CheckDeviceTransferredPeriodicService.this);
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
                                PeriodicServiceUtils.stopPeriodicService(
                                        CheckDeviceTransferredPeriodicService.this,
                                        JobServiceId.CHECK_DEVICE_TRANSFERRED
                                );
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
