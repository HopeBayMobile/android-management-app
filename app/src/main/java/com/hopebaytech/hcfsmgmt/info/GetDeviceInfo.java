package com.hopebaytech.hcfsmgmt.info;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/12.
 */
public class GetDeviceInfo {

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

}