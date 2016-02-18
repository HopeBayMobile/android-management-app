package com.hopebaytech.hcfsmgmt.db;

import java.util.ArrayList;
import java.util.List;

import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DataTypeDAO {
	
	public static final String TABLE_NAME = "datatype";
	public static final String KEY_ID = "_id";
	public static final String TYPE_COLUMN = "type";
    public static final String PIN_STATUS_COLUMN = "pin_status";
    public static final String DATE_UPDATED_COLUMN = "date_updated";
    public static final String CREATE_TABLE = 
    		"CREATE TABLE " + TABLE_NAME + " (" + 
    		KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    		TYPE_COLUMN + " TEXT NOT NULL, " +
    		PIN_STATUS_COLUMN + " INTEGER NOT NULL, " +
    		DATE_UPDATED_COLUMN + " INTEGER NOT NULL DEFAULT 0)"; 
    
	public static final String DATA_TYPE_IMAGE = "image";
	public static final String DATA_TYPE_VIDEO = "video";
	public static final String DATA_TYPE_AUDIO = "audio";
    
    private SQLiteDatabase db;
    private Context context;
    
    public DataTypeDAO(Context context) {
    	this.context = context;
    	openDbIfClosed();
    }
    
    public void close() {
    	db.close();
    }
    
    public void openDbIfClosed() {
    	db = HCFSDBHelper.getDataBase(context);
    }
    
    public long insert(DataTypeInfo dataTypeInfo) {
    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(TYPE_COLUMN, dataTypeInfo.getDataType());
    	contentValues.put(PIN_STATUS_COLUMN, dataTypeInfo.isPinned());
    	return db.insert(TABLE_NAME, null, contentValues);
    }
    
    public boolean update(DataTypeInfo dataTypeInfo) {
    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(TYPE_COLUMN, dataTypeInfo.getDataType());
    	contentValues.put(PIN_STATUS_COLUMN, dataTypeInfo.isPinned());
    	contentValues.put(DATE_UPDATED_COLUMN, dataTypeInfo.getDateUpdated());
    	
    	String where = TYPE_COLUMN + "='" + dataTypeInfo.getDataType() + "'";
    	return db.update(TABLE_NAME, contentValues, where, null) > 0;
    }
    
    public boolean update(String dataType, DataTypeInfo imageTypeInfo, String column) {
    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	if (column.equals(TYPE_COLUMN)) {
    		contentValues.put(column, imageTypeInfo.getDataType());
    	} else if (column.equals(DATE_UPDATED_COLUMN)) {
    		contentValues.put(column, imageTypeInfo.getDateUpdated());
    	} else if (column.equals(PIN_STATUS_COLUMN)) {
    		contentValues.put(column, imageTypeInfo.isPinned());
    	} else {
			return false;
		}
    	String where = TYPE_COLUMN + "='" + dataType + "'";
    	return db.update(TABLE_NAME, contentValues, where, null) > 0;
    }
    
    public boolean updateDateUpdated(String dataType, long dateUpdated) {
    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(DATE_UPDATED_COLUMN, dateUpdated);
    	
    	String where = TYPE_COLUMN + "='" + dataType + "'";
    	return db.update(TABLE_NAME, contentValues, where, null) > 0;
    }
    
    public boolean delete(long id) {
    	openDbIfClosed();
    	String where = KEY_ID + "=" + id;
    	return db.delete(TABLE_NAME, where, null) > 0;
    }
    
    public List<DataTypeInfo> getAll() {
    	openDbIfClosed();
    	List<DataTypeInfo> result = new ArrayList<DataTypeInfo>();
    	Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
    	while (cursor.moveToNext()) {
			result.add(getRecord(cursor));
		}
    	cursor.close();
    	return result;
    }
    
    public DataTypeInfo get(String dataType) {
    	openDbIfClosed();
    	DataTypeInfo dataTypeInfo = null;
    	String where = TYPE_COLUMN + "='" + dataType + "'";
    	Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null, null);
    	if (cursor.moveToFirst()) {
    		dataTypeInfo = getRecord(cursor);
    	}
    	cursor.close();
    	return dataTypeInfo;
    }
    
    public DataTypeInfo getRecord(Cursor cursor) {
    	openDbIfClosed();
    	DataTypeInfo result = new DataTypeInfo(context);
    	result.setDataType(cursor.getString(cursor.getColumnIndex(TYPE_COLUMN)));
    	result.setPinned(cursor.getInt(cursor.getColumnIndex(PIN_STATUS_COLUMN)) == 0 ? false : true);
    	result.setDateUpdated(cursor.getLong(cursor.getColumnIndex(DATE_UPDATED_COLUMN)));
    	return result;
    }
    
    public int getCount() {
    	openDbIfClosed();
    	Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    	return cursor.getCount();
    }

}
