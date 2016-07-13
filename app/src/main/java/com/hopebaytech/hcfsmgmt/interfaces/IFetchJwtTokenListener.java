package com.hopebaytech.hcfsmgmt.interfaces;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/19.
 */
public interface IFetchJwtTokenListener {

    /** Callback function when fetch successful */
    void onFetchSuccessful(String jwtToken);

    /** Callback function when fetch failed */
    void onFetchFailed();

}
