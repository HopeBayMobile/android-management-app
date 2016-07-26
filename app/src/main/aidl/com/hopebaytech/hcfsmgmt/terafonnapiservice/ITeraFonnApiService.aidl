package com.hopebaytech.hcfsmgmt.terafonnapiservice;

import com.hopebaytech.hcfsmgmt.terafonnapiservice.IGetJWTandIMEIListener;
import com.hopebaytech.hcfsmgmt.terafonnapiservice.IFetchAppDataListener;
import com.hopebaytech.hcfsmgmt.terafonnapiservice.ITrackAppStatusListener;
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

    /** Check datalocation of all apps in appList */
    int checkAppAvailable(String packageName);

    /** Get the HCFS status and related information */
    String getHCFSStat();

    /** Check whether HCFS is enabled */
    boolean hcfsEnabled();

}
