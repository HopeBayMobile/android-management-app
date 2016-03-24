package com.hopebaytech.hcfsmgmt.db;

import java.util.ArrayList;
import java.util.List;

import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataTypeDAO {
	
	private final String CLASSNAME = getClass().getSimpleName();
	
	public static final String TABLE_NAME = "datatype";
	public static final String KEY_ID = "_id";
	public static final String TYPE_COLUMN = "type";
    public static final String PIN_STATUS_COLUMN = "pin_status";
    public static final String DATE_UPDATED_COLUMN = "date_updated";
    public static final String DATE_PINNED_COLUMN = "date_pinned";
    public static final String CREATE_TABLE = 
    		"CREATE TABLE " + TABLE_NAME + " (" + 
    		KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    		TYPE_COLUMN + " TEXT NOT NULL, " +
    		PIN_STATUS_COLUMN + " INTEGER NOT NULL, " +
    		DATE_UPDATED_COLUMN + " INTEGER NOT NULL DEFAULT 0, " +  
    		DATE_PINNED_COLUMN + " INTEGER NOT NULL DEFAULT 0)"; 
    
	public static final String DATA_TYPE_IMAGE = "image";
	public static final String DATA_TYPE_VIDEO = "video";
	public static final String DATA_TYPE_AUDIO = "audio";
    
//    private SQLiteDatabase db;
    private Context context;
    
    public DataTypeDAO(Context context) {
    	this.context = context;
//    	openDbIfClosed();
    }
    
    public void close() {
    	getDataBase().close();
    }
    
//    public void openDbIfClosed() {
//    	db = HCFSDBHelper.getDataBase(context);
//    }
    
    public long insert(DataTypeInfo dataTypeInfo) {
//    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(TYPE_COLUMN, dataTypeInfo.getDataType());
    	contentValues.put(PIN_STATUS_COLUMN, dataTypeInfo.isPinned());
    	contentValues.put(DATE_UPDATED_COLUMN, dataTypeInfo.getDateUpdated());
    	contentValues.put(DATE_PINNED_COLUMN, dataTypeInfo.getDatePinned());
    	return getDataBase().insert(TABLE_NAME, null, contentValues);
    }
    
    public boolean update(DataTypeInfo dataTypeInfo) {
//    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(PIN_STATUS_COLUMN, dataTypeInfo.isPinned());
    	contentValues.put(DATE_UPDATED_COLUMN, dataTypeInfo.getDateUpdated());
    	contentValues.put(DATE_PINNED_COLUMN, dataTypeInfo.getDatePinned());
    	
    	String where = TYPE_COLUMN + "='" + dataTypeInfo.getDataType() + "'";
    	return getDataBase().update(TABLE_NAME, contentValues, where, null) > 0;
    }
    
    public boolean update(String dataType, DataTypeInfo dataTypeInfo, String column) {
//    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	if (column.equals(TYPE_COLUMN)) {
    		contentValues.put(column, dataTypeInfo.getDataType());
    	} else if (column.equals(DATE_UPDATED_COLUMN)) {
    		contentValues.put(column, dataTypeInfo.getDateUpdated());
    	} else if (column.equals(PIN_STATUS_COLUMN)) {
    		contentValues.put(column, dataTypeInfo.isPinned());
    	} else if (column.equals(DATE_PINNED_COLUMN)) {
    		contentValues.put(column, dataTypeInfo.getDatePinned());
    	} else {
			return false;
		}
    	String where = TYPE_COLUMN + "='" + dataType + "'";
    	boolean isSuccess = getDataBase().update(TABLE_NAME, contentValues, where, null) > 0;
    	if (!isSuccess) {
    		HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "update", "isSuccess=" + isSuccess);
    	}
    	return isSuccess;    	
    }
    
    public boolean updateDateUpdated(String dataType, long dateUpdated) {
//    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(DATE_UPDATED_COLUMN, dateUpdated);
    	
    	String where = TYPE_COLUMN + "='" + dataType + "'";
    	return getDataBase().update(TABLE_NAME, contentValues, where, null) > 0;
    }
    
    public boolean delete(long id) {
//    	openDbIfClosed();
    	String where = KEY_ID + "=" + id;
    	return getDataBase().delete(TABLE_NAME, where, null) > 0;
    }
    
    public List<DataTypeInfo> getAll() {
//    	openDbIfClosed();
    	List<DataTypeInfo> result = new ArrayList<DataTypeInfo>();
    	Cursor cursor = getDataBase().query(TABLE_NAME, null, null, null, null, null, null, null);
    	while (cursor.moveToNext()) {
			result.add(getRecord(cursor));
		}
    	cursor.close();
    	return result;
    }
    
    public DataTypeInfo get(String dataType) {
//    	openDbIfClosed();
    	DataTypeInfo dataTypeInfo = null;
    	String where = TYPE_COLUMN + "='" + dataType + "'";
    	Cursor cursor = getDataBase().query(TABLE_NAME, null, where, null, null, null, null, null);
    	if (cursor.moveToFirst()) {
    		dataTypeInfo = getRecord(cursor);
    	}
    	cursor.close();
    	return dataTypeInfo;
    }
    
    public DataTypeInfo getRecord(Cursor cursor) {
//    	openDbIfClosed();
    	DataTypeInfo result = new DataTypeInfo(context);
    	result.setPinned(cursor.getInt(cursor.getColumnIndex(PIN_STATUS_COLUMN)) == 0 ? false : true);
    	result.setDataType(cursor.getString(cursor.getColumnIndex(TYPE_COLUMN)));
    	result.setDateUpdated(cursor.getLong(cursor.getColumnIndex(DATE_UPDATED_COLUMN)));
    	result.setDatePinned(cursor.getLong(cursor.getColumnIndex(DATE_PINNED_COLUMN)));
    	return result;
    }
    
    public int getCount() {
//    	openDbIfClosed();
    	Cursor cursor = getDataBase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
    	return cursor.getCount();
    }

	private SQLiteDatabase getDataBase() {
		return HCFSDBHelper.getDataBase(context);
	}

}
