//package com.hopebaytech.hcfsmgmt.db;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.hopebaytech.hcfsmgmt.info.ServiceFileDirInfo;
//import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;
//
///**
// * Store uncompleted pin/unpin operations when user manually close app.
// */
//public class ServiceFileDirDAO {
//
//	private final String CLASSNAME = getClass().getSimpleName();
//	public static final String TABLE_NAME = "service_file_dir";
//	public static final String KEY_ID = "_id";
//	public static final String FILE_PATH_COLUMN = "file_path";
//    public static final String PIN_STATUS_COLUMN = "pin_status";
//    public static final String CREATE_TABLE =
//    		"CREATE TABLE " + TABLE_NAME + " (" +
//    		KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//    		FILE_PATH_COLUMN + " TEXT NOT NULL, " +
//    		PIN_STATUS_COLUMN + " INTEGER NOT NULL)";
//
//    private Context context;
//	private static ServiceFileDirDAO mServiceFileDirDAO;
//
//    private ServiceFileDirDAO(Context context) {
//    	this.context = context;
//    }
//
//	public static ServiceFileDirDAO getInstance(Context context) {
//		if (mServiceFileDirDAO == null) {
//			synchronized (ServiceFileDirDAO.class) {
//				if (mServiceFileDirDAO == null) {
//					mServiceFileDirDAO = new ServiceFileDirDAO(context);
//				}
//			}
//		}
//		return mServiceFileDirDAO;
//	}
//
//    public void close() {
//        getDataBase().close();
//    }
//
//    public long insert(ServiceFileDirInfo info) {
//    	ContentValues contentValues = new ContentValues();
//    	contentValues.put(FILE_PATH_COLUMN, info.getFilePath());
//    	contentValues.put(PIN_STATUS_COLUMN, info.isPinned());
//    	return getDataBase().insert(TABLE_NAME, null, contentValues);
//    }
//
//    public boolean delete(String filePath) {
//    	String where = FILE_PATH_COLUMN + "='" + filePath + "'";
//    	return getDataBase().delete(TABLE_NAME, where, null) > 0;
//    }
//
//    public List<ServiceFileDirInfo> getAll() {
//    	List<ServiceFileDirInfo> result = new ArrayList<ServiceFileDirInfo>();
//    	Cursor cursor = getDataBase().query(TABLE_NAME, null, null, null, null, null, null, null);
//    	while (cursor.moveToNext()) {
//			result.add(getRecord(cursor));
//		}
//    	cursor.close();
//    	return result;
//    }
//
//    public ServiceFileDirInfo get(String filePath) {
//    	ServiceFileDirInfo fileDirInfo = null;
//    	String where = FILE_PATH_COLUMN + "='" + filePath + "'";
//    	Cursor cursor = getDataBase().query(TABLE_NAME, null, where, null, null, null, null, null);
//    	if (cursor.moveToFirst()) {
//    		fileDirInfo = getRecord(cursor);
//    	}
//    	cursor.close();
//    	return fileDirInfo;
//    }
//
//    public ServiceFileDirInfo getRecord(Cursor cursor) {
//    	ServiceFileDirInfo result = new ServiceFileDirInfo();
//    	result.setFilePath(cursor.getString(cursor.getColumnIndex(FILE_PATH_COLUMN)));
//    	result.setPinned(cursor.getInt(cursor.getColumnIndex(PIN_STATUS_COLUMN)) != 0);
//    	return result;
//    }
//
//    public long getCount() {
//    	Cursor cursor = getDataBase().rawQuery("SELECT * FROM " + TABLE_NAME, null);
//    	return cursor.getCount();
//    }
//
//    private SQLiteDatabase getDataBase() {
//        return HCFSDBHelper.getDataBase(context);
//    }
//
//}
