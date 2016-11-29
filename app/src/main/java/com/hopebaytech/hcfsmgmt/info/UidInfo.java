package com.hopebaytech.hcfsmgmt.info;

import java.util.List;

public class UidInfo {

    public static class BoostStatus {
        public static final int NON_BOOSTABLE = 0;
        public static final int INIT_UNBOOST = 1;
        public static final int UNBOOSTED = 2;
        public static final int UNBOOSTING = 3;
        public static final int UNBOOST_FAILED = 4;
        public static final int INIT_BOOST = 5;
        public static final int BOOSTED = 6;
        public static final int BOOSTING = 7;
        public static final int BOOST_FAILED = 8;
    }

    public static class EnabledStatus {
        public static final int DISABLED = 0;
        public static final int ENABLED = 1;
    }

    private int id;
    private boolean isPinned;
    private boolean isSystemApp;
    private boolean isEnabled;
    private int uid;
    private String packageName;
    private List<String> externalDir;
    private int boostStatus;

    public UidInfo() {
    }

    public UidInfo(AppInfo appInfo) {
        setUid(appInfo.getUid());
        setPackageName(appInfo.getPackageName());
        setPinned(appInfo.isPinned());
        setSystemApp(appInfo.isSystemApp());
        setBoostStatus(appInfo.getBoostStatus());
    }

    public UidInfo(boolean isPinned, boolean isSystemApp, int uid, String packageName) {
        this(isPinned,
                isSystemApp,
                isSystemApp ? BoostStatus.NON_BOOSTABLE : BoostStatus.UNBOOSTED,
                uid,
                packageName);
    }

    public UidInfo(boolean isPinned, boolean isSystemApp, int boostStatus, int uid, String packageName) {
        this(isPinned, isSystemApp, true /* isEnabled */, boostStatus, uid, packageName);
    }

    public UidInfo(boolean isPinned, boolean isSystemApp, boolean isEnabled, int boostStatus, int uid, String packageName) {
        this.isPinned = isPinned;
        this.isSystemApp = isSystemApp;
        this.isEnabled = isEnabled;
        this.boostStatus = boostStatus;
        this.uid = uid;
        this.packageName = packageName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String pkgName) {
        this.packageName = pkgName;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean isSystemApp) {
        this.isSystemApp = isSystemApp;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }

    public int getBoostStatus() {
        return boostStatus;
    }

    public void setBoostStatus(int boostStatus) {
        this.boostStatus = boostStatus;
    }

    public List<String> getExternalDir() {
        return externalDir;
    }

    public void setExternalDir(List<String> externalDir) {
        this.externalDir = externalDir;
    }

    @Override
    public String toString() {
        return "{isPinned=" + isPinned + ", isSystemApp=" + isSystemApp + ", uid=" + uid + ", packageName=" + packageName + ", boostStatus=" + boostStatus + "}";
    }
}
