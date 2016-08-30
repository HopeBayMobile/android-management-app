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
    public static final String ACTION_NOTIFY_LOCAL_STORAGE_USED_RATIO = "hbt.intent.action.NOTIFY_LOCAL_STORAGE_USED_RATIO";
    public static final String ACTION_PIN_APP = "hbt.intent.action.PIN_APP";
    public static final String ACTION_PIN_FILE_DIRECTORY = "hbt.intent.action.PIN_FILE_DIR";
    public static final String ACTION_ADD_UID_AND_PIN_SYS_APP_WHEN_BOOT_UP = "hbt.intent.action.ADD_UID_AND_PIN_SYS_APP_WHEN_BOOT_UP";
    public static final String ACTION_ADD_UID_TO_DB_AND_UNPIN_USER_APP = "hbt.intent.action.ADD_UID_TO_DB_AND_UNPIN_USER_APP";
    public static final String ACTION_REMOVE_UID_FROM_DB = "hbt.intent.action.REMOVE_UID_FROM_DB";
    public static final String ACTION_RESET_DATA_XFER = "hbt.intent.action.RESET_DATA_XFER";
    public static final String ACTION_ONGOING_NOTIFICATION = "hbt.intent.action.ONGOING_NOTIFICATION";
    public static final String ACTION_PIN_UNPIN_UDPATED_APP = "hbt.intent.action.PIN_UNPIN_UPDATED_APP";
    public static final String ACTION_CHECK_DEVICE_STATUS = "hbt.intent.action.CHECK_DEVICE_STATUS";
    public static final String ACTION_NOTIFY_INSUFFICIENT_PIN_SPACE = "hbt.intent.action.NOTIFY_INSUFFICIENT_PIN_SPACE";
    public static final String ACTION_UPDATE_EXTERNAL_APP_DIR = "hbt.intent.action.UPDATE_EXTERNAL_APP_DIR";
    public static final String ACTION_RESTORE_STAGE_1 = "hbt.intent.action.RESTORE_STAGE_1";
    public static final String ACTION_RESTORE_STAGE_2 = "hbt.intent.action.RESTORE_STAGE_2";
    public static final String ACTION_MINI_RESTORE_DONE = "hbt.intent.action.MINI_RESTORE_COMPLETED";
    public static final String ACTION_FULL_RESTORE_DONE = "hbt.intent.action.FULL_RESTORE_DONE";
    public static final String ACTION_RESTORE_NOTIFICATION = "hbt.intent.action.RESTORE_NOTIFICATION";
    public static final String ACTION_REBOOT_SYSETM = "hbt.intent.action.REBOOT_SYSTEM";

    // Intent key
    public static final String KEY_OPERATION = "intent_key_action";
    public static final String KEY_PIN_FILE_DIR_FILEAPTH = "intent_key_pin_firdir_filepath";
    public static final String KEY_PIN_FILE_DIR_PIN_STATUS = "intent_key_pin_firdir_pin_status";
    public static final String KEY_PIN_APP_DATA_DIR = "intent_key_pin_app_data_dir";
    public static final String KEY_PIN_APP_SOURCE_DIR = "intent_key_pin_app_source_dir";
    public static final String KEY_PIN_APP_EXTERNAL_DIR = "intent_key_pin_app_external_dir";
    public static final String KEY_PIN_APP_PIN_STATUS = "intent_key_pin_app_pin_status";
    public static final String KEY_PIN_APP_NAME = "intent_key_pin_app_name";
    public static final String KEY_PIN_PACKAGE_NAME = "intent_key_pin_package_name";
    public static final String KEY_SERVER_CLIENT_ID = "server_client_id";
    public static final String KEY_UID = "intent_key_uid";
    public static final String KEY_PACKAGE_NAME = "intent_key_package_name";
    public static final String KEY_ONGOING = "intent_key_ongoing";
    public static final String KEY_SILENT_SIGN_IN = "intent_key_silent_sign_in";
    public static final String KEY_GOOGLE_SIGN_IN_DISPLAY_NAME = "google_sign_in_display_name";
    public static final String KEY_GOOGLE_SIGN_IN_EMAIL = "google_sign_in_email";
    public static final String KEY_GOOGLE_SIGN_IN_PHOTO_URI = "google_sign_in_photo_uri";

}
