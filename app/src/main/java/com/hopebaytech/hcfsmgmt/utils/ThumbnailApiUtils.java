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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import org.json.JSONObject;

/**
 * @author Vince
 *      Created by Vince on 2016/10/24.
 */

public class ThumbnailApiUtils {
    private final int image = 0;
    private final int video = 1;
    private int retry = 3;
    private int interval = 1000;

    private final String CLASSNAME = getClass().getSimpleName();
    private Context mContext;

    public void createThumbnailImages(Context mContext, JSONObject jsonObj) {
        this.mContext =  mContext;
        try {
            Logs.i(CLASSNAME, "createThumbnailImages", jsonObj.toString());

            int thumbnailType = jsonObj.getInt("thumbnail_type");
            String filePath = jsonObj.getString("file_path");
            // skip thumbnails
            if (filePath.matches("^/mnt/runtime/.*/emulated/.*/DCIM/.thumbnails/.*"))
                return;
            // replace /mnt/runtime/.*/emulated path to /storage/emulated
            // to search image id in media db
            filePath = filePath.replaceFirst("^/mnt/runtime/.*/emulated", "/storage/emulated");
            switch (thumbnailType) {
                case image:
                    for (int i = 0; i < retry; i++) {
                        if (getImageThumbnail(filePath) != null) {
                            break;
                        } else {
                            Logs.e(CLASSNAME, "createThumbnailImages",
                                    "Failed to create thumbnail from image, path: " + filePath);
                        }
                        Thread.sleep(interval);
                    }
                    break;

                case video:
                    for (int i = 0; i < retry; i++) {
                        if (getVideoThumbnail(filePath) != null) {
                            break;
                        } else {
                            Logs.e(CLASSNAME, "createThumbnailImages",
                                    "Failed to create thumbnail from video, path: " + filePath);
                        }
                        Thread.sleep(interval);
                    }
                    break;
            }
        } catch (Exception e) {
            Logs.e(CLASSNAME, "createThumbnailImages", e.toString());
        }
    }

    public Bitmap getThumbnail(Context context, long id, int type) {
        Bitmap thumbnail = null;
        ContentResolver cr = context.getContentResolver();
        Logs.d(CLASSNAME, "getThumbnail", String.valueOf(id)+  " " + String.valueOf(type));
        if (type == image) {
            thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
        } else if(type == video) {
            thumbnail = MediaStore.Video.Thumbnails.getThumbnail(cr, id, MediaStore.Video.Thumbnails.MINI_KIND, null);
        }
        return thumbnail;
    }

    private Bitmap getImageThumbnail(String path) {
        Bitmap thumbnail = null;
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.DATA + "=?", new String[]{path}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            } else {
                Logs.e(CLASSNAME, "getImageThumbnail", "cursor.moveToFirst = false");
            }
            cursor.close();
        } else {
            Logs.e(CLASSNAME, "getImageThumbnail", "cursor = null");
        }
        return thumbnail;
    }

    private Bitmap getVideoThumbnail(String path) {
        Bitmap thumbnail = null;
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.DATA + "=?", new String[]{path}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                thumbnail = MediaStore.Video.Thumbnails.getThumbnail(cr, id, MediaStore.Video.Thumbnails.MINI_KIND, null);
            }
            cursor.close();
        } else {
            Logs.e(CLASSNAME, "getVideoThumbnail", "cursor = null");
        }
        return thumbnail;
    }
}
