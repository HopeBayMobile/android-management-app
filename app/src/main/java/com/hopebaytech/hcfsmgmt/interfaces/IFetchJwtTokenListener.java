package com.hopebaytech.hcfsmgmt.interfaces;

/**
 * Listener for fetching available JWT token from MGMT server
 *
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public interface IFetchJwtTokenListener {

    /** Callback function when fetch successful */
    void onFetchSuccessful(String jwtToken);

    /** Callback function when fetch failed */
    void onFetchFailed();

}
