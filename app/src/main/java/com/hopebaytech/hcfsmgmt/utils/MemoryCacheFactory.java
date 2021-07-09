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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/13.
 */
public class MemoryCacheFactory {

    public static LruCache<Integer, Drawable> createMemoryCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8; // Use 1/8 memory size of max memory
        return new LruCache<Integer, Drawable>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Drawable drawable) {
                // The cache size will be measured in kilobytes rather than number of items.
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap == null) {
                    return 0;
                } else {
                    return bitmap.getByteCount() / 1024;
                }
            }
        };
    }

}
