package com.hopebaytech.hcfsmgmt.db;

import java.util.ArrayList;
import java.util.List;

import com.hopebaytech.hcfsmgmt.info.ServiceAppInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Store uncompleted pin/unpin operations when user manually close app.
 */
public class ServiceAppDAO {

	private final String CLASSNAME = getClass().getSimpleName();
	public static final String TABLE_NAME = "service_app";
	public static final String KEY_ID = "_id";
	public static final String APP_NAME_COLUMN = "app_name";
	public static final String PACKAGE_NAME_COLUMN = "package_name";
    public static final String PIN_STATUS_COLUMN = "pin_status";
    public static final String DATA_DIR_COLUMN = "data_dir";
    public static final String SOURCE_DIR_COLUMN = "source_dir";
    public static final String EXTERNAL_DIR_COLUMN = "external_dir";
    public static final String CREATE_TABLE = 
    		"CREATE TABLE " + TABLE_NAME + " (" + 
    		KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    		APP_NAME_COLUMN + " TEXT NOT NULL, " +
    		PACKAGE_NAME_COLUMN + " TEXT NOT NULL, " +
    		PIN_STATUS_COLUMN + " INTEGER NOT NULL, " + 
    		DATA_DIR_COLUMN + " TEXT NOT NULL, " +
    		SOURCE_DIR_COLUMN + " TEXT NOT NULL, " +
    		EXTERNAL_DIR_COLUMN + " TEXT)";
    
//    private SQLiteDatabase db;
    private Context context;
    
    public ServiceAppDAO(Context context) {
    	this.context = context;
//    	openDbIfClosed();
    }
    
//    public void openDbIfClosed() {
//    	db = HCFSDBHelper.getDataBase(context);
//    }
    
    public void close() {
		getDataBase().close();
    }
    
    public long insert(ServiceAppInfo appInfo) {
//    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(APP_NAME_COLUMN, appInfo.getAppName());
    	contentValues.put(PACKAGE_NAME_COLUMN, appInfo.getPackageName());
    	contentValues.put(PIN_STATUS_COLUMN, appInfo.isPinned());
    	contentValues.put(DATA_DIR_COLUMN, appInfo.getDataDir());
    	contentValues.put(SOURCE_DIR_COLUMN, appInfo.getSourceDir());
    	if (appInfo.getExternalDir() != null) {
    		contentValues.put(EXTERNAL_DIR_COLUMN, appInfo.getExternalDir());
    	}
    	return getDataBase().insert(TABLE_NAME, null, contentValues);
    }
    
    public boolean delete(ServiceAppInfo appInfo) {
//    	openDbIfClosed();
    	String where = APP_NAME_COLUMN + "='" + appInfo.getAppName() + "'";
    	return getDataBase().delete(TABLE_NAME, where, null) > 0;
    }
    
    public List<ServiceAppInfo> getAll() {
//    	openDbIfClosed();
    	List<ServiceAppInfo> result = new ArrayList<>();
    	Cursor cursor = getDataBase().query(TABLE_NAME, null, null, null, null, null, null, null);
    	while (cursor.moveToNext()) {
			result.add(getRecord(cursor));
		}
    	cursor.close();
    	return result;
    }
    
    public ServiceAppInfo get(String appName) {
//    	openDbIfClosed();
    	ServiceAppInfo appInfo = null;
    	String where = DATA_DIR_COLUMN + "='" + appName + "'";
    	Cursor cursor = getDataBase().query(TABLE_NAME, null, where, null, null, null, null, null);
    	if (cursor.moveToFirst()) {
    		appInfo = getRecord(cursor);
    	}
    	cursor.close();
    	return appInfo;
    }
    
    public ServiceAppInfo getRecord(Cursor cursor) {
//    	openDbIfClosed();
    	ServiceAppInfo result = new ServiceAppInfo();
    	result.setAppName(cursor.getString(cursor.getColumnIndex(APP_NAME_COLUMN)));
    	result.setPackageName(cursor.getString(cursor.getColumnIndex(PACKAGE_NAME_COLUMN)));
    	result.setDataDir(cursor.getString(cursor.getColumnIndex(DATA_DIR_COLUMN)));
    	result.setSourceDir(cursor.getString(cursor.getColumnIndex(SOURCE_DIR_COLUMN)));
    	String externalDir = cursor.getString(cursor.getColumnIndex(EXTERNAL_DIR_COLUMN));
    	if (externalDir != null) {
    		result.setExternalDir(externalDir);
    	}
    	result.setPinned(cursor.getInt(cursor.getColumnIndex(PIN_STATUS_COLUMN)) != 0);
    	return result;
    }
    
    public long getCount() {
//    	openDbIfClosed();
    	Cursor cursor = getDataBase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
    	return cursor.getCount();
    }

	private SQLiteDatabase getDataBase() {
		return HCFSDBHelper.getDataBase(context);
	}

}
