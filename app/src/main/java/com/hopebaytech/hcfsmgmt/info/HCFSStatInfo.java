package com.hopebaytech.hcfsmgmt.info;

import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

public class HCFSStatInfo {

	public static final String STAT_DATA = "data";
	public static final String STAT_DATA_CLOUD_TOTAL = "cloud_total";
	public static final String STAT_DATA_CLOUD_USED = "cloud_used";
	public static final String STAT_DATA_VOL_USED = "vol_used";
	public static final String STAT_DATA_CACHE_TOTAL = "cache_total";
	public static final String STAT_DATA_CACHE_DIRTY = "cache_dirty";
	public static final String STAT_DATA_CACHE_USED = "cache_used";
	public static final String STAT_DATA_PIN_TOTAL = "pin_total";
	public static final String STAT_DATA_PIN_MAX = "pin_max";
	public static final String STAT_DATA_XFER_UP = "xfer_up";
	public static final String STAT_DATA_XFER_DOWN = "xfer_down";
	public static final String STAT_DATA_CLOUD_CONN = "cloud_conn";

	private long cloudTotal; /* in bytes */
	private long cloudUsed; /* in bytes */
	private long volUsed; /* in bytes */
	private long cacheTotal; /* in bytes */
	private long cacheDirtyUsed; /* in bytes */
	private long cacheUsed; /* in bytes */
	private long pinMax; /* in bytes */
	private long pinTotal; /* in bytes */
	private long xferUpload; /* in bytes */
	private long xferDownload; /* in bytes */
	private boolean cloudConn;
	
	public String getVolUsed() {
		return UnitConverter.convertByteToProperUnit(volUsed);
	}

	public void setVolUsed(long volUsed) {
		this.volUsed = volUsed;
	}

	public String getCacheUsed() {
		return UnitConverter.convertByteToProperUnit(cacheUsed);
	}

	public void setCacheUsed(long cacheUsed) {
		this.cacheUsed = cacheUsed;
	}

	public String getCloudTotal() {
		return UnitConverter.convertByteToProperUnit(cloudTotal);
	}

	public void setCloudTotal(long cloudTotal) {
		this.cloudTotal = cloudTotal;
	}

	public String getCloudUsed() {
		return UnitConverter.convertByteToProperUnit(cloudUsed);
	}

	public void setCloudUsed(long cloudUsed) {
		this.cloudUsed = cloudUsed;
	}

	public String getCacheTotal() {
		return UnitConverter.convertByteToProperUnit(cacheTotal);
	}
	
	public long getRawCacheTotal() {
		return cacheTotal;
	}

	public void setCacheTotal(long cacheTotal) {
		this.cacheTotal = cacheTotal;
	}

	public String getCacheDirtyUsed() {
		return UnitConverter.convertByteToProperUnit(cacheDirtyUsed);
	}
	
	public long getRawCacheDirtyUsed() {
		return cacheDirtyUsed;
	}

	public void setCacheDirtyUsed(long cacheDirtyUsed) {
		this.cacheDirtyUsed = cacheDirtyUsed;
	}

	public String getPinTotal() {
		return UnitConverter.convertByteToProperUnit(pinTotal);
	}
	
	public long getRawPinTotal() {
		return pinTotal;
	}

	public void setPinTotal(long pinTotal) {
		this.pinTotal = pinTotal;
	}

	public String getPinMax() {
		return UnitConverter.convertByteToProperUnit(pinMax);
	}

	public void setPinMax(long pinMax) {
		this.pinMax = pinMax;
	}

	public String getXferDownload() {
		return UnitConverter.convertByteToProperUnit(xferDownload);
	}

	public String getXferUpload() {
		return UnitConverter.convertByteToProperUnit(xferUpload);
	}

	public void setXferUpload(long xferUpload) {
		this.xferUpload = xferUpload;
	}

	public String getBwDownload() {
		return UnitConverter.convertByteToProperUnit(xferDownload);
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
                                                                                                                                                                      
	public int getCloudUsedPercentage() {
		int percentage;
		float tmp = ((float) cloudUsed / cloudTotal * 100);
		if (tmp > 0 && tmp < 1) {
			percentage = 1;
		} else {
			percentage = (int) ((float) cloudUsed / cloudTotal * 100);
		}
		return percentage;
	}

	public int getCacheUsedPercentage() {
		int percentage;
		float tmp = ((float) cacheUsed / cloudTotal * 100);
		if (tmp > 0 && tmp < 1) {
			percentage = 1;
		} else {
			percentage = (int) ((float) cacheUsed / cloudTotal * 100);
		}
		return percentage;
	}

	public int getPinnedUsedPercentage() {
		int percentage;
		float tmp = ((float) pinTotal / pinMax * 100);
		if (tmp > 0 && tmp < 1) {
			percentage = 1;
		} else {
			percentage = (int) ((float) pinTotal / pinMax * 100);
		}
		return percentage;
	}

	public int getDirtyPercentage() {
		int percentage;
		float tmp = ((float) cacheDirtyUsed / cacheTotal * 100);
		if (tmp > 0 && tmp < 1) { 
			percentage = 1;
		} else {
			percentage = (int) ((float) cacheDirtyUsed / cacheTotal * 100);
		}
		return percentage;
	}

	public int getXterDownloadPercentage() {
		int percentage;
		float tmp = ((float) xferDownload / (xferUpload + xferDownload) * 100);
		if (tmp > 0 && tmp < 1) {
			percentage = 1;
		} else {
			percentage = (int) ((float) xferDownload / (xferUpload + xferDownload) * 100);
		}
		return percentage;
	}

}