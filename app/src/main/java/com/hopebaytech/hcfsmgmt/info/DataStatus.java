package com.hopebaytech.hcfsmgmt.info;

import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/25.
 */
public class DataStatus {

    /**
     * <p>The data of file/app is all on the device. Or, the tera connection status is one of the
     * following status:
     * <li>{@link HCFSConnStatus#TRANS_NORMAL}</li>
     * <li>{@link HCFSConnStatus#TRANS_IN_PROGRESS}</li>
     * <li>{@link HCFSConnStatus#TRANS_SLOW}</li>
     * </p>
     */
    public static final int AVAILABLE = 0;

    /**
     * <p>The data of file/app is NOT all on the device. Or, the tera connection status is one of the
     * following status:
     * <li>{@link HCFSConnStatus#TRANS_FAILED}</li>
     * <li>{@link HCFSConnStatus#TRANS_NOT_ALLOWED}</li>
     * </p>
     */
    public static final int UNAVAILABLE = 1;

}
