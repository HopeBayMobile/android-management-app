package com.hopebaytech.hcfsmgmt.db;

import java.util.ArrayList;
import java.util.List;

import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AppDAO {
	
	public static final String TABLE_NAME = "app";
	public static final String KEY_ID = "_id";
	public static final String PACKAGE_NAME_COLUMN = "package_name";
    public static final String PIN_STATUS_COLUMN = "pin_status";
    public static final String DATA_STATUS_COLUMN = "data_status";
    public static final String CREATE_TABLE = 
    		"CREATE TABLE " + TABLE_NAME + " (" + 
    		KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    		PACKAGE_NAME_COLUMN + " TEXT NOT NULL, " +
    		PIN_STATUS_COLUMN + " INTEGER NOT NULL, " + 
    		DATA_STATUS_COLUMN + " INTEGER NOT NULL)";
    
    private SQLiteDatabase db;
    private Context context;
    
    public AppDAO(Context context) {
    	this.context = context;
    	openDbIfClosed();
    }
    
    public void openDbIfClosed() {
    	db = HCFSDBHelper.getDataBase(context);
    }
    
    public void close() {
    	db.close();
    }
    
    public AppInfo insert(AppInfo appInfo) {
    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(PACKAGE_NAME_COLUMN, appInfo.getPackageName());
    	contentValues.put(PIN_STATUS_COLUMN, appInfo.isPinned());
    	contentValues.put(DATA_STATUS_COLUMN, appInfo.getDataStatus());
    	
    	long id = db.insert(TABLE_NAME, null, contentValues);
    	appInfo.setDbId(id);
    	return appInfo;
    }
    
    public boolean update(AppInfo appInfo) {
    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(PACKAGE_NAME_COLUMN, appInfo.getPackageName());
    	contentValues.put(PIN_STATUS_COLUMN, appInfo.isPinned());
    	contentValues.put(DATA_STATUS_COLUMN, appInfo.getDataStatus());
    	
    	String where = PACKAGE_NAME_COLUMN + "='" + appInfo.getPackageName() + "'";
    	return db.update(TABLE_NAME, contentValues, where, null) > 0;
    }
    
    public boolean delete(long id) {
    	openDbIfClosed();
    	String where = KEY_ID + "=" + id;
    	return db.delete(TABLE_NAME, where, null) > 0;
    }
    
    public List<AppInfo> getAll() {
    	openDbIfClosed();
    	List<AppInfo> result = new ArrayList<AppInfo>();
    	Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
    	while (cursor.moveToNext()) {
			result.add(getRecord(cursor));
		}
    	cursor.close();
    	return result;
    }
    
    public AppInfo get(String packageNamee) {
    	openDbIfClosed();
    	AppInfo appInfo = null;
    	String where = PACKAGE_NAME_COLUMN + "='" + packageNamee + "'";
    	Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null, null);
    	if (cursor.moveToFirst()) {
    		appInfo = getRecord(cursor);
    	}
    	cursor.close();
    	return appInfo;
    }
    
    public AppInfo getRecord(Cursor cursor) {
    	openDbIfClosed();
    	AppInfo result = new AppInfo(context);
    	result.setDbId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
    	result.setPackageName(cursor.getString(cursor.getColumnIndex(PACKAGE_NAME_COLUMN)));
    	result.setPinned(cursor.getInt(cursor.getColumnIndex(PIN_STATUS_COLUMN)) == 0 ? false : true);
    	result.setDataStatus(cursor.getInt(cursor.getColumnIndex(DATA_STATUS_COLUMN)));
    	return result;
    }
    
    public long getCount() {
    	openDbIfClosed();
    	Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    	return cursor.getCount();
    }

}
