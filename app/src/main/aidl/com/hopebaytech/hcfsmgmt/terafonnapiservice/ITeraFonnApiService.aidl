package com.hopebaytech.hcfsmgmt.terafonnapiservice;

import com.hopebaytech.hcfsmgmt.terafonnapiservice.IGetJWTandIMEIListener;
import com.hopebaytech.hcfsmgmt.terafonnapiservice.IFetchAppDataListener;
import com.hopebaytech.hcfsmgmt.terafonnapiservice.ITrackAppStatusListener;
import com.hopebaytech.hcfsmgmt.terafonnapiservice.ICheckAppAvailableListener;
import com.hopebaytech.hcfsmgmt.terafonnapiservice.AppInfo;

interface ITeraFonnApiService {

    void setJWTandIMEIListener(IGetJWTandIMEIListener listener);

    boolean getJWTandIMEI();

    /** Register the FetchAppData listener */
    void setFetchAppDataListener(IFetchAppDataListener listener);

    /** Force system to download all data not in local of this app */
    boolean fetchAppData(String packageName);

    /** Register the FetchAppData listener */
    void setTrackAppStatusListener(ITrackAppStatusListener listener);

    /**
     * Keep track of app in pkgNameList, and notify the status change
     * (e.g. the location of a package changed from local to cloud) by TrackAppStatus listener.
     * */
    boolean addTrackAppStatus(in List<String> packageNameList);

    /** Stop tracking Apps in the given pakcage name list */
    boolean removeTrackAppStatus(in List<String> packageNameList);

    /** Clear the package tracked list. No packages are tracked anymore. */
    boolean clearTrackAppStatus();

    /** Get app status of all apps in packageNameList */
    AppInfo getAppInfo(in List<String> packageNameList);

    /** Pin an App with given package name */
    boolean pinApp(String packageName);

    /** Unpin an App with given package name */
    boolean unpinApp(String packageName);

    /** Check data location of an app */
    int checkAppAvailable(String packageName);

    /** Check data location of an app and callback while get status */
    void postCheckAppAvailable(String packageName, ICheckAppAvailableListener listener);

    /** Get the HCFS status and related information */
    String getHCFSStat();

    /** Check whether HCFS is enabled */
    boolean hcfsEnabled();

    /** Stop to upload Tera data to cloud
     * @return <li>1 if system is clean now. That is, there is no dirty data.</li>
     * <li>0 when setting sync point completed.</li>
     * <li>Negative error code in case that error occurs</li>
     */
    int startUploadTeraData();

    /**
     * Start to upload Tera data to cloud
     *
     * @return <li>1 if no sync point is set.</li>
     * <li>0 when canceling the setting completed.</li>
     * <li>Negative error code in case that error occurs</li>
     **/
    int stopUploadTeraData();

    /** Get Tera free space size **/
    long getTeraFreeSpace();

    /** Get Tera total space size **/
    long getTeraTotalSpace();

    /** Get Tera settings is wifi only **/
    boolean isWifiOnly();

    /** Get connection status **/
    int getConnStatus();

    /** Check the allow pin/unpin apps flag in advanced settings **/
    boolean isAllowPinUnpinApps();

    /** Get app boost status **/
    int getAppBoostStatus(String packageName);

    /**
     * The activate status of boost or unboost
     *
     * @return <li>0, not boost or unboost is activated.</li>
     * <li>1, unboost is activated.</li>
     * <li>2, boost is activated.</li>
     **/
    int getBoostUnboostActivateStatus();

    /** Check if app is boosting **/
    boolean isBoostOrUnboostInProgress(String packageName);
}
