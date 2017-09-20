package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class LogServerUtils {
    private final static String TAG = "LogServerUtils";
    private static final String LOG_SERVER_URL = "http://ota.tera.mobi/upload/logs/Hit/";
    //private static final String LOG_SERVER_URL = "http://172.16.11.188/upload/logs/Hit/"; // Local Test server

    public static void sendLog(Context context) {

        try {
            HttpUtil.HttpRequestBody requestBody = HttpUtil.createRequestBody(
                    "application/json; charset=utf-8",
                    getJSONStringLog(context));

            HttpUtil.HttpRequest request = HttpUtil.buildPostRequest(null, LOG_SERVER_URL, requestBody);
            HttpUtil.HttpResponse response = HttpUtil.executeSynchronousRequest(request);
            if (response != null) {
                Logs.d(TAG, "sendLogs", "response:" + response.getBody());
            }
        } catch (Exception e) {
            Logs.d(TAG, "sendLogs", "JSONException: " + e);
        }
    }

    private static String getJSONStringLog(Context context) throws JSONException {
        Logs.d(TAG, "getJSONStringLog", "Preparing");
        JSONArray nonSystemApps = new JSONArray();
        JSONObject logContent = new JSONObject();

        // Basic information
        String dateOrigin = new java.util.Date().toString();
        String imei = HCFSMgmtUtils.getDeviceImei(context);
        logContent.put("LocalIp", getIp());
        logContent.put("TimeStamp", dateOrigin);
        logContent.put("IMEI", GeneratorHashUtils.generateSHA1(imei));
        logContent.put("Source", "Change this value for different version of tera");

        // HCFS status
        HCFSStatInfo info = HCFSMgmtUtils.getHCFSStatInfo();
        if (info != null) {
            logContent.put("CloudTotal", info.getCloudTotal());
            logContent.put("CloudUsed", info.getCloudUsed());
            logContent.put("PinTotal", info.getPinTotal());
            logContent.put("DirtyUsed", info.getCacheDirtyUsed());
            logContent.put("CacheUsed", info.getCacheUsed());
            logContent.put("CacheTotal", info.getCacheTotal());
            logContent.put("DataDownloadToday", info.getXferDownload());
            logContent.put("DataUploadToday", info.getXferUpload());
        }

        // Get installed Apps(non-system)
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList) {
            Boolean isSystemApp = HCFSMgmtUtils.isSystemPackage(packageInfo.applicationInfo);
            if (!isSystemApp)
                nonSystemApps.put(packageInfo.packageName);
        }
        logContent.put("InstalledApps", nonSystemApps);

        return logContent.toString();
    }

    private static String getIp() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                Enumeration<InetAddress> addresses = nis.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return "null";
    }
}
