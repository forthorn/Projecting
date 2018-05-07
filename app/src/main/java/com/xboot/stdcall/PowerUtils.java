package com.xboot.stdcall;

import java.util.Calendar;

/**
 *
 */
public class PowerUtils {

    public static boolean cancelPowerOnOff() {
        try {
            return setPowerOnOff((byte) 0, (byte) 5, (byte) 0, (byte) 5, (byte) 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

//    @Override
//    public void setPowerOnAtTime(long milliseconds) {
//        Intent intent = new Intent("rk.android.SET_RTC_TIME");
//        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
//
//        Calendar instance = Calendar.getInstance();
//        instance.setTimeInMillis(milliseconds);
//        intent.putExtra("hour", instance.get(Calendar.HOUR_OF_DAY));
//        intent.putExtra("minute", instance.get(Calendar.MINUTE));
//        mContext.sendBroadcast(intent);
//    }


    public static boolean setPowerOnOff(long mTimeOff, long mTimeOn) {
        if (mTimeOn == -1 || mTimeOff == -1) {
            return false;
        }
        long nowTime = Calendar.getInstance().getTimeInMillis();
        // 关机
        long second = (mTimeOff - nowTime) / 1000;// 总共多少秒
        byte eH = (byte) (second / 3600);// 总共多少小时
        byte eM = (byte) ((second / 60) % 60);// 剩余分钟

        // 开机
        second = (mTimeOn - mTimeOff) / 1000;
        byte sH = (byte) (second / 3600);
        byte sM = (byte) ((second / 60) % 60);
        return setPowerOnOff(sH, sM, eH, eM, (byte) 3);
    }

    protected static boolean setPowerOnOff(byte on_h, byte on_m, byte off_h, byte off_m,
                                           byte enable) {
        try {
            int fd = posix.open("/dev/McuCom", posix.O_RDWR, 0666);
            // 颠倒了
            posix.poweronoff(on_h, on_m, off_h, off_m, enable, fd);
            posix.close(fd);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
