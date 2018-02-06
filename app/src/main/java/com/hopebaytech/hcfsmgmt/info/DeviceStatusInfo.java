package com.hopebaytech.hcfsmgmt.info;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/17.
 */
public class DeviceStatusInfo implements Parcelable {

    private int id;
    private String imei;
    private String serviceStatus;
    private String model;
    private String containerIndex;

    public DeviceStatusInfo() {}

    protected DeviceStatusInfo(Parcel in) {
        id = in.readInt();
        imei = in.readString();
        serviceStatus = in.readString();
        model = in.readString();
        containerIndex = in.readString();
    }

    public static final Creator<DeviceStatusInfo> CREATOR = new Creator<DeviceStatusInfo>() {
        @Override
        public DeviceStatusInfo createFromParcel(Parcel in) {
            return new DeviceStatusInfo(in);
        }

        @Override
        public DeviceStatusInfo[] newArray(int size) {
            return new DeviceStatusInfo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }


    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("imei", imei);
            jsonObject.put("serviceStatus", serviceStatus);
            jsonObject.put("model", model);
            jsonObject.put("containerIndex", containerIndex);
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
        dest.writeInt(id);
        dest.writeString(imei);
        dest.writeString(serviceStatus);
        dest.writeString(model);
        dest.writeString(containerIndex);
    }

    public String getContainerIndex() {
        return containerIndex;
    }

    public void setContainerIndex(String index) {
        this.containerIndex = index;
    }
}
