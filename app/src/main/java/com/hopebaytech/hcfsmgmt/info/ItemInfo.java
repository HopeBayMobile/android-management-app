package com.hopebaytech.hcfsmgmt.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.AppFileFragment;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;

public abstract class ItemInfo {

    protected Context mContext;
    public static final int ICON_COLORFUL = 255;
    public static final int ICON_TRANSPARENT = 50;

    /**
     * The pin/unpin status of the item
     */
    private boolean isPinned;

    private String name;
    private boolean isProcessing;
    private long lastProcessTime;
    private int position;

    public AppFileFragment.RecyclerViewHolder viewHolder;

    public ItemInfo(Context context) {
        this.mContext = context;
    }

    public AppFileFragment.RecyclerViewHolder getViewHolder() {
        return viewHolder;
    }

    public void setViewHolder(AppFileFragment.RecyclerViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    public String getName() {
        return name;
    }

    public long getLastProcessTime() {
        return lastProcessTime;
    }

    public void setLastProcessTime(long lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public void setProcessing(boolean isProcessing) {
        this.isProcessing = isProcessing;
    }

    public void setName(String infoName) {
        this.name = infoName;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Nullable
    public abstract Bitmap getIconImage();

    public abstract Drawable getIconDrawable();

    public Drawable getPinViewImage(boolean isPinned) {
        return ContextCompat.getDrawable(
                mContext,
                isPinned ? R.drawable.icon_btn_app_pin : R.drawable.icon_btn_app_unpin
        );
    }

    public abstract int hashCode();

    public abstract int getIconAlpha();

}
