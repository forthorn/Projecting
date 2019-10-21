package com.forthorn.projecting;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.forthorn.projecting.receiver.BootReceiver;
import com.forthorn.projecting.util.LogUtils;

import java.lang.ref.WeakReference;


/**
 * @author: forthorn
 * @email: yan360311@gmail.com
 * @version: V1.0
 * @date: 2019-06-24
 * @description: App守护进程服务
 * 当App5秒内没有接收到停止的广播
 * 那就主动唤起App界面到前台页面
 * 启动后，10秒内没有收到广播消息去清除看门狗
 */
public class AppDaemonService extends Service {

    private static final String TAG = "AppDaemon";
    private static final long INTERVEL = 20000L;
    public static final int MSG_CHECK = 0X0;
    public static final String ACTION_DAEMON = "com.forthorn.projecting.DAEMON";
    public static final String ACTION_STOP_DAEMON = "com.forthorn.projecting.STOP_DAEMON";

    private WatchDogReceiver mWatchDogReceiver;
    private int mCount;
    private AppDaemonHandler mHandler = new AppDaemonHandler(this);


    public static class AppDaemonHandler extends Handler {

        private WeakReference<AppDaemonService> mReference;

        AppDaemonHandler(AppDaemonService service) {
            mReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CHECK) {
                if (mReference != null && mReference.get() != null) {
                    mReference.get().checkDog();
                }
            }
        }
    }

    /**
     * 检测看门狗是否计数器是否溢出
     */
    private void checkDog() {
        //超过了计数器，发送广播启动主页面
        if (mCount >= 2) {
            sendBroadcast(new Intent(BootReceiver.ACTION_LAUNCH_APP));
            mCount = 0;
        }
        mCount++;
        mHandler.sendEmptyMessageDelayed(MSG_CHECK, INTERVEL);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mWatchDogReceiver == null) {
            mWatchDogReceiver = new WatchDogReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DAEMON);
        intentFilter.addAction(ACTION_STOP_DAEMON);
        registerReceiver(mWatchDogReceiver, intentFilter);
        LogUtils.e("守护服务", "开启守护服务");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.e("守护服务", "运行守护服务");
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(MSG_CHECK, INTERVEL);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return new Binder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.e("守护服务", "结束守护服务");
        unregisterReceiver(mWatchDogReceiver);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "onRebind");
        super.onRebind(intent);
    }


    public class WatchDogReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (ACTION_DAEMON.equals(intent.getAction())) {
                Log.e(TAG, "ACTION_DAEMON");
//                Toast.makeText(context, "ACTION_DAEMON", Toast.LENGTH_SHORT).show();
                mCount--;
                if (mCount < 0) {
                    mCount = 0;
                }
            } else if (ACTION_STOP_DAEMON.equals(intent.getAction())) {
                Log.e(TAG, "ACTION_STOP_DAEMON");
//                Toast.makeText(context, "ACTION_STOP_DAEMON", Toast.LENGTH_SHORT).show();
                mCount = 0;
                mHandler.removeCallbacksAndMessages(null);
            }
        }
    }
}
