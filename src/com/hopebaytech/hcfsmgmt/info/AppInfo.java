package com.hopebaytech.hcfsmgmt.info;

import java.io.File;

import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

public class AppInfo extends ItemInfo {

	private long dbId;
	private int uid;
	private ApplicationInfo appInfo;
	private String packageName;
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

	public String getSourceDir() {
		/* Default sourceDir = /data/app/<package-name>-1/base.apk */
		String sourceDir = appInfo.sourceDir;
		int lastIndex = sourceDir.lastIndexOf("/");
		String sourceDirWithoutApkEnd = sourceDir.substring(0, lastIndex);
		return sourceDirWithoutApkEnd;
	}
	
	@Nullable
	public String getExternalDir() {
		String externalPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
		File externalAndroidFile = new File(externalPath);
		for (File type : externalAndroidFile.listFiles()) {
			File[] typeName = type.listFiles();
			for (File fileName : typeName) {
				if (fileName.getName().equals(getPackageName())) {
					return fileName.getAbsolutePath().replace(HCFSMgmtUtils.REPLACE_FILE_PATH_OLD, HCFSMgmtUtils.REPLACE_FILE_PATH_NEW);
				}
			}
		}
		return null;
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

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public String getPackageName() {
		if (packageName == null) {
			Log.i(HCFSMgmtUtils.TAG, "getPackageName - NULL" );
			packageName = appInfo.packageName;
		}
		return packageName;
	}

}