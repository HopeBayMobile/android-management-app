package com.hopebaytech.hcfsmgmt.info;


import android.os.Parcel;
import android.os.Parcelable;

public class SwiftConfigInfo implements Parcelable {

    public static final String PARCEL_KEY = "swiftConfigInfoParcel";

    private String url;
    private String account;
    private String key;

    public SwiftConfigInfo() {
    }

    public SwiftConfigInfo(Parcel parcel) {
        this.url = parcel.readString();
        this.account = parcel.readString();
        this.key = parcel.readString();
    }

    public static final Parcelable.Creator<SwiftConfigInfo> CREATOR = new Parcelable.Creator<SwiftConfigInfo>() {
        public SwiftConfigInfo createFromParcel(Parcel parcel) {
            return new SwiftConfigInfo(parcel);
        }

        public SwiftConfigInfo[] newArray(int size) {
            return new SwiftConfigInfo[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(account);
        dest.writeString(key);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
