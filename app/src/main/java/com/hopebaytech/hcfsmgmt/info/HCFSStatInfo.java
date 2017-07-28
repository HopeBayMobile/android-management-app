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
    public static final String STAT_META_SIZE_TOTAL = "max_meta_size";
    public static final String STAT_META_SIZE_USED = "meta_used_size";
    public static final int CLOUD_DISCONN = 0;
    public static final int CLOUD_CONN = 1;
    public static final int CLOUD_CONN_RETRY = 2;

    /**
     * unit: bytes
     */
    private long physicalTotal;
    private long physicalUsed;
    private long systemTotal;
    private long systemUsed;
    private long cloudTotal;
    private long cloudUsed;
    private long teraTotal;
    private long teraUsed;
    private long volUsed;
    private long cacheTotal;
    private long cacheDirtyUsed;
    private long cacheUsed;
    private long pinMax;
    private long pinTotal;
    private long xferUpload;
    private long xferDownload;
    private long metaTotal;
    private long metaUsed;

    /**
     * unit: bytes
     */
    private int cloudConn;

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

    public String getFormatPhysicalTotal() {
        return UnitConverter.convertByteToProperUnit(physicalTotal); }

    public void setPhycialTotal(long physicalTotal) { this.physicalTotal = physicalTotal; }

    public String getFormatPhysicalUsed() {
        return UnitConverter.convertByteToProperUnit(physicalUsed); }

    public void setPhycialUsed(long phycialUsed) { this.physicalUsed = phycialUsed; }

    public String getFormatSystemTotal() {
        return UnitConverter.convertByteToProperUnit(systemTotal); }

    public void setSystemTotal(long systemTotal) { this.systemTotal = systemTotal; }

    public String getFormatSystemUsed() {
        return UnitConverter.convertByteToProperUnit(systemUsed); }

    public void setSystemUsed(long systemUsed) { this.systemUsed = systemUsed; }

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

    public void setCacheTotal(long cacheTotal) {
        this.cacheTotal = cacheTotal - this.metaTotal;
    }

    public String getFormatCacheUsed() {
        return UnitConverter.convertByteToProperUnit(cacheUsed);
    }

    public void setCacheUsed(long cacheUsed) {
        this.cacheUsed = cacheUsed - this.metaUsed;
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
        if (cloudConn == CLOUD_CONN)
            return true;
        else
            return false;
    }

    public boolean isRetryConn() {
        if (cloudConn == CLOUD_CONN_RETRY)
            return true;
        else
            return false;
    }

    public void setCloudConn(int cloudConn) {
        this.cloudConn = cloudConn;
    }


    public int getCloudUsedPercentage() {
        return UnitConverter.calculateUsagePercentage(cloudUsed, cloudTotal);
    }

    public int getPhysicalUsedPercentage() {
        return UnitConverter.calculateUsagePercentage(physicalUsed, physicalTotal);
    }

    public int getSystemUsedPercentage(){
        return UnitConverter.calculateUsagePercentage(systemUsed, systemTotal);
    }

    public int getTeraUsedPercentage() {
        return UnitConverter.calculateUsagePercentage(teraUsed, teraTotal);
    }

    public int getCacheUsedPercentage() {
        return UnitConverter.calculateUsagePercentage(cacheUsed, cacheTotal);
    }

    public int getPinnedUsedPercentage() {
        return UnitConverter.calculateUsagePercentage(pinTotal, pinMax);
    }

    public int getDirtyPercentage() {
        return UnitConverter.calculateUsagePercentage(cacheDirtyUsed, cacheTotal);
    }

    public int getXterDownloadPercentage() {
        float total = xferUpload + xferDownload;
        return UnitConverter.calculateUsagePercentage(xferDownload, total);
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

    /* Set/Get meta total size */
    public void setMetaTotal(long metaTotal) { this.metaTotal = metaTotal; }
    public long getMetaTotal() { return this.metaTotal; }

    /* Set/Get meta used size */
    public void setMetaUsed(long metaUsed) { this.metaUsed = metaUsed; }
    public long getMetaUsed() { return this.metaUsed; }
}
