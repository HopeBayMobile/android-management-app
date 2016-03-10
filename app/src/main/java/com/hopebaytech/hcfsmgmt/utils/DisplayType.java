package com.hopebaytech.hcfsmgmt.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

public class DisplayType {

	public static final int APP_SYSTEM = 0;
	public static final int APP_USER = 1;
	public static final int APP_ALL = 2;

	public static ArrayList<ItemInfo> getListOfInstalledApps(Context context, int flags) {
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		if (context != null) {
			Map<String, String> externalPkgNameMap = new HashMap<String, String>();
			String externalPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
			File externalAndroidFile = new File(externalPath);
			for (File type : externalAndroidFile.listFiles()) {
				File[] fileList = type.listFiles();
				for (File file : fileList) {
					String path = file.getAbsolutePath();
					String[] splitPath = path.split("/");
					String pkgName = splitPath[splitPath.length - 1];
					externalPkgNameMap.put(pkgName, path);
				}
			}

			PackageManager pm = context.getPackageManager();
			List<ApplicationInfo> applicationInfoList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
			for (ApplicationInfo applicationInfo : applicationInfoList) {
				boolean isSystemApp = HCFSMgmtUtils.isSystemPackage(applicationInfo);
				if (flags == APP_SYSTEM) {
					if (!isSystemApp) {
						continue;
					}
				} else if (flags == APP_USER) {
					if (isSystemApp) {
						continue;
					}
				}

				AppInfo appInfo = new AppInfo(context);
				appInfo.setUid(applicationInfo.uid);
				appInfo.setApplicationInfo(applicationInfo);
				appInfo.setItemName(applicationInfo.loadLabel(pm).toString());
				if (externalPkgNameMap.containsKey(applicationInfo.packageName)) {
					appInfo.setExternalDir(externalPkgNameMap.get(applicationInfo.packageName));
				}

                UidDAO uidDAO = new UidDAO(context);
                boolean isAppPinned = HCFSMgmtUtils.isAppPinned(appInfo, uidDAO);
				appInfo.setPinned(isAppPinned);
				items.add(appInfo);
			}
		}
		return items;
	}
	
	public static ArrayList<ItemInfo> getListOfDataType(Context context, DataTypeDAO dataTypeDAO) {
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		String[] dataTypeArray = context.getResources().getStringArray(R.array.file_management_list_data_types);
		for (int i = 0; i < dataTypeArray.length; i++) {
			DataTypeInfo dataTypeInfo = null;
			if (dataTypeArray[i].equals(context.getString(R.string.file_management_list_data_type_image))) {
				dataTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_IMAGE);
				dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_IMAGE);
				dataTypeInfo.setIconImage(R.drawable.ic_folder_photo);
			} else if (dataTypeArray[i].equals(context.getString(R.string.file_management_list_data_type_video))) {
				dataTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_VIDEO);
				dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_VIDEO);
				dataTypeInfo.setIconImage(R.drawable.ic_folder_video);
			} else if (dataTypeArray[i].equals(context.getString(R.string.file_management_list_data_type_audio))) {
				dataTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_AUDIO);
				dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_AUDIO);
				dataTypeInfo.setIconImage(R.drawable.ic_folder_music);
			}

			if (dataTypeInfo != null) {
				dataTypeInfo.setItemName(dataTypeArray[i]);
				items.add(dataTypeInfo);
			}
		}
		return items;
	}
	
	public static ArrayList<ItemInfo> getListOfFileDirs(Context context, File currentFile) {
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		if (isExternalStorageReadable()) {
			File[] fileList = currentFile.listFiles();
			Arrays.sort(fileList);
			for (int i = 0; i < fileList.length; i++) {
				FileDirInfo fileDirInfo = new FileDirInfo(context);
				File file = fileList[i];
				fileDirInfo.setItemName(file.getName());
				fileDirInfo.setCurrentFile(file);
				items.add(fileDirInfo);
			}
		}
		return items;
	}
	
	/** Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/** Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

}
