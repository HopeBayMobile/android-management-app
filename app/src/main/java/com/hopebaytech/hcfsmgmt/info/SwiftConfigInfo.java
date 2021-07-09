/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
