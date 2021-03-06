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
package com.hopebaytech.hcfsmgmt.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteTableLockedException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UidDAO {

    private final String CLASSNAME = getClass().getSimpleName();
    public static final String TABLE_NAME = "uid";
    public static final String KEY_ID = "_id";
    public static final String PIN_STATUS_COLUMN = "pin_status"; // 0 unpin, 1 normal pin, 2 priority pin
    public static final String SYSTEM_APP_COLUMN = "system_app"; // 0 user app, 1 system app
    public static final String ENABLED_COLUMN = "enabled"; // 0 disabled, 1 enabled
    public static final String UID_COLUMN = "uid";
    public static final String PACKAGE_NAME_COLUMN = "package_name";
    public static final String EXTERNAL_DIR_COLUMN = "external_dir";
    public static final String BOOST_STATUS_COLUMN = "boost_status";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PIN_STATUS_COLUMN + " INTEGER NOT NULL, " +
                    SYSTEM_APP_COLUMN + " INTEGER NOT NULL, " +
                    ENABLED_COLUMN + " INTEGER NOT NULL, " +
                    BOOST_STATUS_COLUMN + " INTEGER NOT NULL, " +
                    UID_COLUMN + " TEXT NOT NULL, " +
                    PACKAGE_NAME_COLUMN + " TEXT NOT NULL, " +
                    EXTERNAL_DIR_COLUMN + " TEXT)";

    private static UidDAO sUidDAO;
    private static SQLiteDatabase sSqLiteDatabase;

    private UidDAO() {
    }

    public static UidDAO getInstance(Context context) {
        if (sUidDAO == null) {
            synchronized (UidDAO.class) {
                if (sUidDAO == null) {
                    sUidDAO = new UidDAO();
                }
            }
        }
        sSqLiteDatabase = Uid2PkgDBHelper.getDataBase(context);
        return sUidDAO;
    }

    public boolean insert(UidInfo uidInfo) {
        ContentValues cv = new ContentValues();
        int pinStatus = 0;
        if (uidInfo.isPinned()) {
            if (uidInfo.isSystemApp()) {
                pinStatus = 2;
            } else {
                pinStatus = 1;
            }
        }
        cv.put(PIN_STATUS_COLUMN, pinStatus);
        cv.put(SYSTEM_APP_COLUMN, uidInfo.isSystemApp() ? 1 : 0);
        cv.put(ENABLED_COLUMN, uidInfo.isEnabled() ? 1 : 0);
        cv.put(BOOST_STATUS_COLUMN, uidInfo.getBoostStatus());
        cv.put(UID_COLUMN, uidInfo.getUid());
        cv.put(PACKAGE_NAME_COLUMN, uidInfo.getPackageName());

        List<String> externalDirList = uidInfo.getExternalDir();
        if (externalDirList != null) {
            StringBuilder sb = new StringBuilder();
            String comma = ",";
            for (int i = 0; i < externalDirList.size(); i++) {
                sb.append(externalDirList.get(i));
                if (i != externalDirList.size() - 1) {
                    sb.append(comma);
                }
            }
            cv.put(EXTERNAL_DIR_COLUMN, sb.toString());
        }

        boolean isSuccess = false;
        boolean isDatabaseLocked;
        do {
            try {
                isSuccess = sSqLiteDatabase.insert(TABLE_NAME, null, cv) > -1;
                String logMsg = "isPinned=" + uidInfo.isPinned() +
                        ", pinStatus=" + pinStatus +
                        ", isSystemApp=" + uidInfo.isSystemApp() +
                        ", boostStatus=" + uidInfo.getBoostStatus() +
                        ", uid=" + uidInfo.getUid() +
                        ", packageName=" + uidInfo.getPackageName() +
                        ", externalDir=" + uidInfo.getExternalDir();
                if (isSuccess) {
                    Logs.d(CLASSNAME, "insert", logMsg);
                } else {
                    Logs.e(CLASSNAME, "insert", logMsg);
                }
                isDatabaseLocked = false;
            } catch (SQLiteDatabaseLockedException e) {
                Logs.w(CLASSNAME, "insert", Log.getStackTraceString(e));
                isDatabaseLocked = true;
            }
            try {
                Thread.sleep(Interval.DATABASE_OPERATION_RETRY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isDatabaseLocked);
        return isSuccess;
    }

    public boolean update(String packageName, ContentValues cv) {
        boolean isSuccess = false;
        boolean isDatabaseLocked;
        String where = PACKAGE_NAME_COLUMN + "='" + packageName + "'";
        do {
            try {
                isSuccess = sSqLiteDatabase.update(TABLE_NAME, cv, where, null) > 0;
                if (isSuccess) {
                    Logs.d(CLASSNAME, "update", "packageName=" + packageName + ", cv: " + cv.toString());
                } else {
                    Logs.e(CLASSNAME, "update", "packageName=" + packageName + ", cv: " + cv.toString());
                }
                isDatabaseLocked = false;
            } catch (SQLiteDatabaseLockedException e) {
                Logs.w(CLASSNAME, "update", Log.getStackTraceString(e));
                isDatabaseLocked = true;
            }
            try {
                Thread.sleep(Interval.DATABASE_OPERATION_RETRY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isDatabaseLocked);
        return isSuccess;
    }

    public boolean update(UidInfo uidInfo) {
        if (uidInfo == null) {
            return false;
        }

        int pinStatus = 0;
        if (uidInfo.isPinned()) {
            if (uidInfo.isSystemApp()) {
                pinStatus = 2;
            } else {
                pinStatus = 1;
            }
        }

        ContentValues cv = new ContentValues();
        cv.put(PIN_STATUS_COLUMN, pinStatus);
        cv.put(SYSTEM_APP_COLUMN, uidInfo.isSystemApp() ? 1 : 0);
        cv.put(ENABLED_COLUMN, uidInfo.isEnabled() ? 1 : 0);
        cv.put(UID_COLUMN, uidInfo.getUid());
        cv.put(PACKAGE_NAME_COLUMN, uidInfo.getPackageName());
        cv.put(EXTERNAL_DIR_COLUMN, convertListToString(uidInfo.getExternalDir()));
        cv.put(BOOST_STATUS_COLUMN, uidInfo.getBoostStatus());

        String where = KEY_ID + "='" + uidInfo.getId() + "'";
        boolean isSuccess = false;
        boolean isDatabaseLocked;
        do {
            try {
                isSuccess = sSqLiteDatabase.update(TABLE_NAME, cv, where, null) > 0;
                if (isSuccess) {
                    Logs.d(CLASSNAME, "update", cv.toString());
                } else {
                    Logs.e(CLASSNAME, "update", cv.toString());
                }
                isDatabaseLocked = false;
            } catch (SQLiteDatabaseLockedException e) {
                Logs.w(CLASSNAME, "update", Log.getStackTraceString(e));
                isDatabaseLocked = true;
            }
            try {
                Thread.sleep(Interval.DATABASE_OPERATION_RETRY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isDatabaseLocked);
        return isSuccess;
    }

    private String convertListToString(List<String> externalDirList) {
        if (externalDirList == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String comma = ",";
        for (int i = 0; i < externalDirList.size(); i++) {
            sb.append(externalDirList.get(i));
            if (i != externalDirList.size() - 1) {
                sb.append(comma);
            }
        }
        return sb.toString();
    }

    /**
     * Update specific column
     */
    public boolean update(UidInfo uidInfo, String column) {
        String logMsg = "uidInfo=" + uidInfo + ", column=" + column;
        Logs.d(CLASSNAME, "update", logMsg);
        if (uidInfo.getPackageName() == null) {
            Logs.e(CLASSNAME, "update", "msg=package name cannot be null");
            return false;
        } else {
            ContentValues contentValues = new ContentValues();
            if (column.equals(PIN_STATUS_COLUMN)) {
                int pinStatus = 0;
                if (uidInfo.isPinned()) {
                    if (uidInfo.isSystemApp()) {
                        pinStatus = 2;
                    } else {
                        pinStatus = 1;
                    }
                }
                contentValues.put(PIN_STATUS_COLUMN, pinStatus);
            } else if (column.equals(SYSTEM_APP_COLUMN)) {
                contentValues.put(SYSTEM_APP_COLUMN, uidInfo.isSystemApp() ? 1 : 0);
            } else if (column.equals(ENABLED_COLUMN)) {
                contentValues.put(ENABLED_COLUMN, uidInfo.isEnabled() ? 1 : 0);
            } else if (column.equals(BOOST_STATUS_COLUMN)) {
                contentValues.put(BOOST_STATUS_COLUMN, uidInfo.getBoostStatus());
            } else if (column.equals(UID_COLUMN)) {
                contentValues.put(UID_COLUMN, uidInfo.getUid());
            } else if (column.equals(PACKAGE_NAME_COLUMN)) {
                contentValues.put(PACKAGE_NAME_COLUMN, uidInfo.getPackageName());
            } else if (column.equals(EXTERNAL_DIR_COLUMN)) {
                List<String> externalDirList = uidInfo.getExternalDir();
                StringBuilder sb = new StringBuilder();
                String comma = ",";
                for (int i = 0; i < externalDirList.size(); i++) {
                    sb.append(externalDirList.get(i));
                    if (i != externalDirList.size() - 1) {
                        sb.append(comma);
                    }
                }
                contentValues.put(EXTERNAL_DIR_COLUMN, sb.toString());
            }
            String where = PACKAGE_NAME_COLUMN + "='" + uidInfo.getPackageName() + "'";

            boolean isDatabaseLocked;
            boolean isSuccess = false;
            do {
                try {
                    if (get(uidInfo.getPackageName()) != null) {
                        isSuccess = sSqLiteDatabase.update(TABLE_NAME, contentValues, where, null) > 0;
                        if (!isSuccess) {
                            Logs.e(CLASSNAME, "update", "isSuccess=" + isSuccess + ", uidInfo=" + uidInfo);
                        }
                    } else {
                        isSuccess = insert(uidInfo);
                    }
                    isDatabaseLocked = false;
                } catch (SQLiteDatabaseLockedException e) {
                    Logs.w(CLASSNAME, "update", Log.getStackTraceString(e));
                    isDatabaseLocked = true;
                }
                try {
                    Thread.sleep(Interval.DATABASE_OPERATION_RETRY_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (isDatabaseLocked);
            return isSuccess;
        }
    }

    public boolean delete(String packageName) {
        String where = PACKAGE_NAME_COLUMN + "='" + packageName + "'";

        boolean isSuccess = false;
        boolean isDatabaseLocked;
        do {
            try {
                isSuccess = sSqLiteDatabase.delete(TABLE_NAME, where, null) > 0;
                if (isSuccess) {
                    Logs.d(CLASSNAME, "delete", "packageName=" + packageName);
                } else {
                    Logs.e(CLASSNAME, "delete", "packageName=" + packageName);
                }
                isDatabaseLocked = false;
            } catch (SQLiteDatabaseLockedException e) {
                Logs.w(CLASSNAME, "delete", Log.getStackTraceString(e));
                isDatabaseLocked = true;
            }
            try {
                Thread.sleep(Interval.DATABASE_OPERATION_RETRY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isDatabaseLocked);
        return isSuccess;
    }

    public List<UidInfo> getAll() {
        List<UidInfo> result = new ArrayList<>();
        boolean isDatabaseLocked;
        do {
            try {
                Cursor cursor = sSqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    result.add(getRecord(cursor));
                }
                cursor.close();
                isDatabaseLocked = false;
            } catch (SQLiteDatabaseLockedException e) {
                Logs.w(CLASSNAME, "getAll", Log.getStackTraceString(e));
                result.clear();
                isDatabaseLocked = true;
            }
            try {
                Thread.sleep(Interval.DATABASE_OPERATION_RETRY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isDatabaseLocked);
        return result;
    }

    public List<UidInfo> get(ContentValues cv) {
        List<UidInfo> uidInfoList = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        String AND = " and ";
        for (String key : cv.keySet()) {
            stringBuilder.append(key).append("='").append(cv.get(key)).append("'").append(AND);
        }
        String where = stringBuilder.substring(0, stringBuilder.length() - AND.length());

        boolean isDatabaseLocked;
        do {
            try {
                Cursor cursor = sSqLiteDatabase.query(TABLE_NAME, null, where, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    uidInfoList.add(getRecord(cursor));
                }
                cursor.close();
                isDatabaseLocked = false;
            } catch (SQLiteDatabaseLockedException e) {
                Logs.w(CLASSNAME, "get", Log.getStackTraceString(e));
                uidInfoList.clear();
                isDatabaseLocked = true;
            }
            try {
                Thread.sleep(Interval.DATABASE_OPERATION_RETRY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isDatabaseLocked);
        return uidInfoList;
    }

    /**
     * The value of queryMap can be {@link List} or <code>Object[]</code>. <strong>Note: the array of
     * primitive data type is not supported, i.e. int[], double[]. Instead, use Integer[], Double[]</strong>.
     */
    public List<UidInfo> get(Map<String, Object> queryMap) {
        List<UidInfo> uidInfoList = new ArrayList<>();

        final String AND = " and ";
        final String IN = " in ";
        final String LEFT_PARENTHESIS = "(";
        final String RIGHT_PARENTHESIS = ")";
        final String COMMA = ",";
        final String SINGLE_QUOTE = "'";

        StringBuilder stringBuilder = new StringBuilder();
        for (String key : queryMap.keySet()) {
            Object value = queryMap.get(key);
            if (value instanceof List || value instanceof Object[]) {
                StringBuilder inConditionBuilder = new StringBuilder();
                inConditionBuilder.append(LEFT_PARENTHESIS);
                if (value instanceof List) {
                    for (Object inValue : (List) value) {
                        inConditionBuilder.append(SINGLE_QUOTE);
                        inConditionBuilder.append(inValue.toString());
                        inConditionBuilder.append(SINGLE_QUOTE);
                        inConditionBuilder.append(COMMA);
                    }
                } else { // Object[]
                    for (Object inValue : (Object[]) value) {
                        inConditionBuilder.append(SINGLE_QUOTE);
                        inConditionBuilder.append(inValue.toString());
                        inConditionBuilder.append(SINGLE_QUOTE);
                        inConditionBuilder.append(COMMA);
                    }
                }
                inConditionBuilder.delete(inConditionBuilder.length() - COMMA.length(), inConditionBuilder.length());
                inConditionBuilder.append(RIGHT_PARENTHESIS);

                stringBuilder.append(key).append(IN).append(inConditionBuilder.toString()).append(AND);
            } else {
                stringBuilder.append(key).append("=").append(SINGLE_QUOTE).append(value).append(SINGLE_QUOTE).append(AND);
            }
        }
        String where = stringBuilder.substring(0, stringBuilder.length() - AND.length());
        boolean isDatabaseLocked;
        do {
            try {
                Cursor cursor = sSqLiteDatabase.query(TABLE_NAME, null, where, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    uidInfoList.add(getRecord(cursor));
                }
                cursor.close();
                isDatabaseLocked = false;
            } catch (SQLiteDatabaseLockedException e) {
                Logs.w(CLASSNAME, "get", Log.getStackTraceString(e));
                isDatabaseLocked = true;
            }
            try {
                Thread.sleep(Interval.DATABASE_OPERATION_RETRY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isDatabaseLocked);
        return uidInfoList;
    }

    @Nullable
    public UidInfo get(String packageName) {
        UidInfo uidInfo = null;
        String where = PACKAGE_NAME_COLUMN + "='" + packageName + "'";

        boolean isDatabaseLocked;
        do {
            try {
                Cursor cursor = sSqLiteDatabase.query(TABLE_NAME, null, where, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    uidInfo = getRecord(cursor);
                }
                cursor.close();
                isDatabaseLocked = false;
            } catch (SQLiteDatabaseLockedException e) {
                Logs.w(CLASSNAME, "get", Log.getStackTraceString(e));
                isDatabaseLocked = true;
            }
            try {
                Thread.sleep(Interval.DATABASE_OPERATION_RETRY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isDatabaseLocked);
        return uidInfo;
    }

    public UidInfo getRecord(Cursor cursor) {
        UidInfo result = new UidInfo();
        result.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        result.setPinned(cursor.getInt(cursor.getColumnIndex(PIN_STATUS_COLUMN)) != 0); // 0 unpin, 1 normal pin, 2 priority pin
        result.setSystemApp(cursor.getInt(cursor.getColumnIndex(SYSTEM_APP_COLUMN)) == 1);
        result.setEnabled(cursor.getInt(cursor.getColumnIndex(ENABLED_COLUMN)) == 1);
        result.setBoostStatus(cursor.getInt(cursor.getColumnIndex(BOOST_STATUS_COLUMN)));
        result.setUid(cursor.getInt(cursor.getColumnIndex(UID_COLUMN)));
        result.setPackageName(cursor.getString(cursor.getColumnIndex(PACKAGE_NAME_COLUMN)));

        String externalDir = cursor.getString(cursor.getColumnIndex(EXTERNAL_DIR_COLUMN));
        if (externalDir != null && !externalDir.isEmpty()) {
            List<String> externalDirList = new ArrayList<>();
            String[] list = externalDir.split(",");
            for (String dir : list) {
                externalDirList.add(dir);
            }
            result.setExternalDir(externalDirList);
        }
        return result;
    }

    public long getCount() {
        int count = 0;
        boolean isDatabaseLocked;
        do {
            try {
                Cursor cursor = sSqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
                count = cursor.getCount();
                cursor.close();
                isDatabaseLocked = false;
            } catch (SQLiteDatabaseLockedException e) {
                Logs.w(CLASSNAME, "getCount", Log.getStackTraceString(e));
                isDatabaseLocked = true;
            }
            try {
                Thread.sleep(Interval.DATABASE_OPERATION_RETRY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isDatabaseLocked);
        return count;
    }

}
