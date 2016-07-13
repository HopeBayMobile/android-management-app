package com.hopebaytech.hcfsmgmt.interfaces;

import android.database.Cursor;

import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/19.
 */
public interface IGenericDAO<T> {

    T getRecord(Cursor cursor);

    int getCount();

    List<T> getAll();

    boolean insert(T info);

    void close();

    void clear();

    void delete(String key);

    boolean update(T info);

}
