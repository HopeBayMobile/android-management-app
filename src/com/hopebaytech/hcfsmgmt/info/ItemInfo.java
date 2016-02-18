package com.hopebaytech.hcfsmgmt.info;

import com.hopebaytech.hcfsmgmt.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

public abstract class ItemInfo {

	public static final int DATA_STATUS_CLOUD = 0;
	public static final int DATA_STATUS_HYBRID = 1;
	public static final int DATA_STATUS_LOCAL = 2;

	protected Context context;
//	protected BaseFileMgmtFragment baseFileMgmtFragment;
	private boolean isPinned;
	private String infoName;
	private boolean isProcessing;
	private long lastProcessTime;
	
	public ItemInfo(Context context) {
		this.context = context;
	}

//	public ItemInfo(Context context, BaseFileMgmtFragment baseFileMgmtFragment) {
//		this.context = context;
//		this.baseFileMgmtFragment = baseFileMgmtFragment;		
//	}
	
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

	public Drawable getPinImage(int status) {
		Drawable pinDrawable = null;
//		if (isPinned) {
//			pinDrawable = ContextCompat.getDrawable(context, R.drawable.pinned);
//		} else {
//			pinDrawable = ContextCompat.getDrawable(context, R.drawable.unpinned);
//		}
		if (isPinned) {
			if (status == FileStatus.LOCAL) {
				pinDrawable = ContextCompat.getDrawable(context, R.drawable.pinned);
			} else if (status == FileStatus.HYBRID || status == FileStatus.CLOUD) {
				pinDrawable = ContextCompat.getDrawable(context, R.drawable.pinning);
			} else {
				// TODO default image
			}
		} else {
			switch (status) {
			case FileStatus.LOCAL:
				pinDrawable = ContextCompat.getDrawable(context, R.drawable.unpinned_local);
				break;
			case FileStatus.HYBRID:
				pinDrawable = ContextCompat.getDrawable(context, R.drawable.unpinned_hybrid);
				break;
			case FileStatus.CLOUD:
				pinDrawable = ContextCompat.getDrawable(context, R.drawable.unpinned_cloud);
				break;
			default:
				// TODO default image
				break;
			}
//			pinDrawable = ContextCompat.getDrawable(context, R.drawable.unpinned);
		}
		return pinDrawable;
	}

	public boolean isPinned() {
		return isPinned;
	}

	public void setPinned(boolean isPinned) {
		this.isPinned = isPinned;
	}
	
	@Nullable
	public abstract Bitmap getIconImage();
	
	public abstract int getLocationStatus();
	
	public abstract int hashCode();

}