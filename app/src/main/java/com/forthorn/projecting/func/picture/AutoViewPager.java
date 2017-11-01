package com.forthorn.projecting.func.picture;

import android.content.Context;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by: Forthorn
 * Date: 10/26/2017.
 * Description:
 */

public class AutoViewPager extends ViewPager {

    private static final String TAG = "AutoViewPager";
    private int currentItem;
    private Timer mTimer;

    public AutoViewPager(Context context) {
        super(context);
    }

    public AutoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(AutoViewPager viewPager, BaseViewPagerAdapter adapter) {
        adapter.init(viewPager, adapter);
    }

    public void start() {
        //先停止
        onStop();

        if (mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(new AutoTask(), 3000, 3000);

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            currentItem = getCurrentItem();
            if (currentItem == getAdapter().getCount() - 1) {
                currentItem = 0;
            } else {
                currentItem++;
            }
            setCurrentItem(currentItem);
        }
    };

    private AutoHandler mHandler = new AutoHandler();


    public void onPageSelected(int position) {

    }

    private class AutoTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(runnable);
        }
    }

    private final static class AutoHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    public void onStop() {
        //先取消定时器
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void onDestroy() {
        onStop();
    }

    public void onResume() {
        start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onStop();
                break;
            case MotionEvent.ACTION_UP:
                onResume();
                break;
        }
        return super.onTouchEvent(ev);
    }


}
