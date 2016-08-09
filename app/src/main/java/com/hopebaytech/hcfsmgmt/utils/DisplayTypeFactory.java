package com.hopebaytech.hcfsmgmt.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

public class DisplayTypeFactory {

	private static final String CLASSNAME = DisplayTypeFactory.class.getSimpleName();

	public static final int APP_SYSTEM = 0;
	public static final int APP_USER = 1;
	public static final int APP_ALL = 2;

	public static ArrayList<ItemInfo> getListOfInstalledApps(Context context, int flags) {
		ArrayList<ItemInfo> itemInfoList = new ArrayList<>();
		if (context != null) {
			Map<String, ArrayList<String>> externalPkgNameMap = new HashMap<>();
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				String externalPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
				Logs.d(CLASSNAME, "getListOfInstalledApps", "externalPath=" + externalPath);
				File externalAndroidFile = new File(externalPath);
				if (externalAndroidFile.exists()) {
					for (File type : externalAndroidFile.listFiles()) {
						File[] fileList = type.listFiles();
						for (File file : fileList) {
							String path = file.getAbsolutePath();
							String[] splitPath = path.split("/");
							String pkgName = splitPath[splitPath.length - 1];

							ArrayList<String> externalPathList = externalPkgNameMap.get(pkgName);
							if (externalPathList == null) {
								externalPathList = new ArrayList<>();
							}
							externalPathList.add(path);
							externalPkgNameMap.put(pkgName, externalPathList);
//					externalPkgNameMap.put(pkgName, path);
						}
					}
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
				appInfo.setSystemApp(isSystemApp);
				appInfo.setApplicationInfo(applicationInfo);
				appInfo.setName(applicationInfo.loadLabel(pm).toString());
				if (externalPkgNameMap.containsKey(applicationInfo.packageName)) {
//					appInfo.setExternalDir(externalPkgNameMap.get(applicationInfo.packageName));
					appInfo.setExternalDirList(externalPkgNameMap.get(applicationInfo.packageName));
				}

//                UidDAO uidDAO = new UidDAO(mContext);
//                boolean isAppPinned = HCFSMgmtUtils.isAppPinned(appInfo, uidDAO);
//				appInfo.setPinned(isAppPinned);
				itemInfoList.add(appInfo);
			}
		}
		return itemInfoList;
	}

	public static ArrayList<ItemInfo> getListOfDataType(Context context) {
		DataTypeDAO dataTypeDAO = DataTypeDAO.getInstance(context);
		ArrayList<ItemInfo> itemInfoList = new ArrayList<>();
		String[] dataTypeArray = context.getResources().getStringArray(R.array.file_mgmt_list_data_types);
		for (int i = 0; i < dataTypeArray.length; i++) {
			DataTypeInfo dataTypeInfo = null;
			if (dataTypeArray[i].equals(context.getString(R.string.file_mgmt_list_data_type_image))) {
				dataTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_IMAGE);
				dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_IMAGE);
				dataTypeInfo.setIconImage(R.drawable.icon_folder_picture);
			} else if (dataTypeArray[i].equals(context.getString(R.string.file_mgmt_list_data_type_video))) {
				dataTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_VIDEO);
				dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_VIDEO);
				dataTypeInfo.setIconImage(R.drawable.icon_folder_video);
			} else if (dataTypeArray[i].equals(context.getString(R.string.file_mgmt_list_data_type_audio))) {
				dataTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_AUDIO);
				dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_AUDIO);
				dataTypeInfo.setIconImage(R.drawable.icon_folder_music);
			}

			if (dataTypeInfo != null) {
				dataTypeInfo.setName(dataTypeArray[i]);
				itemInfoList.add(dataTypeInfo);
			}
		}
		return itemInfoList;
	}
	
	public static ArrayList<ItemInfo> getListOfFileDirs(Context context, File currentFile) {
		ArrayList<ItemInfo> itemInfoList = new ArrayList<>();
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
			File[] fileList = currentFile.listFiles();
			Arrays.sort(fileList);
			for (int i = 0; i < fileList.length; i++) {
				File file = fileList[i];
				FileDirInfo fileDirInfo = new FileDirInfo(context);
				fileDirInfo.setName(file.getName());
				fileDirInfo.setDirectory(file.isDirectory());
				fileDirInfo.setFilePath(file.getAbsolutePath());
//				fileDirInfo.setCurrentFile(file);
				itemInfoList.add(fileDirInfo);
			}
		}
		return itemInfoList;
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
