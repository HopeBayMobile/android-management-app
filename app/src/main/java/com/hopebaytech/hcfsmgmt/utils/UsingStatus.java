package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class UsingStatus {
    private final static String TAG = "UsingStatus";

    public static void sendLog(Context context) {
        try {
            String logURL = "http://ota.tera.mobi/upload/logs/Hit/";
            //String logURL = "http://172.16.11.188:5555"; // Local Test server
            HttpUtil.HttpRequestBody requestBody = HttpUtil.createRequestBody(
                    "application/json; charset=utf-8",
                    getJSONStringLog(context));

            HttpUtil.HttpRequest request = HttpUtil.buildPostRequest(null, logURL, requestBody);
            HttpUtil.HttpResponse response = HttpUtil.executeSynchronousRequest(request);
            Logs.d(TAG, "sendLogs(can)", "response:" + response.getBody());
        } catch (JSONException e) {
            Logs.d(TAG, "sendLogs(can)", "JSONException: " + e);
        }
    }

    private static String getJSONStringLog(Context context) throws JSONException {
        Logs.d(TAG, "getJSONStringLog", "Preparing");
        AccountDAO accountDAO = AccountDAO.getInstance(context);
        AccountInfo accountInfo = accountDAO.getFirst();

        JSONObject basicInfo = new JSONObject();
        JSONObject hcfsStatus = new JSONObject();
        JSONArray installApps = new JSONArray();
        JSONObject dataObject = new JSONObject();
        JSONObject requestJSONObject = new JSONObject();

        // Basic information
        // DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        // String date = dateFormat.format(new java.util.Date());
        String dateOrigin = new java.util.Date().toString();
        String imei = HCFSMgmtUtils.getDeviceImei(context);
        //String accountName = accountInfo != null ? accountInfo.getName() : "UserName";
        //String accountEmail = accountInfo != null ? accountInfo.getEmail() : "UserEmail";

        // basic Info
        basicInfo.put("TimeStamp", dateOrigin);
        basicInfo.put("IMEI", imei);
        //basicInfo.put("UserName", accountName);
        //basicInfo.put("UserEmail", accountEmail);

        // HCFS status
        HCFSStatInfo info = HCFSMgmtUtils.getHCFSStatInfo();
        hcfsStatus.put("CloudTotal", info.getCloudTotal());
        hcfsStatus.put("CloudUsed", info.getCloudUsed());
        hcfsStatus.put("PinTotal", info.getPinTotal());
        hcfsStatus.put("DirtyUsed", info.getCacheDirtyUsed());
        hcfsStatus.put("CacheUsed", info.getCacheUsed());
        hcfsStatus.put("CacheTotal", info.getCacheTotal());
        hcfsStatus.put("DataDownloadToday", info.getXferDownload());
        hcfsStatus.put("DataUploadToday", info.getXferDownload());

        // Install App
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList) {
            Boolean isSystemApp = HCFSMgmtUtils.isSystemPackage(packageInfo.applicationInfo);
            if (!isSystemApp)
                installApps.put(packageInfo.packageName);
        }

        // Setup Data JSON object
        dataObject.put("LocalIp", getIp());
        dataObject.put("basicInfo", basicInfo);
        dataObject.put("HcfsStatus", hcfsStatus);
        dataObject.put("InstallApps", installApps);

        // Setup Final request JSON Object
        requestJSONObject.put(imei, dataObject);

        return requestJSONObject.toString();
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
