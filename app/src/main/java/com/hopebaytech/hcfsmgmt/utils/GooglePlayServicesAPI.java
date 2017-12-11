package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GooglePlayServicesAPI {
    public static boolean hasGooglePlayServices(Context context) {
        return (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                == ConnectionResult.SUCCESS);
    }
}
