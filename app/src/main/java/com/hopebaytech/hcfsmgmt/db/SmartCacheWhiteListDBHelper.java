package com.hopebaytech.hcfsmgmt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SmartCacheWhiteListDBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "smart_cache_white_list.db";
	public static final int VERSION = 1;
	private static SQLiteDatabase database;
	private static SmartCacheWhiteListDBHelper mSmartCacheWhiteListDBHelper ;

	public SmartCacheWhiteListDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SmartCacheWhiteListDAO.CREATE_TABLE);
		db.execSQL(SmartCacheWhiteListVersionDAO.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public static SQLiteDatabase getDataBase(Context context) {
		if (mSmartCacheWhiteListDBHelper == null) {
			mSmartCacheWhiteListDBHelper = new SmartCacheWhiteListDBHelper(context, DATABASE_NAME, null, VERSION);
		}
		if (database == null || !database.isOpen()) {
			database = mSmartCacheWhiteListDBHelper.getWritableDatabase();
		}
		return database;
	}

}
