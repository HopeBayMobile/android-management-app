package com.hopebaytech.hcfsmgmt.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import java.util.ArrayList;

public class AppInfo extends ItemInfo {

    private final String CLASSNAME = AppInfo.class.getSimpleName();
    private long dbId;
    private int uid;
    private ApplicationInfo applicationInfo;
    private String packageName;
    private ArrayList<String> externalDirList;
    private String[] sharedLibraryFiles;
    private int appSize;
    private boolean isSystemApp;
    private Context context;

    public AppInfo(Context context) {
        super(context);
        this.context = context;
    }

//	public AppInfo(AppInfo applicationInfo) {
//		super(applicationInfo.mContext);
//		this.dbId = applicationInfo.dbId;
//		this.uid = applicationInfo.uid;
//		this.applicationInfo = applicationInfo.applicationInfo;
//		this.packageName = applicationInfo.packageName;
//		this.externalDir = applicationInfo.externalDir;
//		this.sharedLibraryFiles = applicationInfo.sharedLibraryFiles;
//		this.appSize = applicationInfo.appSize;
//		this.mContext = applicationInfo.mContext;
//		this.status = applicationInfo.status;
//	}

    public int getUid() {
        return uid;
    }

    @Nullable
    public Bitmap getIconImage() {
        Bitmap iconImage = null;
        Drawable drawable = context.getPackageManager().getApplicationIcon(applicationInfo);
        if (!(drawable instanceof VectorDrawable)) {
            iconImage = ((BitmapDrawable) drawable).getBitmap();
        }

        if (iconImage == null) {
            drawable = ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default);
            iconImage = ((BitmapDrawable) drawable).getBitmap();
        }
        return iconImage;
    }

    public void setApplicationInfo(ApplicationInfo appInfo) {
        this.applicationInfo = appInfo;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getSourceDir() {
        /** Default sourceDir = /data/app/<package-name>-1/base.apk */
        String sourceDir = applicationInfo.sourceDir;
        int lastIndex = sourceDir.lastIndexOf("/");
        String sourceDirWithoutApkSuffix = sourceDir.substring(0, lastIndex);
        return sourceDirWithoutApkSuffix;
    }

    @Nullable
    public ArrayList<String> getExternalDirList() {
        return externalDirList;
    }

//	public void setExternalDir(String externalDir) {
//		this.externalDir = externalDir;
//	}

    public void setExternalDirList(ArrayList<String> externalDirList) {
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
        if (NetworkUtils.isNetworkConnected(mContext)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean downloadToRun = sharedPreferences.getBoolean(mContext.getString(R.string.pref_download_to_run), false);
            if (downloadToRun) {
                if (HCFSMgmtUtils.getDirLocationStatus(getSourceDir()) == LocationStatus.LOCAL &&
                        HCFSMgmtUtils.getDirLocationStatus(getDataDir()) == LocationStatus.LOCAL) {
                    return ItemStatus.STATUS_AVAILABLE;
                } else {
                    return ItemStatus.STATUS_UNAVAILABLE_WAIT_TO_DOWNLOAD;
                }
            } else {
                return ItemStatus.STATUS_AVAILABLE;
            }
        } else {
            int externalLocationStatus = getExternalLocationStatus();
            if (HCFSMgmtUtils.getDirLocationStatus(getSourceDir()) == LocationStatus.LOCAL &&
                    HCFSMgmtUtils.getDirLocationStatus(getDataDir()) == LocationStatus.LOCAL &&
                    externalLocationStatus == LocationStatus.LOCAL) {
                return ItemStatus.STATUS_AVAILABLE;
            } else {
                return ItemStatus.STATUS_UNAVAILABLE_NONE_NETWORK;
            }
        }
    }

    private int getExternalLocationStatus() {
        int externalStatus = LocationStatus.LOCAL;

        int externalLocalCounter = 0;
        int externalHybridCounter = 0;
        int externalCloudCounter = 0;
        if (externalDirList != null) {
            for (String externalDir : externalDirList) {
                if (HCFSMgmtUtils.getDirLocationStatus(externalDir) == LocationStatus.LOCAL) {
                    externalLocalCounter++;
                } else if (HCFSMgmtUtils.getDirLocationStatus(externalDir) == LocationStatus.HYBRID) {
                    externalHybridCounter++;
                } else if (HCFSMgmtUtils.getDirLocationStatus(externalDir) == LocationStatus.CLOUD) {
                    externalCloudCounter++;
                }
            }

            if (externalHybridCounter == 0 && externalCloudCounter == 0) {
                externalStatus = LocationStatus.LOCAL;
            } else if (externalHybridCounter == 0 && externalLocalCounter == 0) {
                externalStatus = LocationStatus.CLOUD;
            } else {
                externalStatus = LocationStatus.HYBRID;
            }
        }
        return externalStatus;
    }

    public int getAppLocationStatus() {
        int locationStatus;
        int srcStatus = HCFSMgmtUtils.getDirLocationStatus(getSourceDir());
        int dataStatus = HCFSMgmtUtils.getDirLocationStatus(getDataDir());
        if (externalDirList != null) {
            int externalStatus = getExternalLocationStatus();
            if (srcStatus == LocationStatus.LOCAL && dataStatus == LocationStatus.LOCAL && externalStatus == LocationStatus.LOCAL) {
                locationStatus = LocationStatus.LOCAL;
            } else if (srcStatus == LocationStatus.CLOUD && dataStatus == LocationStatus.CLOUD && externalStatus == LocationStatus.CLOUD) {
                locationStatus = LocationStatus.CLOUD;
            } else {
                locationStatus = LocationStatus.HYBRID;
            }
        } else {
            if (srcStatus == LocationStatus.LOCAL && dataStatus == LocationStatus.LOCAL) {
                locationStatus = LocationStatus.LOCAL;
            } else if (srcStatus == LocationStatus.CLOUD && dataStatus == LocationStatus.CLOUD) {
                locationStatus = LocationStatus.CLOUD;
            } else {
                locationStatus = LocationStatus.HYBRID;
            }
        }
        return locationStatus;
    }

    @Override
    public Drawable getPinUnpinImage(boolean isPinned) {
        return HCFSMgmtUtils.getPinUnpinImage(context, isPinned);
    }

    @Override
    public int hashCode() {
        return getSourceDir().hashCode();
    }

    @Override
    public int getIconAlpha() {
        return getAppStatus() == ItemStatus.STATUS_AVAILABLE ? ICON_COLORFUL : ICON_TRANSPARENT;
    }

    public ApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }
}