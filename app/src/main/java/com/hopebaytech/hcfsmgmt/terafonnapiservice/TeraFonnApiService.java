package com.hopebaytech.hcfsmgmt.terafonnapiservice;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.info.LocationStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSApiUtils;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

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
import java.util.concurrent.Executors;

public class TeraFonnApiService extends Service {

    private final String TAG = "TeraFonnService";
    private final String CLASSNAME = getClass().getSimpleName();
    private IFetchAppDataListener mFetchAppDataListener;
    private ITrackAppStatusListener mTrackAppStatusListener;
    private Map<String, AppStatus> mPackageNameMap;
    private ExecutorService mCacheExecutor;

    private final ITeraFonnApiService.Stub mBinder = new ITeraFonnApiService.Stub() {

        @Override
        public void setFetchAppDataListener(IFetchAppDataListener listener) throws RemoteException {
            mFetchAppDataListener = listener;
        }

        @Override
        public boolean fetchAppData(final String packageName) throws RemoteException {
            boolean isSuccess = false;
            if (mPackageNameMap.containsKey(packageName)) {
                isSuccess = true;
                mCacheExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mFetchAppDataListener.onPreFetch(packageName);
                            int progress = 0;
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
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return isSuccess;
        }

        @Override
        public void setTrackAppStatusListener(ITrackAppStatusListener listener) throws RemoteException {
            mTrackAppStatusListener = listener;
        }

        @Override
        public boolean addTrackAppStatus(List<String> packageNameList) throws RemoteException {
            boolean isSuccess = false;
            try {
                if (packageNameList != null) {
                    isSuccess = true;
                    for (String packageName : packageNameList) {
                        if (!mPackageNameMap.containsKey(packageName)) {
                            mPackageNameMap.put(packageName, getDefaultAppStatus(packageName));
                        }
                    }
                }
            } catch (Exception e) {
                isSuccess = false;
                log(Log.ERROR, CLASSNAME, "addTrackAppStatus", Log.getStackTraceString(e));
            }
            return isSuccess;
        }

        @Override
        public boolean removeTrackAppStatus(List<String> packageNameList) throws RemoteException {
            boolean isSuccess = false;
            try {
                if (packageNameList != null) {
                    for (String packageName : packageNameList) {
                        mPackageNameMap.remove(packageName);
                    }
                    isSuccess = true;
                }
            } catch (Exception e) {
                log(Log.ERROR, CLASSNAME, "removeTrackAppStatus", Log.getStackTraceString(e));
            }
            return isSuccess;
        }

        @Override
        public boolean clearTrackAppStatus() throws RemoteException {
            boolean isSuccess;
            try {
                mPackageNameMap.clear();
                isSuccess = true;
            } catch (Exception e) {
                isSuccess = false;
                log(Log.ERROR, CLASSNAME, "clearTrackAppStatus", Log.getStackTraceString(e));
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
                    appStatusList.add(getDefaultAppStatus(packageName));
                }
            }
            appInfo.setAppStatusList(appStatusList);
            return appInfo;
        }

        @Override
        public boolean pinApp(String packageName) throws RemoteException {
            return pinOrUnpin(true, packageName);
        }

        @Override
        public boolean unpinApp(String packageName) throws RemoteException {
            return pinOrUnpin(false, packageName);
        }

        @Override
        public int checkAppAvailable(String packageName) throws RemoteException {
            return getDefaultStatus(packageName);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mPackageNameMap = new ConcurrentHashMap<>();
        mCacheExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log(Log.DEBUG, CLASSNAME, "onStartCommand", null);
        Thread pollingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Set<String> keySet = mPackageNameMap.keySet();
                        List<String> keyList = new ArrayList<>(keySet);
                        if (keyList.size() != 0 && mTrackAppStatusListener != null) {
                            for (String packageName : keyList) {
                                AppStatus appStatus = mPackageNameMap.get(packageName);
                                int reportStatus = getDifferentStatus(packageName, appStatus.getStatus());
                                if (reportStatus != -1) mTrackAppStatusListener.onStatusChanged(packageName, reportStatus);
                            }
                        }

//                        mTrackAppStatusListener.onTrackFailed();

                        Thread.sleep(3000);
                    } catch (Exception e) {
                        mTrackAppStatusListener = null;
                        mFetchAppDataListener = null;
                        mPackageNameMap.clear();
                        log(Log.ERROR, CLASSNAME, "onStartCommand", Log.getStackTraceString(e));
                        break;
                    }
                }
            }
        });
        pollingThread.start();

//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void log(int logLevel, String className, String funcName, String logMsg) {
        if (logMsg == null) {
            logMsg = "";
        }

        switch (logLevel) {
            case Log.DEBUG:
                Log.d(TAG, className + "(" + funcName + "): " + logMsg);
                break;
            case Log.INFO :
                Log.i(TAG, className + "(" + funcName + "): " + logMsg);
                break;
            case Log.WARN:
                Log.w(TAG, className + "(" + funcName + "): " + logMsg);
                break;
            case Log.ERROR:
                Log.e(TAG, className + "(" + funcName + "): " + logMsg);
                break;
        }
    }

    private AppStatus getDefaultAppStatus(String packageName) {
        Random random = new Random();
        // TODO: not implemented yet
        boolean isOnFetching = random.nextBoolean();

        boolean isPin = true;
        String dataDir = getDataDir(packageName);
        List<String> appPath = getExternalDir(packageName);

        appPath.add(dataDir);

        List<Boolean> pinStatus = new ArrayList<Boolean>();
        for (String path : appPath) {
            pinStatus.add(HCFSMgmtUtils.isPathPinned(path));
        }
        if (pinStatus.contains(false)) {
            isPin = false;
        }

        int status = getDefaultStatus(packageName);
        if (status != AppStatus.STATUS_AVAILABLE) {
            status = AppStatus.STATUS_UNAVAILABLE;
        }

        return new AppStatus(packageName, isPin, isOnFetching, status);
    }

    private int getDefaultStatus(String packageName) {
        String dataDir = getDataDir(packageName);
        if (NetworkUtils.isNetworkConnected(TeraFonnApiService.this)) {
            if (downloadToRun()) {
                if (HCFSMgmtUtils.getDirStatus(dataDir) == LocationStatus.LOCAL) {
                    return AppStatus.STATUS_AVAILABLE;
                } else {
                    return AppStatus.STATUS_UNAVAILABLE_WAIT_TO_DOWNLOAD;
                }
            } else {
                return AppStatus.STATUS_AVAILABLE;
            }
        } else {
            if (getDefaultLocation(packageName) == AppStatus.LOCATION_LOCAL) {
                return AppStatus.STATUS_AVAILABLE;
            } else {
                return AppStatus.STATUS_UNAVAILABLE_NONE_NETWORK;
            }
        }
    }

    private Boolean downloadToRun() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_download_to_run", false);
    }

    private int getDefaultLocation(String packageName) {
        int location = AppStatus.LOCATION_LOCAL;

        try {
            String dataDir = getDataDir(packageName);
            List<String> appPath = getExternalDir(packageName);

            appPath.add(dataDir);

            List<Integer> appLocation = new ArrayList<Integer>();

            for (String path : appPath) {
                appLocation.add(HCFSMgmtUtils.getDirStatus(path));
            }

            if (appLocation.contains(LocationStatus.HYBRID)) {
                location = AppStatus.LOCATION_HYBRID;
            } else if (appLocation.contains(LocationStatus.CLOUD)) {
                location = AppStatus.LOCATION_CLOUD;
            } else {
                location = AppStatus.LOCATION_LOCAL;
            }

            log(Log.DEBUG, CLASSNAME, "getDefaultLocation", "APP Location: " + String.valueOf(location));
        } catch (Exception e) {
            log(Log.ERROR, CLASSNAME, "getDefaultLocation", "Error: " + e.toString());
        }
        return location;
    }

    private int getDifferentStatus(String packageName, int currentStatus) {
        int status = getDefaultStatus(packageName);
        if (status != AppStatus.STATUS_AVAILABLE) {
            status = AppStatus.STATUS_UNAVAILABLE;
        }
        return status != currentStatus ? status : -1;
    }

    private Boolean pinOrUnpin(Boolean pinOP, String packageName) {
        boolean isSuccess = false;
        String OP = pinOP ? "Pin" : "Unpin";
        String dataDir = getDataDir(packageName);

        if (dataDir != "") {
            try {
                String jsonResult = pinOP ? HCFSApiUtils.pin(dataDir) : HCFSApiUtils.unpin(dataDir);
                JSONObject jObject = new JSONObject(jsonResult);
                isSuccess = jObject.getBoolean("result");

                String logMsg = "operation=" + OP + ", filePath=" + dataDir + ", jsonResult=" + jsonResult;
                if (isSuccess) {
                    log(Log.INFO, CLASSNAME, "pinFileOrDirectory", logMsg);

                    List<String> externalPath = getExternalDir(packageName);
                    if (externalPath.size() != 0) {
                        for (String path : externalPath) {
                            jsonResult = pinOP ? HCFSApiUtils.pin(path) : HCFSApiUtils.unpin(path);
                            jObject = new JSONObject(jsonResult);
                            isSuccess = jObject.getBoolean("result");

                            logMsg = "operation=" + OP + ", filePath=" + path + ", jsonResult=" + jsonResult;
                            if (isSuccess) {
                                log(Log.INFO, CLASSNAME, "pinFileOrDirectory", logMsg);
                            } else {
                                log(Log.ERROR, CLASSNAME, "pinFileOrDirectory", logMsg);
                                break;
                            }
                        }
                    }

                } else {
                    log(Log.ERROR, CLASSNAME, "pinFileOrDirectory", logMsg);
                }

            } catch (JSONException e) {
                log(Log.ERROR, CLASSNAME, "pinFileOrDirectory", Log.getStackTraceString(e));
            }
        }

        return isSuccess;
    }

    private String getSourceDir(String packageName) {
        PackageManager m = getPackageManager();
        String sourceDir = "";
        try {
            PackageInfo p = m.getPackageInfo(packageName, 0);
            sourceDir = p.applicationInfo.sourceDir;
            sourceDir = sourceDir.substring(0, sourceDir.lastIndexOf("/"));
            log(Log.INFO, CLASSNAME, "getSourceDir", sourceDir);
        } catch (PackageManager.NameNotFoundException e) {
            log(Log.WARN, CLASSNAME, "getSourceDir",  "Error: Package (" + packageName + ") not found ");
        }

        return sourceDir;
    }

    private String getDataDir(String packageName) {
        PackageManager m = getPackageManager();
        String dataDir = "";
        try {
            PackageInfo p = m.getPackageInfo(packageName, 0);
            dataDir = p.applicationInfo.dataDir;
            log(Log.INFO, CLASSNAME, "getDataDir", dataDir);
        } catch (PackageManager.NameNotFoundException e) {
            log(Log.WARN, CLASSNAME, "getDataDir",  "Error: Package (" + packageName + ") not found ");
        }

        return dataDir;
    }

    private List getExternalDir(String packageName) {
        List<String> externalDir = new ArrayList<String>();
        String externalPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
        File externalAndroidFile = new File(externalPath);
        for (File type : externalAndroidFile.listFiles()) {
            File[] fileList = type.listFiles();
            for (File file : fileList) {
                String path = file.getAbsolutePath();
                if (path.indexOf(packageName) != -1) {
                    log(Log.INFO, CLASSNAME, "getExternalDir", path);
                    externalDir.add(path);
                    break;
                }
            }
        }

        return externalDir;
    }

    private int getAppProgress(String packageName) {
        int progress = -1;

        try {
            Boolean cloudConn = new JSONObject(HCFSApiUtils.getHCFSStat()).getJSONObject("data").getBoolean("cloud_conn");
            //log(Log.DEBUG, CLASSNAME, "getAppProgress", String.valueOf(cloudConn));
            if (NetworkUtils.isNetworkConnected(TeraFonnApiService.this) && cloudConn) {
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
                        log(Log.DEBUG, CLASSNAME, "getAppProgress", jsonResult);
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

                log(Log.DEBUG, CLASSNAME, "getAppProgress", String.valueOf(progress));
            }
        } catch (Exception e) {
            progress = -1;
            log(Log.ERROR, CLASSNAME, "getAppProgress", e.toString());
        }

        return progress;
    }

}
