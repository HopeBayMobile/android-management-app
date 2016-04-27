package com.hopebaytech.hcfsmgmt.terafonnapiservice;

interface IFetchAppDataListener {

    /** Callback function before starting to fetch data. */
    void onPreFetch(String packageName);

    /** Callback function when fetching progress is changed. */
    void onProgressUpdate(String packageName, int progress);

    /** Callback function when data fetching finished. */
    void onPostFetch(String packageName);

    /** Callback function to notify a fetching event is failed. */
    void onFetchFailed(String packageName);

}
