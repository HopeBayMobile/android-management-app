package com.hopebaytech.hcfsmgmt.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aaron on 2016/4/19.
 */
public class AccountDAO implements IGenericDAO<AccountInfo> {

    private final String CLASSNAME = getClass().getSimpleName();

    public static final String TABLE_NAME = "account";
    public static final String KEY_ID = "_id";
    public static final String NAME_COLUMN = "name";
    public static final String EMAIL_COLUMN = "email";
    public static final String IMG_URL_COLUMN = "img_url";
    public static final String IMG_BASE64_COLUMN = "img_base64";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NAME_COLUMN + " TEXT NOT NULL, " +
                    EMAIL_COLUMN + " TEXT NOT NULL, " +
                    IMG_URL_COLUMN + " TEXT, " +
                    IMG_BASE64_COLUMN + " TEXT)";

    private Context context;
    private static AccountDAO mAccountDAO;
    private static SQLiteDatabase mDataBase;

    private AccountDAO(Context context) {
        this.context = context;
    }

    public static AccountDAO getInstance(Context context) {
        if (mAccountDAO == null) {
            synchronized (AccountDAO.class) {
                if (mAccountDAO == null) {
                    mAccountDAO = new AccountDAO(context);
                }
            }
        }
        mDataBase = HCFSDBHelper.getDataBase(context);
        return mAccountDAO;
    }

    @Override
    public AccountInfo getRecord(Cursor cursor) {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(cursor.getString(cursor.getColumnIndex(NAME_COLUMN)));
        accountInfo.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL_COLUMN)));
        accountInfo.setImgUrl(cursor.getString(cursor.getColumnIndex(IMG_URL_COLUMN)));
        accountInfo.setImgBase64(cursor.getString(cursor.getColumnIndex(IMG_BASE64_COLUMN)));
        return accountInfo;
    }

    @Override
    public int getCount() {
        Cursor cursor = mDataBase.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<AccountInfo> getAll() {
        List<AccountInfo> accountInfoList = new ArrayList<>();
        Cursor cursor = mDataBase.query(TABLE_NAME, null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            accountInfoList.add(getRecord(cursor));
        }
        cursor.close();
        return accountInfoList;
    }

    @Override
    public boolean insert(AccountInfo info) {
        ContentValues cv = new ContentValues();
        cv.put(NAME_COLUMN, info.getName());
        cv.put(NAME_COLUMN, info.getEmail());
        cv.put(NAME_COLUMN, info.getImgUrl());
        cv.put(NAME_COLUMN, info.getImgBase64());
        return mDataBase.insert(TABLE_NAME, null, cv) != -1;
    }

    @Override
    public void close() {
        HCFSDBHelper.closeDataBase();
    }

    @Override
    public void clear() {
        mDataBase.delete(TABLE_NAME, null, null);
    }

    @Override
    public void delete(String key) {

    }

}
