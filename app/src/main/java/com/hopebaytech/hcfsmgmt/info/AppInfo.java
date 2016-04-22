package com.hopebaytech.hcfsmgmt.info;

import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AppInfo extends ItemInfo implements Cloneable {

	private long dbId;
	private int uid;
	private ApplicationInfo applicationInfo;
	private String packageName;
//	private String externalDir;
	private ArrayList<String> externalDirList;
	private String[] sharedLibraryFiles;
	private int appSize;
	private Context context;
	private int status = -1;

	public AppInfo(Context context) {
		super(context);
		this.context = context;
	}

//	public AppInfo(AppInfo applicationInfo) {
//		super(applicationInfo.context);
//		this.dbId = applicationInfo.dbId;
//		this.uid = applicationInfo.uid;
//		this.applicationInfo = applicationInfo.applicationInfo;
//		this.packageName = applicationInfo.packageName;
//		this.externalDir = applicationInfo.externalDir;
//		this.sharedLibraryFiles = applicationInfo.sharedLibraryFiles;
//		this.appSize = applicationInfo.appSize;
//		this.context = applicationInfo.context;
//		this.status = applicationInfo.status;
//	}

	public int getUid() {
		return uid;
	}

	public Bitmap getIconImage() {
		Drawable drawable = context.getPackageManager().getApplicationIcon(applicationInfo);
		if (!(drawable instanceof VectorDrawable)) {
			return ((BitmapDrawable) drawable).getBitmap();
		}
		return null;
	}

	public void setApplicationInfo(ApplicationInfo appInfo) {
		this.applicationInfo = appInfo;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getSourceDir() {
		/** Default sourceDir = /data/app/<package-name>-1/base.apk */
		String sourceDir = applicationInfo.sourceDir;
		int lastIndex = sourceDir.lastIndexOf("/");
		String sourceDirWithoutApkSuffix = sourceDir.substring(0, lastIndex);
		return sourceDirWithoutApkSuffix;
	}

	@Nullable
	public ArrayList<String> getExternalDirList() {
		// Log.w(HCFSMgmtUtils.TAG, "getExternalDirList");
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
		return externalDirList;
	}

	// public void findToSetExternalDir() {
	// String externalPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
	// File externalAndroidFile = new File(externalPath);
	// for (File type : externalAndroidFile.listFiles()) {
	// File[] typeName = type.listFiles();
	// for (File fileName : typeName) {
	// if (fileName.getName().equals(getPackageName())) {
	//// this.externalDir = fileName.getAbsolutePath().replace(HCFSMgmtUtils.REPLACE_FILE_PATH_OLD, HCFSMgmtUtils.REPLACE_FILE_PATH_NEW);
	// this.externalDir = fileName.getAbsolutePath();
	// break;
	// }
	// }
	// }
	// }

//	public void setExternalDir(String externalDir) {
//		this.externalDir = externalDir;
//	}

	public void setExternalDirList(ArrayList<String> externalDirList) {
		this.externalDirList = externalDirList;
	}

	public String getDataDir() {
		return applicationInfo.dataDir;
	}

	public String getNativeLibraryDir() {
		return applicationInfo.nativeLibraryDir;
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
			packageName = applicationInfo.packageName;
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
		if (getExternalDirList() != null) {

//			int externalStatus = HCFSMgmtUtils.getDirStatus(getExternalDirList());
			int externalLocalCounter = 0;
			int externalHybridCounter = 0;
			int externalCloudCounter = 0;
			ArrayList<String> externalDirList = getExternalDirList();
			for (String externalDir: externalDirList) {
				if (HCFSMgmtUtils.getDirStatus(externalDir) == LocationStatus.LOCAL) {
					externalLocalCounter++;
				} else if (HCFSMgmtUtils.getDirStatus(externalDir) == LocationStatus.HYBRID) {
					externalHybridCounter++;
				} else if (HCFSMgmtUtils.getDirStatus(externalDir) == LocationStatus.CLOUD) {
					externalCloudCounter++;
				}
			}

			int externalStatus;
			if (externalHybridCounter == 0 && externalCloudCounter == 0) {
				externalStatus = LocationStatus.LOCAL;
			} else if (externalHybridCounter == 0 && externalLocalCounter == 0) {
				externalStatus = LocationStatus.CLOUD;
			} else {
				externalStatus = LocationStatus.HYBRID;
			}

			if (srcStatus == LocationStatus.LOCAL && dataStatus == LocationStatus.LOCAL && externalStatus == LocationStatus.LOCAL) {
				status = LocationStatus.LOCAL;
			} else if (srcStatus == LocationStatus.CLOUD && dataStatus == LocationStatus.CLOUD && externalStatus == LocationStatus.CLOUD) {
				status = LocationStatus.CLOUD;
			} else {
				status = LocationStatus.HYBRID;
			}
		} else {
			if (srcStatus == LocationStatus.LOCAL && dataStatus == LocationStatus.LOCAL) {
				status = LocationStatus.LOCAL;
			} else if (srcStatus == LocationStatus.CLOUD && dataStatus == LocationStatus.CLOUD) {
				status = LocationStatus.CLOUD;
			} else {
				status = LocationStatus.HYBRID;
			}
		}
		return status;
	}

	@Override
	public Drawable getPinUnpinImage() {
		return HCFSMgmtUtils.getPinUnpinImage(context, isPinned());
	}

	@Override
	public int hashCode() {
		return getSourceDir().hashCode();
	}

    public ApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }
}