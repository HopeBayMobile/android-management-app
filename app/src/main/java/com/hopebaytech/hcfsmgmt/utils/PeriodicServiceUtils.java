package com.hopebaytech.hcfsmgmt.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * @author Vince
 *         Created by Vince on 2016/7/14.
 */

public class PeriodicServiceUtils {

    private static final String CLASSNAME = PeriodicServiceUtils.class.getSimpleName();

    public static final String KEY_INTERVAL = "key_interval";

    // Not allowed to initialize this class
    private PeriodicServiceUtils() {
    }

    /**
     * Start a polling service with given class
     *
     * @param context        A Context of the application package implementing this class.
     * @param intervalMillis interval in milliseconds between subsequent repeats of the service.
     * @param cls            The component class that is to be used for the service.
     */
    public static void startPollingService(Context context, int intervalMillis, Class<?> cls) {
        Intent intentService = new Intent(context, cls);
        intentService.putExtra(KEY_INTERVAL, intervalMillis);
        context.startService(intentService);
    }

    /**
     * Stop a polling service with given class
     *
     * @param context A Context of the application package implementing this class.
     * @param cls     The component class that is to be used for the service.
     */
    public static void stopPollingService(Context context, Class<?> cls) {
        Intent intentService = new Intent(context, cls);
        context.stopService(intentService);
    }

    /**
     * Start a periodic service
     *
     * @param context        A Context of the application package implementing this class.
     * @param intervalMillis Interval in milliseconds between subsequent repeats of the service.
     * @param jobId          The job id of the starting polling service.
     * @param cls            The component class that is to be used for the service.
     */
    public static void startPeriodicService(Context context, int intervalMillis, int jobId, Class<? extends JobService> cls) {
        Logs.d(CLASSNAME, "startPeriodicService",
                "intervalMillis=" + intervalMillis
                        + ", jobId=" + jobId
                        + ", cls=" + cls.getCanonicalName());
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, new ComponentName(context.getPackageName(), cls.getName()));
        builder.setPeriodic(intervalMillis);
        jobScheduler.schedule(builder.build());
    }

    /**
     * Stop a periodic service
     *
     * @param context A Context of the application package implementing this class.
     * @param jobId   The job id to be canceled.
     */
    public static void stopPeriodicService(Context context, int jobId) {
        Logs.d(CLASSNAME, "stopPeriodicService", "jobId=" + jobId);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
    }

}
