package com.hopebaytech.hcfsmgmt.info;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/12.
 */
public class DeviceServiceInfo {

    public static class Category {
        public static final String LOCK = "pb_001";
        public static final String RESET = "pb_002";
        public static final String TX_WAITING = "pb_003";
        public static final String UNREGISTERED = "pb_004";
    }

    public static class State {
        public static final String ACTIVATED = "activated";
        public static final String DISABLED = "disabled";
        public static final String TXReady = "TXReady";
    }

    private String message;
    private int responseCode;

    @StringRes
    public int getMessage(@StringRes int defaultMsgResId) {
        int errorMsgResId = defaultMsgResId;
        if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
            switch (errorCode) {
                case MgmtCluster.INCORRECT_MODEL:
                case MgmtCluster.INCORRECT_VENDOR:
                    errorMsgResId = R.string.activate_failed_not_supported_device;
                    break;
                case MgmtCluster.DEVICE_EXPIRED:
                    errorMsgResId = R.string.activate_failed_device_expired;
                    break;
                case MgmtCluster.MAPPING_EXISTED:
                    errorMsgResId = R.string.activate_failed_device_in_use;
                    break;
            }
        } else {
            errorMsgResId = HTTPErrorMessage.getErrorMessageResId(responseCode);
        }
        return errorMsgResId;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

}
