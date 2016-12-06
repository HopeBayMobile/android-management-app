package com.hopebaytech.hcfsmgmt.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/22.
 */
public class BitmapFactoryUtils {

    private final static String CLASSNAME = BitmapFactoryUtils.class.getSimpleName();

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

    public static Bitmap decodeFile(String filePath, int width, int height) {
        // Decode image size first, set inJustDecodeBounds to true allowing the caller to query the
        // bitmap without having to allocate the memory for its pixels.
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opt);

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (opt.outWidth / scale > width && opt.outHeight / scale > height) {
            scale *= 2;
        }

        // Decode with inSampleSize
        opt = new BitmapFactory.Options();
        opt.inSampleSize = scale;
        return BitmapFactory.decodeFile(filePath, opt);
    }

}
