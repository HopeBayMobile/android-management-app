package com.hopebaytech.hcfsmgmt.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.AppFileFragment;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayTypeFactory {

    private static final String CLASSNAME = DisplayTypeFactory.class.getSimpleName();

    public static final int APP_SYSTEM = 0;
    public static final int APP_USER = 1;
    public static final int APP_ALL = 2;


    public static ArrayList<ItemInfo> getListOfInstalledApps(Context context, int flags) {
        return getListOfInstalledApps(context, flags, false /* filter by pin */);
    }

    public static ArrayList<ItemInfo> getListOfInstalledApps(Context context, int flags, boolean filterByPin) {
        ArrayList<ItemInfo> itemInfoList = new ArrayList<>();
        if (context == null) {
            return itemInfoList;
        }

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
                    }
                }
            }
        }

        UidDAO uidDAO = UidDAO.getInstance(context);
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList) {
            boolean isSystemApp = HCFSMgmtUtils.isSystemPackage(packageInfo.applicationInfo);
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
            appInfo.setUid(packageInfo.applicationInfo.uid);
            appInfo.setSystemApp(isSystemApp);
            appInfo.setPackageInfo(packageInfo);
            appInfo.setApplicationInfo(packageInfo.applicationInfo);
            appInfo.setName(packageInfo.applicationInfo.loadLabel(pm).toString());
            if (externalPkgNameMap.containsKey(packageInfo.packageName)) {
                appInfo.setExternalDirList(externalPkgNameMap.get(packageInfo.packageName));
            }

            if (filterByPin) {
                UidInfo uidInfo = uidDAO.get(appInfo.getPackageName());
                if (uidInfo == null || !uidInfo.isPinned()) {
                    continue;
                }
            }
            itemInfoList.add(appInfo);
        }

        return itemInfoList;
    }

    public static ArrayList<ItemInfo> getListOfDataType(Context context) {
        DataTypeDAO dataTypeDAO = DataTypeDAO.getInstance(context);
        ArrayList<ItemInfo> itemInfoList = new ArrayList<>();
        String[] dataTypeArray = context.getResources().getStringArray(R.array.file_mgmt_list_data_types);
        for (int i = 0; i < dataTypeArray.length; i++) {
            DataTypeInfo dataTypeInfo = null;
            if (dataTypeArray[i].equals(context.getString(R.string.app_file_list_data_type_image))) {
                dataTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_IMAGE);
                dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_IMAGE);
                dataTypeInfo.setIconImage(R.drawable.icon_folder_picture);
            } else if (dataTypeArray[i].equals(context.getString(R.string.app_file_list_data_type_video))) {
                dataTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_VIDEO);
                dataTypeInfo.setDataType(DataTypeDAO.DATA_TYPE_VIDEO);
                dataTypeInfo.setIconImage(R.drawable.icon_folder_video);
            } else if (dataTypeArray[i].equals(context.getString(R.string.app_file_list_data_type_audio))) {
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

    public static ArrayList<ItemInfo> getListOfFileDirs(Context context, File currentFile, boolean filterByPin) {
        ArrayList<ItemInfo> itemInfoList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            File[] fileList = currentFile.listFiles();
            for (File file : fileList) {
                FileInfo fileInfo = new FileInfo(context);
                fileInfo.setName(file.getName());
                fileInfo.setDirectory(file.isDirectory());
                fileInfo.setFilePath(file.getAbsolutePath());
                fileInfo.setLastModified(file.lastModified());
                fileInfo.setSize(file.length());

                if (filterByPin) {
                    boolean isPinned = HCFSMgmtUtils.isPathPinned(fileInfo.getFilePath());
                    if (!isPinned) {
                        continue;
                    }
                }
                itemInfoList.add(fileInfo);
            }
        }
        return itemInfoList;

    }

    public static ArrayList<ItemInfo> getListOfFileDirs(Context context, File currentFile) {
        return getListOfFileDirs(context, currentFile, false /* filer by pin */);
    }

    /**
     * Checks if external storage is available for read and write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * @param itemList the {@link AppInfo} or {@link FileInfo} item list
     * @param sortType the sort type to apply to the input item list
     * @see com.hopebaytech.hcfsmgmt.fragment.AppFileFragment.SortType#BY_NAME
     * @see com.hopebaytech.hcfsmgmt.fragment.AppFileFragment.SortType#BY_INSTALLED_TIME
     * @see com.hopebaytech.hcfsmgmt.fragment.AppFileFragment.SortType#BY_MODIFIED_TIME
     * @see com.hopebaytech.hcfsmgmt.fragment.AppFileFragment.SortType#BY_SIZE
     */
    public static void sort(ArrayList<ItemInfo> itemList, int sortType) {
        if (itemList == null || itemList.size() == 0) {
            return;
        }

        ItemInfo checkItem = itemList.get(0);
        if (checkItem instanceof AppInfo) {
            switch (sortType) {
                case AppFileFragment.SortType.BY_NAME:
                    // Sorts the specified list by name in ascending order
                    Collections.sort(itemList, new Comparator<ItemInfo>() {
                        @Override
                        public int compare(ItemInfo lhs, ItemInfo rhs) {
                            return lhs.getName().compareTo(rhs.getName());
                        }
                    });
                    break;
                case AppFileFragment.SortType.BY_INSTALLED_TIME:
                    // Sorts the specified list by installed time in descending order
                    Collections.sort(itemList, new Comparator<ItemInfo>() {
                        @Override
                        public int compare(ItemInfo lhs, ItemInfo rhs) {
                            long lhsInstallTime = ((AppInfo) lhs).getPackageInfo().firstInstallTime;
                            long rhsInstallTime = ((AppInfo) rhs).getPackageInfo().firstInstallTime;
                            long diff = rhsInstallTime - lhsInstallTime;
                            if (diff > 0) {
                                return Integer.MAX_VALUE;
                            }
                            if (diff < 0) {
                                return Integer.MIN_VALUE;
                            }
                            return 0;
                        }
                    });
                    break;
            }
        } else if (checkItem instanceof FileInfo) {
            switch (sortType) {
                case AppFileFragment.SortType.BY_NAME:
                    // Sorts the specified list by name in ascending order
                    Collections.sort(itemList, new Comparator<ItemInfo>() {
                        @Override
                        public int compare(ItemInfo lhs, ItemInfo rhs) {
                            return lhs.getName().compareTo(rhs.getName());
                        }
                    });
                    break;
                case AppFileFragment.SortType.BY_MODIFIED_TIME:
                    // Sorts the specified list by last modified time in descending order
                    Collections.sort(itemList, new Comparator<ItemInfo>() {
                        @Override
                        public int compare(ItemInfo lhs, ItemInfo rhs) {
                            long lhsLastModified = ((FileInfo) lhs).getLastModified();
                            long rhsLastModified = ((FileInfo) rhs).getLastModified();
                            long diff = rhsLastModified - lhsLastModified;
                            if (diff > 0) {
                                return Integer.MAX_VALUE;
                            }
                            if (diff < 0) {
                                return Integer.MIN_VALUE;
                            }
                            return 0;
                        }
                    });
                    break;
                case AppFileFragment.SortType.BY_SIZE:
                    // Sorts the specified list by size in descending order
                    Collections.sort(itemList, new Comparator<ItemInfo>() {
                        @Override
                        public int compare(ItemInfo lhs, ItemInfo rhs) {
                            long lhsFileSize = ((FileInfo) lhs).getSize();
                            long rhsFileSize = ((FileInfo) rhs).getSize();
                            long diff = rhsFileSize - lhsFileSize;
                            if (diff > 0) {
                                return Integer.MAX_VALUE;
                            }
                            if (diff < 0) {
                                return Integer.MIN_VALUE;
                            }
                            return 0;
                        }
                    });
                    break;
            }
        }
    }

}
