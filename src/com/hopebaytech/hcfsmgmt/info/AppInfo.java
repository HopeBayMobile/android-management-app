package com.hopebaytech.hcfsmgmt.info;

import com.hopebaytech.hcfsmgmt.db.AppDAO;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

public class AppInfo extends ItemInfo {

	private long dbId;
	private int uid;
	private ApplicationInfo appInfo;
	private String packageName;
	private String sourceDir;
	private String nativeLibraryDir;
	private String[] sharedLibraryFiles;
	private int appSize;

	public AppInfo(Context context) {
		super(context);
	}

	public int getUid() {
		return uid;
	}

	public Drawable getIconImage() {
		return context.getPackageManager().getApplicationIcon(appInfo);
	}

	public void setApplicationInfo(ApplicationInfo appInfo) {
		this.appInfo = appInfo;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getPackageName() {
		return appInfo.packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getSourceDir() {
		/* Default sourceDir = /data/app/<package-name>-1/base.apk */
		String sourceDir = appInfo.sourceDir;
		int lastIndex = sourceDir.lastIndexOf("/");
		String sourceDirWithoutApkEnd = sourceDir.substring(0, lastIndex);
		return sourceDirWithoutApkEnd;
	}

	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

	public String getDataDir() {
		return appInfo.dataDir;
	}

	public String getNativeLibraryDir() {
		return appInfo.nativeLibraryDir;
	}

	@Nullable
	public String[] getSharedLibraryFiles() {
		return sharedLibraryFiles;
	}

	public void setSharedLibraryFiles(String[] sharedLibraryFiles) {
		this.sharedLibraryFiles = sharedLibraryFiles;
	}

	public int getAppSize() {
		return appSize;
	}

	public void setAppSize(int appSize) {
		this.appSize = appSize;
	}

	public long getDbId() {
		return dbId;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

}