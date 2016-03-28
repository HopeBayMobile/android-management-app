package com.hopebaytech.hcfsmgmt.info;

import android.graphics.drawable.Drawable;

/**
 * Created by Aaron on 2016/3/28.
 */
public class DrawableInfo {

    private Drawable drawable;
    private int resourceId;

    public DrawableInfo(Drawable drawable, int resourceId) {
        this.drawable = drawable;
        this.resourceId = resourceId;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }
}
