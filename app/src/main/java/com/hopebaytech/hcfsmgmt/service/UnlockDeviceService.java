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

import com.hopebaytech.hcfsmgmt.info.UnlockDeviceInfo;
import com.hopebaytech.hcfsmgmt.misc.JobServiceId;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.PeriodicServiceUtils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/12/21.
 */
public class UnlockDeviceService extends JobService {

    private final String CLASSNAME = UnlockDeviceService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Logs.d(CLASSNAME, "onStartJob", "jobId=" + params.getJobId());
        unlockDevice(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Logs.d(CLASSNAME, "onStopJob", "jobId=" + params.getJobId());
        return false;
    }

    private void unlockDevice(final JobParameters params) {
        final String imei = HCFSMgmtUtils.getDeviceImei(this);
        MgmtCluster.getJwtToken(this, new MgmtCluster.OnFetchJwtTokenListener() {
            @Override
            public void onFetchSuccessful(String jwtToken) {
                MgmtCluster.UnlockDeviceProxy unlockDeviceProxy = new MgmtCluster.UnlockDeviceProxy(jwtToken, imei);
                unlockDeviceProxy.setOnUnlockDeviceListener(new MgmtCluster.UnlockDeviceProxy.OnUnlockDeviceListener() {
                    @Override
                    public void onUnlockDeviceSuccessful(UnlockDeviceInfo unlockDeviceInfo) {
                        Logs.d(CLASSNAME, "onUnlockDeviceSuccessful", "unlockDeviceInfo=" + unlockDeviceInfo);

                        // Call jobFinished to inform the JobManager we've finished executing this time.
                        jobFinished(params, true /* needsReschedule */);

                        // Call stopPeriodicService to completely stop this job service
                        PeriodicServiceUtils.stopPeriodicService(
                                UnlockDeviceService.this,
                                JobServiceId.UNLOCK_DEVICE
                        );
                    }

                    @Override
                    public void onUnlockDeviceFailed(UnlockDeviceInfo unlockDeviceInfo) {
                        Logs.e(CLASSNAME, "onUnlockDeviceFailed", "unlockDeviceInfo=" + unlockDeviceInfo);
                    }
                });
                unlockDeviceProxy.unlock();
            }

            @Override
            public void onFetchFailed() {
                Logs.e(CLASSNAME, "onFetchFailed", null);
            }
        });
    }

}
