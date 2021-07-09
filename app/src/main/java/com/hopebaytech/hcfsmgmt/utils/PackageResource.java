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
package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PackageResource {

    private Context mContext;
    private PackageManager mPackageManager;

    public PackageResource(Context context) {

        mContext = context;
        mPackageManager = context.getPackageManager();
    }

    public String[] getMinimalApkResourceFileName(String packageName) {
        ArrayList<String> resourceNameList = new ArrayList<String>();
        try {
            final Resources resources = mPackageManager.getResourcesForApplication(packageName);
            final ApplicationInfo info = mPackageManager.getApplicationInfo(
                    packageName, PackageManager.GET_META_DATA);
            if (info.icon != 0) {
                resourceNameList.add(
                        getResourceFileName(resources.getResourceName(info.icon)));
            }
            addLaunchIcons(resources, resourceNameList, packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resourceNameList.toArray(new String[resourceNameList.size()]);
    }

    private String getResourceFileName(String resourceName) {
        // format is like com.google.android.play.games:mipmap/ic_launcher_play_games
        return resourceName.substring(resourceName.indexOf('/') + 1);
    }

    private List<ResolveInfo> getLaunchActivities(String packageName) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage(packageName);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return mPackageManager.queryIntentActivities(intent, 0);
    }

    private void addLaunchIcons(Resources resources,
        ArrayList<String> resourceNameList, String packageName) {
        final List<ResolveInfo> list = getLaunchActivities(packageName);
        if (list == null) {
            return;
        }

        for (ResolveInfo res : list) {
            if (res.activityInfo.icon != 0) {
                final String name = getResourceFileName(
                        resources.getResourceName(res.activityInfo.icon));
                if (!resourceNameList.contains(name)) {
                    resourceNameList.add(name);
                }
            }
        }
    }
}
