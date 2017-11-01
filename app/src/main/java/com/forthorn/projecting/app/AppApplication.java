package com.forthorn.projecting.app;

import android.app.Application;

import cn.jpush.im.android.api.JMessageClient;

/**
 * Created by: Forthorn
 * Date: 8/19/2017.
 * Description:
 */

public class AppApplication extends Application {

    private static AppApplication mAppApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppApplication = this;
        JMessageClient.init(getApplicationContext(), true);
        JMessageClient.setDebugMode(true);
        JMessageClient.setNotificationFlag(JMessageClient.FLAG_NOTIFY_WITH_LED | JMessageClient.FLAG_NOTIFY_WITH_VIBRATE);
    }

    public static AppApplication getApplication() {
        return mAppApplication;
    }

    public static AppApplication getContext() {
        return mAppApplication;
    }


}
