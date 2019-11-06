package com.xboot.stdcall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.forthorn.projecting.Config;
import com.forthorn.projecting.HomeActivity;
import com.forthorn.projecting.app.AppApplication;
import com.juli.settimezone.cn.Mainforsettimezone;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 */
public class PowerUtils {


    private static PowerUtils sInstance;
    private Handler mHandler;
//    public static final int MSG_CANCEL = 9001;
    public static final int MSG_POWER_ON = 9002;
    public static final int MSG_POWER_OFF = 9003;
    public static final int MSG_CHECK_SCREEN = 9004;

    public static final int SCREEN_OFF = 0;
    public static final int SCREEN_ON = 1;

    private PowerUtils() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_POWER_ON:
                        //开机
                        Log.e("MSG_POWER_ON", "MSG_POWER_ON");
                        powerOn();
                        break;
                    case MSG_POWER_OFF:
                        //关机
                        Log.e("MSG_POWER_OFF", "MSG_POWER_OFF");
                        powerOff();
                        break;
                    case MSG_CHECK_SCREEN:
                        Log.e("MSG_CHECK_SCREEN", "MSG_CHECK_SCREEN");
                        checkScreen();
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 检测屏幕状态
     */
    private void checkScreen() {
        Intent intent = new Intent(HomeActivity.ACTION_CHECK_SCREEN);
        AppApplication.getApplication().sendBroadcast(intent);
    }

    /**
     * x88没法真正关机，只能休眠
     */
    private void powerOff() {
        Log.e("执行关机", "执行关机");
        // TODO: 2019-10-28  关机的话，需要断开一切连接，清除任务
        KeyUtil.getInstance().performKey(KeyEvent.KEYCODE_POWER);
    }

    /**
     * 重启
     * 可能广播生效，可能命令行生效
     */
    private void powerOn() {
        Log.e("执行开机", "执行开机");
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        AppApplication.getApplication().sendBroadcast(intent);
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * X88机型睡眠
     */
    public void sleep() {
        KeyUtil.getInstance().performKey(KeyEvent.KEYCODE_POWER);
    }

    /**
     * X88机型唤醒
     */
    public void wakeUp() {
        KeyUtil.getInstance().performKey(KeyEvent.KEYCODE_POWER);
    }

    private static class InstanceHolder {
        private static PowerUtils sInstance = new PowerUtils();
    }

    public static PowerUtils getInstance() {
        return InstanceHolder.sInstance;
    }


    /**
     * 设置屏幕开关状态
     *
     * @param value 0-关 ，1-开
     */
    @SuppressLint("DefaultLocale")
    public void setScreenStatus(int value) {
        delayCheckScreenStatus();
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", String.format("echo %d > /sys/class/display/HDMI/enable", value)});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void delayCheckScreenStatus() {
        mHandler.removeMessages(MSG_CHECK_SCREEN);
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_SCREEN, 60 * 1000);
    }

    public boolean cancelPowerOnOff(Context context, String brand, Mainforsettimezone mainforsettimezone) {
        // TODO: 2019-05-23  根据品牌判断
        if (HomeActivity.BRAND_ROCKX88.equals(brand)) {
            Log.e("cancelPowerOnOff", "cancelPowerOnOff");
//            mHandler.removeMessages(MSG_CANCEL);
            mHandler.removeMessages(MSG_POWER_ON);
            mHandler.removeMessages(MSG_POWER_OFF);
            return true;
        } else if (HomeActivity.BRAND_QUANZHI.equals(brand)) {
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


    public boolean setPowerOnOff(Context context, String brand, long mTimeOff, long mTimeOn, Mainforsettimezone mainforsettimezone) {
        if (mTimeOn == -1 || mTimeOff == -1) {
            return false;
        }
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        long nowTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).getTimeInMillis();
        if (HomeActivity.BRAND_ROCKX88.equals(brand)) {
            long timeOffOffset = mTimeOff - nowTime;
            long timeOnOffset = mTimeOn - nowTime;
//             TODO: 2019-10-28  测试
//            timeOffOffset = 60000L;
//            timeOnOffset = 120000L;
            mHandler.removeMessages(MSG_POWER_ON);
            mHandler.removeMessages(MSG_POWER_OFF);
            Log.e("设置开关机时间Msg", "关机时间：" + timeOffOffset);
            Log.e("设置开关机时间Msg", "开机时间：" + timeOnOffset);
            mHandler.sendEmptyMessageDelayed(MSG_POWER_OFF, timeOffOffset);
            mHandler.sendEmptyMessageDelayed(MSG_POWER_ON, timeOnOffset);
            return true;
        } else if (HomeActivity.BRAND_QUANZHI.equals(brand)) {
            long timeOffOffset = mTimeOff - nowTime;
            long timeOnOffset = mTimeOn - nowTime;
            //设定开机时间
            quanzhiPowerOn(context, timeOnOffset);
            //设定关机时间
            quanzhiPowerOff(context, timeOffOffset);
            return true;
        } else if (HomeActivity.BRAND_ROCKCHIP.equals(brand)) {
            //开机时间,年,月,日,时,分
            int[] on = new int[6];
            //关机时间,年,月,日,时,分
            int[] off = new int[6];
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
            // 总共多少秒
            long second = (mTimeOff - nowTime) / 1000;
            // 总共多少小时
            long eH = second / 3600;
            // 剩余分钟
            long eM = (second / 60) % 60;
            // 开机
            second = (mTimeOn - mTimeOff) / 1000;
            long sH = (second / 3600);
            long sM = ((second / 60) % 60);

            Log.e("setPowerOnOff", "off" + simpleDateFormat.format(mTimeOff) + "On:" +
                    simpleDateFormat.format(mTimeOn) + "sH：" + sH + "sM" + sM
                    + "eH:" + eH + "eM:" + eM);
            if (Config.DEBUG) {
                Toast.makeText(context, "off" + simpleDateFormat.format(mTimeOff) + "On:" +
                        simpleDateFormat.format(mTimeOn) + "sH：" + (byte) sH + "sM" + (byte) sM
                        + "eH:" + (byte) eH + "eM:" + (byte) eM, Toast.LENGTH_SHORT).show();
            }
            return setPowerOnOff((byte) sH, (byte) sM, (byte) eH, (byte) eM, (byte) 3);
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
        long time = System.currentTimeMillis() + timeOffset;
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
        long time = System.currentTimeMillis() + timeOffset;
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
