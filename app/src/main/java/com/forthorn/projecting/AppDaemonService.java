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


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK:
                    checkDog();
                    break;
            }
        }
    };

    /**
     * 检测看门狗是否计数器是否溢出
     */
    private void checkDog() {
        Log.e(TAG, "checkDog");
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
        Log.e(TAG, "onCreate");
        if (mWatchDogReceiver == null) {
            mWatchDogReceiver = new WatchDogReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DAEMON);
        intentFilter.addAction(ACTION_STOP_DAEMON);
        registerReceiver(mWatchDogReceiver, intentFilter);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
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
        Log.e(TAG, "onDestroy");
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
