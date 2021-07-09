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
