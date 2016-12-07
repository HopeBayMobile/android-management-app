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

    /**
     * This method initiate a the HCFS to start the restoration process.
     *  Restoration process stage-1 will start immediately after this method
     *  called.
     *
     * @return a json string contains: true if success, false otherwise.
     */
    public static native String triggerRestore();

    /**
     * Check the restoration status, the return json string contains two keys, "result" and "code".
     * <p>
     * result: true if success, false otherwise.
     * </p>
     * <p>
     * code:
     * <li>0 if not being restored</li>
     * <li>1 if in stage 1 of restoration process</li>
     * <li>2 if in stage 2 of restoration process</li>
     * </p>
     *
     * @return a json string contains .
     */
    public static native String checkRestoreStatus();

    /**
     * This method trigger the HCFS to backup package
     * list, /data/system/packages.xml.
     *
     * @return a json string contains: true if success, false otherwise.
     */
    public static native String notifyApplistChange();

    /**
     * This method is used for checking the status whether
     * an installed package is boosted or unboosted.
     *
     * @param packageName The package name needs to be checked
     * @return a json string contains:
     * <li>0 the package is boosted</li>
     * <li>1 the package is unboosted</li>
     * <li>others: false</li>
     */
    public static native String checkPackageBoostStatus(String packageName);

    /**
     * This method will create the environment of booster. The booster
     * will be created with boosterSize and be mounted to /data/mnt/hcfsblock/ .
     *
     * @param boosterSize The initial size of booster.
     * @return a json string contains: true if success, false otherwise.
     */
    public static native String enableBooster(long boosterSize);

    /**
     * This method will destroy the environment of booster. The booster
     * will be unmounted and the existed booster image file will be deleted.
     *
     * @return a json string contains: true if success, false otherwise.
     */
    public static native String disableBooster();

    /**
     * This method tirgger the hcfsapid to start to move all packages selected
     * from /data/data/ to booster. The list of packages will stored in
     * database, uid.db table uid, maintained by Tera Mgmt APP.
     * <strong>Note:</strong> This API will return immediately when called.
     * hcfsapid will send an event to notify the result after "boost"
     * finished/failed.
     *
     * @return a json string contains: true if success, false otherwise.
     */
    public static native String triggerBoost();

    /**
     * This method tirgger the hcfsapid to start to move all packages selected
     * from booster to /data/data/. The list of packages will stored in
     * database, uid.db table uid, maintained by Tera Mgmt APP.
     * <strong>Note:</strong> This API will return immediately when called.
     * hcfsapid will send an event to notify the result after "unboost"
     * finished/failed.
     *
     * @return a json string contains: true if success, false otherwise.
     */
    public static native String triggerUnboost();

    /**
     * This method will clear up the remaining symbolic link and empty folder of
     * the booster. This method is called when the Tera App receive
     * Intent.ACTION_PACKAGE_REMOVED from PackageMangerService. This
     * method is used to clear the remaining symlink, /data/data/<pkgName>, and
     * the empty folder, /data/mnt/hcfsblock/<pkgName>/, after the APP is
     * uninstalled.
     *
     * @param packageName The package name of the uninstalled app
     * @return a json string contains: true if success, false otherwise.
     */
    public static native String clearBoosterPackageRemaining(String packageName);

    /**
     * This method will unmount the booster.
     * <strong>Note:</strong> Before calling this method, ALL APPS NEEDED
     * TO BE BOOSTED SHOULD BE DIABLED.
     *
     * @return a json string contains: true if success, false otherwise.
     */
    public static native String umountBooster();

    /**
     * This method will mount the booster.
     *
     * @return a json string contains: true if success, false otherwise.
     */
    public static native String mountBooster();

    /**
     * This method will make minimal base apk
     *
     * @param packagePath package path in /data/app
     * @param blocking quick return
     * @return a json string contains: true if success, false otherwise.
     */
    public static native String createMinimalApk(String packagePath, int blocking);

    /**
     * This method will check minimal base apk is existed
     *
     * @param packagePath package path in /data/app
     * @return a json string contains: true if success, false otherwise.
     * <li>0 not existed</li>
     * <li>1 existed</li>
     * <li>2 make minimal base apk</li>
     * @param blocking quick return
     */
    public static native String checkMinimalApk(String packagePath, int blocking);

}
