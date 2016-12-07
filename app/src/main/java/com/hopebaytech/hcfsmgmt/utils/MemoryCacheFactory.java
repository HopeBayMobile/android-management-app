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
