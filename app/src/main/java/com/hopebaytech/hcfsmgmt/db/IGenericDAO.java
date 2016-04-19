package com.hopebaytech.hcfsmgmt.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by Aaron on 2016/4/19.
 */
public interface IGenericDAO<T> {

    T getRecord(Cursor cursor);
    SQLiteDatabase getDataBase();
    int getCount();
    List<T> getAll();
    boolean insert();
    void close();
    void clear();
    void delete(String key);

}
