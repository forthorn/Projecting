package com.forthorn.projecting.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.forthorn.projecting.app.AppApplication;
import com.forthorn.projecting.app.AppConstant;
import com.forthorn.projecting.entity.Download;
import com.forthorn.projecting.entity.Schedule;
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
        Task oldTask = findTask(task.getId());
        if (oldTask != null) {
            updateTask(task);
            return;
        }
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_ID, task.getId());
        values.put(DBHelper.TASK_TYPE, task.getType());
        values.put(DBHelper.TASK_DEVICE_ID, task.getEquip_id());
        values.put(DBHelper.TASK_STATUS, task.getStatus());
        values.put(DBHelper.TASK_RUNNING_STATUS, task.getRunningStatus());
        values.put(DBHelper.TASK_CREATE_TIME, task.getCreate_time());
        values.put(DBHelper.TASK_LAST_MODIFY, task.getLast_modify());
        values.put(DBHelper.TASK_HOUR, task.getHour());
        values.put(DBHelper.TASK_DURATION, task.getDuration());
        values.put(DBHelper.TASK_DATE, task.getDate());
        values.put(DBHelper.TASK_CONTENT, task.getContent());
        values.put(DBHelper.TASK_START_TIME, task.getStart_time());
        values.put(DBHelper.TASK_FINISH_TIME, task.getFinish_time());
        Log.e("ContentValues", values.toString());
        Log.e("插入Task", task.toString());
        db.insert(DBHelper.TASK_TABLE, null, values);
        db.close();
    }


    public void updateTask(Task task) {
        db = dbHelper.getWritableDatabase();
//        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_ID, task.getId());
        values.put(DBHelper.TASK_TYPE, task.getType());
        values.put(DBHelper.TASK_DEVICE_ID, task.getEquip_id());
        values.put(DBHelper.TASK_STATUS, task.getStatus());
        values.put(DBHelper.TASK_RUNNING_STATUS, task.getRunningStatus());
        values.put(DBHelper.TASK_CREATE_TIME, task.getCreate_time());
        values.put(DBHelper.TASK_LAST_MODIFY, task.getLast_modify());
        values.put(DBHelper.TASK_HOUR, task.getHour());
        values.put(DBHelper.TASK_DURATION, task.getDuration());
        values.put(DBHelper.TASK_DATE, task.getDate());
        values.put(DBHelper.TASK_CONTENT, task.getContent());
        values.put(DBHelper.TASK_START_TIME, task.getStart_time());
        values.put(DBHelper.TASK_FINISH_TIME, task.getFinish_time());
        db.update(DBHelper.TASK_TABLE, values, DBHelper.TASK_ID + "=?", new String[]{String.valueOf(task.getId())});
        Log.e("更新Task", task.toString());
        db.close();
    }

    public void deleteTask(Task task) {
        db = dbHelper.getWritableDatabase();
//        db.beginTransaction();
        db.delete(DBHelper.TASK_TABLE, DBHelper.TASK_ID + "=?", new String[]{String.valueOf(task.getId())});
        Log.e("删除Task", task.toString());
        db.close();
    }


    public Task findTask(int id) {
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from TASK_TABLE where task_id =?", new String[]{String.valueOf(id)});
        if (cursor.moveToNext()) {
            Task task = new Task();
            task.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_ID)));
            task.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_TYPE)));
            task.setEquip_id(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DEVICE_ID)));
            task.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_STATUS)));
            task.setRunningStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_RUNNING_STATUS)));
            task.setCreate_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_CREATE_TIME)));
            task.setLast_modify(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_LAST_MODIFY)));
            task.setHour(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_HOUR)));
            task.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DURATION)));
            task.setDate(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DATE)));
            task.setContent(cursor.getString(cursor.getColumnIndex(DBHelper.TASK_CONTENT)));
            task.setStart_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_START_TIME)));
            task.setFinish_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_FINISH_TIME)));
            db.close();
            return task;
        }
        db.close();
        return null;
    }

    public List<Task> findAllTask() {
        db = dbHelper.getWritableDatabase();
//        db.beginTransaction();
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from TASK_TABLE", new String[]{});
        while (cursor.moveToNext()) {
            Task task = new Task();
            task.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_ID)));
            task.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_TYPE)));
            task.setEquip_id(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DEVICE_ID)));
            task.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_STATUS)));
            task.setRunningStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_RUNNING_STATUS)));
            task.setCreate_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_CREATE_TIME)));
            task.setLast_modify(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_LAST_MODIFY)));
            task.setHour(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_HOUR)));
            task.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DURATION)));
            task.setDate(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DATE)));
            task.setContent(cursor.getString(cursor.getColumnIndex(DBHelper.TASK_CONTENT)));
            task.setStart_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_START_TIME)));
            task.setFinish_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_FINISH_TIME)));
            tasks.add(task);
        }
        return tasks;
    }


    public void deleteOverdueTask() {
        db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int time = (int) (System.currentTimeMillis() / 1000L);
            Cursor cursor = db.rawQuery("select * from TASK_TABLE where task_finish_time < " + time, null);
            while (cursor.moveToNext()) {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_ID)));
                task.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_TYPE)));
                task.setEquip_id(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DEVICE_ID)));
                task.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_STATUS)));
                task.setRunningStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_RUNNING_STATUS)));
                task.setCreate_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_CREATE_TIME)));
                task.setLast_modify(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_LAST_MODIFY)));
                task.setHour(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_HOUR)));
                task.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DURATION)));
                task.setDate(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DATE)));
                task.setContent(cursor.getString(cursor.getColumnIndex(DBHelper.TASK_CONTENT)));
                task.setStart_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_START_TIME)));
                task.setFinish_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_FINISH_TIME)));
                db.delete(DBHelper.TASK_TABLE, DBHelper.TASK_ID + "=?",
                        new String[]{String.valueOf(task.getId())});
                Log.e("删除过期Task", task.toString());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<Task> findPauseTask() {
        db = dbHelper.getWritableDatabase();
        List<Task> tasks = new ArrayList<>();
        db.beginTransaction();
        try {
            int time = (int) (System.currentTimeMillis() / 1000L);
            Cursor cursor = db.rawQuery("select * from TASK_TABLE where task_start_time <= " + time, null);
            while (cursor.moveToNext()) {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_ID)));
                task.setType(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_TYPE)));
                task.setEquip_id(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DEVICE_ID)));
                task.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_STATUS)));
                task.setRunningStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_RUNNING_STATUS)));
                task.setCreate_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_CREATE_TIME)));
                task.setLast_modify(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_LAST_MODIFY)));
                task.setHour(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_HOUR)));
                task.setDuration(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DURATION)));
                task.setDate(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DATE)));
                task.setContent(cursor.getString(cursor.getColumnIndex(DBHelper.TASK_CONTENT)));
                task.setStart_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_START_TIME)));
                task.setFinish_time(cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_FINISH_TIME)));
                if (task.getFinish_time() < time) {
                    db.delete(DBHelper.TASK_TABLE, DBHelper.TASK_ID + " =?",
                            new String[]{String.valueOf(task.getId())});
                    Log.e("查暂停-删过期Task", task.toString());
                } else {
                    if (task.getRunningStatus() == AppConstant.TASK_RUNNING_STATUS_GOING) {
                        tasks.add(task);
                        Log.e("查到暂停Task", task.toString());
                    }
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
            db.close();
        }
        return tasks;
    }

    public void insertDownload(Download download) {
        db = dbHelper.getWritableDatabase();
//        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(DBHelper.DOWNLOAD_ID, download.getId());
        values.put(DBHelper.DOWNLOAD_TASK_ID, download.getTaskId());
        values.put(DBHelper.DOWNLOAD_STATUS, download.getStatus());
        values.put(DBHelper.DOWNLOAD_URL, download.getUrl());
        values.put(DBHelper.DOWNLOAD_PATH, download.getPath());
        values.put(DBHelper.DOWNLOAD_TIME, download.getTime());
        values.put(DBHelper.DOWNLOAD_FILE_SIZE, download.getFileSize());
        db.insert(DBHelper.DOWNLOAD_TABLE, null, values);
        db.close();
    }

    public Download findDownload(int id) {
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from DOWNLOAD_TABLE where download_id =?", new String[]{String.valueOf(id)});
        if (cursor.moveToNext()) {
            Download download = new Download();
            download.setId(id);
            download.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_STATUS)));
            download.setTaskId(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_TASK_ID)));
            download.setUrl(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_URL)));
            download.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_PATH)));
            download.setTime(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_TIME)));
            download.setFileSize(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_FILE_SIZE)));
            db.close();
            return download;
        }
        db.close();
        return null;
    }

    public Download findDownloadedDownload(String url) {
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from DOWNLOAD_TABLE where download_url =? and download_status =?",
                new String[]{url, String.valueOf(AppConstant.DOWNLOAD_STATUS_COMPLETE)});
        if (cursor.moveToNext()) {
            Download download = new Download();
            download.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_ID)));
            download.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_STATUS)));
            download.setTaskId(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_TASK_ID)));
            download.setUrl(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_URL)));
            download.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_PATH)));
            download.setTime(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_TIME)));
            download.setFileSize(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_FILE_SIZE)));
            db.close();
            return download;
        }
        db.close();
        return null;
    }

    public Download findEarliestDownload() {
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from DOWNLOAD_TABLE order by download_time ASC", null);
        if (cursor.moveToNext()) {
            Download download = new Download();
            download.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_ID)));
            download.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_STATUS)));
            download.setTaskId(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_TASK_ID)));
            download.setUrl(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_URL)));
            download.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_PATH)));
            download.setTime(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_TIME)));
            download.setFileSize(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_FILE_SIZE)));
            db.close();
            return download;
        }
        db.close();
        return null;
    }

    public List<Download> findAllDownload() {
        db = dbHelper.getWritableDatabase();
        List<Download> downloads = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from DOWNLOAD_TABLE", null);
        while (cursor.moveToNext()) {
            Download download = new Download();
            download.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_ID)));
            download.setStatus(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_STATUS)));
            download.setTaskId(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_TASK_ID)));
            download.setUrl(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_URL)));
            download.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.DOWNLOAD_PATH)));
            download.setTime(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_TIME)));
            download.setFileSize(cursor.getInt(cursor.getColumnIndex(DBHelper.DOWNLOAD_FILE_SIZE)));
            downloads.add(download);
        }
        db.close();
        return downloads;
    }

    public void deleteDownload(Download download) {
        db = dbHelper.getWritableDatabase();
        int id = download.getId();
        db.delete(DBHelper.DOWNLOAD_TABLE, DBHelper.DOWNLOAD_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateDownload(Download download) {
        db = dbHelper.getWritableDatabase();
        int id = download.getId();
        ContentValues values = new ContentValues();
        values.put(DBHelper.DOWNLOAD_ID, download.getId());
        values.put(DBHelper.DOWNLOAD_TASK_ID, download.getTaskId());
        values.put(DBHelper.DOWNLOAD_STATUS, download.getStatus());
        values.put(DBHelper.DOWNLOAD_URL, download.getUrl());
        values.put(DBHelper.DOWNLOAD_PATH, download.getPath());
        values.put(DBHelper.DOWNLOAD_TIME, download.getTime());
        values.put(DBHelper.DOWNLOAD_FILE_SIZE, download.getFileSize());
        db.update(DBHelper.DOWNLOAD_TABLE, values, DBHelper.DOWNLOAD_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateSchedule(List<Schedule.ScheduleBean> scheduleList) {
        if (scheduleList == null) {
            return;
        }
        db = dbHelper.getWritableDatabase();
//        db.delete(DBHelper.ONOFF_TABLE, DBHelper.ONOFF_START_TIME + "=?", new String[]{"*"});
        db.execSQL("DELETE FROM " + DBHelper.ONOFF_TABLE);
        int size = scheduleList.size();
        for (int i = 0; i < size; i++) {
            ContentValues values = new ContentValues();
            values.put(DBHelper.ONOFF_ID, i + "");
            values.put(DBHelper.ONOFF_START_TIME, scheduleList.get(i).getStartTime());
            values.put(DBHelper.ONOFF_START_DAY, scheduleList.get(i).getStartDay());
            values.put(DBHelper.ONOFF_OFF_TIME, scheduleList.get(i).getOffTime());
            values.put(DBHelper.ONOFF_OFF_DAY, scheduleList.get(i).getOffDay());
            db.insert(DBHelper.ONOFF_TABLE, null, values);
        }
        db.close();
    }

    public List<Schedule.ScheduleBean> findAllSchedule() {
        db = dbHelper.getWritableDatabase();
        List<Schedule.ScheduleBean> schedule = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from ONOFF_TABLE", null);
        while (cursor.moveToNext()) {
            Schedule.ScheduleBean download = new Schedule.ScheduleBean();
            download.setStartTime(cursor.getString(cursor.getColumnIndex(DBHelper.ONOFF_START_TIME)));
            download.setStartDay(cursor.getString(cursor.getColumnIndex(DBHelper.ONOFF_START_DAY)));
            download.setOffTime(cursor.getString(cursor.getColumnIndex(DBHelper.ONOFF_OFF_TIME)));
            download.setOffDay(cursor.getString(cursor.getColumnIndex(DBHelper.ONOFF_OFF_DAY)));
            schedule.add(download);
        }
        db.close();
        return schedule;
    }

}
