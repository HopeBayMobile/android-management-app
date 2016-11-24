package com.hopebaytech.hcfsmgmt.main;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.SystemProperties;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/30.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Foreground.init(this);
    }

    public static class Foreground implements Application.ActivityLifecycleCallbacks {

        private static Foreground instance;
        private boolean foreground;

        private Foreground() {
        }

        private static void init(Application app) {
            if (instance == null) {
                instance = new Foreground();
                app.registerActivityLifecycleCallbacks(instance);
            }
        }

        public static Foreground get() {
            return instance;
        }

        public static Foreground get(Application application) {
            if (instance == null) {
                init(application);
            }
            return instance;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            // if ro.build.type is not "user", increase the log level to Log.DEBUG
            String buildType = SystemProperties.get("ro.build.type");
            if (!buildType.equals("user")) {
                Logs.LOG_LEVEL = Log.DEBUG;
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            foreground = true;
        }

        @Override
        public void onActivityPaused(Activity activity) {
            foreground = false;
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

        public boolean isForeground() {
            return foreground;
        }

    }

}
