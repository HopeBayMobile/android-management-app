package com.hopebaytech.hcfsmgmt.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hopebaytech.hcfsmgmt.info.BoosterWhiteListVersionInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IGenericDAO;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author GuoYu
 *         Created by GuoYu on 2016/11/03.
 */
public class BoosterWhiteListVersionDAO implements IGenericDAO<BoosterWhiteListVersionInfo> {

    private final String CLASSNAME = getClass().getSimpleName();

    public static final String TABLE_NAME = "booster_white_list_version";
    public static final String KEY_ID = "_id";
    public static final String WHITE_LIST_VERSION_COLUMN = "white_list_version";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    WHITE_LIST_VERSION_COLUMN + " TEXT NOT NULL)";

    private Context context;
    private static BoosterWhiteListVersionDAO mBoosterWhiteListVersionDAO;
    private static SQLiteDatabase mDataBase;

    private BoosterWhiteListVersionDAO(Context context) {
        this.context = context;
    }

    public static BoosterWhiteListVersionDAO getInstance(Context context) {
        if (mBoosterWhiteListVersionDAO == null) {
            synchronized (BoosterWhiteListVersionDAO.class) {
                if (mBoosterWhiteListVersionDAO == null) {
                    mBoosterWhiteListVersionDAO = new BoosterWhiteListVersionDAO(context);
                }
            }
        }
        mDataBase = Uid2PkgDBHelper.getDataBase(context);
        return mBoosterWhiteListVersionDAO;
    }

    @Override
    public BoosterWhiteListVersionInfo getRecord(Cursor cursor) {
        BoosterWhiteListVersionInfo scwListVersionInfo = new BoosterWhiteListVersionInfo();
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
    public List<BoosterWhiteListVersionInfo> getAll() {
        List<BoosterWhiteListVersionInfo> scwListVersionInfoList = new ArrayList<>();
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            scwListVersionInfoList.add(getRecord(cursor));
        }
        cursor.close();
        return scwListVersionInfoList;
    }

    public BoosterWhiteListVersionInfo getFirst() {
        BoosterWhiteListVersionInfo scwListVersionInfo = null;
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            scwListVersionInfo = getRecord(cursor);
        }
        return scwListVersionInfo;
    }

    @Override
    public boolean insert(BoosterWhiteListVersionInfo info) {
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
    public boolean update(BoosterWhiteListVersionInfo info) {
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
        return Uid2PkgDBHelper.getDataBase(context);
    }

}
