package com.forthorn.projecting.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.forthorn.projecting.app.AppConstant;
import com.forthorn.projecting.baserx.RxEvent;
import com.forthorn.projecting.baserx.RxManager;

/**
 * Created by: Forthorn
 * Date: 11/4/2017.
 * Description:
 */

public class AlarmReceiver extends BroadcastReceiver {

    private AlarmListener mAlarmListener;
    private RxManager mRxManager = new RxManager();

    public AlarmListener getAlarmListener() {
        return mAlarmListener;
    }

    public void setAlarmListener(AlarmListener alarmListener) {
        mAlarmListener = alarmListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int taskId = intent.getIntExtra(AppConstant.TASK_ID, -1);
        int taskRunningStatus = intent.getIntExtra(AppConstant.TASK_RUNNING_STATUS, 2);
        Log.e("onReceive", "Task： ID=" + taskId + "运行状态：" + taskRunningStatus);
        if (taskId != -1) {
            if (taskRunningStatus == 0) {
//                mRxManager.post(RxEvent.EXECUTE_TASK, taskId);
            } else if (taskRunningStatus == 1) {
//                mRxManager.post(RxEvent.FINISH_TASK, taskId);
            }
        }
    }


    public interface AlarmListener {
        void executeTask(int taskId);
        void finishTask(int taskId);
    }
}
