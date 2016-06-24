package com.hopebaytech.hcfsmgmt.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.ArrayList;
import java.util.List;

public class UidDAO {

    private final String CLASSNAME = getClass().getSimpleName();
    public static final String TABLE_NAME = "uid";
    public static final String KEY_ID = "_id";
    public static final String PIN_STATUS_COLUMN = "pin_status";
    public static final String SYSTEM_APP_COLUMN = "system_app";
    public static final String UID_COLUMN = "uid";
    public static final String PACKAGE_NAME_COLUMN = "package_name";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PIN_STATUS_COLUMN + " INTEGER NOT NULL, " +
                    SYSTEM_APP_COLUMN + " INTEGER NOT NULL, " +
                    UID_COLUMN + " TEXT NOT NULL, " +
                    PACKAGE_NAME_COLUMN + " TEXT NOT NULL)";

    private Context context;
    private static UidDAO mUidDAO;

    private UidDAO(Context context) {
        this.context = context;
    }

    public static UidDAO getInstance(Context context) {
        if (mUidDAO == null) {
            synchronized (UidDAO.class) {
                if (mUidDAO == null) {
                    mUidDAO = new UidDAO(context);
                }
            }
        }
        return mUidDAO;
    }


    public void close() {
        getDataBase().close();
    }

    public boolean insert(UidInfo uidInfo) {
        ContentValues contentValues = new ContentValues();
        int pinStatus = 0;
        if (uidInfo.isPinned()) {
            if (uidInfo.isSystemApp()) {
                pinStatus = 2;
            } else {
                pinStatus = 1;
            }
        }
        contentValues.put(PIN_STATUS_COLUMN, pinStatus);
        contentValues.put(SYSTEM_APP_COLUMN, uidInfo.isSystemApp() ? 1 : 0);
        contentValues.put(UID_COLUMN, uidInfo.getUid());
        contentValues.put(PACKAGE_NAME_COLUMN, uidInfo.getPackageName());
        boolean isInserted = getDataBase().insert(TABLE_NAME, null, contentValues) > -1;
        String logMsg = "isPinned=" + uidInfo.isPinned() +
                ", pinStatus=" + pinStatus +
                ", isSystemApp=" + uidInfo.isSystemApp() +
                ", uid=" + uidInfo.getUid() +
                ", packageName=" + uidInfo.getPackageName();
        if (isInserted) {
            Logs.d(CLASSNAME, "insert", logMsg);
            return true;
        } else {
            Logs.e(CLASSNAME, "insert", logMsg);
            return false;
        }
    }

    /** Update specific column */
    public boolean update(UidInfo uidInfo, String column) {
        String logMsg = "uidInfo=" + uidInfo + ", column=" + column;
        Logs.d(CLASSNAME, "update", logMsg);
        if (uidInfo.getPackageName() == null) {
            Logs.e(CLASSNAME, "update", "msg=package name cannot be null");
            return false;
        } else {
            ContentValues contentValues = new ContentValues();
            if (column.equals(PIN_STATUS_COLUMN)) {
                contentValues.put(PIN_STATUS_COLUMN, uidInfo.isPinned() ? 1 : 0);
            } else if (column.equals(SYSTEM_APP_COLUMN)) {
                contentValues.put(SYSTEM_APP_COLUMN, uidInfo.isSystemApp() ? 1 : 0);
            } else if (column.equals(UID_COLUMN)) {
                contentValues.put(UID_COLUMN, uidInfo.getUid());
            } else if (column.equals(PACKAGE_NAME_COLUMN)) {
                contentValues.put(PACKAGE_NAME_COLUMN, uidInfo.getPackageName());
            }
            String where = PACKAGE_NAME_COLUMN + "='" + uidInfo.getPackageName() + "'";
            boolean isSuccess;
            if (get(uidInfo.getPackageName()) != null) {
                isSuccess = getDataBase().update(TABLE_NAME, contentValues, where, null) > 0;
                if (!isSuccess) {
                    Logs.e(CLASSNAME, "update", "isSuccess=" + isSuccess + ", uidInfo=" + uidInfo);
                }
            } else {
                isSuccess = insert(uidInfo);
            }
            return isSuccess;
        }
    }

    public boolean delete(String packageName) {
        String where = PACKAGE_NAME_COLUMN + "='" + packageName + "'";
        boolean isDeleted = getDataBase().delete(TABLE_NAME, where, null) > 0;
        if (isDeleted) {
            Logs.d(CLASSNAME, "delete", "packageName=" + packageName);
        } else {
            Logs.e(CLASSNAME, "delete", "packageName=" + packageName);
        }
        return isDeleted;
    }

    public boolean deleteAll() {
        return getDataBase().delete(TABLE_NAME, null, null) > 0;
    }

    public List<UidInfo> getAll() {
        List<UidInfo> result = new ArrayList<UidInfo>();
        Cursor cursor = getDataBase().query(TABLE_NAME, null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }
        cursor.close();
        return result;
    }

    @Nullable
    public UidInfo get(String packageName) {
        UidInfo uidInfo = null;
        String where = PACKAGE_NAME_COLUMN + "='" + packageName + "'";
        Cursor cursor = getDataBase().query(TABLE_NAME, null, where, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            uidInfo = getRecord(cursor);
        }
        cursor.close();
        return uidInfo;
    }

    public UidInfo getRecord(Cursor cursor) {
        UidInfo result = new UidInfo();
        result.setPinned(cursor.getInt(cursor.getColumnIndex(PIN_STATUS_COLUMN)) != 0);
        result.setSystemApp(cursor.getInt(cursor.getColumnIndex(SYSTEM_APP_COLUMN)) == 1);
        result.setUid(cursor.getInt(cursor.getColumnIndex(UID_COLUMN)));
        result.setPackageName(cursor.getString(cursor.getColumnIndex(PACKAGE_NAME_COLUMN)));
        return result;
    }

    public long getCount() {
        Cursor cursor = getDataBase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    private SQLiteDatabase getDataBase() {
        return Uid2PkgDBHelper.getDataBase(context);
    }

}
