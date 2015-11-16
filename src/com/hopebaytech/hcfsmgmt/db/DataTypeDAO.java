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
    public static final String DATA_STATUS_COLUMN = "data_status";
    public static final String CREATE_TABLE = 
    		"CREATE TABLE " + TABLE_NAME + " (" + 
    		KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    		TYPE_COLUMN + " TEXT NOT NULL, " +
    		PIN_STATUS_COLUMN + " INTEGER NOT NULL, " + 
    		DATA_STATUS_COLUMN + " INTEGER NOT NULL)";
    
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
    	contentValues.put(DATA_STATUS_COLUMN, dataTypeInfo.getDataStatus());
    	return db.insert(TABLE_NAME, null, contentValues);
    }
    
    public boolean update(DataTypeInfo dataTypeInfo) {
    	openDbIfClosed();
    	ContentValues contentValues = new ContentValues();
    	contentValues.put(TYPE_COLUMN, dataTypeInfo.getDataType());
    	contentValues.put(PIN_STATUS_COLUMN, dataTypeInfo.isPinned());
    	contentValues.put(DATA_STATUS_COLUMN, dataTypeInfo.getDataStatus());
    	
    	String where = TYPE_COLUMN + "='" + dataTypeInfo.getDataType() + "'";
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
    	result.setDataStatus(cursor.getInt(cursor.getColumnIndex(DATA_STATUS_COLUMN)));
    	return result;
    }
    
    public long getCount() {
    	openDbIfClosed();
    	long result = 0;
    	Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
    	if (cursor.moveToFirst()) {
    		result = cursor.getLong(cursor.getColumnIndex(KEY_ID)); 
    	}
    	return result;
    }

}
