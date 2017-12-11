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
