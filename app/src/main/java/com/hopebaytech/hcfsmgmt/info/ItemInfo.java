package com.hopebaytech.hcfsmgmt.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.hopebaytech.hcfsmgmt.fragment.FileMgmtFragment;

public abstract class ItemInfo {

	protected Context mContext;
    protected final int ICON_COLORFUL = 255;
    protected final int ICON_TRANSPARENT = 50;

    private boolean isPinned;
    private String infoName;
    private boolean isProcessing;
    private long lastProcessTime;

	public FileMgmtFragment.RecyclerViewHolder viewHolder;
	
	public ItemInfo(Context context) {
		this.mContext = context;
	}
	
	public FileMgmtFragment.RecyclerViewHolder getViewHolder() {
		return viewHolder;
	}

	public void setViewHolder(FileMgmtFragment.RecyclerViewHolder viewHolder) {
		this.viewHolder = viewHolder;
	}

	public String getItemName() {
		return infoName;
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

	public void setItemName(String infoName) {
		this.infoName = infoName;
	}
	
	public boolean isPinned() {
		return isPinned;
	}

	public void setPinned(boolean isPinned) {
		this.isPinned = isPinned;
	}
	
	@Nullable
	public abstract Bitmap getIconImage();
	
	@Nullable
	public abstract Drawable getPinUnpinImage(boolean isPinned);
	
	public abstract int hashCode();

	public abstract int getIconAlpha();

}