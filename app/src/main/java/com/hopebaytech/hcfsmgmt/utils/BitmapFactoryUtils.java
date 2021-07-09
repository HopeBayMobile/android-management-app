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
