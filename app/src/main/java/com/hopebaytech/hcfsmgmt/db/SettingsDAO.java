package com.hopebaytech.hcfsmgmt.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IGenericDAO;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 2016/8/19.
 */
public class SettingsDAO implements IGenericDAO<SettingsInfo> {
    private final String CLASSNAME = getClass().getSimpleName();

    public static final String TABLE_NAME = "settings";
    public static final String KEY_ID = "_id";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY + " TEXT NOT NULL, " +
                    VALUE + " TEXT)";


    private static SettingsDAO mSettingsDAO;
    private static SQLiteDatabase mDataBase;

    public static SettingsDAO getInstance(Context context) {
        if (mSettingsDAO == null) {
            synchronized (SettingsDAO.class) {
                if (mSettingsDAO == null) {
                    mSettingsDAO = new SettingsDAO();
                }
            }
        }
        mDataBase = HCFSDBHelper.getDataBase(context);
        return mSettingsDAO;
    }

    @Override
    public SettingsInfo getRecord(Cursor cursor) {
        SettingsInfo settingsInfo = new SettingsInfo();
        settingsInfo.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        settingsInfo.setKey(cursor.getString(cursor.getColumnIndex(KEY)));
        settingsInfo.setValue(cursor.getString(cursor.getColumnIndex(VALUE)));
        return settingsInfo;
    }

    @Override
    public int getCount() {
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<SettingsInfo> getAll() {
        List<SettingsInfo> teraStatInfoList = new ArrayList<>();
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            teraStatInfoList.add(getRecord(cursor));
        }
        cursor.close();
        return teraStatInfoList;
    }

    @Nullable
    public SettingsInfo getFirst() {
        SettingsInfo settingsInfo = null;
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            settingsInfo = getRecord(cursor);
        }
        return settingsInfo;
    }

    @Override
    public boolean insert(SettingsInfo info) {
        ContentValues cv = new ContentValues();
        cv.put(KEY, info.getKey());
        cv.put(VALUE, info.getValue());

        return mDataBase.insert(TABLE_NAME, null, cv) != -1;
    }

    @Override
    public void close() {
        HCFSDBHelper.closeDataBase();
    }

    @Override
    public void clear() {
        mDataBase.delete(TABLE_NAME, null, null);
    }

    @Override
    public void delete(String key) {

    }

    @Override
    public boolean update(SettingsInfo info) {
        ContentValues cv = new ContentValues();
        cv.put(KEY, info.getKey());
        cv.put(VALUE, info.getValue());

        String where = KEY + "='" + info.getKey() + "'";

        boolean isSuccess = mDataBase.update(TABLE_NAME, cv, where, null) > 0;
        if (!isSuccess) {
            Logs.d(CLASSNAME, "update", "Update item no exist. Try to insert.");
            isSuccess = insert(info);
        }
        Logs.d(CLASSNAME, "update", "id=" + info.getId() +
                ", KEY=" + info.getKey() +
                ", VALUE=" + info.getValue() +
                ", isSuccess=" + isSuccess);
        return isSuccess;
    }


    public SettingsInfo get(String key) {
        SettingsInfo settingsInfo = null;
        String where = KEY + "='" + key + "'";
        Cursor cursor = mDataBase.query(TABLE_NAME, null, where, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            settingsInfo = getRecord(cursor);
        }
        cursor.close();
        return settingsInfo;
    }
}
