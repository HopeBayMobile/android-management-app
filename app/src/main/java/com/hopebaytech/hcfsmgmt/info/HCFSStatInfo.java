package com.hopebaytech.hcfsmgmt.info;

import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

public class HCFSStatInfo {

    public static final String STAT_DATA = "data";
    public static final String STAT_DATA_QUOTA = "quota";
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
    public static final String STAT_DATA_DATA_TRANSFER = "data_transfer";

    /**
     * unit: bytes
     */
    private long cloudTotal;

    /**
     * unit: bytes
     */
    private long cloudUsed;

    /**
     * unit: bytes
     */
    private long teraTotal;

    /**
     * unit: bytes
     */
    private long teraUsed;

    /**
     * unit: bytes
     */
    private long volUsed;

    /**
     * unit: bytes
     */
    private long cacheTotal;

    /**
     * unit: bytes
     */
    private long cacheDirtyUsed;

    /**
     * unit: bytes
     */
    private long cacheUsed;

    /**
     * unit: bytes
     */
    private long pinMax;

    /**
     * unit: bytes
     */
    private long pinTotal;

    /**
     * unit: bytes
     */
    private long xferUpload;

    /**
     * unit: bytes
     */
    private long xferDownload;

    /**
     * unit: boolean
     */
    private boolean cloudConn;

    /**
     * unit: int
     * 0 means no data transfer,
     * 1 means data transfer in progress,
     * 2 means data transfer in progress but slow
     */
    private int dataTransfer;

    public String getFormatVolUsed() {
        return UnitConverter.convertByteToProperUnit(volUsed);
    }

    public void setVolUsed(long volUsed) {
        this.volUsed = volUsed;
    }

    public String getFormatCacheUsed() {
        return UnitConverter.convertByteToProperUnit(cacheUsed);
    }

    public void setCacheUsed(long cacheUsed) {
        this.cacheUsed = cacheUsed;
    }

    public String getFormatCloudTotal() {
        return UnitConverter.convertByteToProperUnit(cloudTotal);
    }

    public void setCloudTotal(long cloudTotal) {
        this.cloudTotal = cloudTotal;
    }

    public String getFormatCloudUsed() {
        return UnitConverter.convertByteToProperUnit(cloudUsed);
    }

    public void setCloudUsed(long cloudUsed) {
        this.cloudUsed = cloudUsed;
    }

    /** Tera Storage usage */
    public String getFormatTeraTotal() {
        return UnitConverter.convertByteToProperUnit(teraTotal);
    }

    public void setTeraTotal(long teraTotal) {
        this.teraTotal = teraTotal;
    }

    public String getFormatTeraUsed() {
        return UnitConverter.convertByteToProperUnit(teraUsed);
    }

    public void setTeraUsed(long teraUsed) {
        this.teraUsed = teraUsed;
    }


    public String getFormatCacheTotal() {
        return UnitConverter.convertByteToProperUnit(cacheTotal);
    }

    public long getRawCacheTotal() {
        return cacheTotal;
    }

    public void setCacheTotal(long cacheTotal) {
        this.cacheTotal = cacheTotal;
    }

    public String getFormatCacheDirtyUsed() {
        return UnitConverter.convertByteToProperUnit(cacheDirtyUsed);
    }

    public long getRawCacheDirtyUsed() {
        return cacheDirtyUsed;
    }

    public void setCacheDirtyUsed(long cacheDirtyUsed) {
        this.cacheDirtyUsed = cacheDirtyUsed;
    }

    public String getFormatPinTotal() {
        return UnitConverter.convertByteToProperUnit(pinTotal);
    }

    public long getRawPinTotal() {
        return pinTotal;
    }

    public void setPinTotal(long pinTotal) {
        this.pinTotal = pinTotal;
    }

    public String getFormatPinMax() {
        return UnitConverter.convertByteToProperUnit(pinMax);
    }

    public void setPinMax(long pinMax) {
        this.pinMax = pinMax;
    }

    public String getFormatXferDownload() {
        return UnitConverter.convertByteToProperUnit(xferDownload);
    }

    public String getFormatXferUpload() {
        return UnitConverter.convertByteToProperUnit(xferUpload);
    }

    public void setXferUpload(long xferUpload) {
        this.xferUpload = xferUpload;
    }

    public String getFormatBwDownload() {
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
        float tmp = ((float) volUsed / cloudTotal * 100);
        if (tmp > 0 && tmp < 1) {
            percentage = 1;
        } else {
            percentage = (int) ((float) volUsed / cloudTotal * 100);
        }
        return percentage;
    }

    public int getTeraUsedPercentage() {
        int percentage;
        float tmp = ((float) teraUsed / teraTotal * 100);
        if (tmp > 0 && tmp < 1) {
            percentage = 1;
        } else {
            percentage = (int) ((float) teraUsed / teraTotal * 100);
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

    public int getDataTransfer() {
        return dataTransfer;
    }

    public void setDataTransfer(int dataTransfer) {
        this.dataTransfer = dataTransfer;
    }

    public long getCloudTotal() {
        return cloudTotal;
    }

    public long getCloudUsed() {
        return cloudUsed;
    }

    public long getTeraTotal() {
        return teraTotal;
    }

    public long getTeraUsed() {
        return teraUsed;
    }

    public long getVolUsed() {
        return volUsed;
    }

    public long getCacheTotal() {
        return cacheTotal;
    }

    public long getCacheDirtyUsed() {
        return cacheDirtyUsed;
    }

    public long getCacheUsed() {
        return cacheUsed;
    }

    public long getPinMax() {
        return pinMax;
    }

    public long getPinTotal() {
        return pinTotal;
    }

    public long getXferUpload() {
        return xferUpload;
    }

    public long getXferDownload() {
        return xferDownload;
    }
}
