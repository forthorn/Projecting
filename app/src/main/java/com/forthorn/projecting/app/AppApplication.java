package com.forthorn.projecting.app;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.forthorn.projecting.BuildConfig;
import com.forthorn.projecting.HomeActivity;
import com.forthorn.projecting.R;
import com.forthorn.projecting.util.LogUtils;
import com.liulishuo.filedownloader.FileDownloader;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import cn.jpush.im.android.api.JMessageClient;

/**
 * Created by: Forthorn
 * Date: 8/19/2017.
 * Description:
 */

public class AppApplication extends Application implements Thread.UncaughtExceptionHandler {

    private static AppApplication mAppApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppApplication = this;
        JMessageClient.init(getApplicationContext(), true);
        JMessageClient.setDebugMode(true);
        JMessageClient.setNotificationFlag(JMessageClient.FLAG_NOTIFY_WITH_LED | JMessageClient.FLAG_NOTIFY_WITH_VIBRATE);
        FileDownloader.setup(this);
        Thread.setDefaultUncaughtExceptionHandler(this);
        initBugly();
        LogUtils.init(this);
    }

    public static AppApplication getApplication() {
        return mAppApplication;
    }

    public static AppApplication getContext() {
        return mAppApplication;
    }

    private void initBugly() {
        Beta.autoInit = true;
        Beta.autoCheckUpgrade = true;
        Beta.upgradeCheckPeriod = 60 * 1000;
        Beta.initDelay = 3 * 1000;
        Beta.smallIconId = R.mipmap.ic_launcher;
        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        Beta.showInterruptedStrategy = true;
        Beta.canShowUpgradeActs.add(HomeActivity.class);
        Bugly.init(getApplicationContext(), "a153c22bee", BuildConfig.DEBUG);
    }

    public void restartApp() {
        Intent intent = new Intent(mAppApplication, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent restartIntent = PendingIntent.getActivity(mAppApplication, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) mAppApplication.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 200, restartIntent);
        android.os.Process.killProcess(android.os.Process.myPid());  //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (null != e && null != t) {
            restartApp();
        }
    }
}
