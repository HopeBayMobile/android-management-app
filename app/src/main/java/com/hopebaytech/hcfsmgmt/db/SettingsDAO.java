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


    private static SettingsDAO sSettingsDAO;
    private static SQLiteDatabase sSqLiteDatabase;

    private SettingsDAO() {
    }

    public static SettingsDAO getInstance(Context context) {
        if (sSettingsDAO == null) {
            synchronized (SettingsDAO.class) {
                if (sSettingsDAO == null) {
                    sSettingsDAO = new SettingsDAO();
                }
            }
        }
        sSqLiteDatabase = TeraDBHelper.getDataBase(context);
        return sSettingsDAO;
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
        Cursor cursor = sSqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<SettingsInfo> getAll() {
        List<SettingsInfo> teraStatInfoList = new ArrayList<>();
        Cursor cursor = sSqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            teraStatInfoList.add(getRecord(cursor));
        }
        cursor.close();
        return teraStatInfoList;
    }

    @Nullable
    public SettingsInfo getFirst() {
        SettingsInfo settingsInfo = null;
        Cursor cursor = sSqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null, null);
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

        return sSqLiteDatabase.insert(TABLE_NAME, null, cv) != -1;
    }

    @Override
    public void close() {
        TeraDBHelper.closeDataBase();
    }

    @Override
    public void clear() {
        sSqLiteDatabase.delete(TABLE_NAME, null, null);
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
        boolean isSuccess = sSqLiteDatabase.update(TABLE_NAME, cv, where, null) > 0;
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

    @Nullable
    public SettingsInfo get(String key) {
        SettingsInfo settingsInfo = null;
        String where = KEY + "='" + key + "'";
        Cursor cursor = sSqLiteDatabase.query(TABLE_NAME, null, where, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            settingsInfo = getRecord(cursor);
        }
        cursor.close();
        return settingsInfo;
    }
}
