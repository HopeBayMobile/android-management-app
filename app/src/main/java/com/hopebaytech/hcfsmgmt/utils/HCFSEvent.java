package com.hopebaytech.hcfsmgmt.utils;

/**
 * @author Vince
 *         Created by Vince on 2016/7/19.
 */
public class HCFSEvent {

    public static final int TEST = 0;
    public static final int TOKEN_EXPIRED = 1;
    public static final int UPLOAD_COMPLETED = 2;
    public static final int EXCEED_PIN_MAX = 3;
    public static final int SPACE_NOT_ENOUGH = 4;
    public static final int RESTORE_STAGE_1 = 5;
    public static final int RESTORE_STAGE_2 = 6;

    public static class ErrorCode {

        /**
         * No such file or directory.
         */
        public static final int ENOENT = -2;

        /**
         * No space left on device.
         */
        public static final int ENOSPC = -28;

        /**
         * Network is down.
         */
        public static final int ENETDOWN = -100;

    }

}
