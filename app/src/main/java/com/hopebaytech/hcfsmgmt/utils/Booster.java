package com.hopebaytech.hcfsmgmt.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/15.
 */

public class Booster {

    private static final String CLASSNAME = Booster.class.getSimpleName();

    private static final long BOOSTER_MAXIMUM_SPACE = 4L * 1024 * 1024 * 1024; // booster_size <= 4G
    private static final String BOOSTER_PARTITION = "/data/mnt/hcfsblock";

    public static class Type {
        public static final int UNBOOSTED = 1;
        public static final int BOOSTED = 2;
    }

    /**
     * @return 0 if boosted, 1 if unboosted, error otherwise.
     * */
    public static boolean isPackageBoosted(String packageName) {
        boolean isBoosted = false;
        try {
            String jsonResult = HCFSApiUtils.checkPackageBoostStatus(packageName);
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            int code = jObject.getInt("code");
            if (isSuccess) {
                isBoosted = (code == 0);
                Logs.d(CLASSNAME, "isPackageBoosted", "jObject=" + jObject);
            } else {
                Logs.e(CLASSNAME, "isPackageBoosted", "jObject=" + jObject);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "isPackageBoosted", Log.getStackTraceString(e));
        }
        return isBoosted;
    }

    public static int getInstalledAppBoostStatus(String packageName) {
        return isPackageBoosted(packageName) ? UidInfo.BoostStatus.BOOSTED : UidInfo.BoostStatus.UNBOOSTED;
    }

    public static boolean enableBooster(long boosterSize) {
        Logs.i(CLASSNAME, "enableBooster", "boosterSize=" + boosterSize);
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.enableBooster(boosterSize);
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                Logs.d(CLASSNAME, "enableBooster", "jObject=" + jObject);
            } else {
                Logs.e(CLASSNAME, "enableBooster", "jObject=" + jObject);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "enableBooster", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static boolean disableBooster() {
        Logs.i(CLASSNAME, "disableBooster", null);
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.disableBooster();
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                Logs.d(CLASSNAME, "disableBooster", "jObject=" + jObject);
            } else {
                Logs.e(CLASSNAME, "disableBooster", "jObject=" + jObject);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "disableBooster", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static boolean triggerBoost() {
        Logs.i(CLASSNAME, "triggerBoost", null);
        boolean isTriggered = false;
        try {
            String jsonResult = HCFSApiUtils.triggerBoost();
            String logMsg = "jsonResult=" + jsonResult;
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                int code = jObject.getInt("code");
                isTriggered = (code == 1);
                Logs.d(CLASSNAME, "triggerBoost", logMsg);
            } else {
                Logs.e(CLASSNAME, "triggerBoost", logMsg);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "triggerBoost", Log.getStackTraceString(e));
        }
        return isTriggered;
    }

    public static boolean triggerUnboost() {
        Logs.i(CLASSNAME, "triggerUnboost", null);
        boolean isTriggered = false;
        try {
            String jsonResult = HCFSApiUtils.triggerUnboost();
            String logMsg = "jsonResult=" + jsonResult;
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                int code = jObject.getInt("code");
                isTriggered = (code == 1);
                Logs.d(CLASSNAME, "triggerUnboost", logMsg);
            } else {
                Logs.e(CLASSNAME, "triggerUnboost", logMsg);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "triggerUnboost", Log.getStackTraceString(e));
        }
        return isTriggered;
    }

    public static void enableApp(Context context, String packageName) {
        Logs.i(CLASSNAME, "enableApp", "packageName=" + packageName);
        PackageManager pm = context.getPackageManager();
        pm.setApplicationEnabledSetting(packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, 0);
    }

    public static void enableApps(Context context) {
        Logs.i(CLASSNAME, "enableApps", null);

        ContentValues cv = new ContentValues();
        cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.BOOSTED);

        UidDAO uidDAO = UidDAO.getInstance(context);
        List<UidInfo> disabledList = uidDAO.get(cv);
        for (UidInfo uidInfo: disabledList) {
            enableApp(context, uidInfo.getPackageName());
        }
    }

    public static void disableApps(Context context) {
        Logs.i(CLASSNAME, "disableApps", null);

        ContentValues cv = new ContentValues();
        cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.BOOSTED);

        UidDAO uidDAO = UidDAO.getInstance(context);
        List<UidInfo> enabledList = uidDAO.get(cv);
        for (UidInfo uidInfo: enabledList) {
            disableApp(context, uidInfo.getPackageName());
        }
    }

    public static void disableApp(Context context, String pkgName) {
        Logs.i(CLASSNAME, "disableApp", "pkgName=" + pkgName);
        PackageManager pm = context.getPackageManager();
        pm.setApplicationEnabledSetting(pkgName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER, 0);
    }

    public static long getMinimumAvailableBoosterSpace() {
        return 100 * 1024 * 1024; // 100 MB
    }

    /**
     * Get the available booster space which maximum value is {@link #BOOSTER_MAXIMUM_SPACE}.
     *
     * @return the available booster space
     */
    public static long getAvailableBoosterSpace() {
        // booster uses pinned space
        HCFSStatInfo hcfsStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
        long availableSpace = 0L;
        if (hcfsStatInfo != null) {
            availableSpace = hcfsStatInfo.getPinMax() - hcfsStatInfo.getPinTotal();
            if (availableSpace > BOOSTER_MAXIMUM_SPACE) {
                return BOOSTER_MAXIMUM_SPACE;
            } else {
                return availableSpace;
            }
        }
        return availableSpace;
    }

    private static class AppDataSizeHelper {

        private Context mContext;
        private List<AppInfo> mPackageNameList;
        private long mRequiredBoostSize;
        private CountDownLatch mCountDownLatch;

        private AppDataSizeHelper(Context context, List<AppInfo> packageNameList) {
            this.mContext = context;
            this.mPackageNameList = packageNameList;
            this.mCountDownLatch = new CountDownLatch(packageNameList.size());
        }

        private long getDataSize() {
            PackageManager pm = mContext.getPackageManager();
            for (AppInfo appInfo : mPackageNameList) {
                try {
                    Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                    getPackageSizeInfo.invoke(pm, appInfo.getPackageName(), new IPackageStatsObserver.Stub() {
                        @Override
                        public synchronized void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                            mRequiredBoostSize += pStats.dataSize;
                            mCountDownLatch.countDown();
                        }
                    });
                } catch (Exception e) {
                    Logs.e(CLASSNAME, "getDataSize", Log.getStackTraceString(e));
                }
            }

            try {
                mCountDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return mRequiredBoostSize;
        }

    }

    /**
     * Check whether the app data size is smaller than free space which subtract cache total from
     * occupied size (unpinned but dirty)
     *
     * @param context
     * @param packageNameList the list of package name
     * @return true if enough space to unboost apps, false otherwise.
     */
    public static boolean isEnoughUnboosterSpace(Context context, List<AppInfo> packageNameList) {
        HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
        if (statInfo == null) {
            Logs.e(CLASSNAME, "isEnoughUnboosterSpace", "HCFSStatInfo is null.");
            return false;
        }

        AppDataSizeHelper appDataSizeHelper = new AppDataSizeHelper(context, packageNameList);
        long appDataSize = appDataSizeHelper.getDataSize();

        long occupiedSize = HCFSMgmtUtils.getOccupiedSize();
        long freeSpace = statInfo.getCacheTotal() - occupiedSize;

        return appDataSize < freeSpace;
    }

    public static long getBoosterFreeSpace() {
        File partition = new File(BOOSTER_PARTITION);
        return partition.getFreeSpace();
    }

    public static long getBoosterUsedSpace() {
        File partition = new File(BOOSTER_PARTITION);
        return partition.getTotalSpace() - partition.getFreeSpace();
    }

    public static long getBoosterTotalSpace() {
        File partition = new File(BOOSTER_PARTITION);
        return partition.getTotalSpace();
    }

    /**
     * Check whether the app data size is smaller than free space which subtracts pin max size from
     * pin total size.
     *
     * @param context
     * @param packageNameList the list of package name
     * @return true if enough space to boost apps, false otherwise.
     */
    public static boolean isEnoughBoosterSpace(Context context, List<AppInfo> packageNameList) {
        boolean isSpaceEnough = false;
        long freeSpaceInBooster = getBoosterFreeSpace();
        //long usedSpaceInBooster = file.getTotalSpace() - freeSpaceInBooster;
        AppDataSizeHelper appDataSizeHelper = new AppDataSizeHelper(context, packageNameList);
        long appDataSize = appDataSizeHelper.getDataSize();

        if (freeSpaceInBooster > appDataSize) {
            isSpaceEnough = true;
        }
        /* temp mark, the first version of ther booster size can't be changed after it is created.
        else {
            if ( (usedSpaceInBooster + totalNeedSize ) <= BOOSTER_MAXIMUM_SPACE) {
                boolean expandOk = expandBoosterSize();
                if (expandOk){
                    isSpaceEnough = true;
                } else { // expand failed!
                    isSpaceEnough = false;
                }
            } else { // booser maximum space is up to 4G
                isSpaceEnough = false;
            }
        } */

        return isSpaceEnough;
    }

    /* temp mark, the first version of ther booster size can't be changed after it is created.
    public static boolean expandBoosterSize() {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.expandBoosterSize();
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                Logs.i(CLASSNAME, "expandBoosterSize", "jObject=" + jObject);
            } else {
                Logs.e(CLASSNAME, "expandBoosterSize", "jObject=" + jObject);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "expandBoosterSize", Log.getStackTraceString(e));
        }
        return isSuccess;
    }
    */

    public static boolean clearBoosterPackageRemaining(String packageName) {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.clearBoosterPackageRemaining(packageName);
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                Logs.i(CLASSNAME, "clearBoosterPackageRemaining", "jObject=" + jObject);
            } else {
                Logs.e(CLASSNAME, "clearBoosterPackageRemaining", "jObject=" + jObject);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "clearBoosterPackageRemaining", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static void recoverBoostStatusWhenFailed(Context context) {
        int recoverBoostStatus = 0;
        ContentValues cv = new ContentValues();
        UidDAO uidDAO = UidDAO.getInstance(context);
        List<UidInfo> recoverList = null;
        switch (Booster.currentProcessBoostStatus(context)) {
            case UidInfo.BoostStatus.BOOSTING:
                cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.INIT_BOOST);
                recoverList = uidDAO.get(cv);

                cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.BOOST_FAILED);
                recoverList.addAll(uidDAO.get(cv));

                recoverBoostStatus = UidInfo.BoostStatus.UNBOOSTED;
                break;
            case UidInfo.BoostStatus.UNBOOSTING:
                cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.INIT_UNBOOST);
                recoverList = uidDAO.get(cv);

                cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.UNBOOST_FAILED);
                recoverList.addAll(uidDAO.get(cv));

                recoverBoostStatus = UidInfo.BoostStatus.BOOSTED;
                break;
        }

        if (recoverList != null) {
            for (UidInfo uidInfo : recoverList) {
                uidInfo.setBoostStatus(recoverBoostStatus);
                uidDAO.update(uidInfo);
            }
        }
    }

    public static int currentProcessBoostStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(SettingsFragment.PREF_BOOSTER_STATUS, 0);
    }

    public static void removeBoostStatusInXml(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SettingsFragment.PREF_BOOSTER_STATUS);
        editor.apply();
    }

    public static void updateBoostStatusInXml(Context context, int boostStatus) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SettingsFragment.PREF_BOOSTER_STATUS, boostStatus);
        editor.apply();
    }

    @Nullable
    public static List<AppInfo> getAppList(Context context, int boosterType) {
        List<UidInfo> uidInfoList = null;
        ContentValues cv = new ContentValues();
        UidDAO uidDAO = UidDAO.getInstance(context);
        switch (boosterType) {
            case Type.UNBOOSTED:
                cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.INIT_BOOST);
                uidInfoList = uidDAO.get(cv);

                cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.UNBOOSTED);
                uidInfoList.addAll(uidDAO.get(cv));
                break;
            case Type.BOOSTED:
                cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.INIT_UNBOOST);
                uidInfoList = uidDAO.get(cv);

                cv.put(UidDAO.BOOST_STATUS_COLUMN, UidInfo.BoostStatus.BOOSTED);
                uidInfoList.addAll(uidDAO.get(cv));
                break;
        }

        List<AppInfo> appInfoList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        if (uidInfoList != null) {
            for (UidInfo uidInfo: uidInfoList) {
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(uidInfo.getPackageName(), 0);

                    AppInfo appInfo = new AppInfo(context);
                    appInfo.setApplicationInfo(packageInfo.applicationInfo);
                    appInfo.setUid(packageInfo.applicationInfo.uid);
                    appInfo.setSystemApp(HCFSMgmtUtils.isSystemPackage(packageInfo.applicationInfo));
                    appInfo.setPackageInfo(packageInfo);
                    appInfo.setName(packageInfo.applicationInfo.loadLabel(pm).toString());
                    appInfo.setExternalDirList(null); // ignore external dir in booster scenario

                    appInfoList.add(appInfo);
                } catch (PackageManager.NameNotFoundException e) {
                    Logs.w(CLASSNAME, "getAppList", Log.getStackTraceString(e));
                }

            }

        }

        return appInfoList;
    }

    public static boolean umountBooster() {
        Logs.i(CLASSNAME, "umountBooster", null);
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.umountBooster();
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                Logs.d(CLASSNAME, "umountBooster", "jObject=" + jObject);
            } else {
                Logs.e(CLASSNAME, "umountBooster", null);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "umountBooster", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static boolean mountBooster() {
        Logs.i(CLASSNAME, "mountBooster", null);
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.mountBooster();
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                Logs.d(CLASSNAME, "mountBooster", "jObject=" + jObject);
            } else {
                Logs.e(CLASSNAME, "mountBooster", null);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "mountBooster", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static boolean isBoosterMounted() {
        File partition = new File(BOOSTER_PARTITION);
        return partition.exists();
    }

}
