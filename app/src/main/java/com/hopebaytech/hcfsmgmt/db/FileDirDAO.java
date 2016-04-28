package com.hopebaytech.hcfsmgmt.db;//package com.hopebaytech.hcfsmgmt.db;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//
//public class FileDirDAO {
//	
//	public static final String TABLE_NAME = "filedir";
//	public static final String KEY_ID = "_id";
//	public static final String FILE_PATH_COLUMN = "file_path";
//    public static final String PIN_STATUS_COLUMN = "pin_status";
//    public static final String CREATE_TABLE = 
//    		"CREATE TABLE " + TABLE_NAME + " (" + 
//    		KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
//    		FILE_PATH_COLUMN + " TEXT NOT NULL, " +
//    		PIN_STATUS_COLUMN + " INTEGER NOT NULL)";
//    
//    private SQLiteDatabase db;
//    private Context mContext;
//    
//    public FileDirDAO(Context mContext) {
//    	this.mContext = mContext;
//    	openDbIfClosed();
//    }
//    
//    public void close() {
//    	db.close();
//    }
//    
//    public void openDbIfClosed() {
//    	db = HCFSDBHelper.getDataBase(mContext);
//    }
//    
//    public long insert(FileDirInfo fileDirInfo) {
//    	openDbIfClosed();
//    	ContentValues contentValues = new ContentValues();
//    	contentValues.put(FILE_PATH_COLUMN, fileDirInfo.getFilePath());
//    	contentValues.put(PIN_STATUS_COLUMN, fileDirInfo.isPinned());
//    	return db.insert(TABLE_NAME, null, contentValues);
//    }
//    
//    public boolean update(FileDirInfo fileDirInfo) {
//    	openDbIfClosed();
//    	ContentValues contentValues = new ContentValues();
//    	contentValues.put(FILE_PATH_COLUMN, fileDirInfo.getFilePath());
//    	contentValues.put(PIN_STATUS_COLUMN, fileDirInfo.isPinned());
//    	
//    	String where = FILE_PATH_COLUMN + "='" + fileDirInfo.getFilePath() + "'";
//    	return db.update(TABLE_NAME, contentValues, where, null) > 0;
//    }
//    
//    public boolean delete(long id) {
//    	openDbIfClosed();
//    	String where = KEY_ID + "=" + id;
//    	return db.delete(TABLE_NAME, where, null) > 0;
//    }
//    
//    public List<FileDirInfo> getAll() {
//    	openDbIfClosed();
//    	List<FileDirInfo> result = new ArrayList<FileDirInfo>();
//    	Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
//    	while (cursor.moveToNext()) {
//			result.add(getRecord(cursor));
//		}
//    	cursor.close();
//    	return result;
//    }
//    
//    public FileDirInfo get(String filePath) {
//    	openDbIfClosed();
//    	FileDirInfo fileDirInfo = null;
//    	String where = FILE_PATH_COLUMN + "='" + filePath + "'";
//    	Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null, null);
//    	if (cursor.moveToFirst()) {
//    		fileDirInfo = getRecord(cursor);
//    	}
//    	cursor.close();
//    	return fileDirInfo;
//    }
//    
//    public FileDirInfo getRecord(Cursor cursor) {
//    	openDbIfClosed();
//    	FileDirInfo result = new FileDirInfo(mContext);
//    	result.setCurrentFile(new File(cursor.getString(cursor.getColumnIndex(FILE_PATH_COLUMN))));
//    	result.setPinned(cursor.getInt(cursor.getColumnIndex(PIN_STATUS_COLUMN)) == 0 ? false : true);
//    	return result;
//    }
//    
//    public long getCount() {
//    	openDbIfClosed();
//    	Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
//    	return cursor.getCount();
//    }
//
//}
