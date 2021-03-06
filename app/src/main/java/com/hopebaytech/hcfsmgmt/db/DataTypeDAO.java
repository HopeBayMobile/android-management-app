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

import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.ArrayList;
import java.util.List;

public class DataTypeDAO {

    private final String CLASSNAME = getClass().getSimpleName();

    public static final String TABLE_NAME = "datatype";
    public static final String KEY_ID = "_id";
    public static final String TYPE_COLUMN = "type";
    public static final String PIN_STATUS_COLUMN = "pin_status";
    public static final String DATE_UPDATED_COLUMN = "date_updated";
    public static final String DATE_PINNED_COLUMN = "date_pinned";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TYPE_COLUMN + " TEXT NOT NULL, " +
                    PIN_STATUS_COLUMN + " INTEGER NOT NULL, " +
                    DATE_UPDATED_COLUMN + " INTEGER NOT NULL DEFAULT 0, " +
                    DATE_PINNED_COLUMN + " INTEGER NOT NULL DEFAULT 0)";

    public static final String DATA_TYPE_IMAGE = "image";
    public static final String DATA_TYPE_VIDEO = "video";
    public static final String DATA_TYPE_AUDIO = "audio";

    private Context context;
    private static DataTypeDAO mDataTypeDAO;

    private DataTypeDAO(Context context) {
        this.context = context;
    }

    public static DataTypeDAO getInstance(Context context) {
        if (mDataTypeDAO == null) {
            synchronized (DataTypeDAO.class) {
                if (mDataTypeDAO == null) {
                    mDataTypeDAO = new DataTypeDAO(context);
                }
            }
        }
        return mDataTypeDAO;
    }

    public void close() {
        getDataBase().close();
    }

    public long insert(DataTypeInfo dataTypeInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TYPE_COLUMN, dataTypeInfo.getDataType());
        contentValues.put(PIN_STATUS_COLUMN, dataTypeInfo.isPinned());
        contentValues.put(DATE_UPDATED_COLUMN, dataTypeInfo.getDateUpdated());
        contentValues.put(DATE_PINNED_COLUMN, dataTypeInfo.getDatePinned());
        return getDataBase().insert(TABLE_NAME, null, contentValues);
    }

    public boolean update(DataTypeInfo dataTypeInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PIN_STATUS_COLUMN, dataTypeInfo.isPinned());
        contentValues.put(DATE_UPDATED_COLUMN, dataTypeInfo.getDateUpdated());
        contentValues.put(DATE_PINNED_COLUMN, dataTypeInfo.getDatePinned());

        String where = TYPE_COLUMN + "='" + dataTypeInfo.getDataType() + "'";
        return getDataBase().update(TABLE_NAME, contentValues, where, null) > 0;
    }

    public boolean update(String dataType, DataTypeInfo dataTypeInfo, String column) {
        ContentValues contentValues = new ContentValues();
        if (column.equals(TYPE_COLUMN)) {
            contentValues.put(column, dataTypeInfo.getDataType());
        } else if (column.equals(DATE_UPDATED_COLUMN)) {
            contentValues.put(column, dataTypeInfo.getDateUpdated());
        } else if (column.equals(PIN_STATUS_COLUMN)) {
            contentValues.put(column, dataTypeInfo.isPinned());
        } else if (column.equals(DATE_PINNED_COLUMN)) {
            contentValues.put(column, dataTypeInfo.getDatePinned());
        } else {
            return false;
        }
        String where = TYPE_COLUMN + "='" + dataType + "'";
        boolean isSuccess = getDataBase().update(TABLE_NAME, contentValues, where, null) > 0;
        if (!isSuccess) {
            Logs.e(CLASSNAME, "update", "isSuccess=" + isSuccess);
        }
        return isSuccess;
    }

    public boolean updateDateUpdated(String dataType, long dateUpdated) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DATE_UPDATED_COLUMN, dateUpdated);

        String where = TYPE_COLUMN + "='" + dataType + "'";
        return getDataBase().update(TABLE_NAME, contentValues, where, null) > 0;
    }

    public boolean delete(long id) {
        String where = KEY_ID + "=" + id;
        return getDataBase().delete(TABLE_NAME, where, null) > 0;
    }

    public List<DataTypeInfo> getAll() {
        List<DataTypeInfo> result = new ArrayList<DataTypeInfo>();
        Cursor cursor = getDataBase().query(TABLE_NAME, null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }
        cursor.close();
        return result;
    }

    public DataTypeInfo get(String dataType) {
        DataTypeInfo dataTypeInfo = null;
        String where = TYPE_COLUMN + "='" + dataType + "'";
        Cursor cursor = getDataBase().query(TABLE_NAME, null, where, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            dataTypeInfo = getRecord(cursor);
        }
        cursor.close();
        return dataTypeInfo;
    }

    public DataTypeInfo getRecord(Cursor cursor) {
        DataTypeInfo result = new DataTypeInfo(context);
        result.setPinned(cursor.getInt(cursor.getColumnIndex(PIN_STATUS_COLUMN)) == 0 ? false : true);
        result.setDataType(cursor.getString(cursor.getColumnIndex(TYPE_COLUMN)));
        result.setDateUpdated(cursor.getLong(cursor.getColumnIndex(DATE_UPDATED_COLUMN)));
        result.setDatePinned(cursor.getLong(cursor.getColumnIndex(DATE_PINNED_COLUMN)));
        return result;
    }

    public int getCount() {
        Cursor cursor = getDataBase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return cursor.getCount();
    }

    private SQLiteDatabase getDataBase() {
        return TeraDBHelper.getDataBase(context);
    }

}
