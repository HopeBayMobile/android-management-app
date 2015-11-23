package com.hopebaytech.hcfsmgmt.info;

import java.util.Locale;

public class HCFSStatInfo {

	public static final String STAT_DATA = "data";
	public static final String STAT_DATA_CLOUD_TOTAL = "cloud_total";
	public static final String STAT_DATA_CLOUD_USED = "cloud_used";
	public static final String STAT_DATA_CACHE_TOTAL = "cache_total";
	public static final String STAT_DATA_CACHE_DIRTY = "cache_dirty";
	public static final String STAT_DATA_CACHE_CLEAN = "cache_clean";
	public static final String STAT_DATA_PIN_TOTAL = "pin_total";
	public static final String STAT_DATA_PIN_MAX = "pin_max";
	public static final String STAT_DATA_XFER_UP = "xfer_up";
	public static final String STAT_DATA_XFER_DOWN = "xfer_down";
	public static final String STAT_DATA_CLOUD_CONN = "cloud_conn";
	
	private long cloudTotal; // bytes
	private long cloudUsed; // bytes
	private long cacheTotal; // bytes
	private long cacheDirtyUsed; // bytes
	private long cacheCleanUsed; // bytes
	private long pinMax; // bytes
	private long pinTotal; // bytes
	private long xferUpload; // bytes
	private long xferDownload; // bytes
	private boolean cloudConn;

	public String getCacheUsed() {
		return convertByteToProperUnit(cacheDirtyUsed + cacheCleanUsed);
	}

	public String getCloudTotal() {
		return convertByteToProperUnit(cloudTotal);
	}

	public void setCloudTotal(long cloudTotal) {
		this.cloudTotal = cloudTotal;
	}

	public String getCloudUsed() {
		return convertByteToProperUnit(cloudUsed);
	}

	public void setCloudUsed(long cloudUsed) {
		this.cloudUsed = cloudUsed;
	}

	public String getCacheTotal() {
		return convertByteToProperUnit(cacheTotal);
	}

	public void setCacheTotal(long cacheTotal) {
		this.cacheTotal = cacheTotal;
	}

	public String getCacheDirtyUsed() {
		return convertByteToProperUnit(cacheDirtyUsed);
	}

	public void setCacheDirtyUsed(long cacheDirtyUsed) {
		this.cacheDirtyUsed = cacheDirtyUsed;
	}

	public String getCacheCleanUsed() {
		return convertByteToProperUnit(cacheCleanUsed);
	}

	public void setCacheCleanUsed(long cacheCleanUsed) {
		this.cacheCleanUsed = cacheCleanUsed;
	}

	public String getPinTotal() {
		return convertByteToProperUnit(pinTotal);
	}

	public void setPinTotal(long pinTotal) {
		this.pinTotal = pinTotal;
	}

	public String getPinMax() {
		return convertByteToProperUnit(pinMax);
	}

	public void setPinMax(long pinMax) {
		this.pinMax = pinMax;
	}

	public String getXferDownload() {
		return convertByteToProperUnit(xferDownload);
	}

	public String getXferUpload() {
		return convertByteToProperUnit(xferUpload);
	}

	public void setXferUpload(long xferUpload) {
		this.xferUpload = xferUpload;
	}

	public String getBwDownload() {
		return convertByteToProperUnit(xferDownload);
	}

	public void setXferDownload(long xferDownload) {
		this.xferDownload = xferDownload;
	}

	public boolean isCloudConn() {
		return cloudConn;
	}

	public void setCloudConn(boolean cloudConn) {
		this.cloudConn = cloudConn;
	}

	private String convertByteToProperUnit(long amount) {
		float result = amount;
		String[] unit = new String[] {
			"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"
		};
		int unitIndex = 0;
		while (true) {
			float tmp = result / 1000f;
			if ((long) tmp > 0) {
				result = tmp;
				unitIndex++;
			} else {
				break;
			}
		}
		
		if (result == (long) result) {
			return String.format(Locale.getDefault(), "%d" + unit[unitIndex], (long) result);
		} else {
			return String.format(Locale.getDefault(), "%.1f" + unit[unitIndex], result);
		}
	}

	public int getCloudUsedPercentage() {
		return (int) ((float) cloudUsed / cloudTotal * 100);
	}

	public int getCacheUsedPercentage() {
		return (int) ((float) (cacheDirtyUsed + cacheCleanUsed) / cloudTotal * 100);
	}

	public int getPinnedUsedPercentage() {
		return (int) ((float) pinTotal / pinMax * 100);
	}

	public int getDirtyPercentage() {
		return (int) ((float) cacheDirtyUsed / cloudTotal * 100);
	}

	public int getXterDownloadPercentage() {
		return (int) ((float) xferDownload / (xferUpload + xferDownload) * 100);
	}

}
