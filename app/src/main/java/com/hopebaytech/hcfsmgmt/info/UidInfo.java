package com.hopebaytech.hcfsmgmt.info;

public class UidInfo {

    private boolean isPinned;
    private int uid;
    private String packageName;
    private boolean isSystemApp;

    public UidInfo() {

    }

    public UidInfo(boolean isPinned, boolean isSystemApp, int uid, String packageName) {
        this.isPinned = isPinned;
        this.uid = uid;
        this.packageName = packageName;
        this.isSystemApp = isSystemApp;
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

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }

    @Override
    public String toString() {
        return "{isPinned=" + isPinned + ", isSystemApp=" + isSystemApp + ", uid=" + uid + ", packageName=" + packageName + "}";
    }
}