package com.hopebaytech.hcfsmgmt.info;

import java.util.ArrayList;
import java.util.List;

/**
 * @author GuoYu
 *         Created by GuoYu on 2016/11/16.
 */
public class BoosterDeviceInfo {

    private String message;
    private int responseCode;
    private String responseContent;
    private int whiteListVersion;
    private List<String> whiteList = new ArrayList<>();

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

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public int getWhiteListVersion() {
        return whiteListVersion;
    }

    public void setWhiteListVersion(int whiteListVersion) {
        this.whiteListVersion = whiteListVersion;
    }

    public List<String> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }

}
