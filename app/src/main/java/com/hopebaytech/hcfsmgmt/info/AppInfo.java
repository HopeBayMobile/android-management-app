package com.hopebaytech.hcfsmgmt.info;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import java.util.List;

public class AppInfo extends ItemInfo implements Cloneable {

    private static final String CLASSNAME = AppInfo.class.getSimpleName();

    private long dbId;
    private int uid;
    private ApplicationInfo applicationInfo;
    private String packageName;
    private List<String> externalDirList;
    private String[] sharedLibraryFiles;
    private int appSize;
    private boolean isSystemApp;
    private Context context;

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
        // Default sourceDir = /data/app/<package-name>-1/base.apk
        String sourceDir = applicationInfo.sourceDir;
        int lastIndex = sourceDir.lastIndexOf("/");
        return sourceDir.substring(0, lastIndex); // source dir path without apk suffix, such as /data/app/<package-name>-1/
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
        if (HCFSMgmtUtils.getDirLocationStatus(getSourceDir()) == LocationStatus.LOCAL &&
                HCFSMgmtUtils.getDirLocationStatus(getDataDir()) == LocationStatus.LOCAL &&
                getExternalDirLocationStatus() == LocationStatus.LOCAL) {
            return ItemStatus.AVAILABLE;
        }
        return ItemStatus.UNAVAILABLE;
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
    public Drawable getPinUnpinImage(boolean isPinned) {
        return HCFSMgmtUtils.getPinUnpinImage(context, isPinned);
    }

    @Override
    public int hashCode() {
        return getSourceDir().hashCode();
    }

    @Override
    public int getIconAlpha() {
        HCFSStatInfo hcfsStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
        if (HCFSConnStatus.isAvailable(mContext, hcfsStatInfo)) {
            return ICON_COLORFUL;
        }
        return getAppStatus() == ItemStatus.AVAILABLE ? ICON_COLORFUL : ICON_TRANSPARENT;
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

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}