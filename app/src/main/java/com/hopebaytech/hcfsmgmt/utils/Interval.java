package com.hopebaytech.hcfsmgmt.utils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/4.
 */
public class Interval {

    // Base interval
    public static final int SECOND = 1000;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;

    // Custom interval
    public static final int RESET_XFER_HOURS_OF_DAY = 23;
    public static final int RESET_XFER_MINUTES_OF_DAY = 59;
    public static final int RESET_XFER_SECOND_OF_DAY = 59;

    /**
     * Interval for resetting the amount of data transfer.
     */
    public static final int RESET_DATA_XFER = DAY;

    /**
     * Interval for updating the external app dir path to database, such as
     * /storage/emulated/0/Android/data/<package_name>, /storage/emulated/0/Android/obb/<package_name>, etc.
     */
    public static final int MONITOR_EXTERNAL_APP_DIR = DAY;

    /**
     * Interval for checking whether the local storage used ratio exceed the threshold set by user.
     */
    public static final int MONITOR_LOCAL_STORAGE_USED_SPACE = HOUR;

    /**
     * Interval for checking whether the pin space is insufficient.
     */
    public static final int MONITOR_PINNED_SPACE = 10 * MINUTE;

    /**
     * Interval checking device service when receiving token expired event from hcfs.
     * This polling service is started only when the device service is not "activated" state.
     */
    public static final int CHECK_DEVICE_SERVICE_WHEN_TOKEN_EXPIRED = HOUR;

    /**
     * Interval for checking device service for waiting user to restore Tera from another
     * phone after the data content of device is synced to cloud in the transferring device procedure.
     */
    public static final int WAIT_RESTORE_AFTER_TRANSFER_DEVICE = 10 * SECOND;

    /**
     * Interval for automatically refreshing UI in APP/FILE page
     */
    public static final int AUTO_REFRESH_UI = 3 * SECOND;

    /**
     * Interval for checking pin/unpin status in queue in order to show/dismiss progress circle.
     * Keep showing progress circle if the system pin/unpin status is not the same as the user
     * expected pin status, dismiss progress circle otherwise.
     */
    public static final int CHECK_PIN_STATUS = SECOND;

    /**
     * Interval for executing pin/unpin api requested by user.
     */
    public static final int EXECUTE_PIN_API = SECOND;

    /**
     * Interval for not allowing user to refresh APP/FILE page.
     */
    public static final int NOT_ALLOW_REFRESH = 2 * SECOND;

    /**
     * Interval for updating information in overview page.
     */
    public static final int UPDATE_OVERVIEW_INFO = 3 * SECOND;

    /**
     * Interval for updating the ongoing notification information, such as tera connection status
     * and used space.
     */
    public static final int UPDATE_NOTIFICATION_INFO = 5 * MINUTE;

    /**
     * Interval for pining /storage/emulated/0/android folder
     */
    public static final int PIN_ANDROID_FOLDER = 10 * SECOND;

    /**
     * Interval for delaying the movement from settings page to smart cache page
     */
    public static final int MOVE_TO_SMART_PAGE_DELAY_TIME = (int) (1.2f * SECOND);

    /**
     * Interval for monitoring the booster used space.
     */
    public static final int MONITOR_BOOSTER_USED_SPACE = 10 * MINUTE;

    /**
     * Interval for retrying the previous database operation.
     */
    public static final int DATABASE_OPERATION_RETRY_TIME = 10;

    /**
     * Interval for updating tera connection status in TransferContentUploadingFragment
     */
    public static final int UPDATE_TERA_CONN_STATUS_IN_TRANSFER_CONTENT = 5000;

    /**
     * Interval for unlocking device until the device is unlocked.
     */
    public static final int UNLOCK_DEVICE = 500;

    /**
     * Interval for sending logs.
     */
    public static final int SEND_LOGS = HOUR;

}
