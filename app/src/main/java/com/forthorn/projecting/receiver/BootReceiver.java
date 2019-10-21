package com.forthorn.projecting.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.forthorn.projecting.HomeActivity;
import com.forthorn.projecting.util.LogUtils;

/**
 * Created by: Forthorn
 * Date: 11/6/2017.
 * Description:
 */

public class BootReceiver extends BroadcastReceiver {

    public static final String ACTION_LAUNCH_APP = "com.forthorn.projecting.LAUNCH";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_LAUNCH_APP.equals(intent.getAction())) {
            LogUtils.e("守护服务", "守护服务重新打开应用");
            Intent intent2 = new Intent(context, HomeActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
        } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            LogUtils.e("开机启动", "监听到开机广播，打开应用");
            Intent intent2 = new Intent(context, HomeActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
        }
    }
}
