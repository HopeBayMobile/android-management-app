package com.hopebaytech.hcfsmgmt.terafonnapiservice;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/3/21.
 */
public class AppInfo implements Parcelable {

    private boolean result;
    private List<AppStatus> appStatusList;

    protected AppInfo() {

    }

    protected AppInfo(Parcel in) {
        result = (in.readInt() == 1);
        appStatusList = new ArrayList<>();
        in.readList(appStatusList, getClass().getClassLoader());
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(result ? 1 : 0);
        dest.writeList(appStatusList);
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public List<AppStatus> getAppStatusList() {
        return appStatusList;
    }

    public void setAppStatusList(List<AppStatus> appStatusList) {
        this.appStatusList = appStatusList;
    }
}
