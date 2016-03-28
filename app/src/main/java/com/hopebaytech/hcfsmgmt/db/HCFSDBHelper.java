package com.hopebaytech.hcfsmgmt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

public class HCFSDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "hopebay.db";
    public static final int VERSION = 1;
    private final String CLASSNAME = getClass().getSimpleName();
    private static SQLiteDatabase database;
    private static HCFSDBHelper mHCFSDBHelper;

    public HCFSDBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreate", "");
        db.execSQL(DataTypeDAO.CREATE_TABLE);
        db.execSQL(ServiceFileDirDAO.CREATE_TABLE);
        db.execSQL(ServiceAppDAO.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static SQLiteDatabase getDataBase(Context context) {
        if (mHCFSDBHelper == null) {
            mHCFSDBHelper = new HCFSDBHelper(context, DATABASE_NAME, null, VERSION);
        }
        if (database == null || !database.isOpen()) {
            database = mHCFSDBHelper.getWritableDatabase();
        }
        return database;
    }

}
