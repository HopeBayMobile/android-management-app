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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.hopebaytech.hcfsmgmt.utils.Logs;

public class TeraDBHelper extends SQLiteOpenHelper {

    private static final String CLASSNAME = TeraDBHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "hopebay.db";
    public static final int VERSION = 1;
    private static SQLiteDatabase mDatabase;
    private static TeraDBHelper mTeraDBHelper;
    private static int mOpenCounter;

    public TeraDBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logs.d(CLASSNAME, "onCreate", "");
        db.execSQL(AccountDAO.CREATE_TABLE);
        db.execSQL(TeraStatDAO.CREATE_TABLE);
        db.execSQL(SettingsDAO.CREATE_TABLE);
//        db.execSQL(DataTypeDAO.CREATE_TABLE);
//        db.execSQL(ServiceFileDirDAO.CREATE_TABLE);
//        db.execSQL(ServiceAppDAO.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static synchronized SQLiteDatabase getDataBase(Context context) {
        mOpenCounter++;
        if (mTeraDBHelper == null) {
            mTeraDBHelper = new TeraDBHelper(context, DATABASE_NAME, null, VERSION);
        }
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = mTeraDBHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public static synchronized void closeDataBase() {
        mOpenCounter--;
        if (mOpenCounter == 0) {
            mDatabase.close();
        }
    }

}
