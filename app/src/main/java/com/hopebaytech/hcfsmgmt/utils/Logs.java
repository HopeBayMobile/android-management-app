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

import android.util.Log;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/24.
 */
public class Logs {

    public static final String TAG = "HopeBay";
    public static int LOG_LEVEL = Log.INFO;

    public static void init() {
        String buildType = SystemProperties.get("ro.build.type");
        if (!buildType.equals("user")) {
            LOG_LEVEL = Log.DEBUG;
        }
    }

    public static void i(String className, String funcName, String logMsg) {
        log(Log.INFO, className, funcName, logMsg);
    }

    public static void w(String className, String funcName, String logMsg) {
        log(Log.WARN, className, funcName, logMsg);
    }

    public static void e(String className, String funcName, String logMsg) {
        log(Log.ERROR, className, funcName, logMsg);
    }

    public static void d(String className, String funcName, String logMsg) {
        log(Log.DEBUG, className, funcName, logMsg);
    }

    public static void i(String className, String innerClassName, String funcName, String logMsg) {
        log(Log.INFO, className, innerClassName, funcName, logMsg);
    }

    public static void w(String className, String innerClassName, String funcName, String logMsg) {
        log(Log.WARN, className, innerClassName, funcName, logMsg);
    }

    public static void e(String className, String innerClassName, String funcName, String logMsg) {
        log(Log.ERROR, className, innerClassName, funcName, logMsg);
    }

    public static void d(String className, String innerClassName, String funcName, String logMsg) {
        log(Log.DEBUG, className, innerClassName, funcName, logMsg);
    }

    private static void log(int logLevel, String className, String funcName, String logMsg) {
        if (logLevel >= LOG_LEVEL) {
            if (logMsg == null) {
                logMsg = "";
            }
            if (logLevel == Log.DEBUG) {
                Log.d(TAG, className + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.INFO) {
                Log.i(TAG, className + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.WARN) {
                Log.w(TAG, className + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.ERROR) {
                Log.e(TAG, className + "(" + funcName + "): " + logMsg);
            }
        }
    }

    private static void log(int logLevel, String className, String innerClassName, String funcName, String logMsg) {
        if (logLevel >= LOG_LEVEL) {
            if (logMsg == null) {
                logMsg = "";
            }
            if (logLevel == Log.DEBUG) {
                Log.d(TAG, className + "->" + innerClassName + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.INFO) {
                Log.i(TAG, className + "->" + innerClassName + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.WARN) {
                Log.w(TAG, className + "->" + innerClassName + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.ERROR) {
                Log.e(TAG, className + "->" + innerClassName + "(" + funcName + "): " + logMsg);
            }
        }
    }

    private static String getClassName() {
        StackTraceElement stackTraceElements[] = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTraceElements[4];
        String className = stackTraceElement.getClassName();
        return className.subSequence(
                className.lastIndexOf(".") + 1, className.length()).toString();
    }

    private static String getMethodName() {
        StackTraceElement stackTraceElements[] = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTraceElements[4];
        return stackTraceElement.getMethodName();
    }

    public static void d(String logMsg) {
        Log.d(TAG, getClassName() + "(" + getMethodName() + "): " + logMsg);
    }

    public static void e(String logMsg) {
        Log.e(TAG, getClassName() + "(" + getMethodName() + "): " + logMsg);
    }

    public static void w(String logMsg) {
        Log.w(TAG, getClassName() + "(" + getMethodName() + "): " + logMsg);
    }
}
