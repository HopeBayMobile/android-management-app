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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.Iterator;
import java.util.List;

public class BrowserUtils {

    public static boolean isBrowserAvailable(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.testTera.com.tw"));
        PackageManager pm = context.getPackageManager();

        List resolvedActivityList = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        Iterator iterator = resolvedActivityList.iterator();
        while (iterator.hasNext()) {
            ResolveInfo info = (ResolveInfo) iterator.next();

            if (hasUsableBrowser(info)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasUsableBrowser(ResolveInfo resolveInfo) {
        if (resolveInfo.filter.hasAction("android.intent.action.VIEW") &&
                resolveInfo.filter.hasCategory("android.intent.category.BROWSABLE") &&
                resolveInfo.filter.schemesIterator() != null) {
            if (resolveInfo.filter.authoritiesIterator() != null) {
                return false;
            } else {
                boolean supportsHttp = false;
                boolean supportsHttps = false;
                Iterator schemeIter = resolveInfo.filter.schemesIterator();

                do {
                    if (!schemeIter.hasNext()) {
                        return false;
                    }

                    String scheme = (String) schemeIter.next();
                    supportsHttp |= "http".equals(scheme);
                    supportsHttps |= "https".equals(scheme);
                } while (!supportsHttp || !supportsHttps);

                return true;
            }
        } else {
            return false;
        }
    }

    public static void toDownloadPage(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + "com.android.chrome"));
        context.startActivity(intent);
    }
}
