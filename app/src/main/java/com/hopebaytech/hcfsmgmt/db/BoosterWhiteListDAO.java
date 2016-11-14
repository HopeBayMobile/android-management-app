package com.hopebaytech.hcfsmgmt.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hopebaytech.hcfsmgmt.info.BoosterWhiteListInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IGenericDAO;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author GuoYu
 *         Created by GuoYu on 2016/10/27.
 */
public class BoosterWhiteListDAO implements IGenericDAO<BoosterWhiteListInfo> {

    private final String CLASSNAME = getClass().getSimpleName();

    public static final String TABLE_NAME = "booster_white_list";
    public static final String KEY_ID = "_id";
    public static final String PACKAGE_NAME_COLUMN = "package_name";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PACKAGE_NAME_COLUMN + " TEXT NOT NULL)";

    private Context context;
    private static BoosterWhiteListDAO mBoosterWhiteListDAO;
    private static SQLiteDatabase mDataBase;

    private BoosterWhiteListDAO(Context context) {
        this.context = context;
    }

    public static BoosterWhiteListDAO getInstance(Context context) {
        if (mBoosterWhiteListDAO == null) {
            synchronized (BoosterWhiteListDAO.class) {
                if (mBoosterWhiteListDAO == null) {
                    mBoosterWhiteListDAO = new BoosterWhiteListDAO(context);
                }
            }
        }
        mDataBase = Uid2PkgDBHelper.getDataBase(context);
        return mBoosterWhiteListDAO;
    }

    @Override
    public BoosterWhiteListInfo getRecord(Cursor cursor) {
        BoosterWhiteListInfo boosterWhiteListInfo = new BoosterWhiteListInfo();
        boosterWhiteListInfo.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
        boosterWhiteListInfo.setPackageName(cursor.getString(cursor.getColumnIndex(PACKAGE_NAME_COLUMN)));
        return boosterWhiteListInfo;
    }

    @Override
    public int getCount() {
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<BoosterWhiteListInfo> getAll() {
        List<BoosterWhiteListInfo> boosterWhiteListInfoList = new ArrayList<>();
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            boosterWhiteListInfoList.add(getRecord(cursor));
        }
        cursor.close();
        return boosterWhiteListInfoList;
    }

    public BoosterWhiteListInfo getFirst() {
        BoosterWhiteListInfo boosterWhiteListInfo = null;
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            boosterWhiteListInfo = getRecord(cursor);
        }
        return boosterWhiteListInfo;
    }

    @Override
    public boolean insert(BoosterWhiteListInfo info) {
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
    public boolean update(BoosterWhiteListInfo info) {
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
        return Uid2PkgDBHelper.getDataBase(context);
    }

}
