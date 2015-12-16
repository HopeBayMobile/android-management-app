package com.hopebaytech.hcfsmgmt.db;

import java.util.ArrayList;
import java.util.List;

import com.hopebaytech.hcfsmgmt.info.UidInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UidDAO {

	public static final String TABLE_NAME = "uid";
	public static final String KEY_ID = "_id";
	public static final String UID_COLUMN = "uid";
	public static final String PACKAGE_NAME_COLUMN = "package_name";
	public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + UID_COLUMN
			+ " TEXT NOT NULL, " + PACKAGE_NAME_COLUMN + " TEXT NOT NULL)";

	private SQLiteDatabase db;
	private Context context;

	public UidDAO(Context context) {
		this.context = context;
		openDbIfClosed();
	}

	public void openDbIfClosed() {
		db = UidPkgNameDBHelper.getDataBase(context);
	}

	public void close() {
		db.close();
	}

	public boolean insert(UidInfo uidInfo) {
		openDbIfClosed();
		ContentValues contentValues = new ContentValues();
		contentValues.put(UID_COLUMN, uidInfo.getUid());
		contentValues.put(PACKAGE_NAME_COLUMN, uidInfo.getPackageName());
		return db.insert(TABLE_NAME, null, contentValues) > -1;
	}

	public boolean update(UidInfo uidInfo) {
		openDbIfClosed();
		ContentValues contentValues = new ContentValues();
		contentValues.put(UID_COLUMN, uidInfo.getUid());
		contentValues.put(PACKAGE_NAME_COLUMN, uidInfo.getPackageName());
		String where = PACKAGE_NAME_COLUMN + "='" + uidInfo.getPackageName() + "'";
		return db.update(TABLE_NAME, contentValues, where, null) > 0;
	}

	public boolean delete(String packageName) {
		openDbIfClosed();
		String where = PACKAGE_NAME_COLUMN + "='" + packageName + "'";
		return db.delete(TABLE_NAME, where, null) > 0;
	}
	
	public boolean deleteAll() {
		openDbIfClosed();
		return db.delete(TABLE_NAME, null, null) > 0;
	}

	public List<UidInfo> getAll() {
		openDbIfClosed();
		List<UidInfo> result = new ArrayList<UidInfo>();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			result.add(getRecord(cursor));
		}
		cursor.close();
		return result;
	}

	public UidInfo get(String packageName) {
		openDbIfClosed();
		UidInfo uidInfo = null;
		String where = PACKAGE_NAME_COLUMN + "='" + packageName + "'";
		Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			uidInfo = getRecord(cursor);
		}
		cursor.close();
		return uidInfo;
	}

	public UidInfo getRecord(Cursor cursor) {
		openDbIfClosed();
		UidInfo result = new UidInfo();
		result.setUid(cursor.getInt(cursor.getColumnIndex(UID_COLUMN)));
		result.setPackageName(cursor.getString(cursor.getColumnIndex(PACKAGE_NAME_COLUMN)));
		return result;
	}

	public long getCount() {
		openDbIfClosed();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		return cursor.getCount();
	}

}
