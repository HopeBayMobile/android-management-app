package com.hopebaytech.hcfsmgmt.terafonnapiservice;

interface ITrackAppStatusListener {

    /** Callback function when status changed */
    void onStatusChanged(String packageName, int status);

    /** Callback function when data tracking failed */
    void onTrackFailed(String packageName);

}
