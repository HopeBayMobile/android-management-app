package com.hopebaytech.hcfsmgmt.info;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/17.
 */
public class DeviceListInfo implements Parcelable {

    public static final int TYPE_RESTORE_NONE = 0;
    public static final int TYPE_RESTORE_FROM_MY_TERA = 1;
    public static final int TYPE_RESTORE_FROM_BACKUP = 2;

    public static final int TYPE_RESTORE_FROM_GOOGLE_DRIVE = 11;
    public static final int TYPE_RESTORE_FROM_SWIFT = 12;

    /**
     * {@link #TYPE_RESTORE_FROM_MY_TERA}, {@link #TYPE_RESTORE_FROM_BACKUP}
     */
    private int type;
    private String message;
    private int responseCode;
    private String responseContent;
    private List<DeviceStatusInfo> deviceStatusInfoList = new ArrayList<>();

    public DeviceListInfo() {
    }

    protected DeviceListInfo(Parcel in) {
        message = in.readString();
        responseCode = in.readInt();
        type = in.readInt();
        in.readTypedList(deviceStatusInfoList, DeviceStatusInfo.CREATOR);
    }

    public static final Creator<DeviceListInfo> CREATOR = new Creator<DeviceListInfo>() {
        @Override
        public DeviceListInfo createFromParcel(Parcel in) {
            return new DeviceListInfo(in);
        }

        @Override
        public DeviceListInfo[] newArray(int size) {
            return new DeviceListInfo[size];
        }
    };

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static Creator<DeviceListInfo> getCREATOR() {
        return CREATOR;
    }

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

    public void addDeviceStatusInfo(DeviceStatusInfo info) {
        this.deviceStatusInfoList.add(info);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public boolean isEmpty() {
        return deviceStatusInfoList.isEmpty();
    }

    public int size() {
        return deviceStatusInfoList.size();
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", message);
            jsonObject.put("responseCode", responseCode);
        } catch (JSONException e) {
            return Log.getStackTraceString(e);
        }
        return jsonObject.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeInt(responseCode);
        dest.writeInt(type);
        dest.writeTypedList(deviceStatusInfoList);
    }
}
