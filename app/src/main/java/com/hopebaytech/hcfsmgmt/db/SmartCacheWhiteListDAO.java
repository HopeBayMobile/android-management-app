package com.hopebaytech.hcfsmgmt.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hopebaytech.hcfsmgmt.info.SmartCacheWhiteListInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IGenericDAO;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author GuoYu
 *         Created by GuoYu on 2016/10/27.
 */
public class SmartCacheWhiteListDAO implements IGenericDAO<SmartCacheWhiteListInfo> {

    private final String CLASSNAME = getClass().getSimpleName();

    public static final String TABLE_NAME = "smart_cache_while_list";
    public static final String KEY_ID = "_id";
    public static final String PACKAGE_NAME_COLUMN = "package_name";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PACKAGE_NAME_COLUMN + " TEXT NOT NULL)";

    private Context context;
    private static SmartCacheWhiteListDAO mSmartCacheWhiteListDAO;
    private static SQLiteDatabase mDataBase;

    private SmartCacheWhiteListDAO(Context context) {
        this.context = context;
    }

    public static SmartCacheWhiteListDAO getInstance(Context context) {
        if (mSmartCacheWhiteListDAO == null) {
            synchronized (SmartCacheWhiteListDAO.class) {
                if (mSmartCacheWhiteListDAO == null) {
                    mSmartCacheWhiteListDAO = new SmartCacheWhiteListDAO(context);
                }
            }
        }
        mDataBase = SmartCacheWhiteListDBHelper.getDataBase(context);
        return mSmartCacheWhiteListDAO;
    }

    @Override
    public SmartCacheWhiteListInfo getRecord(Cursor cursor) {
        SmartCacheWhiteListInfo scwListInfo = new SmartCacheWhiteListInfo();
        scwListInfo.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        scwListInfo.setPackageName(cursor.getString(cursor.getColumnIndex(PACKAGE_NAME_COLUMN)));
        return scwListInfo;
    }

    @Override
    public int getCount() {
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<SmartCacheWhiteListInfo> getAll() {
        List<SmartCacheWhiteListInfo> scwListInfoList = new ArrayList<>();
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            scwListInfoList.add(getRecord(cursor));
        }
        cursor.close();
        return scwListInfoList;
    }

    public SmartCacheWhiteListInfo getFirst() {
        SmartCacheWhiteListInfo scwListInfo = null;
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            scwListInfo = getRecord(cursor);
        }
        return scwListInfo;
    }

    @Override
    public boolean insert(SmartCacheWhiteListInfo info) {

        String packageName = info.getPackageName();

        ContentValues cv = new ContentValues();
        cv.put(PACKAGE_NAME_COLUMN, packageName);

        boolean isSuccess = mDataBase.insert(TABLE_NAME, null, cv) != -1;
        Logs.d(CLASSNAME, "insert",
                ", packageName=" + packageName +
                        ", isSuccess=" + isSuccess);

        return isSuccess;
    }

    @Override
    public void close() {
        getDataBase().close();
    }

    @Override
    public void clear() {
        mDataBase.delete(TABLE_NAME, null, null);
    }

    @Override
    public void delete(String key) {

    }

    @Override
    public boolean update(SmartCacheWhiteListInfo info) {
        ContentValues cv = new ContentValues();
        cv.put(PACKAGE_NAME_COLUMN, info.getPackageName());

        String where = PACKAGE_NAME_COLUMN + "='" + info.getPackageName() + "'";
        boolean isSuccess = mDataBase.update(TABLE_NAME, cv, where, null) > 0;
        Logs.d(CLASSNAME, "update", "id=" + info.getId() +
                ", packageName=" + info.getPackageName() +
                ", isSuccess=" + isSuccess);

        return isSuccess;
    }

    private SQLiteDatabase getDataBase() {
        return SmartCacheWhiteListDBHelper.getDataBase(context);
    }

}
