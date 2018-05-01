package com.xboot.stdcall;

import java.util.Calendar;

public class PowerUtils {

    public void cancelPowerOnOff() {
        try {
            setPowerOnOff((byte) 0, (byte) 5, (byte) 0, (byte) 5, (byte) 0);
        } catch (Exception e) {
            e.printStackTrace();
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


    public void setPowerOnOff(long mTimeOff, long mTimeOn) {
        if (mTimeOn == -1 || mTimeOff == -1) {
            return;
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
        setPowerOnOff(sH, sM, eH, eM, (byte) 3);
    }

    protected void setPowerOnOff(byte on_h, byte on_m, byte off_h, byte off_m,
                                 byte enable) {
        try {
            int fd = posix.open("/dev/McuCom", posix.O_RDWR, 0666);
            // 颠倒了
            posix.poweronoff(on_h, on_m, off_h, off_m, enable, fd);
            posix.close(fd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
