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

import com.hopebaytech.hcfsmgmt.info.TeraStatInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IGenericDAO;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/19.
 */
public class TeraStatDAO implements IGenericDAO<TeraStatInfo> {

    private final String CLASSNAME = getClass().getSimpleName();

    public static final String TABLE_NAME = "tera_stat";
    public static final String KEY_ID = "_id";
    public static final String ENABLED_COLUMN = "enabled";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ENABLED_COLUMN + " INTEGER NOT NULL)";

    private static TeraStatDAO sTeraStatDAO;
    private static SQLiteDatabase sSqLiteDatabase;

    private TeraStatDAO() {
    }

    public static TeraStatDAO getInstance(Context context) {
        if (sTeraStatDAO == null) {
            synchronized (TeraStatDAO.class) {
                if (sTeraStatDAO == null) {
                    sTeraStatDAO = new TeraStatDAO();
                }
            }
        }
        sSqLiteDatabase = TeraDBHelper.getDataBase(context);
        return sTeraStatDAO;
    }

    @Override
    public TeraStatInfo getRecord(Cursor cursor) {
        TeraStatInfo teraStatInfo = new TeraStatInfo();
        teraStatInfo.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        teraStatInfo.setEnabled(cursor.getInt(cursor.getColumnIndex(ENABLED_COLUMN)) != 0);
        return teraStatInfo;
    }

    @Override
    public int getCount() {
        Cursor cursor = sSqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<TeraStatInfo> getAll() {
        List<TeraStatInfo> teraStatInfoList = new ArrayList<>();
        Cursor cursor = sSqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            teraStatInfoList.add(getRecord(cursor));
        }
        cursor.close();
        return teraStatInfoList;
    }

    @Nullable
    public TeraStatInfo getFirst() {
        TeraStatInfo teraStatInfo = null;
        Cursor cursor = sSqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            teraStatInfo = getRecord(cursor);
        }
        return teraStatInfo;
    }

    @Override
    public boolean insert(TeraStatInfo info) {
        boolean isEnabled = info.isEnabled();

        ContentValues cv = new ContentValues();
        cv.put(ENABLED_COLUMN, isEnabled);

        boolean isSuccess = sSqLiteDatabase.insert(TABLE_NAME, null, cv) != -1;
        if (isSuccess) {
            Logs.d(CLASSNAME, "insert", "isEnabled=" + isEnabled);
        } else {
            Logs.e(CLASSNAME, "insert", "isEnabled=" + isEnabled);
        }
        return isSuccess;
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
    public boolean update(TeraStatInfo info) {
        ContentValues cv = new ContentValues();
        cv.put(ENABLED_COLUMN, info.isEnabled());

        String where = KEY_ID + "=" + info.getId();
        boolean isSuccess = sSqLiteDatabase.update(TABLE_NAME, cv, where, null) > 0;
        Logs.d(CLASSNAME, "update", "id=" + info.getId() +
                ", isEnabled=" + info.isEnabled() +
                ", isSuccess=" + isSuccess);

        return isSuccess;
    }

}
