package com.hopebaytech.hcfsmgmt.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

public abstract class ItemInfo {

	public static final int DATA_STATUS_CLOUD = 0;
	public static final int DATA_STATUS_HYBRID = 1;
	public static final int DATA_STATUS_LOCAL = 2;

	protected Context context;
	private boolean isPinned;
	private String infoName;
	private boolean isProcessing;
	private long lastProcessTime;
	
	public ItemInfo(Context context) {
		this.context = context;
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
	public abstract Drawable getPinUnpinImage();
	
	public abstract int getLocationStatus();
	
	public abstract int hashCode();

}