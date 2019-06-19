package com.xboot.stdcall;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.forthorn.projecting.Config;
import com.forthorn.projecting.HomeActivity;
import com.forthorn.projecting.app.AppApplication;
import com.juli.settimezone.cn.Mainforsettimezone;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 */
public class PowerUtils {


    public static boolean cancelPowerOnOff(Context context, String brand, Mainforsettimezone mainforsettimezone) {
        // TODO: 2019-05-23  根据品牌判断
        if (HomeActivity.BRAND_QUANZHI.equals(brand)) {
            //取消关机
            Intent shutdownintent = new Intent("com.example.jt.shutdowntime");
            shutdownintent.putExtra("message", "cancel");
            context.sendBroadcast(shutdownintent);
            //取消开机
            Intent bootintent = new Intent("com.example.jt.boottime");
            bootintent.putExtra("message", "0,0,0,0,0");
            context.sendBroadcast(bootintent);
            return true;
        } else if (HomeActivity.BRAND_ROCKCHIP.equals(brand)) {
            //MODE=2 星期， MODE=3每天
            mainforsettimezone.clean();
            context.sendBroadcast(new Intent("android.time.zone.clean"));
//            context.sendBroadcast(new Intent("android.timezone.setagan"));
            return true;
        } else {
            try {
                if (Config.DEBUG) {
                    Toast.makeText(context, "取消开关机", Toast.LENGTH_SHORT).show();
                }
                return setPowerOnOff((byte) 0, (byte) 3, (byte) 0, (byte) 3, (byte) 0);
            } catch (Exception e) {
                e.printStackTrace();
                if (Config.DEBUG) {
                    Toast.makeText(context, "取消开关机异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return false;
            }

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


    public static boolean setPowerOnOff(Context context, String brand, long mTimeOff, long mTimeOn, Mainforsettimezone mainforsettimezone) {
        if (mTimeOn == -1 || mTimeOff == -1) {
            return false;
        }
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        long nowTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).getTimeInMillis();
        if (HomeActivity.BRAND_QUANZHI.equals(brand)) {
            long timeOffOffset = mTimeOff - nowTime;
            long timeOnOffset = mTimeOn - nowTime;
            //设定开机时间
            quanzhiPowerOn(context, timeOnOffset);
            //设定关机时间
            quanzhiPowerOff(context, timeOffOffset);
            return true;
        } else if (HomeActivity.BRAND_ROCKCHIP.equals(brand)) {
            int[] on = new int[6];//开机时间,年,月,日,时,分
            int[] off = new int[6];//关机时间,年,月,日,时,分
            Calendar localCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
            localCalendar.setTime(new Date(mTimeOff));
            String str1 = String.valueOf(localCalendar.get(Calendar.YEAR));
            String str2 = String.valueOf(1 + localCalendar.get(Calendar.MONTH));
            String str3 = String.valueOf(localCalendar.get(Calendar.DAY_OF_MONTH));
            String str4 = String.valueOf(localCalendar.get(Calendar.HOUR_OF_DAY));
            String str5 = String.valueOf(localCalendar.get(Calendar.MINUTE));
            off[0] = Integer.valueOf(str1);
            off[1] = Integer.valueOf(str2);
            off[2] = Integer.valueOf(str3);
            off[3] = Integer.valueOf(str4);
            off[4] = Integer.valueOf(str5);
            off[5] = 0;
            localCalendar.setTime(new Date(mTimeOn));
            String s1 = String.valueOf(localCalendar.get(Calendar.YEAR));
            String s2 = String.valueOf(1 + localCalendar.get(Calendar.MONTH));
            String s3 = String.valueOf(localCalendar.get(Calendar.DAY_OF_MONTH));
            String s4 = String.valueOf(localCalendar.get(Calendar.HOUR_OF_DAY));
            String s5 = String.valueOf(localCalendar.get(Calendar.MINUTE));
            on[0] = Integer.valueOf(s1);
            on[1] = Integer.valueOf(s2);
            on[2] = Integer.valueOf(s3);
            on[3] = Integer.valueOf(s4);
            on[4] = Integer.valueOf(s5);
            on[5] = 0;
            localCalendar.setTime(new Date());
            mainforsettimezone.Newtimeinfter(on, off, 0);
            return true;
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 关机
            long second = (mTimeOff - nowTime) / 1000;// 总共多少秒
            Long eH = second / 3600;// 总共多少小时
            Long eM = (second / 60) % 60;// 剩余分钟
            // 开机
            second = (mTimeOn - mTimeOff) / 1000;
            Long sH = (second / 3600);
            Long sM = ((second / 60) % 60);

            Log.e("setPowerOnOff", "off" + simpleDateFormat.format(mTimeOff) + "On:" +
                    simpleDateFormat.format(mTimeOn) + "sH：" + sH + "sM" + sM
                    + "eH:" + eH + "eM:" + eM);
            if (Config.DEBUG) {
                Toast.makeText(context, "off" + simpleDateFormat.format(mTimeOff) + "On:" +
                        simpleDateFormat.format(mTimeOn) + "sH：" + sH.byteValue() + "sM" + sM.byteValue()
                        + "eH:" + eH.byteValue() + "eM:" + eM.byteValue(), Toast.LENGTH_SHORT).show();
            }
            return setPowerOnOff(sH.byteValue(), sM.byteValue(), eH.byteValue(), eM.byteValue(), (byte) 3);
        }
    }

    protected static boolean setPowerOnOff(byte on_h, byte on_m, byte off_h, byte off_m,
                                           byte enable) {
        try {
            int fd = posix.open("/dev/McuCom", posix.O_RDWR, 0666);
            // 颠倒了
            int ret = posix.poweronoff(off_h, off_m, on_h, on_m, enable, fd);
            posix.close(fd);
            if (Config.DEBUG) {
                Toast.makeText(AppApplication.getApplication(), "设置开关机成功：fd: " + fd + ", ret:" + ret, Toast.LENGTH_SHORT).show();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (Config.DEBUG) {
                Toast.makeText(AppApplication.getApplication(), "设置开关机异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }


    private static void quanzhiPowerOn(Context context, long timeOffset) {
        // TODO: 2019-05-23  如果定时开关机的时间距离现在大于24小时，则不进行设置，否则将会错乱
        if (timeOffset > 24 * 60 * 60 * 1000) {
            return;
        }
        long time = new Date().getTime() + timeOffset;
        String boottime = "1,0,0" + getStringTime(time);
        Intent bootintent = new Intent("com.example.jt.boottime");
        bootintent.putExtra("message", boottime);
        context.sendBroadcast(bootintent);
    }


    private static void quanzhiPowerOff(Context context, long timeOffset) {
        // TODO: 2019-05-23  如果定时开关机的时间距离现在大于24小时，则不进行设置，否则将会错乱
        if (timeOffset > 24 * 60 * 60 * 1000) {
            return;
        }
        long time = new Date().getTime() + timeOffset;
        String shutdownTime = "1,0,0" + getStringTime(time);
        Intent shutdownintent = new Intent("com.example.jt.shutdowntime");
        shutdownintent.putExtra("message", shutdownTime);
        context.sendBroadcast(shutdownintent);
    }


    private static String getStringTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH,mm");
        String OriTime = format.format(time);
        String uploadTime = "";
        if (OriTime != null) {
            String[] spliteInfo = OriTime.split(",");
            int[] convertTime = new int[spliteInfo.length];
            String[] convertInfo = new String[spliteInfo.length];
            String convertStartTime = "";
            for (int i = 0; i < spliteInfo.length; i++) {
                convertTime[i] = Integer.parseInt(spliteInfo[i]);
                if (convertTime[i] < 10) {
                    convertInfo[i] = convertTime[i] + "";
                    convertStartTime = convertStartTime + "," + convertInfo[i];
                } else {
                    convertInfo[i] = convertTime[i] + "";
                    convertStartTime = convertStartTime + "," + spliteInfo[i];
                }
            }
            uploadTime = convertStartTime;
            Log.e("getStringTime", "UploadTime" + uploadTime);
        }
        return uploadTime;
    }


}
