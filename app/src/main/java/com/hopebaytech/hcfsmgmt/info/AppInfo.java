package com.hopebaytech.hcfsmgmt.info;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.LocationStatus;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class AppInfo extends ItemInfo implements Cloneable {

    private static final String CLASSNAME = AppInfo.class.getSimpleName();

    private long dbId;
    private int uid;
    private ApplicationInfo applicationInfo;
    private PackageInfo packageInfo;
    private String packageName;
    private List<String> externalDirList;
    private String[] sharedLibraryFiles;
    private int appSize;
    private boolean isSystemApp;
    private boolean isEnabled;
    private int boostStatus;
    private int mHashCode;

    private Context context;

    /**
     * None real time data status of this app, only be updated when getAppStatus() is called.
     */
    private int dataStatus;

    public AppInfo(Context context) {
        super(context);
        this.context = context;
    }

    public int getUid() {
        return uid;
    }

    @Nullable
    public Bitmap getIconImage() {
        Bitmap iconImage = null;

        Drawable drawable = context.getPackageManager().getApplicationIcon(applicationInfo);
        //if (!(drawable instanceof VectorDrawable)) {
        //    iconImage = ((BitmapDrawable) drawable).getBitmap();
        //}
        iconImage = ((BitmapDrawable) drawable).getBitmap();

        if (iconImage == null) {
            drawable = ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default);
            iconImage = ((BitmapDrawable) drawable).getBitmap();
        } else {
            int width;
            int height;
            width = height = (int) mContext.getResources().getDimension(R.dimen.icon_image_width);
            if (!iconImage.isRecycled()) {
                iconImage = ThumbnailUtils.extractThumbnail(iconImage, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            }
        }

        return iconImage;
    }

    public Drawable getIconDrawable() {
        Drawable iconDrawable = context.getPackageManager().getApplicationIcon(applicationInfo);
        if (iconDrawable == null) {
            iconDrawable = ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default);
        }

        int width = (int) mContext.getResources().getDimension(R.dimen.icon_image_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.icon_image_height);
        Bitmap bitmap = ((BitmapDrawable) iconDrawable).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return new BitmapDrawable(mContext.getResources(), bitmapResized);
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public void setApplicationInfo(ApplicationInfo appInfo) {
        this.applicationInfo = appInfo;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getSourceDir() {
        // TODO: split apk and source file

        // Default Android 4.4 sourceDir = /data/app/<package-name>.apk
        String sourceDir = applicationInfo.sourceDir;

        // Default Android 6.0 sourceDir = /data/app/<package-name>-1/base.apk
        if (Build.VERSION.SDK_INT >= 23) {
            int lastIndex = sourceDir.lastIndexOf("/");
            return sourceDir.substring(0, lastIndex); // source dir path without apk suffix, such as /data/app/<package-name>-1/
        }
        return sourceDir;
    }


    @Nullable
    public List<String> getExternalDirList() {
        return externalDirList;
    }

    public void setExternalDirList(List<String> externalDirList) {
        this.externalDirList = externalDirList;
    }

    public String getDataDir() {
        return applicationInfo.dataDir;
    }

    public String getNativeLibraryDir() {
        return applicationInfo.nativeLibraryDir;
    }

    @Nullable
    public String[] getSharedLibraryFiles() {
        return sharedLibraryFiles;
    }

    /**
    * Only for Android Kitkat now.
    * Default path is "/data/dalvik-cache/"
    * Name of dex file should like "prefix + package_name + postfix"
    * prefix contain: data@app@, system@app@, system@framework@, system@priv-app@
    * postfix contain: "-1.apk@classes.dex" or "-2.apk@classes.dex"
    *
    * We only use data@app@ here.
    */
    public String getDalvikCacheFilePath() {

        if (Build.VERSION.SDK_INT >= 23) {
            return null;
        }

        String dalvikCachePath = Environment.getDataDirectory().toString() + "/dalvik-cache/";
        Logs.d(CLASSNAME, "getDalvikCacheFIlePath", "dalvikCachePath=" + dalvikCachePath);
        String pkgName = getPackageName();
        Logs.d(CLASSNAME, "getDalvikCacheFIlePath", "pkgName=" + pkgName);
        String prefix = "data@app@";

        String path;
        String postfix[] = {"-1.apk@classes.dex", "-2.apk@classes.dex"};
        for (String pf: postfix) {
            String filename = prefix + pkgName + pf;
            path = dalvikCachePath + filename;
            final File file = new File(path);
            if (file.exists()) {
                return path;
            }
        }
        Logs.d(CLASSNAME, "getDalvikCacheDirPath", "dalvik-cache is not exists for= " + pkgName);
        return null;
    }

    /**
     * Only for Android Kitkat now.
     * Default path is "/data/app-lib/"
     * Name of lib folder should like "<pkgName>-1" or "<pkgName>-2"
     */
    public String getAppLibDirPath(){

        if (Build.VERSION.SDK_INT >= 23) {
            return null;
        }

        String appLibPath = Environment.getDataDirectory().toString() + "/app-lib/";
        Logs.d(CLASSNAME, "getAppLibDirPath", "appLibPath=" + appLibPath);
        String pkgName = getPackageName();
        Logs.d(CLASSNAME, "getAppLibDirPath", "pkgName=" + pkgName);

        String postfix[] = {"-1", "-2"};
        for (String pf: postfix) {
            String path = appLibPath + pkgName + pf;
            File file = new File(path);
            if (file.exists()) {
                return path;
            }
        }
        Logs.d(CLASSNAME, "getAppLibDirPath", "app-lib is not exists=" + pkgName);
        return null;
    }

    public int getAppSize() {
        return appSize;
    }

    public void setAppSize(int appSize) {
        this.appSize = appSize;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getPackageName() {
        if (packageName == null) {
            packageName = applicationInfo.packageName;
        }
        return packageName;
    }

    public int getAppStatus() {
        boolean isSourceDirInLocal = true;
        String sourceDir = getSourceDir();
        if (!sourceDir.startsWith("/system")) {
            // if the sourceDir is not belong to system app (/system/app or /system/priv-app), check
            // its location status
            isSourceDirInLocal = HCFSMgmtUtils.getDirLocationStatus(sourceDir) == LocationStatus.LOCAL;
        }

        if (isSourceDirInLocal &&
                HCFSMgmtUtils.getDirLocationStatus(getDataDir()) == LocationStatus.LOCAL &&
                getExternalDirLocationStatus() == LocationStatus.LOCAL) {
            Logs.i(CLASSNAME, "HCFS getAppStatus ", sourceDir + " is available");
            return (dataStatus = DataStatus.AVAILABLE);
        }
        return (dataStatus = DataStatus.UNAVAILABLE);
    }

    /**
     * @return The data status of this app, but the data status is not guaranteed to be correct.
     * @see AppInfo#dataStatus
     */
    public int getLazyAppStatus() {
        return dataStatus;
    }

    private int getExternalDirLocationStatus() {
        if (externalDirList != null) {
            for (String externalDir : externalDirList) {
                int locationStatus = HCFSMgmtUtils.getDirLocationStatus(externalDir);
                if (locationStatus == LocationStatus.NOT_LOCAL) {
                    return LocationStatus.NOT_LOCAL;
                }
            }
        }
        return LocationStatus.LOCAL;
    }

    @Override
    public int hashCode() {
        if (mHashCode == 0) {
            mHashCode = getSourceDir().hashCode();
        }
        return mHashCode;
    }

    @Override
    public int getIconAlpha() {
        HCFSStatInfo hcfsStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
        if (HCFSConnStatus.isAvailable(mContext, hcfsStatInfo)) {
            return ICON_COLORFUL;
        }
        return getAppStatus() == DataStatus.AVAILABLE ? ICON_COLORFUL : ICON_TRANSPARENT;
    }

    public ApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }

    boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getBoostStatus() {
        return boostStatus;
    }

    public void setBoostStatus(int boostStatus) {
        boostStatus = boostStatus;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
