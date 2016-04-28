package com.hopebaytech.hcfsmgmt.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Aaron on 2016/4/13.
 */
public class MemoryCacheFactory {

    public static LruCache<Integer, Bitmap> createMemoryCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        return new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                /** The cache size will be measured in kilobytes rather than number of items. */
                return bitmap.getByteCount() / 1024;
            }

        };
    }

}
