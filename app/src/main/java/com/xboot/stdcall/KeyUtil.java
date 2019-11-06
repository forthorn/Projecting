package com.xboot.stdcall;

import android.app.Instrumentation;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 模拟按键工具
 * Instrumentation模拟按键不能在Application Thread进行
 */
public class KeyUtil {

    private static Instrumentation sInstrumentation;
    private static ExecutorService sSingleThreadPool;

    private KeyUtil() {
        sInstrumentation = new Instrumentation();
        sSingleThreadPool = Executors.newSingleThreadExecutor();
    }

    private static class SingletonInstance {
        private static final KeyUtil INSTANCE = new KeyUtil();
    }

    public static KeyUtil getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void performKey(final int key) {
        if (sInstrumentation == null || sSingleThreadPool == null) {
            sInstrumentation = new Instrumentation();
            sSingleThreadPool = Executors.newSingleThreadExecutor();
        }
        sSingleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.e("KeyUtil", "KeyUtil: " + key);
                sInstrumentation.sendKeyDownUpSync(key);
            }
        });
    }


}