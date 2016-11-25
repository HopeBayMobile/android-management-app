package com.hopebaytech.hcfsmgmt.service;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.PollingServiceUtils;
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
                    jobScheduler.cancel(PollingServiceUtils.JOB_ID_PIN_ANDROID_FOLDER);
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
