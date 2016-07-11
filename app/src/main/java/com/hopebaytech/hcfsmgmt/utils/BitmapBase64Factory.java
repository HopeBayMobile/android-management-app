package com.hopebaytech.hcfsmgmt.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/22.
 */
public class BitmapBase64Factory {

    private final static String CLASSNAME = BitmapBase64Factory.class.getSimpleName();

    /**
     * example: encodeToBase64(myBitmap, Bitmap.CompressFormat.JPEG, 100);
     */
    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        Logs.d(CLASSNAME, "encodeToBase64", null);
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String base64) {
        Logs.d(CLASSNAME, "decodeBase64", null);
        byte[] decodedBytes = Base64.decode(base64, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}
