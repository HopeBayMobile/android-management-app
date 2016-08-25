package com.hopebaytech.hcfsmgmt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.hopebaytech.hcfsmgmt.utils.Logs;

public class HCFSDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "hopebay.db";
    public static final int VERSION = 1;
    private final String CLASSNAME = getClass().getSimpleName();
    private static SQLiteDatabase mDatabase;
    private static HCFSDBHelper mHCFSDBHelper;
    private static int mOpenCounter;

    public HCFSDBHelper(Context context, String name, CursorFactory factory, int version) {
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
        if (mHCFSDBHelper == null) {
            mHCFSDBHelper = new HCFSDBHelper(context, DATABASE_NAME, null, VERSION);
        }
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = mHCFSDBHelper.getWritableDatabase();
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
