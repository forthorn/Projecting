package com.forthorn.projecting.app;

import android.app.Application;
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

public class AppApplication extends Application {

    private static AppApplication mAppApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppApplication = this;
        JMessageClient.init(getApplicationContext(), true);
        JMessageClient.setDebugMode(true);
        JMessageClient.setNotificationFlag(JMessageClient.FLAG_NOTIFY_WITH_LED | JMessageClient.FLAG_NOTIFY_WITH_VIBRATE);
        FileDownloader.setup(this);
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
}
