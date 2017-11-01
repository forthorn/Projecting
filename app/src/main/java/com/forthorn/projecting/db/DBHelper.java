package com.forthorn.projecting.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "Projecting.db";
    public static final String TASK_TABLE = "TASK_TABLE";
    public static final String TASK_ID = "_id";
    public static final String TASK_TYPE = "task_type";
    public static final String TASK_STATUS = "task_status";
    public static final String TASK_HOUR = "task_hour";
    public static final String TASK_DURATION = "task_duration";
    public static final String TASK_DATE = "task_date";
    public static final String TASK_CONTENT = "task_content";

    public static final String DOWNLOAD_TABLE = "DOWNLOAD_TABLE";
    public static final String DOWNLOAD_ID = "_id";
    public static final String DOWNLOAD_TASK_ID = "task_id";
    public static final String DOWNLOAD_STATUS = "download_status";
    public static final String DOWNLOAD_URL = "download_url";
    public static final String DOWNLOAD_PATH = "download_path";


    private static final String CREATE_TASK_TABLE = "CREATE TABLE IF NOT EXISTS" + TASK_TABLE
            + "(" + TASK_ID + "integer primary key ," + TASK_TYPE + "int," + TASK_STATUS + "int,"
            + TASK_HOUR + "int," + TASK_DURATION + "int," + TASK_DATE + "int," + TASK_CONTENT + "text" + ")";

    private static final String CREATE_DOWNLOAD_TABLE = "CREATE TABLE IF NOT EXISTS" + DOWNLOAD_TABLE
            + "(" + DOWNLOAD_ID + "integer primary key ," + DOWNLOAD_TASK_ID + "int," + DOWNLOAD_STATUS + "int,"
            + DOWNLOAD_URL + "text," + DOWNLOAD_PATH + "text" + ")";

    //数据库当前版本
    public static final int DB_VERSION = 1;

    private static DBHelper dbHelper = null;

    public DBHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(context, DB_NAME, DB_VERSION);
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TASK_TABLE);
        db.execSQL(CREATE_DOWNLOAD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //目前是第一版，之后在此做更新操作
    }


}
