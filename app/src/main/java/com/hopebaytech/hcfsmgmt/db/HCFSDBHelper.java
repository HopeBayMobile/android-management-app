package com.hopebaytech.hcfsmgmt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class HCFSDBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "hopebay.db";
	public static final int VERSION = 1;
	private static SQLiteDatabase database;
	
	public HCFSDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
//		db.execSQL(AppDAO.CREATE_TABLE); TODO
		db.execSQL(DataTypeDAO.CREATE_TABLE);
		db.execSQL(ServiceFileDirDAO.CREATE_TABLE);
		db.execSQL(ServiceAppDAO.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		db.execSQL("DROP TABLE IF EXISTS " + AppDAO.TABLE_NAME);
//		onCreate(db);
	}
	
	public static SQLiteDatabase getDataBase(Context context) {
		if (database == null || !database.isOpen()) {
			database = new HCFSDBHelper(context, DATABASE_NAME, null, VERSION).getWritableDatabase();
		}
		return database;
	}
	
}