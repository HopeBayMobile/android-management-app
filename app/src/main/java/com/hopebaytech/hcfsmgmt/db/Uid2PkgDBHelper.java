package com.hopebaytech.hcfsmgmt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class Uid2PkgDBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "uid.db";
	public static final int VERSION = 1;
	private static SQLiteDatabase database;
	private static Uid2PkgDBHelper mUid2PkgDBHelper;
	
	public Uid2PkgDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(UidDAO.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
	public static SQLiteDatabase getDataBase(Context context) {
		if (mUid2PkgDBHelper == null) {
			mUid2PkgDBHelper = new Uid2PkgDBHelper(context, DATABASE_NAME, null, VERSION);
		}
		if (database == null || !database.isOpen()) {
			database = mUid2PkgDBHelper.getWritableDatabase();
		}
		return database;
	}
	
}
