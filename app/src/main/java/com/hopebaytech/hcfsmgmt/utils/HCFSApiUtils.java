package com.hopebaytech.hcfsmgmt.utils;

public class HCFSApiUtils {

    static {
        System.loadLibrary("terafonnapi");
    }

    public static native String getFileStatus(String filePath);

    public static native String getDirStatus(String filePath);

    public static native String getHCFSConfig(String key);

    //	public static native String pin(String pinPath);
    public static native String pin(String pinPath, int pinType);

    public static native String getPinStatus(String filePath);

    public static native String setHCFSConfig(String key, String value);

    public static native String getHCFSStat();

    public static native String unpin(String unpinPath);

    public static native String reloadConfig();

    public static native String resetXfer();

    public static native String setHCFSSyncStatus(int enabled);

    public static native String getHCFSSyncStatus();

    public static native byte[] getEncryptedIMEI(String imei);

    public static native String getOccupiedSize();

    public static native String setNotifyServer(String pathName);

    public static native byte[] getDecryptedJsonString(String jsonString);

    public static native String setSwiftToken(String url, String token);

    /***
     * @return <li>1 if system is clean now. That is, there is no dirty data.</li>
     * <li>0 when setting sync point completed.</li>
     * <li>Negative error code in case that error occurs</li>
     */
    public static native String startUploadTeraData();

    /***
     * @return <li>1 if no sync point is set.</li>
     * <li>0 when canceling the setting completed.</li>
     * <li>Negative error code in case that error occurs</li>
     */
    public static native String stopUploadTeraData();

    public static native String collectSysLogs();
    
    public static native String triggerRestore();

    /***
     * @return <li>0 if not being restored</li>
     * <li>1 if in stage 1 of restoration process</li>
     * <li>2 if in stage 2 of restoration process</li>
     */
    public static native String checkRestoreStatus();

    public static native String notifyApplistChange();
}
