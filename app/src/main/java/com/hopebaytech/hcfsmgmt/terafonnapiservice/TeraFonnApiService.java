package com.hopebaytech.hcfsmgmt.terafonnapiservice;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.utils.ExecutorFactory;
import com.hopebaytech.hcfsmgmt.utils.HCFSApiUtils;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.LocationStatus;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.PinType;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class TeraFonnApiService extends Service {

    private static final String CLASSNAME = TeraFonnApiService.class.getSimpleName();

    private boolean isServiceRunning;
    private IGetJWTandIMEIListener mGetJwtAndImeiListener;
    private IFetchAppDataListener mFetchAppDataListener;
    private ITrackAppStatusListener mTrackAppStatusListener;

    private Map<String, Integer> mPackageStatusMap = new ConcurrentHashMap<>();
    private ExecutorService mExecutor = ExecutorFactory.createThreadPoolExecutor();
    private TrackAppStatusThread mTrackAppStatusThread = new TrackAppStatusThread();

    private final ITeraFonnApiService.Stub mBinder = new ITeraFonnApiService.Stub() {

        @Override
        public void setJWTandIMEIListener(IGetJWTandIMEIListener listener) throws RemoteException {
            mGetJwtAndImeiListener = listener;
        }

        @Override
        public boolean getJWTandIMEI() throws RemoteException {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    MgmtCluster.getJwtToken(TeraFonnApiService.this, new MgmtCluster.OnFetchJwtTokenListener() {
                        @Override
                        public void onFetchSuccessful(String jwt) {
                            String imei = HCFSMgmtUtils.getDeviceImei(TeraFonnApiService.this);
                            try {
                                mGetJwtAndImeiListener.onDataGet(imei, jwt);
                            } catch (Exception e) {
                                Logs.e(CLASSNAME, "getJWTandIMEI", "onFetchSuccessful", Log.getStackTraceString(e));
                            }
                        }

                        @Override
                        public void onFetchFailed() {
                            try {
                                mGetJwtAndImeiListener.onDataGet("", "");
                            } catch (Exception e) {
                                Logs.e(CLASSNAME, "getJWTandIMEI", "onFetchFailed", Log.getStackTraceString(e));
                            }
                        }
                    });

                }
            });
            return true;
        }

        @Override
        public void setFetchAppDataListener(IFetchAppDataListener listener) throws RemoteException {
            mFetchAppDataListener = listener;
        }

        @Override
        public boolean fetchAppData(final String packageName) throws RemoteException {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mFetchAppDataListener == null) {
                        Logs.w(CLASSNAME, "fetchAppData", "run", "mFetchAppDataListener is null");
                        return;
                    }
                    try {
                        mFetchAppDataListener.onPreFetch(packageName);
                        int progress;
                        while (true) {
                            progress = getAppProgress(packageName);
                            if (progress >= 100 || progress < 0) {
                                break;
                            } else {
                                mFetchAppDataListener.onProgressUpdate(packageName, progress);
                            }
                            Thread.sleep(3000);
                        }

                        if (progress < 0) {
                            mFetchAppDataListener.onFetchFailed(packageName);
                        } else {
                            mFetchAppDataListener.onPostFetch(packageName);
                        }
                    } catch (Exception e) {
                        Logs.e(CLASSNAME, "fetchAppData", "run", Log.getStackTraceString(e));
                    }
                }
            });

            return true;
        }

        @Override
        public void setTrackAppStatusListener(ITrackAppStatusListener listener) throws RemoteException {
            mTrackAppStatusListener = listener;
        }

        @Override
        public boolean addTrackAppStatus(List<String> packageNameList) throws RemoteException {
            boolean isSuccess = false;
            try {
                if (packageNameList == null) {
                    return false;
                }
                for (final String packageName : packageNameList) {
                    if (mPackageStatusMap.containsKey(packageName)) {
                        continue;
                    }
                    // Given a default app status for the package, the status will be changed later
                    // in TrackAppStatusRunnable.
                    mPackageStatusMap.put(packageName, AppStatus.UNAVAILABLE /* default app status */);
                }
                isSuccess = true;
            } catch (Exception e) {
                Logs.e(CLASSNAME, "addTrackAppStatus", Log.getStackTraceString(e));
            }
            mTrackAppStatusThread._continue();
            return isSuccess;
        }

        @Override
        public boolean removeTrackAppStatus(List<String> packageNameList) throws RemoteException {
            boolean isSuccess = false;
            try {
                if (packageNameList == null) {
                    return false;
                }
                for (String packageName : packageNameList) {
                    mPackageStatusMap.remove(packageName);
                }
                isSuccess = true;
            } catch (Exception e) {
                Logs.e(CLASSNAME, "removeTrackAppStatus", Log.getStackTraceString(e));
            }
            return isSuccess;
        }

        @Override
        public boolean clearTrackAppStatus() throws RemoteException {
            mTrackAppStatusThread.pause();
            boolean isSuccess;
            try {
                mPackageStatusMap.clear();
                isSuccess = true;
            } catch (Exception e) {
                isSuccess = false;
                Logs.e(CLASSNAME, "clearTrackAppStatus", Log.getStackTraceString(e));
            }
            return isSuccess;
        }

        @Override
        public AppInfo getAppInfo(List<String> packageNameList) throws RemoteException {
            AppInfo appInfo = new AppInfo();
            List<AppStatus> appStatusList = new ArrayList<>();
            if (packageNameList == null) {
                appInfo.setResult(false);
            } else {
                appInfo.setResult(true);
                for (String packageName : packageNameList) {
                    appStatusList.add(getAppStatus(packageName));
                }
            }
            appInfo.setAppStatusList(appStatusList);
            return appInfo;
        }

        @Override
        public boolean pinApp(String packageName) throws RemoteException {
            return handleFailureOfPinOrUnpin(true, packageName);
        }

        @Override
        public boolean unpinApp(String packageName) throws RemoteException {
            return handleFailureOfPinOrUnpin(false, packageName);
        }

        @Override
        public int checkAppAvailable(String packageName) throws RemoteException {
            int status;
            HCFSStatInfo hcfsStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
            boolean isAvailable = HCFSConnStatus.isAvailable(TeraFonnApiService.this, hcfsStatInfo);
            if (isAvailable) {
                status = AppStatus.AVAILABLE;
            } else {
                status = getPackageStatus(packageName);
            }
            mPackageStatusMap.put(packageName, status);
            return status;
        }

        @Override
        public String getHCFSStat() throws RemoteException {
            String data;
            try {
                data = HCFSApiUtils.getHCFSStat();
            } catch (Exception e) {
                data = "{\"result\": false}";
                Logs.e(CLASSNAME, "getHCFSStat", e.toString());
            }
            return data;
        }

        @Override
        public boolean hcfsEnabled() throws RemoteException {
            return TeraCloudConfig.isTeraCloudActivated(TeraFonnApiService.this);
        }

        @Override
        public int startUploadTeraData() throws RemoteException {
            return HCFSMgmtUtils.startUploadTeraData();
        }

        @Override
        public int stopUploadTeraData() throws RemoteException {
            return HCFSMgmtUtils.stopUploadTeraData();
        }

        @Override
        public long getTeraFreeSpace() {
            HCFSStatInfo hcfsStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
            if (hcfsStatInfo != null) {
                return hcfsStatInfo.getTeraTotal() - hcfsStatInfo.getTeraUsed();
            }
            return 0;
        }

        @Override
        public long getTeraTotalSpace() {
            HCFSStatInfo hcfsStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
            if (hcfsStatInfo != null) {
                return hcfsStatInfo.getTeraTotal();
            }
            return 0;
        }

        @Override
        public boolean isWifiOnly() {
            boolean wifiOnly = true;
            SettingsDAO mSettingsDAO = SettingsDAO.getInstance(TeraFonnApiService.this);
            SettingsInfo settingsInfo = mSettingsDAO.get(SettingsFragment.PREF_SYNC_WIFI_ONLY);
            if (settingsInfo != null) {
                wifiOnly = Boolean.valueOf(settingsInfo.getValue());
            }
            return wifiOnly;
        }

        public int getConnStatus() throws RemoteException {
            HCFSStatInfo info = HCFSMgmtUtils.getHCFSStatInfo();
            return HCFSConnStatus.getConnStatus(TeraFonnApiService.this, info);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Logs.d(CLASSNAME, "onStartCommand", null);

        if (isServiceRunning) {
            Logs.d(CLASSNAME, "onStartCommand", "Service is running");
            return super.onStartCommand(intent, flags, startId);
        } else {
            isServiceRunning = true;
            mTrackAppStatusThread.start();
        }

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Logs.d(CLASSNAME, "onTaskRemoved", null);
        releaseResource();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.d(CLASSNAME, "onDestroy", null);
        releaseResource();
    }

    private void releaseResource() {
        Logs.d(CLASSNAME, "releaseResource", null);
        isServiceRunning = false;
        mGetJwtAndImeiListener = null;
        mTrackAppStatusListener = null;
        mFetchAppDataListener = null;
        if (mPackageStatusMap != null) {
            mPackageStatusMap.clear();
            mPackageStatusMap = null;
        }
        if (mExecutor != null) {
            mExecutor.shutdownNow();
            mExecutor = null;
        }
        if (mTrackAppStatusThread != null) {
            mTrackAppStatusThread.interrupt();
            mTrackAppStatusThread = null;
        }
    }

    private AppStatus getAppStatus(String packageName) {
        Random random = new Random();
        // TODO: not implemented yet
        boolean isOnFetching = random.nextBoolean();

        /*
        String dataDir = getDataDir(packageName);
        List<String> appPath = getExternalDir(packageName);

        appPath.add(dataDir);

        List<Boolean> pinStatus = new ArrayList<>();
        for (String path : appPath) {
            pinStatus.add(HCFSMgmtUtils.isPathPinned(path));
        }
        if (pinStatus.contains(false)) {
            isPin = false;
        }
        */

        boolean isPinned = false;
        try {
            UidDAO uidDAO = UidDAO.getInstance(this);
            UidInfo uidInfo = uidDAO.get(packageName);
            isPinned = uidInfo != null && uidInfo.isPinned();
        } catch (Exception e) {
            Logs.e(CLASSNAME, "getAppStatus", Log.getStackTraceString(e));
        }
        return new AppStatus(packageName, isPinned, isOnFetching, AppStatus.UNAVAILABLE);
    }

    private int getPackageStatus(String packageName) {
        try {
            UidDAO uidDAO = UidDAO.getInstance(TeraFonnApiService.this);
            UidInfo uidInfo = uidDAO.get(packageName);

            ApplicationInfo applicationInfo = getPackageManager().
                    getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            com.hopebaytech.hcfsmgmt.info.AppInfo appInfo =
                    new com.hopebaytech.hcfsmgmt.info.AppInfo(TeraFonnApiService.this);
            appInfo.setApplicationInfo(applicationInfo);
            if (uidInfo != null) {
                appInfo.setExternalDirList(uidInfo.getExternalDir());
            }
            return appInfo.getAppStatus();
        } catch (PackageManager.NameNotFoundException e) {
            Logs.e(CLASSNAME, "getPackageStatus", Log.getStackTraceString(e));
        }
        return AppStatus.UNAVAILABLE;
    }

    private Boolean handleFailureOfPinOrUnpin(Boolean pinOP, String packageName) {
        boolean isSuccess = pinOrUnpin(pinOP, packageName);
        if (isSuccess) {
            updateDB(pinOP, packageName);
        } else {
            pinOrUnpin(!pinOP, packageName);
        }
        return isSuccess;
    }

    private Boolean pinOrUnpin(Boolean pinOP, String packageName) {
        boolean isSuccess = false;
        String OP = pinOP ? "Pin" : "Unpin";
        String dataDir = getDataDir(packageName);

        if (dataDir.equals("")) {
            return false;
        }

        try {
            String jsonResult = pinOP ? HCFSApiUtils.pin(dataDir, PinType.NORMAL) : HCFSApiUtils.unpin(dataDir);
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");

            String logMsg = "operation=" + OP + ", filePath=" + dataDir + ", jsonResult=" + jsonResult;
            if (isSuccess) {
                Logs.i(CLASSNAME, "pinFileOrDirectory", logMsg);
                List<String> externalPath = getExternalDir(packageName);

                if (externalPath.size() == 0) {
                    return true;
                }

                for (String path : externalPath) {
                    jsonResult = pinOP ? HCFSApiUtils.pin(path, PinType.NORMAL) : HCFSApiUtils.unpin(path);
                    jObject = new JSONObject(jsonResult);
                    isSuccess = jObject.getBoolean("result");

                    logMsg = "operation=" + OP + ", filePath=" + path + ", jsonResult=" + jsonResult;
                    if (isSuccess) {
                        Logs.i(CLASSNAME, "pinFileOrDirectory", logMsg);
                    } else {
                        Logs.e(CLASSNAME, "pinFileOrDirectory", logMsg);
                        break;
                    }
                }
            } else {
                Logs.e(CLASSNAME, "pinFileOrDirectory", logMsg);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "pinFileOrDirectory", Log.getStackTraceString(e));
        }

        return isSuccess;
    }

    private void updateDB(boolean pinOP, String packageName) {
        try {
            UidDAO uidDAO = UidDAO.getInstance(this);
            UidInfo uidInfo = uidDAO.get(packageName);
            if (uidInfo != null) {
                uidInfo.setPinned(pinOP);
                uidDAO.update(uidInfo, UidDAO.PIN_STATUS_COLUMN);
            } else {
                PackageManager pm = getPackageManager();
                ApplicationInfo packageInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                uidDAO.insert(new UidInfo(pinOP, false, packageInfo.uid, packageName));
            }
        } catch (Exception e) {
            Logs.e(CLASSNAME, "updateDB", e.toString());
        }
    }

    private String getSourceDir(String packageName) {
        PackageManager m = getPackageManager();
        String sourceDir = "";
        try {
            PackageInfo p = m.getPackageInfo(packageName, 0);
            sourceDir = p.applicationInfo.sourceDir;
            sourceDir = sourceDir.substring(0, sourceDir.lastIndexOf("/"));
            Logs.i(CLASSNAME, "getSourceDir", sourceDir);
        } catch (PackageManager.NameNotFoundException e) {
            Logs.w(CLASSNAME, "getSourceDir", "Error: Package (" + packageName + ") not found ");
        }

        return sourceDir;
    }

    private String getDataDir(String packageName) {
        PackageManager m = getPackageManager();
        String dataDir = "";
        try {
            PackageInfo p = m.getPackageInfo(packageName, 0);
            dataDir = p.applicationInfo.dataDir;
            Logs.i(CLASSNAME, "getDataDir", dataDir);
        } catch (PackageManager.NameNotFoundException e) {
            Logs.w(CLASSNAME, "getDataDir", "Error: Package (" + packageName + ") not found ");
        }

        return dataDir;
    }

    private List<String> getExternalDir(String packageName) {
        List<String> externalDir = new ArrayList<>();
        String externalPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
        try {
            File externalAndroidFile = new File(externalPath);
            for (File type : externalAndroidFile.listFiles()) {
                File[] fileList = type.listFiles();
                for (File file : fileList) {
                    String path = file.getAbsolutePath();
                    if (path.contains(packageName)) {
                        Logs.i(CLASSNAME, "getExternalDir", path);
                        externalDir.add(path);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            externalDir.clear();
        }

        return externalDir;
    }

    private int getAppProgress(String packageName) {
        int progress = -1;

        try {
            if (!HCFSConnStatus.isAvailable(TeraFonnApiService.this, HCFSMgmtUtils.getHCFSStatInfo())) {
                return progress;
            }

            String dataDir = getDataDir(packageName);
            if (dataDir.equals("")) return -1;

            String sourceDir = getSourceDir(packageName);
            if (sourceDir.equals("")) return -1;

            List<String> appPath = getExternalDir(packageName);
            appPath.add(dataDir);
            appPath.add(sourceDir);
            boolean getDirStatusResult = true;
            int appTotal = 0;
            int localTotal = 0;

            for (String path : appPath) {
                String jsonResult = HCFSApiUtils.getDirStatus(path);
                JSONObject jObject = new JSONObject(jsonResult);
                if (jObject.getBoolean("result") && jObject.getInt("code") == 0) {
                    JSONObject dataObj = jObject.getJSONObject("data");
                    int numLocal = dataObj.getInt("num_local");
                    int numHybrid = dataObj.getInt("num_hybrid");
                    int numCloud = dataObj.getInt("num_cloud");
                    appTotal = appTotal + numLocal + numHybrid + numCloud;
                    localTotal = localTotal + numLocal;
                    Logs.d(CLASSNAME, "getAppProgress", jsonResult);
                } else {
                    getDirStatusResult = false;
                    break;
                }
            }

            if (getDirStatusResult) {
                progress = Math.round((float) (localTotal / appTotal) * 100);
            } else {
                progress = -1;
            }

            Logs.d(CLASSNAME, "getAppProgress", String.valueOf(progress));
        } catch (Exception e) {
            progress = -1;
            Logs.e(CLASSNAME, "getAppProgress", e.toString());
        }
        return progress;
    }

    private class TrackAppStatusThread extends Thread {

        private boolean running;

        private void pause() {
            this.running = false;
        }

        private void _continue() {
            this.running = true;
            synchronized (this) {
                notify();
            }
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    if (!running) {
                        synchronized (this) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (mTrackAppStatusListener == null) {
                        Logs.w(CLASSNAME, "onStartCommand", "TrackAppStatusListener is null");
                        Thread.sleep(3000);
                        continue;
                    }

                    final Set<String> packageKeySet = mPackageStatusMap.keySet();
                    HCFSStatInfo hcfsStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
                    final boolean isAvailable = HCFSConnStatus.isAvailable(TeraFonnApiService.this, hcfsStatInfo);
                    for (final String packageName : packageKeySet) {
                        mExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    int appStatus = mPackageStatusMap.get(packageName);

                                    int reportStatus;
                                    if (isAvailable) {
                                        reportStatus = AppStatus.AVAILABLE;
                                    } else {
                                        reportStatus = getPackageStatus(packageName);
                                    }

                                    if (appStatus != reportStatus) {
                                        mTrackAppStatusListener.onStatusChanged(packageName, reportStatus);
                                        mPackageStatusMap.put(packageName, reportStatus);
                                    }
                                } catch (Exception e) {
                                    Logs.e(CLASSNAME, "onStartCommand", Log.getStackTraceString(e));
                                    try {
                                        mTrackAppStatusListener.onTrackFailed(packageName);
                                    } catch (Exception e1) {
                                        Logs.e(CLASSNAME, "onStartCommand", Log.getStackTraceString(e1));
                                    }
                                }
                            }
                        });
                    }
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Logs.w(CLASSNAME, "onStartCommand", Log.getStackTraceString(e));
                    break;
                }
            }
        }
    }

}
