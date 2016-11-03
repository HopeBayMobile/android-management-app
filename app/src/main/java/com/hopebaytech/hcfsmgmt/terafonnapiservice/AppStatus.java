package com.hopebaytech.hcfsmgmt.terafonnapiservice;

import android.os.Parcel;
import android.os.Parcelable;

import com.hopebaytech.hcfsmgmt.info.DataStatus;

/**
 * @author Aaron, Vince
 *         Created by Aaron on 2016/3/21.
 */
public class AppStatus implements Parcelable {

    public static final int UNKNOWN = DataStatus.UNKNOWN;
    public static final int AVAILABLE = DataStatus.AVAILABLE;
    public static final int UNAVAILABLE = DataStatus.UNAVAILABLE;

    private String packageName;
    private boolean isPinned;
    private boolean onFetching;
    private int status;

    protected AppStatus() {
    }

    protected AppStatus(String packageName, boolean isPinned, boolean onFetching, int status) {
        this.packageName = packageName;
        this.isPinned = isPinned;
        this.onFetching = onFetching;
        this.status = status;
    }

    protected AppStatus(Parcel in) {
        packageName = in.readString();
        isPinned = (in.readInt() == 1);
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
        dest.writeInt(isPinned ? 1 : 0);
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
        return isPinned;
    }

    public void setPin(boolean pin) {
        this.isPinned = pin;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
