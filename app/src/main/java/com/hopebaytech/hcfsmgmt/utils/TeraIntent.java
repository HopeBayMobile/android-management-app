package com.hopebaytech.hcfsmgmt.utils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/18.
 */
public class TeraIntent {

    // Intent action
    public static final String ACTION_UPLOAD_COMPLETED = "hbt.intent.action.UPLOAD_COMPLETED";
    public static final String ACTION_TRANSFER_COMPLETED = "hbt.intent.action.TRANSFER_COMPLETED";
    public static final String ACTION_TOKEN_EXPIRED = "hbt.intent.action.TOKEN_EXPIRED";
    public static final String ACTION_EXCEED_PIN_MAX = "hbt.intent.action.EXCEED_PIN_MAX";
    public static final String ACTION_MONITOR_LOCAL_STORAGE_USED_SPACE = "hbt.intent.action.MONITOR_LOCAL_STORAGE_USED_SPACE";
    public static final String ACTION_PIN_APP = "hbt.intent.action.PIN_APP";
    public static final String ACTION_PIN_FILE_DIRECTORY = "hbt.intent.action.PIN_FILE_DIR";
    public static final String ACTION_ADD_UID_AND_PIN_SYS_APP_WHEN_BOOT_UP = "hbt.intent.action.ADD_UID_AND_PIN_SYS_APP_WHEN_BOOT_UP";
    public static final String ACTION_ADD_UID_INFO_TO_DATABASE = "hbt.intent.action.ADD_UID_INFO_TO_DATABASE";
    public static final String ACTION_REMOVE_UID_FROM_DB = "hbt.intent.action.REMOVE_UID_FROM_DB";
    public static final String ACTION_RESET_DATA_XFER = "hbt.intent.action.RESET_DATA_XFER";
    public static final String ACTION_ONGOING_NOTIFICATION = "hbt.intent.action.ONGOING_NOTIFICATION";
    public static final String ACTION_PIN_UNPIN_UDPATED_APP = "hbt.intent.action.PIN_UNPIN_UPDATED_APP";
    public static final String ACTION_CHECK_DEVICE_STATUS = "hbt.intent.action.CHECK_DEVICE_STATUS";
    public static final String ACTION_MONITOR_PINNED_SPACE = "hbt.intent.action.MONITOR_PINNED_SPACE";
    public static final String ACTION_MONITOR_EXTERNAL_APP_DIR = "hbt.intent.action.MONITOR_EXTERNAL_APP_DIR";
    public static final String ACTION_RESTORE_STAGE_1 = "hbt.intent.action.RESTORE_STAGE_1";
    public static final String ACTION_RESTORE_STAGE_2 = "hbt.intent.action.RESTORE_STAGE_2";
    public static final String ACTION_MINI_RESTORE_DONE = "hbt.intent.action.MINI_RESTORE_COMPLETED";
    public static final String ACTION_FULL_RESTORE_DONE = "hbt.intent.action.FULL_RESTORE_DONE";
    public static final String ACTION_RESTORE_NOTIFICATION = "hbt.intent.action.RESTORE_NOTIFICATION";
    public static final String ACTION_REBOOT_SYSETM = "hbt.intent.action.REBOOT_SYSTEM";
    public static final String ACTION_MINI_RESTORE_COMPLETED = "hbt.intent.action.MINI_RESTORE_COMPLETED";
    public static final String ACTION_FULL_RESTORE_COMPLETED = "hbt.intent.action.FULL_RESTORE_COMPLETED";
    public static final String ACTION_MINI_RESTORE_REBOOT_SYSTEM = "hbt.intent.action.REBOOT_SYSTEM";
    public static final String ACTION_CHECK_RESTORE_STATUS = "hbt.intent.action.CHECK_RESTORE_STATUS";
    public static final String ACTION_FACTORY_RESET = "hbt.intent.action.FACTORY_RESET";
    public static final String ACTION_RETRY_RESTORE_WHEN_CONN_FAILED = "hbt.intent.action.RETRY_RESTORE_WHEN_CONN_FAILED";
    public static final String ACTION_ALLOW_PIN_UNPIN = "hbt.intent.action.ALLOW_PIN_UNPIN";
    public static final String ACTION_BOOSTER_PROCESS_COMPLETED = "hbt.intent.action.BOOSTER_PROCESS_COMPLETED";
    public static final String ACTION_BOOSTER_PROCESS_FAILED = "hbt.intent.action.BOOSTER_PROCESS_FAILED";
    public static final String ACTION_CHECK_AND_FIX_BOOSTER = "hbt.intent.action.CHECK_AND_FIX_BOOSTER";
    public static final String ACTION_MONITOR_BOOSTER_USED_SPACE = "hbt.intent.action.MONITOR_BOOSTER_USED_SPACE";
    public static final String ACTION_SEND_LOGS = "hbt.intent.action.SEND_LOGS";
    public static final String ACTION_ERASE_DATA = "hbt.intent.action.ERASE_DATA";

    // Intent key
    public static final String KEY_UID = "intent_key_uid";
    public static final String KEY_PACKAGE_NAME = "intent_key_package_name";
    public static final String KEY_ONGOING = "intent_key_ongoing";
    public static final String KEY_GOOGLE_SIGN_IN_DISPLAY_NAME = "intent_key_google_sign_in_display_name";
    public static final String KEY_GOOGLE_SIGN_IN_EMAIL = "intent_key_google_sign_in_email";
    public static final String KEY_GOOGLE_SIGN_IN_PHOTO_URI = "intent_key_google_sign_in_photo_uri";
    public static final String KEY_RESTORE_ERROR_CODE = "intent_key_restore_error_code";
    public static final String KEY_ALLOW_PIN_UNPIN = "pin_unpin_enabled";
}
