package com.hopebaytech.hcfsmgmt.info;

import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/17.
 */
public class DeviceListInfo {

    private int responseCode;
    private List<DeviceStatusInfo> deviceStatusInfoList;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public List<DeviceStatusInfo> getDeviceStatusInfoList() {
        return deviceStatusInfoList;
    }

    public void setDeviceStatusInfoList(List<DeviceStatusInfo> deviceStatusInfoList) {
        this.deviceStatusInfoList = deviceStatusInfoList;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
