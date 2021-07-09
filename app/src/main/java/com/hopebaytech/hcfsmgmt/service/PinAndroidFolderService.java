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
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.os.Environment;

import com.hopebaytech.hcfsmgmt.misc.JobServiceId;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.ThreadPool;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/24.
 */
public class PinAndroidFolderService extends JobService {

    private final String CLASSNAME = PinAndroidFolderService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters params) {
        Logs.d(CLASSNAME, "onStartJob", "params=" + params);
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                int code = 0;
                String externalAndroidPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
                if (!HCFSMgmtUtils.isPathPinned(externalAndroidPath)) {
                    code = HCFSMgmtUtils.pinFileOrDirectory(externalAndroidPath);
                }

                jobFinished(params, false);
                if (code == 0) { // Pin successful
                    JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    jobScheduler.cancel(JobServiceId.PIN_ANDROID_FOLDER);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Logs.d(CLASSNAME, "onStopJob", "params=" + params);
        return false;
    }

}
