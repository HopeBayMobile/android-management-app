package com.hopebaytech.hcfsmgmt.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Aaron on 2016/4/13.
 */
public class MemoryCacheFactory {

    public static LruCache<String, Bitmap> createMemoryCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        LruCache<String, Bitmap> memoryCache= new LruCache<String, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                /** The cache size will be measured in kilobytes rather than number of items. */
                return bitmap.getByteCount() / 1024;
            }

        };
        return memoryCache;
    }

}
