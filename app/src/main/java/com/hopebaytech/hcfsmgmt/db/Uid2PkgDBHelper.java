package com.hopebaytech.hcfsmgmt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.hopebaytech.hcfsmgmt.info.UidInfo;

public class Uid2PkgDBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "uid.db";
	public static final int VERSION = 2;
	private static SQLiteDatabase database;
	private static Uid2PkgDBHelper mUid2PkgDBHelper;

	public Uid2PkgDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(UidDAO.CREATE_TABLE);
        db.execSQL(BoosterWhiteListDAO.CREATE_TABLE);
        db.execSQL(BoosterWhiteListVersionDAO.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            addBoostStatusAndBoosterWhiteListTables(db);
        }
	}

	public static SQLiteDatabase getDataBase(Context context) {
		if (mUid2PkgDBHelper == null) {
			mUid2PkgDBHelper = new Uid2PkgDBHelper(context, DATABASE_NAME, null, VERSION);
		}
		if (database == null || !database.isOpen()) {
			database = mUid2PkgDBHelper.getWritableDatabase();
		}
		return database;
	}

    private void addBoostStatusAndBoosterWhiteListTables(SQLiteDatabase db) {
            String uidTableName = UidDAO.TABLE_NAME;
            String uidOldTableTempName = "uid_old_table";

            String OLD_COLUMNS = UidDAO.KEY_ID + ", " + UidDAO.PIN_STATUS_COLUMN + ", " +
                                 UidDAO.SYSTEM_APP_COLUMN + ", " + UidDAO.UID_COLUMN + ", " +
                                 UidDAO.PACKAGE_NAME_COLUMN + ", " + UidDAO.EXTERNAL_DIR_COLUMN;

            String RENAME_OLD_UID_TABLE = "ALTER TABLE " + uidTableName + " RENAME TO " + uidOldTableTempName;
            String COPY_OLD_UID_DATA_TO_NEW_UID_TABLE = "INSERT INTO " + uidTableName + "(" + OLD_COLUMNS + ") " +
                        "SELECT " + OLD_COLUMNS + " FROM " + uidOldTableTempName;
            String DROP_UID_OLD_TEMP_TABLE = "DROP TABLE IF EXISTS "  + uidOldTableTempName;

            String UPDATE_SYSTEM_APP_BOOST_STATUS = "update " + uidTableName + " set " +
                        UidDAO.BOOST_STATUS_COLUMN + "=" + UidInfo.BoostStatus.NON_BOOSTABLE +
                        " where " + UidDAO.SYSTEM_APP_COLUMN + "=1";

            String UPDATE_NON_SYSTEM_APP_BOOST_STATUS = "update " + uidTableName + " set " +
                        UidDAO.BOOST_STATUS_COLUMN + "=" + UidInfo.BoostStatus.UNBOOSTED +
                        " where " + UidDAO.SYSTEM_APP_COLUMN + "=0";

            db.execSQL(RENAME_OLD_UID_TABLE);
	    	db.execSQL(UidDAO.CREATE_TABLE);
            db.execSQL(BoosterWhiteListDAO.CREATE_TABLE);
            db.execSQL(BoosterWhiteListVersionDAO.CREATE_TABLE);
            db.execSQL(COPY_OLD_UID_DATA_TO_NEW_UID_TABLE);
            db.execSQL(DROP_UID_OLD_TEMP_TABLE);
            db.execSQL(UPDATE_SYSTEM_APP_BOOST_STATUS);
            db.execSQL(UPDATE_NON_SYSTEM_APP_BOOST_STATUS);
    }

}
