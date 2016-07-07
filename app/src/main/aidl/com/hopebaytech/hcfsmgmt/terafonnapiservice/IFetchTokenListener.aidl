package com.hopebaytech.hcfsmgmt.terafonnapiservice;

/** Listener for fetching available JWT token from MGMT server */
interface IFetchTokenListener {

    /** Callback function when fetch successful */
    void onFetchSuccessful(String jwtToken);

    /** Callback function when fetch failed */
    void onFetchFailed();

}
