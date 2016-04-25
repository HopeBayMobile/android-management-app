package com.hopebaytech.hcfsmgmt.terafonnapiservice;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Aaron on 2016/3/21.
 */
public class AppStatus implements Parcelable {

    public static final int LOCATION_LOCAL = 1;
    public static final int LOCATION_HYBRID = 2;
    public static final int LOCATION_CLOUD = 3;

    public static final int STATUS_AVAILABLE  = 0;
    public static final int STATUS_UNAVAILABLE = 1;
    public static final int STATUS_UNAVAILABLE_WAIT_TO_DOWNLOAD  = 1;
    public static final int STATUS_UNAVAILABLE_NONE_NETWORK  = 2;

    private String packageName;
    private boolean isPin;
    private boolean onFetching;
    private int status;

    protected AppStatus() {
    }

    protected AppStatus(String packageName, boolean isPin, boolean onFetching, int status) {
        this.packageName = packageName;
        this.isPin = isPin;
        this.onFetching = onFetching;
        this.status = status;
    }

    protected AppStatus(Parcel in) {
        packageName = in.readString();
        isPin = (in.readInt() == 1);
        onFetching = (in.readInt() == 1);
        status = in.readInt();
    }

    public static final Creator<AppStatus> CREATOR = new Creator<AppStatus>() {
        @Override
        public AppStatus createFromParcel(Parcel in) {
            return new AppStatus(in);
        }

        @Override
        public AppStatus[] newArray(int size) {
            return new AppStatus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeInt(isPin ? 1 : 0);
        dest.writeInt(onFetching ? 1 : 0);
        dest.writeInt(status);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isOnFetching() {
        return onFetching;
    }

    public void setOnFetching(boolean onFetching) {
        this.onFetching = onFetching;
    }

    public boolean isPin() {
        return isPin;
    }

    public void setPin(boolean pin) {
        this.isPin = pin;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
