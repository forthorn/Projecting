package com.forthorn.projecting.app;

/**
 * Created by: Forthorn
 * Date: 11/4/2017.
 * Description:
 */

public class AppConstant {
    public static final int TASK_TYPE_SNAPSHOT = 0;
    public static final int TASK_TYPE_VOLUME = 1;
    public static final int TASK_TYPE_SLEEP = 2;
    public static final int TASK_TYPE_WAKE_UP = 3;
    public static final int TASK_TYPE_PICTURE = 4;
    public static final int TASK_TYPE_TEXT = 5;
    public static final int TASK_TYPE_VIDEO = 6;
    public static final int TASK_TYPE_WEATHER = 7;

    public static final int TASK_STATUS_ADD = 0;
    public static final int TASK_STATUS_DELETE = 1;
    public static final int TASK_STATUS_UPDATE = 2;

    public static final int TASK_RUNNING_STATUS_READY = 0;
    public static final int TASK_RUNNING_STATUS_GOING = 1;
    public static final int TASK_RUNNING_STATUS_FINISH = 2;

    public static final int DOWNLOAD_STATUS_READY = 0;
    public static final int DOWNLOAD_STATUS_DOWNLOADING = 1;
    public static final int DOWNLOAD_STATUS_COMPLETE = 2;


    public static final String ALARM_INTENT = "com.forthorn.projecting";
    public static final String TASK_ID = "TASK_ID";
    public static final String TASK_RUNNING_STATUS = "TASK_RUNNING_STATUS";
    public static final String TASK_TYPE = "TASK_TYPE";
}
