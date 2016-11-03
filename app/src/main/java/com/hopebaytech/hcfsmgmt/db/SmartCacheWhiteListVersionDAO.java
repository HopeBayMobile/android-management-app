package com.hopebaytech.hcfsmgmt.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hopebaytech.hcfsmgmt.info.SmartCacheWhiteListVersionInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IGenericDAO;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author GuoYu
 *         Created by GuoYu on 2016/11/03.
 */
public class SmartCacheWhiteListVersionDAO implements IGenericDAO<SmartCacheWhiteListVersionInfo> {

    private final String CLASSNAME = getClass().getSimpleName();

    public static final String TABLE_NAME = "smart_cache_while_list_version";
    public static final String KEY_ID = "_id";
    public static final String WHITE_LIST_VERSION_COLUMN = "white_list_version";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    WHITE_LIST_VERSION_COLUMN + " TEXT NOT NULL)";

    private Context context;
    private static SmartCacheWhiteListVersionDAO mSmartCacheWhiteListVersionDAO;
    private static SQLiteDatabase mDataBase;

    private SmartCacheWhiteListVersionDAO(Context context) {
        this.context = context;
    }

    public static SmartCacheWhiteListVersionDAO getInstance(Context context) {
        if (mSmartCacheWhiteListVersionDAO == null) {
            synchronized (SmartCacheWhiteListVersionDAO.class) {
                if (mSmartCacheWhiteListVersionDAO == null) {
                    mSmartCacheWhiteListVersionDAO = new SmartCacheWhiteListVersionDAO(context);
                }
            }
        }
        mDataBase = SmartCacheWhiteListDBHelper.getDataBase(context);
        return mSmartCacheWhiteListVersionDAO;
    }

    @Override
    public SmartCacheWhiteListVersionInfo getRecord(Cursor cursor) {
        SmartCacheWhiteListVersionInfo scwListVersionInfo = new SmartCacheWhiteListVersionInfo();
        scwListVersionInfo.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        scwListVersionInfo.setWhiteListVersion(cursor.getString(cursor.getColumnIndex(WHITE_LIST_VERSION_COLUMN)));
        return scwListVersionInfo;
    }

    @Override
    public int getCount() {
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<SmartCacheWhiteListVersionInfo> getAll() {
        List<SmartCacheWhiteListVersionInfo> scwListVersionInfoList = new ArrayList<>();
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            scwListVersionInfoList.add(getRecord(cursor));
        }
        cursor.close();
        return scwListVersionInfoList;
    }

    public SmartCacheWhiteListVersionInfo getFirst() {
        SmartCacheWhiteListVersionInfo scwListVersionInfo = null;
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            scwListVersionInfo = getRecord(cursor);
        }
        return scwListVersionInfo;
    }

    @Override
    public boolean insert(SmartCacheWhiteListVersionInfo info) {

        String whiteListVersion = info.getWhiteListVersion();

        ContentValues cv = new ContentValues();
        cv.put(WHITE_LIST_VERSION_COLUMN, whiteListVersion);

        boolean isSuccess = mDataBase.insert(TABLE_NAME, null, cv) != -1;
        Logs.d(CLASSNAME, "insert",
                "whiteListVersion=" + whiteListVersion +
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
    public boolean update(SmartCacheWhiteListVersionInfo info) {
        ContentValues cv = new ContentValues();
        cv.put(WHITE_LIST_VERSION_COLUMN, info.getWhiteListVersion());

        String where = KEY_ID + "=" + info.getId();
        boolean isSuccess = mDataBase.update(TABLE_NAME, cv, where, null) > 0;
        Logs.d(CLASSNAME, "update", "id=" + info.getId() +
                ", whiteListVersion=" + info.getWhiteListVersion() +
                ", isSuccess=" + isSuccess);

        return isSuccess;
    }

    private SQLiteDatabase getDataBase() {
        return SmartCacheWhiteListDBHelper.getDataBase(context);
    }

}
