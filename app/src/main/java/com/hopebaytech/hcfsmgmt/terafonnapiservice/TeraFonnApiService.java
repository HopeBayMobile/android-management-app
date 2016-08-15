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

import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.info.LocationStatus;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSApiUtils;
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.PinType;
import com.hopebaytech.hcfsmgmt.utils.StorageUsage;

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
    private IGetJWTandIMEIListener mGetJWTandIMEIListener;
    private IFetchAppDataListener mFetchAppDataListener;
    private ITrackAppStatusListener mTrackAppStatusListener;
    private Map<String, AppStatus> mPackageNameMap;
    private ExecutorService mCacheExecutor;

    private final ITeraFonnApiService.Stub mBinder = new ITeraFonnApiService.Stub() {

        @Override
        public void setJWTandIMEIListener(IGetJWTandIMEIListener listener) throws RemoteException {
            mGetJWTandIMEIListener = listener;
        }

        @Override
        public boolean getJWTandIMEI() throws RemoteException {
            boolean isSuccess = true;
            mCacheExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        MgmtCluster.getJwtToken(TeraFonnApiService.this, new MgmtCluster.OnFetchJwtTokenListener() {
                            @Override
                            public void onFetchSuccessful(String jwt) {
                                String imei = HCFSMgmtUtils.getDeviceImei(TeraFonnApiService.this);
                                try {
                                    mGetJWTandIMEIListener.onDataGet(imei, jwt);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFetchFailed() {
                                try {
                                    mGetJWTandIMEIListener.onDataGet("", "");
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return isSuccess;
        }

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
                            Logs.w(CLASSNAME, "fetchAppData", "run", Log.getStackTraceString(e));
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
                            mPackageNameMap.put(packageName, getAppStatus(packageName));
                        }
                    }
                }
            } catch (Exception e) {
                isSuccess = false;
                Logs.e(CLASSNAME, "addTrackAppStatus", Log.getStackTraceString(e));
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
                Logs.e(CLASSNAME, "removeTrackAppStatus", Log.getStackTraceString(e));
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
            return getPackageStatus(packageName);
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
            return HCFSConfig.isActivated(TeraFonnApiService.this);
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
            return StorageUsage.getFreeSpace();
        }

        @Override
        public long getTeraTotalSpace() {
            return StorageUsage.getTotalSpace();
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
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Logs.d(CLASSNAME, "onStartCommand", null);
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
                                int reportStatus = getPackageStatus(packageName);
                                if (appStatus.getStatus() != reportStatus) {
                                    mTrackAppStatusListener.onStatusChanged(packageName, reportStatus);
                                }
                                appStatus.setStatus(reportStatus);
                                mPackageNameMap.put(packageName, appStatus);
//                                int reportStatus = getDifferentStatus(packageName, appStatus.getStatus());
//                                if (reportStatus != -1)
//                                    mTrackAppStatusListener.onStatusChanged(packageName, reportStatus);
                            }
                        }
//                        mTrackAppStatusListener.onTrackFailed();
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        mGetJWTandIMEIListener = null;
                        mTrackAppStatusListener = null;
                        mFetchAppDataListener = null;
                        mPackageNameMap.clear();
                        Logs.e(CLASSNAME, "onStartCommand", Log.getStackTraceString(e));
                        break;
                    }
                }
            }
        });
        pollingThread.start();

        return START_STICKY;
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

        int status = getPackageStatus(packageName);
        if (status != AppStatus.STATUS_AVAILABLE) {
            status = AppStatus.STATUS_UNAVAILABLE;
        }

        return new AppStatus(packageName, isPinned, isOnFetching, status);
    }

    private int getPackageStatus(String packageName) {
        if (NetworkUtils.isNetworkConnected(TeraFonnApiService.this)) {
            return AppStatus.STATUS_AVAILABLE;
        } else {
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
        }
        return AppStatus.STATUS_UNAVAILABLE;
//        String dataDir = getDataDir(packageName);
//        if (NetworkUtils.isNetworkConnected(TeraFonnApiService.this)) {
//            if (downloadToRun()) {
//                if (HCFSMgmtUtils.getDirLocationStatus(dataDir) == LocationStatus.LOCAL) {
//                    return AppStatus.STATUS_AVAILABLE;
//                } else {
//                    return AppStatus.STATUS_UNAVAILABLE_WAIT_TO_DOWNLOAD;
//                }
//            } else {
//                return AppStatus.STATUS_AVAILABLE;
//            }
//        } else {
//            if (getDefaultLocation(packageName) == LocationStatus.LOCAL) {
//                return AppStatus.STATUS_AVAILABLE;
//            } else {
//                return AppStatus.STATUS_UNAVAILABLE_NONE_NETWORK;
//            }
//        }
    }

    private int getDefaultLocation(String packageName) {
        int location = LocationStatus.LOCAL;
        try {
            String dataDir = getDataDir(packageName);
            List<String> appPath = getExternalDir(packageName);

            appPath.add(dataDir);

            List<Integer> appLocation = new ArrayList<>();

            for (String path : appPath) {
                appLocation.add(HCFSMgmtUtils.getDirLocationStatus(path));
            }

            if (appLocation.contains(LocationStatus.HYBRID)) {
                location = LocationStatus.HYBRID;
            } else if (appLocation.contains(LocationStatus.CLOUD)) {
                location = LocationStatus.CLOUD;
            } else {
                location = LocationStatus.LOCAL;
            }

            Logs.d(CLASSNAME, "getDefaultLocation", "APP Location: " + String.valueOf(location));
        } catch (Exception e) {
            Logs.e(CLASSNAME, "getDefaultLocation", "Error: " + e.toString());
        }

        return location;
    }

    private int getDifferentStatus(String packageName, int currentStatus) {
        int status = getPackageStatus(packageName);
        if (status != AppStatus.STATUS_AVAILABLE) {
            status = AppStatus.STATUS_UNAVAILABLE;
        }
        return status != currentStatus ? status : -1;
    }

    private Boolean handleFailureOfPinOrUnpin(Boolean pinOP, String packageName) {
        boolean isSuccess = pinOrUnpin(pinOP, packageName);
        if (isSuccess) updateDB(pinOP, packageName);
        else pinOrUnpin(!pinOP, packageName);
        return isSuccess;
    }

    private Boolean pinOrUnpin(Boolean pinOP, String packageName) {
        boolean isSuccess = false;
        String OP = pinOP ? "Pin" : "Unpin";
        String dataDir = getDataDir(packageName);

        if (!dataDir.equals("")) {
            try {
                String jsonResult = pinOP ? HCFSApiUtils.pin(dataDir, PinType.NORMAL) : HCFSApiUtils.unpin(dataDir);
                JSONObject jObject = new JSONObject(jsonResult);
                isSuccess = jObject.getBoolean("result");

                String logMsg = "operation=" + OP + ", filePath=" + dataDir + ", jsonResult=" + jsonResult;
                if (isSuccess) {
                    Logs.i(CLASSNAME, "pinFileOrDirectory", logMsg);
                    List<String> externalPath = getExternalDir(packageName);

                    if (externalPath.size() != 0) {
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
                    }

                } else {
                    Logs.e(CLASSNAME, "pinFileOrDirectory", logMsg);
                }

            } catch (JSONException e) {
                Logs.e(CLASSNAME, "pinFileOrDirectory", Log.getStackTraceString(e));
            }
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
        List<String> externalDir = new ArrayList<String>();
        String externalPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
        try {
            File externalAndroidFile = new File(externalPath);
            for (File type : externalAndroidFile.listFiles()) {
                File[] fileList = type.listFiles();
                for (File file : fileList) {
                    String path = file.getAbsolutePath();
                    if (path.indexOf(packageName) != -1) {
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
            }
        } catch (Exception e) {
            progress = -1;
            Logs.e(CLASSNAME, "getAppProgress", e.toString());
        }

        return progress;
    }

}
