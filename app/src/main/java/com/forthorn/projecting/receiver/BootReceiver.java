package com.forthorn.projecting.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.forthorn.projecting.HomeActivity;
import com.forthorn.projecting.SplashActivity;

/**
 * Created by: Forthorn
 * Date: 11/6/2017.
 * Description:
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent intent2 = new Intent(context, SplashActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
        }
    }
}
