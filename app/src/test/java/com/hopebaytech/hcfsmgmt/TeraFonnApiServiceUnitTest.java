package com.hopebaytech.hcfsmgmt;

/**
 * Created by Vince on 2016/4/25.
 */

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.test.AndroidTestCase;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.terafonnapiservice.TeraFonnApiService;
import com.hopebaytech.hcfsmgmt.utils.HCFSApiUtils;

import junit.framework.Assert;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TeraFonnApiServiceUnitTest extends AndroidTestCase {

    private TeraFonnApiService terafonnAPI = new TeraFonnApiService();
    private final Method methods[] = TeraFonnApiService.class.getDeclaredMethods();

    private final String TAG = "UnitTest";
    private final String CLASSNAME = getClass().getSimpleName();

    String packageName = "com.google.android.youtube";

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void test_getAppProgress() throws Exception {

        for(int i = 0 ; i< methods.length; ++i) {
            if (methods[i].getName().equals("getAppProgress")) {
                methods[i].setAccessible(true);
                Object o = methods[i].invoke(terafonnAPI, packageName);
                int progress = Integer.valueOf(o.toString());
                if (progress >= -1 && progress <= 100) {
                    Assert.assertTrue(true);
                } else {
                    Assert.assertTrue(false);
                }
                break;
            }
        }
    }

/*
    public void test_getDataDir() throws Exception {
        for(int i = 0 ; i< methods.length; ++i) {
            if (methods[i].getName().equals("getDataDir")) {
                methods[i].setAccessible(true);
                Object o = methods[i].invoke(terafonnAPI, packageName);
                PackageManager m = getContext().getPackageManager();
                PackageInfo p = m.getPackageInfo(packageName, 0);
                Assert.assertEquals(p.applicationInfo.dataDir, o.toString());
                break;
            }
        }
    }
*/
/*
    public void test_getSourceDir() throws Exception {
        for(int i = 0 ; i< methods.length; ++i) {
            if (methods[i].getName().equals("getSourceDir")) {
                methods[i].setAccessible(true);
                Object o = methods[i].invoke(terafonnAPI, packageName);
                PackageManager m = getContext().getPackageManager();
                PackageInfo p = m.getPackageInfo(packageName, 0);
                String sourceDir = p.applicationInfo.sourceDir;
                sourceDir = sourceDir.substring(0, sourceDir.lastIndexOf("/"));
                Assert.assertEquals(sourceDir, o.toString());
                break;
            }
        }
    }
*/
/*
    public void test_downloadToRun() throws Exception {
        for(int i = 0 ; i< methods.length; ++i) {
            if (methods[i].getName().equals("downloadToRun")) {
                methods[i].setAccessible(true);
                Object o = methods[i].invoke(terafonnAPI);

                if ( o.toString().equals("true") ||  o.toString().equals("false")) Assert.assertTrue(true);
                else Assert.assertTrue(false);

                break;
            }
        }
    }
*/
/*
    public void test_getDifferentStatus() throws Exception {
        for(int i = 0 ; i< methods.length; ++i) {
            if (methods[i].getName().equals("getDifferentStatus")) {
                methods[i].setAccessible(true);
                Object para[] = {packageName, 0};
                Object o = methods[i].invoke(terafonnAPI, para);
                int status = Integer.valueOf(o.toString());

                if (status < 2 && status >= 0) Assert.assertTrue(true);
                else Assert.assertTrue(false);

                break;
            }
        }
    }
  */

    public void test_getDataDir() throws Exception {
        PackageManager m = getContext().getPackageManager();
        PackageInfo p = m.getPackageInfo(packageName, 0);
        String dataDir = p.applicationInfo.dataDir;

        if (dataDir.startsWith("/data/user") || dataDir.startsWith("/data/data")) Assert.assertTrue(true);
        else Assert.assertTrue(false);
    }

    public void test_getSourceDir() throws Exception {
        PackageManager m = getContext().getPackageManager();
        PackageInfo p = m.getPackageInfo(packageName, 0);
        String sourceDir = p.applicationInfo.sourceDir;

        if (sourceDir.startsWith("/data/app") || sourceDir.startsWith("/system/app")) Assert.assertTrue(true);
        else Assert.assertTrue(false);
    }

    public void test_pin() throws Exception {
        String dataDir =  "/data/user/0/" + packageName;
        String jsonResult = HCFSApiUtils.pin(dataDir);
        JSONObject jObject = new JSONObject(jsonResult);
        log(Log.DEBUG, "test_pin", jsonResult);
        Assert.assertEquals(true, jObject.getBoolean("result"));
    }

    public void test_unpin() throws Exception {
        String dataDir =  "/data/user/0/" + packageName;
        String jsonResult = HCFSApiUtils.unpin(dataDir);
        JSONObject jObject = new JSONObject(jsonResult);
        log(Log.DEBUG, "test_unpin", jsonResult);
        Assert.assertEquals(true, jObject.getBoolean("result"));
    }

    public void test_getDirStatus() throws Exception {
        String dataDir =  "/data/user/0/" + packageName;
        String jsonResult = HCFSApiUtils.getDirStatus(dataDir);
        JSONObject jObject = new JSONObject(jsonResult);
        log(Log.DEBUG, "test_getDirStatus()", jsonResult);
        Assert.assertEquals(true, jObject.getBoolean("result"));
    }

    public void test_getPinStatus() throws Exception {
        String dataDir =  "/data/user/0/" + packageName;
        String jsonResult = HCFSApiUtils.getPinStatus(dataDir);
        JSONObject jObject = new JSONObject(jsonResult);
        log(Log.DEBUG, "test_getDirStatus()", jsonResult);
        Assert.assertEquals(true, jObject.getBoolean("result"));
    }

    public void test_addTrackAppStatus() throws Exception {
        boolean isSuccess = false;
        Map mPackageNameMap = new ConcurrentHashMap<>();
        List<String> packageNameList = new ArrayList<>();
        packageNameList.add(packageName);

        try {
            if (packageNameList != null) {
                isSuccess = true;
                for (String packageName : packageNameList) {
                    if (!mPackageNameMap.containsKey(packageName)) {
                        mPackageNameMap.put(packageName, 0);
                    }
                }
            }
        } catch (Exception e) {
            isSuccess = false;
            log(Log.ERROR, "addTrackAppStatus", Log.getStackTraceString(e));
        }
        Assert.assertEquals(true, isSuccess);
    }

    public void test_clearTrackAppStatus() throws Exception {
        boolean isSuccess = false;
        Map mPackageNameMap = new ConcurrentHashMap<>();
        List<String> packageNameList = new ArrayList<>();
        packageNameList.add(packageName);
        try {
            mPackageNameMap.put(packageName, 0);
            mPackageNameMap.clear();
            isSuccess = true;
        } catch (Exception e) {
            isSuccess = false;
            log(Log.ERROR, "clearTrackAppStatus", Log.getStackTraceString(e));
        }
        Assert.assertEquals(true, isSuccess);
    }

    public void test_removeTrackAppStatus() throws Exception {
        boolean isSuccess = false;
        Map mPackageNameMap = new ConcurrentHashMap<>();
        List<String> packageNameList = new ArrayList<>();
        packageNameList.add(packageName);
        try {
            mPackageNameMap.put(packageName, 0);
            if (packageNameList != null) {
                for (String packageName : packageNameList) {
                    mPackageNameMap.remove(packageName);
                }
                isSuccess = true;
            }
        } catch (Exception e) {
            log(Log.ERROR, "removeTrackAppStatus", Log.getStackTraceString(e));
        }
        Assert.assertEquals(true, isSuccess);
    }

    private void log(int logLevel, String funcName, String logMsg) {
        if (logMsg == null) {
            logMsg = "";
        }
        switch (logLevel) {
            case Log.DEBUG:
                Log.d(TAG, CLASSNAME + "(" + funcName + "): " + logMsg);
                break;
            case Log.INFO :
                Log.i(TAG, CLASSNAME + "(" + funcName + "): " + logMsg);
                break;
            case Log.WARN:
                Log.w(TAG, CLASSNAME + "(" + funcName + "): " + logMsg);
                break;
            case Log.ERROR:
                Log.e(TAG, CLASSNAME + "(" + funcName + "): " + logMsg);
                break;
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

}
