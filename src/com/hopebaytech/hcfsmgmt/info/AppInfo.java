package com.hopebaytech.hcfsmgmt.info;

import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

public class AppInfo extends ItemInfo {

	private long dbId;
	private int uid;
	private ApplicationInfo appInfo;
	private String packageName;
	private String externalDir;
	private String[] sharedLibraryFiles;
	private int appSize;
	private Context context;
	private int status = -1;

	public AppInfo(Context context) {
		super(context);
		this.context = context;
	}

	public int getUid() {
		return uid;
	}

	public Bitmap getIconImage() {
		return ((BitmapDrawable) context.getPackageManager().getApplicationIcon(appInfo)).getBitmap();
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
		// Log.w(HCFSMgmtUtils.TAG, "getExternalDir");
		// String externalPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
		// File externalAndroidFile = new File(externalPath);
		// for (File type : externalAndroidFile.listFiles()) {
		// File[] typeName = type.listFiles();
		// for (File fileName : typeName) {
		// Log.w(HCFSMgmtUtils.TAG, "fileName.getName(): " + fileName.getName());
		// if (fileName.getName().equals(getPackageName())) {
		// return fileName.getAbsolutePath().replace(HCFSMgmtUtils.REPLACE_FILE_PATH_OLD, HCFSMgmtUtils.REPLACE_FILE_PATH_NEW);
		// }
		// }
		// }
		// return null;
		return externalDir;
	}

//	public void findToSetExternalDir() {
//		String externalPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
//		File externalAndroidFile = new File(externalPath);
//		for (File type : externalAndroidFile.listFiles()) {
//			File[] typeName = type.listFiles();
//			for (File fileName : typeName) {
//				if (fileName.getName().equals(getPackageName())) {
////					this.externalDir = fileName.getAbsolutePath().replace(HCFSMgmtUtils.REPLACE_FILE_PATH_OLD, HCFSMgmtUtils.REPLACE_FILE_PATH_NEW);
//					this.externalDir = fileName.getAbsolutePath();
//					break;
//				}
//			}
//		}
//	}

	public void setExternalDir(String externalDir) {
		this.externalDir = externalDir;
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
			packageName = appInfo.packageName;
		}
		return packageName;
	}
	
	@Override
	public int getLocationStatus() {
		return getAppStatus();
	}

	public int getAppStatus() {
		int srcStatus = HCFSMgmtUtils.getDirStatus(getSourceDir());
		int dataStatus = HCFSMgmtUtils.getDirStatus(getDataDir());
		if (getExternalDir() != null) {
			int exeternalStatus = HCFSMgmtUtils.getDirStatus(getExternalDir());
			if (srcStatus == FileStatus.LOCAL && dataStatus == FileStatus.LOCAL && exeternalStatus == FileStatus.LOCAL) {
				status = FileStatus.LOCAL;
			} else if (srcStatus == FileStatus.CLOUD && dataStatus == FileStatus.CLOUD && exeternalStatus == FileStatus.CLOUD) {
				status = FileStatus.CLOUD;
			} else {
				status = FileStatus.HYBRID;
			}
		} else {
			if (srcStatus == FileStatus.LOCAL && dataStatus == FileStatus.LOCAL) {
				status = FileStatus.LOCAL;
			} else if (srcStatus == FileStatus.CLOUD && dataStatus == FileStatus.CLOUD) {
				status = FileStatus.CLOUD;
			} else {
				status = FileStatus.HYBRID;
			}
		}
		return status;
	}

	@Override
	public Drawable getPinUnpinImage() {
		return HCFSMgmtUtils.getPinUnpinImage(context, isPinned(), getAppStatus());
	}

	@Override
	public int hashCode() {
		return getSourceDir().hashCode();
	}

}