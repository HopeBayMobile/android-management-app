/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private static long getPathTotalSpace(String targetPath) {
        File path;
        path = new File(targetPath);
        return path.getTotalSpace();
    }

    private static long getPathFreeSpace(String targetPath) {
        File path;
        path = new File(targetPath);
        return path.getFreeSpace();
    }

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

    public static long getDataTotalSize() {
        return getPathTotalSpace(data_path);
    }

    public static long getDataFreeSize() {
        return getPathFreeSpace(data_path);
    }

    public static long getTotalSystemSize() {
        return getPathTotalSpace(system_path);
    }

    public static long getFreeSystemSize() {
        return getPathFreeSpace(system_path);
    }



}
