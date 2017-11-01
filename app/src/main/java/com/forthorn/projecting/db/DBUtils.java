package com.forthorn.projecting.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.forthorn.projecting.app.AppApplication;
import com.forthorn.projecting.entity.Download;
import com.forthorn.projecting.entity.Task;

import java.util.ArrayList;
import java.util.List;

public class DBUtils {
    private static DBUtils sDBUtils;
    private DBHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private Context mContext;

    private DBUtils(Context context) {
        mContext = context;
        dbHelper = DBHelper.getInstance(mContext);
    }

    private static class InstanceHolder {
        private static final DBUtils DB_UTILS = new DBUtils(AppApplication.getContext());
    }

    public static DBUtils getInstance() {
        return InstanceHolder.DB_UTILS;
    }


    public void insertTask(Task task) {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_ID, task.getId());
        values.put(DBHelper.TASK_TYPE, task.getType());
        values.put(DBHelper.TASK_STATUS, task.getStatus());
        values.put(DBHelper.TASK_HOUR, task.getHour());
        values.put(DBHelper.TASK_DURATION, task.getDuration());
        values.put(DBHelper.TASK_DATE, task.getDate());
        values.put(DBHelper.TASK_CONTENT, task.getContent());
        db.insert(DBHelper.TASK_TABLE, null, values);
        db.close();
    }


    public void updateTask(Task task) {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_ID, task.getId());
        values.put(DBHelper.TASK_TYPE, task.getType());
        values.put(DBHelper.TASK_STATUS, task.getStatus());
        values.put(DBHelper.TASK_HOUR, task.getHour());
        values.put(DBHelper.TASK_DURATION, task.getDuration());
        values.put(DBHelper.TASK_DATE, task.getDate());
        values.put(DBHelper.TASK_CONTENT, task.getContent());
        db.update(DBHelper.TASK_TABLE, values, DBHelper.TASK_ID + "=?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void deleteTask(Task task) {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete(DBHelper.TASK_TABLE, DBHelper.TASK_ID + "=?", new String[]{String.valueOf(task.getId())});
        db.close();
    }


    public Task findTask(int id) {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("select * from TASK_TABLE where _id=?", new String[]{String.valueOf(id)});
        if (cursor.moveToNext()) {
            Task task = new Task();
            task.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_ID)));
            task.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_STATUS)));
            task.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_TYPE)));
            task.setHour(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_HOUR)));
            task.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DURATION)));
            task.setDate(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DATE)));
            task.setContent(cursor.getString(cursor.getColumnIndex(DBHelper.TASK_CONTENT)));
            db.close();
            return task;
        }
        db.close();
        return null;
    }

    public List<Task> findAllTask() {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from TASK_TABLE", new String[]{});
        while (cursor.moveToNext()) {
            Task task = new Task();
            task.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_ID)));
            task.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_STATUS)));
            task.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_TYPE)));
            task.setHour(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_HOUR)));
            task.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DURATION)));
            task.setDate(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DATE)));
            task.setContent(cursor.getString(cursor.getColumnIndex(DBHelper.TASK_CONTENT)));
            tasks.add(task);
        }
        return tasks;
    }


    public void insertDownload(Download download) {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(DBHelper.DOWNLOAD_ID, download.getId());
        values.put(DBHelper.DOWNLOAD_TASK_ID, download.getTaskId());
        values.put(DBHelper.DOWNLOAD_STATUS, download.getStatus());
        values.put(DBHelper.DOWNLOAD_URL, download.getUrl());
        values.put(DBHelper.DOWNLOAD_PATH, download.getPath());
        db.insert(DBHelper.TASK_TABLE, null, values);
        db.close();
    }

    public Download findDownload(int id) {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("select * from DOWNLOAD_TABLE where _id=?", new String[]{String.valueOf(id)});
        if (cursor.moveToNext()) {
            Download download = new Download();
            download.setId(id);
            download.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_STATUS)));
            download.setTaskId(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_TASK_ID)));
            download.setUrl(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_URL)));
            download.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_PATH)));
            db.close();
            return download;
        }
        db.close();
        return null;
    }

    public List<Download> findAllDownload() {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        List<Download> downloads = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from DOWNLOAD_TABLE", new String[]{});
        while (cursor.moveToNext()) {
            Download download = new Download();
            download.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_ID)));
            download.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_STATUS)));
            download.setTaskId(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_TASK_ID)));
            download.setUrl(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_URL)));
            download.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_PATH)));
            downloads.add(download);
        }
        db.close();
        return downloads;
    }

    public void deleteDownload(Download download) {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        int id = download.getId();
        db.delete(DBHelper.DOWNLOAD_TABLE, DBHelper.DOWNLOAD_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateDownload(Download download) {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        int id = download.getId();
        ContentValues values = new ContentValues();
        values.put(DBHelper.DOWNLOAD_TASK_ID, download.getTaskId());
        values.put(DBHelper.DOWNLOAD_STATUS, download.getStatus());
        values.put(DBHelper.DOWNLOAD_URL, download.getUrl());
        values.put(DBHelper.DOWNLOAD_PATH, download.getPath());
        db.update(DBHelper.DOWNLOAD_TABLE, values, DBHelper.DOWNLOAD_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }


}
