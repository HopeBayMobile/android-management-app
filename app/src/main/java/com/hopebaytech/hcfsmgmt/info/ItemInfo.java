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
