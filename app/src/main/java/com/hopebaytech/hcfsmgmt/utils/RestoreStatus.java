package com.hopebaytech.hcfsmgmt.utils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/30.
 */
public class RestoreStatus {

    public static final int NONE = 0;
    public static final int MINI_RESTORE_IN_PROGRESS = 1;
    public static final int MINI_RESTORE_COMPLETED = 2;
    public static final int FULL_RESTORE_IN_PROGRESS = 3;
    public static final int FULL_RESTORE_COMPLETED = 4;

    public static class Error {
        public static final int DAMAGED_BACKUP = HCFSEvent.ErrorCode.ENOENT;
        public static final int OUT_OF_SPACE = HCFSEvent.ErrorCode.ENOSPC;
        public static final int CONN_FAILED = HCFSEvent.ErrorCode.ENETDOWN;
    }

}
