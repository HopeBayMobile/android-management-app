package com.hopebaytech.hcfsmgmt.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for getting the free space and total space of Tera. The total space and free space are
 * the sum of /system, /cache, /data and /data/data.
 *
 * @author Daniel
 *         Created by daniel on 2016/8/9.
 */
public class PhoneStorageUsage {

    private static final String CLASSNAME = PhoneStorageUsage.class.getSimpleName();

    private static final String system_path = "/system";
    private static final String cache_path = "/cache";
    private static final String data_path = "/data";
//    private static final String tera_path = "/data/data";
//    static List<String> paths = Arrays.asList(system_path, cache_path, data_path, tera_path);
    static List<String> paths = Arrays.asList(system_path, cache_path, data_path);

    public static long getFreeSpace() {
        File path;
        long freeSpace = 0;
        for (int i = 0; i < paths.size(); i++) {
            path = new File(paths.get(i));
            freeSpace += path.getFreeSpace();
        }
        return freeSpace;
    }

    public static long getTotalSpace() {
        File path;
        long totalSpace = 0;
        for (int i = 0; i < paths.size(); i++) {
            path = new File(paths.get(i));
            totalSpace += path.getTotalSpace();
        }
        return totalSpace;
    }

}
