package com.hopebaytech.hcfsmgmt.utils;

/**
 * @author Vince
 *         Created by Vince on 2016/7/19.
 */
public class HCFSEvent {

    public static final int TEST = 0;
    public static final int TOKEN_EXPIRED = 1;
    public static final int UPLOAD_COMPLETED = 2;
    public static final int RESTORE_STAGE_1 = 3;
    public static final int RESTORE_STAGE_2 = 4;
    public static final int CREATE_THUMBNAIL = 7;
    public static final int BOOSTER_PROCESS_COMPLETED = 8;
    public static final int BOOSTER_PROCESS_FAILED = 9;

    // The following event is not implemented.
    public static final int EXCEED_PIN_MAX = 5;
    public static final int SPACE_NOT_ENOUGH = 6;

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
