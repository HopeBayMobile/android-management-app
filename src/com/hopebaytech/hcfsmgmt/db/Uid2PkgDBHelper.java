package com.hopebaytech.hcfsmgmt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class Uid2PkgDBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "uid.db";
	public static final int VERSION = 1;
	private static SQLiteDatabase database;
	
	public Uid2PkgDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(UidDAO.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		db.execSQL("DROP TABLE IF EXISTS " + AppDAO.TABLE_NAME);
//		onCreate(db);
	}
	
	public static SQLiteDatabase getDataBase(Context context) {
		if (database == null || !database.isOpen()) {
			database = new Uid2PkgDBHelper(context, DATABASE_NAME, null, VERSION).getWritableDatabase();
		}
		return database;
	}
	
}
