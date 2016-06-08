package com.hopebaytech.hcfsmgmt.utils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/4.
 */
public class Interval {

    public static final int TEN_SECONDS = 10 * 1000;
    public static final int MINUTE = 6 * TEN_SECONDS;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;
    public static final int PIN_DATA_TYPE_FILE = HOUR;
    public static final int RESET_XFER = 24 * HOUR;
    public static final int NOTIFY_UPLOAD_COMPLETED = HOUR;
    public static final int NOTIFY_LOCAL_STORAGE_USED_RATIO = HOUR;
    public static final int NOTIFY_INSUFFICIENT_PIN_SPACE = 10 * MINUTE;

}
