package com.forthorn.projecting;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.danikula.videocache.HttpProxyCacheServer;
import com.forthorn.projecting.api.Api;
import com.forthorn.projecting.api.HostType;
import com.forthorn.projecting.app.AppApplication;
import com.forthorn.projecting.app.AppConstant;
import com.forthorn.projecting.app.BundleKey;
import com.forthorn.projecting.app.DeviceUuidFactory;
import com.forthorn.projecting.app.Status;
import com.forthorn.projecting.baserx.BaseResponse;
import com.forthorn.projecting.baserx.RxEvent;
import com.forthorn.projecting.baserx.RxManager;
import com.forthorn.projecting.db.DBUtils;
import com.forthorn.projecting.downloader.Downloader;
import com.forthorn.projecting.entity.Download;
import com.forthorn.projecting.entity.Event;
import com.forthorn.projecting.entity.IMAccount;
import com.forthorn.projecting.entity.Schedule;
import com.forthorn.projecting.entity.Task;
import com.forthorn.projecting.entity.TaskRes;
import com.forthorn.projecting.func.picture.AutoViewPager;
import com.forthorn.projecting.func.picture.PictureAdapter;
import com.forthorn.projecting.receiver.AlarmReceiver;
import com.forthorn.projecting.util.GsonUtils;
import com.forthorn.projecting.util.LogUtils;
import com.forthorn.projecting.util.SPUtils;
import com.forthorn.projecting.util.ToastUtil;
import com.forthorn.projecting.video.IVideoListener;
import com.forthorn.projecting.video.VideoView;
import com.forthorn.projecting.widget.AutoScrollTextView;
import com.forthorn.projecting.widget.LogView;
import com.forthorn.projecting.widget.NoticeDialog;
import com.juli.settimezone.cn.Mainforsettimezone;
import com.xboot.stdcall.PowerUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.IntegerCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.OfflineMessageEvent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.functions.Action1;

import static com.forthorn.projecting.app.Status.IDLE;


/**
 * 修改默认播放器：{@link com.forthorn.libijk.application.Settings#getPlayer}
 * 修改UUID: {@link HomeActivity#initData()}
 * 板子1 使用sign1
 * 全志 使用 sign2
 * 聚力 使用 sign3 瑞芯微
 */
public class HomeActivity extends Activity implements View.OnClickListener, AlarmReceiver.AlarmListener, IVideoListener {
    //视频
    private FrameLayout mVideoFl;
    private VideoView mVideoView;
    //图片
    private FrameLayout mPictureFl;
    private AutoViewPager mPicturePager;
    private PictureAdapter mPictureAdapter;
    //待机
    private FrameLayout mIdleFl;
    private ImageView mIdleBgIv;
    private ImageView mIdleAboutIv;
    private ImageView mIdleQrcodeIv;
    private TextView mIdleQrcodeTv;
    private LinearLayout mIdleServerStatusLl;
    private TextView mIdleServerStatusTv;
    //文本
    private FrameLayout mTextFl;
    private LinearLayout mTextLl;
    private LinearLayout mTextTvLl;
    private AutoScrollTextView mTextTv;
    private LinearLayout mTextServerHolderLl;
    //log
    private LogView mLogView;

    private Context mContext;
    private List<String> mPicList = new ArrayList<>();
    private Status mStatus;

    private String mUuid;
    private String mIMUsername;
    private String mIMPassword;
    private int mDeviceId;
    private String mDeviceCode;

    private AudioManager mAudioManager;
    private AlarmManager mAlarmManager;
    private AlarmReceiver mAlarmReceiver;

    private RxManager mRxManager;

    private String mSnapFileName;
    private String mSnapFilePath;

    private static final int HANDLER_MESSAGE_TIMING_LOGIN = 0X2;
    private static final int HANDLER_MESSAGE_TIMING_REQUESR_ACCOUNT = 0X3;
    private static final int HANDLER_MESSAGE_TIMING_REQUESR_MESSAGE = 0X4;
    private static final int HANDLER_MESSAGE_START_ALARM = 0X5;
    private static final int HANDLER_MESSAGE_FINISH_ALARM = 0X6;

    private static final int HANDLER_MESSAGE_VOLUME_START = 0X6;
    private static final int HANDLER_MESSAGE_VOLUME_END = 0X7;

    private long nextHourTimeStamp = 0L;
    private List<Integer> mTaskIdList = new ArrayList<>();

    private Map<Integer, Integer> mTaskIds = new HashMap<>();
    private SparseArray<Task> mTaskSparseArray = new SparseArray<>();
    private static final int LOGIN_TIME = 1800000;


    //是否是在执行插播的任务
    private boolean mInterCutting;
    //插播的任务
    private Task mInterCuttingTask;
    private HttpProxyCacheServer proxy;

    //品牌
    private String mBrand = Build.BRAND;

    public static final String BRAND_ONE = "Allwinner";
    public static final String BRAND_QUANZHI = "softwinners";
    public static final String BRAND_ROCKCHIP = "rockchip";

    private Mainforsettimezone mMainforsettimezone;

    private int default_volume = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        proxy = AppApplication.getProxy(this);
        mRxManager = new RxManager();
        mContext = HomeActivity.this;
        mMainforsettimezone = new Mainforsettimezone(this);
        sendBroadcast(new Intent().setAction("android.action.juli.HIDE_STATUSBAR"));
        SQLiteStudioService.instance().start(mContext);
        setContentView(R.layout.activity_home);
        initView();
        initData();
        initEvent();
        initPlayer();
        checkOnOff();
        initIM();
        queryTask();
        querySchedule();
        //每十分钟登录一下
        mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_LOGIN, 600000);
//        mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_LOGIN, LOGIN_TIME);
//        setRequestAlarm();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        sendBroadcast(new Intent().setAction("android.action.juli.DISPLAY_STATUSBAR"));
        mVideoView.stopPlayback();
        mHandler.removeCallbacksAndMessages(null);
        SQLiteStudioService.instance().stop();
        unregisterReceiver(mAlarmReceiver);
        JMessageClient.unRegisterEventReceiver(this);
        mRxManager.clear();
        super.onDestroy();
    }

    private void querySchedule() {
        if (mDeviceId == 0) {
            return;
        }
        Call<Schedule> scheduleCall = Api.getDefault(HostType.VOM_HOST).getOnOff(Api.getCacheControl(), String.valueOf(mDeviceId));
        scheduleCall.enqueue(new Callback<Schedule>() {
            @Override
            public void onResponse(Call<Schedule> call, Response<Schedule> response) {
                Schedule schedule = response.body();
                if (schedule == null) {
                    return;
                }
                if (schedule.getData() != null) {
                    DBUtils.getInstance().updateSchedule(schedule.getData());
                    handleOnOff(schedule.getData());
                }
                if (!TextUtils.isEmpty(schedule.getDefault_volume())) {
                    default_volume = Integer.parseInt(schedule.getDefault_volume());
                }
                handleScheduleVolume(schedule.getVolume_data());
            }

            @Override
            public void onFailure(Call<Schedule> call, Throwable t) {
            }
        });
    }


    private void updateStatus() {
        if (mDeviceId != 0) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int i) {

                }
            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.abandonAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int i) {

                }
            });
            int targetVolume = (int) Math.ceil(current * 100D / maxVolume);
            Call<BaseResponse> updateCall = Api.getDefault(HostType.VOM_HOST).updateStatus(Api.getCacheControl(),
                    mDeviceId, AppConstant.STATUS_WAKE_UP, targetVolume, mInterCutting ? 1 : 0);
            updateCall.enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    BaseResponse baseResponse = response.body();
                    Log.e("updateStatus", baseResponse == null ? "更新信息失败" : baseResponse.getMsg() + "");
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    Log.e("updateStatus", t == null ? "更新信息失败" : t.getMessage() + "");
                }
            });
        }
    }


    private long getNextHourStamp() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        cal.set(Calendar.HOUR_OF_DAY, hour + 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long nextHourTime = cal.getTimeInMillis();
        return nextHourTime / 1000L;
    }

    private void setRequestAlarm() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        Log.e("Alarm", "现在是：" + sdf.format(new Date()));
        //Toast.makeText(mContext, "现在是：" + sdf.format(new Date()), //Toast.LENGTH_SHORT).show();
        nextHourTimeStamp = getNextHourStamp();
        //Toast.makeText(mContext, "下一个整点是：" + sdf.format(new Date(nextHourTimeStamp * 1000L)), //Toast.LENGTH_SHORT).show();
        Log.e("Alarm", "下一个整点是：" + sdf.format(new Date(nextHourTimeStamp * 1000L)));
        //Toast.makeText(mContext, "下一个请求将在：" + sdf.format(new Date(nextHourTimeStamp * 1000L - 300000L)), //Toast.LENGTH_SHORT).show();
        long nextTime = nextHourTimeStamp * 1000L - 3540000L;
        if (nextTime - System.currentTimeMillis() < 0) {
            nextTime = nextTime + 3600000L;
            requestTasks(nextHourTimeStamp);
        }
        LogUtils.e("Alarm", "下一个请求将在：" + sdf.format(new Date(nextTime)));
        LogUtils.e("Alarm", (nextTime - System.currentTimeMillis()) / 1000 + "秒后请求");
        mHandler.removeMessages(HANDLER_MESSAGE_TIMING_REQUESR_MESSAGE);
        mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_REQUESR_MESSAGE, nextTime - System.currentTimeMillis());
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            LogUtils.e("handlerMsg", "what:" + msg.what + "__arg1:" + msg.arg1);
            switch (msg.what) {
                case HANDLER_MESSAGE_TIMING_LOGIN:
                    //Toast.makeText(mContext, "每十分钟登录一次", //Toast.LENGTH_SHORT).show();
//                    requestIMAccount();
                    mHandler.removeMessages(HANDLER_MESSAGE_TIMING_LOGIN);
                    mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_LOGIN, 600000L);
//                    mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_LOGIN, LOGIN_TIME);
                    doAfterLogin();
                    break;
                case HANDLER_MESSAGE_TIMING_REQUESR_ACCOUNT:
                    requestIMAccount();
                    break;
                case HANDLER_MESSAGE_TIMING_REQUESR_MESSAGE:
                    //Toast.makeText(mContext, "提前请求下一个时间段任务", //Toast.LENGTH_SHORT).show();
                    nextHourTimeStamp = getNextHourStamp();
                    requestTasks(nextHourTimeStamp);
                    mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_REQUESR_MESSAGE, 3600000L);
                    break;
                case HANDLER_MESSAGE_VOLUME_START:
                    //设置音量
                    // TODO: 2019-06-15
                    int volume = msg.arg1;
                    adjustScheduleVolume(volume);
                    break;
                case HANDLER_MESSAGE_VOLUME_END:
                    //还原音量
                    if (default_volume != -1) {
                        adjustScheduleVolume(default_volume);
                    }
                    break;
            }
            if (msg.what != HANDLER_MESSAGE_TIMING_LOGIN
                    && msg.what != HANDLER_MESSAGE_TIMING_REQUESR_ACCOUNT
                    && msg.what != HANDLER_MESSAGE_TIMING_REQUESR_MESSAGE
                    && msg.what != HANDLER_MESSAGE_VOLUME_START
                    && msg.what != HANDLER_MESSAGE_VOLUME_END) {
                int taskId = msg.what;
                LogUtils.e("任务消息", "taskID=" + taskId);
                int type = msg.arg1;
                if (type == 0) {
                    return;
                }
                switch (type) {
                    case HANDLER_MESSAGE_START_ALARM:
                        LogUtils.e("任务消息", "task类型=执行任务");
                        executeTask(taskId);
                        break;
                    case HANDLER_MESSAGE_FINISH_ALARM:
                        LogUtils.e("任务消息", "task类型=结束任务");
                        finishTask(taskId);
                        break;
                }
            }
        }
    };

    /**
     * 获取任务
     */
    private void requestTasks(Long timeStamp) {
        if (mDeviceId == 0) {
            return;
        }
        //登录IM
        login();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss:SSS");
        if (Config.DEBUG) {
            String log = "查询" + sdf.format(new Date(timeStamp * 1000L)) + "的任务";
            Toast.makeText(mContext, log, Toast.LENGTH_SHORT).show();
            mLogView.append(log);
        }
        LogUtils.e("request", "请求：" + sdf.format(new Date(timeStamp * 1000L)) + "的任务");
        //Toast.makeText(mContext, "查询" + sdf.format(new Date(timeStamp * 1000L)) + "的任务", //Toast.LENGTH_SHORT).show();
        Call<TaskRes> taskResCall = Api.getDefault(HostType.VOM_HOST).getTaskList(Api.getCacheControl(),
                String.valueOf(mDeviceId), String.valueOf(timeStamp));
        taskResCall.enqueue(new Callback<TaskRes>() {
            @Override
            public void onResponse(Call<TaskRes> call, Response<TaskRes> response) {
                LogUtils.e("查询任务", "结果：" + response.raw().body().toString());
                TaskRes taskRes = response.body();
                if (taskRes == null) {
                    //Toast.makeText(mContext, "查询失败：空", //Toast.LENGTH_SHORT).show();
                    return;
                }
                LogUtils.e("查询任务", "结果：" + taskRes.toString());
                if (taskRes.getData() == null || taskRes.getData().isEmpty()) {
                    //Toast.makeText(mContext, "查询：当前任务数为：0", //Toast.LENGTH_SHORT).show();
                    return;
                }
                //Toast.makeText(mContext, "查询：当前任务数为：" + taskRes.getData().size(), //Toast.LENGTH_SHORT).show();
                for (Task task : taskRes.getData()) {
                    handleTask(task);
                }
            }

            @Override
            public void onFailure(Call<TaskRes> call, Throwable t) {
                LogUtils.e("查询任务", "结果：" + t.getMessage());

            }
        });
    }


    private void queryTask() {
        DBUtils.getInstance().deleteOverdueTask();
        List<Task> list = DBUtils.getInstance().findPauseTask();
        if (!list.isEmpty()) {
            for (Task task : list) {
                executeTask(task.getId());
            }
            LogUtils.e("queryTask", list.toArray().toString());
        } else {
            LogUtils.e("queryTask", "查询目前任务列表为空");
        }
    }


    private void initPlayer() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mVideoView.setUp(this, VideoView.TYPE_VLC);
    }

    private void initIM() {
        JMessageClient.registerEventReceiver(this);
        requestIMAccount();
    }

    private void initView() {
        Window _window = getWindow();
        WindowManager.LayoutParams params = _window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        _window.setAttributes(params);

        //视频
        mVideoFl = (FrameLayout) findViewById(R.id.video_fl);
        mVideoView = (VideoView) findViewById(R.id.video_view);
        //图片
        mPictureFl = (FrameLayout) findViewById(R.id.picture_fl);
        mPicturePager = (AutoViewPager) findViewById(R.id.picture_view_pager);
        //待机
        mIdleFl = (FrameLayout) findViewById(R.id.idle_fl);
        mIdleBgIv = (ImageView) findViewById(R.id.idle_bg_iv);
        mIdleAboutIv = (ImageView) findViewById(R.id.idle_about_iv);
        mIdleQrcodeIv = (ImageView) findViewById(R.id.idle_qrcode_iv);
        mIdleQrcodeTv = (TextView) findViewById(R.id.idle_qrcode_tv);
        mIdleServerStatusLl = (LinearLayout) findViewById(R.id.idle_server_status_ll);
        mIdleServerStatusTv = (TextView) findViewById(R.id.idle_server_status_tv);
//        mIdleAboutIv.requestFocus();
        //文字
        mTextFl = (FrameLayout) findViewById(R.id.text_fl);
        mTextLl = (LinearLayout) findViewById(R.id.text_ll);
        mTextTvLl = (LinearLayout) findViewById(R.id.text_tv_ll);
        mTextTv = (AutoScrollTextView) findViewById(R.id.text_tv);
        mTextServerHolderLl = (LinearLayout) findViewById(R.id.text_server_holder_ll);
        mIdleAboutIv.setOnClickListener(this);

        //Log
        mLogView = (LogView) findViewById(R.id.log_view);
//        mLogView.setVisibility(BuildConfig.DEBUG || true ? View.VISIBLE : View.GONE);
        mLogView.setVisibility(Config.DEBUG ? View.VISIBLE : View.GONE);
    }

    private void initData() {
        DeviceUuidFactory uuidFactory = new DeviceUuidFactory(mContext);
        mUuid = uuidFactory.getDeviceUuid().toString();
//        mUuid = "c3d30ab2-1139-300a-830f-bc4e6900c015";
//        mUuid = "bb46a94c-0169-3914-bd0c-8705b0ff8a22";
//        mUuid = "bb46a94c-0169-3914-bd0c-8705b0ff8a22";
//        mUuid = "12aa0a79-bd17-3b55-b07a-42e6b1d54bfd";
//        mUuid = "16d98032-36d7-3467-98f9-ee2086a6eb71";
//        mUuid = "81292800-f23c-3ff9-bab2-469732b4d806";
//        mUuid = "a37a8a78-b8fb-3e09-8295-405983d8069c";
//        mUuid = "c12280f4-d12c-3fdf-be79-583585244580"; 聚力
        SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_CODE, mUuid);
        mDeviceCode = mUuid;
        mDeviceId = SPUtils.getSharedIntData(mContext, BundleKey.DEVICE_ID);
        Glide.with(mContext).load(R.drawable.ic_idle_bg).into(mIdleBgIv);
        mIdleBgIv.requestFocus();
        mStatus = IDLE;
    }

    private void initEvent() {
        mAlarmReceiver = new AlarmReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstant.ALARM_INTENT);
        registerReceiver(mAlarmReceiver, intentFilter);
        mAlarmReceiver.setAlarmListener(this);
        mRxManager.on(RxEvent.EXECUTE_TASK, new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                executeTask(integer);
            }
        });
        mRxManager.on(RxEvent.FINISH_TASK, new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                finishTask(integer);
            }
        });
    }


    /**
     * 获取IM账号
     */
    private void requestIMAccount() {
        Call<IMAccount> imAccountCall = Api.getDefault(HostType.VOM_HOST).getIMAccount(Api.getCacheControl(), mUuid);
        imAccountCall.enqueue(new Callback<IMAccount>() {
            @Override
            public void onResponse(Call<IMAccount> call, Response<IMAccount> response) {
                IMAccount imAccount = response.body();
                if (imAccount == null || imAccount.getData() == null) {
                    mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_REQUESR_ACCOUNT, 60000);
                    login();
                    return;
                }
                mIMUsername = imAccount.getData().getEquipment_im_account();
                mIMPassword = imAccount.getData().getEquipment_im_password();
                mDeviceId = imAccount.getData().getEquipment_id();
                mDeviceCode = imAccount.getData().getEquipment_code();
                SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_ADDRESS, imAccount.getData().getAddress());
                SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_NAME, imAccount.getData().getEquipment_name());
                SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_AREA,
                        imAccount.getData().getProvince() + "  " + imAccount.getData().getCity());
                SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_TYPE, imAccount.getData().getType());
                SPUtils.setSharedStringData(mContext, BundleKey.IM_ACCOUNT, mIMUsername);
                SPUtils.setSharedStringData(mContext, BundleKey.IM_PASSWORD, mIMPassword);
                SPUtils.setSharedIntData(mContext, BundleKey.DEVICE_ID, mDeviceId);
                SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_CODE, mDeviceCode);
                doAfterLogin();
            }

            @Override
            public void onFailure(Call<IMAccount> call, Throwable t) {
                //Toast.makeText(mContext, t.getMessage(), //Toast.LENGTH_SHORT).show();
                mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_REQUESR_ACCOUNT, 60000);
                login();
            }
        });
    }

    private void doAfterLogin() {
        login();
        updateStatus();
        nextHourTimeStamp = getNextHourStamp();
        requestTasks(nextHourTimeStamp - 3600L);
        setRequestAlarm();
        querySchedule();
    }

    /**
     * 登陆IM
     */
    private void login() {
        mIMUsername = SPUtils.getSharedStringData(mContext, BundleKey.IM_ACCOUNT);
        mIMPassword = SPUtils.getSharedStringData(mContext, BundleKey.IM_PASSWORD);
        if (TextUtils.isEmpty(mIMUsername) || TextUtils.isEmpty(mIMPassword)) {
            return;
        }
        BasicCallback callback = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if (i == 0) {
                    UserInfo myInfo = JMessageClient.getMyInfo();
                    JMessageClient.getNoDisturbGlobal(new IntegerCallback() {
                        @Override
                        public void gotResult(int i, String s, Integer integer) {
                        }
                    });
                    if (Config.DEBUG) {
                        ToastUtil.shortToast(mContext, "登陆成功" + myInfo.getUserName());
                    }
                    mIdleServerStatusTv.setText("在线");
                    mIdleServerStatusTv.setEnabled(true);
                } else {
                    if (Config.DEBUG) {
                        ToastUtil.shortToast(mContext, "离线：Code:" + i + "   Reason:" + s);
                    }
                    mIdleServerStatusTv.setText("离线");
                    mIdleServerStatusTv.setEnabled(false);
                }
            }
        };
        JMessageClient.login(mIMUsername, mIMPassword, callback);
    }


    /**
     * 登出
     */
    private void logout() {
        JMessageClient.logout();
        SPUtils.setSharedStringData(mContext, BundleKey.IM_ACCOUNT, "");
        SPUtils.setSharedStringData(mContext, BundleKey.IM_PASSWORD, "");
        SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_ID, "");
        SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_NAME, "");
        SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_CODE, "");
        SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_ADDRESS, "");
        SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_AREA, "");
        SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_TYPE, "");
        // TODO: 10/31/2017  停止一切任务，设置为Idle状态
        switch (mStatus) {
            case VIDEO:
                break;
            case VIDEO_TEXT:
                break;
            case PICTURE:
                break;
            case TEXT:
                break;
            case IDLE_TEXT:
                break;
            case PICTURE_TEXT:
                break;
        }

    }


    /**
     * IM登陆状态变更回调
     *
     * @param event
     */
    public void onEventMainThread(LoginStateChangeEvent event) {
        LoginStateChangeEvent.Reason reason = event.getReason();//获取变更的原因
        UserInfo myInfo = event.getMyInfo();//获取当前被登出账号的信息
        LogUtils.e("LoginState", reason.name());
        switch (reason) {
            case user_password_change:
//                logout();
                break;
            case user_logout:
//                logout();
                break;
            case user_deleted:
//                logout();
                break;
        }
    }


    /**
     * 由于每小时都查询任务，不再进行离线消息处理
     *
     * @param event
     */
    public void onEventMainThread(OfflineMessageEvent event) {
        List<Message> newMessageList = event.getOfflineMessageList();
        for (Message message : newMessageList) {
            switch (message.getContentType()) {
                case text:  //处理文字消息
                    break;
                case image:
                    break;
                case voice:
                    break;
                case custom:
//                    CustomContent customContent = (CustomContent) message.getContent();
//                    Task task = GsonUtils.convertObj(customContent.toJson(), Task.class);
//                    LogUtils.e("CustomContent", customContent.toJson());
//                    if (task == null) {
//                        return;
//                    }
//                    LogUtils.e("offMsg", task.toString());
//                    handlerOfflineMessageEvent(task);
                    break;
                case eventNotification:
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 处理离线消息
     * 变更：由于每个小时都会请求任务，所以不进行处理
     *
     * @param task
     */
    private void handlerOfflineMessageEvent(Task task) {
        switch (task.getType()) {
            case AppConstant.TASK_TYPE_PICTURE:
                handleTask(task);
                break;
            case AppConstant.TASK_TYPE_TEXT:
                handleTask(task);
                break;
            case AppConstant.TASK_TYPE_VIDEO:
                Downloader.getInstance().download(task);
                handleTask(task);
                break;
            case AppConstant.TASK_TYPE_WEATHER:
                handWeatherTask(task);
                break;
            default:
                break;
        }
        DBUtils.getInstance().deleteOverdueTask();
        if (mStatus != Status.IDLE) {
            return;
        }
        List<Task> list = DBUtils.getInstance().findPauseTask();
        if (!list.isEmpty()) {
            for (Task task2 : list) {
                executeTask(task2.getId());
            }
            LogUtils.e("queryTask", list.toArray().toString());
        } else {
            LogUtils.e("queryTask", "查询目前任务列表为空");
        }
    }


    /**
     * IM收到消息回调
     *
     * @param event
     */
    public void onEventMainThread(MessageEvent event) {
        Message msg = event.getMessage();
//        Log.d("MessageEvent", "收到消息：" + msg.getFromUser().getUserName());
        switch (msg.getContentType()) {
            case text:  //处理文字消息
                TextContent textContent = (TextContent) msg.getContent();
//                Log.d("MessageEvent", "消息：" + textContent.getText());
                handMessageEvent(textContent.getText());
                break;
            case image:
                break;
            case voice:
                break;
            case custom:
                CustomContent customContent = (CustomContent) msg.getContent();
                if (Config.DEBUG) {
                    Log.e("CustomMessage", ":" + customContent.toString());
                    mLogView.append("接收到推送消息:" + customContent.toJson());
                }
                Task task = GsonUtils.convertObj(customContent.toJson(), Task.class);
//                LogUtils.e("CustomContent", customContent.toJson());
                if (task == null) {
                    if (Config.DEBUG) {
                        Toast.makeText(mContext, "消息格式错误", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
//                LogUtils.e("taskMessage", task.toString());
                handMessageEvent(task);
                break;
            case eventNotification:
                break;
            default:
                break;
        }
    }


    private void handMessageEvent(Task task) {
        switch (task.getType()) {
            case AppConstant.TASK_TYPE_SNAPSHOT:
                snapshot(task);
                break;
            case AppConstant.TASK_TYPE_VOLUME:
                adjustVolume(task);
                break;
            case AppConstant.TASK_TYPE_SLEEP:
                sleep();
                break;
            case AppConstant.TASK_TYPE_WAKE_UP:
                wakeUp();
                break;
            case AppConstant.TASK_TYPE_PICTURE:
                handleTask(task);
                break;
            case AppConstant.TASK_TYPE_TEXT:
                handleTask(task);
                break;
            case AppConstant.TASK_TYPE_VIDEO:
                handleTask(task);
                break;
            case AppConstant.TASK_TYPE_WEATHER:
                handWeatherTask(task);
                break;
            case AppConstant.TASK_TYPE_ON_OFF:
                handleOnOffTask(task);
                break;
            case AppConstant.TASK_TYPE_INTERCUT_VIDEO:
                handleIntercutVideo(task);
                break;
            case AppConstant.TASK_TYPE_SCHEDULE_VOLUME:
                handleScheduleVolumeTask(task);
                break;
            default:
                break;
        }
    }


    /**
     * 处理定时音量任务
     *
     * @param task
     */
    private void handleScheduleVolumeTask(Task task) {
        if (task == null) {
            return;
        }
        if (!TextUtils.isEmpty(task.getDefault_volume())) {
            default_volume = Integer.parseInt(task.getDefault_volume());
        }
        List<Schedule.ScheduleVolumeBean> volume_data = task.getVolume_data();
        handleScheduleVolume(volume_data);
    }

    /**
     * 处理定时音量任务
     *
     * @param
     * @param volume_data
     */
    private void handleScheduleVolume(List<Schedule.ScheduleVolumeBean> volume_data) {
        // TODO: 2019-06-15  定时音量任务
        //清空定时音量消息
        mHandler.removeMessages(HANDLER_MESSAGE_VOLUME_START);
        mHandler.removeMessages(HANDLER_MESSAGE_VOLUME_END);
        if (volume_data == null || volume_data.isEmpty()) {
            return;
        }
        //重新设置值，从数据中取值
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        calendar.setTime(new Date());
        int size = volume_data.size();
        //处理数据
        long[] starts = new long[size];
        long[] ends = new long[size];
        long now = System.currentTimeMillis();
        for (int i = 0; i < volume_data.size(); i++) {
            Schedule.ScheduleVolumeBean volumeBean = volume_data.get(i);
            String[] startTimes = volumeBean.getStartTime().split(":");
            String[] endTimes = volumeBean.getEndTime().split(":");
            if (startTimes.length != 2) {
                break;
            }
            if (endTimes.length != 2) {
                break;
            }
            int startHour = Integer.parseInt(startTimes[0]);
            int startMin = Integer.parseInt(startTimes[1]);
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            calendar.set(Calendar.MINUTE, startMin);
            calendar.set(Calendar.SECOND, 0);
            long startTime = calendar.getTimeInMillis();
            int endHour = Integer.parseInt(endTimes[0]);
            int endMin = Integer.parseInt(endTimes[1]);
            calendar.set(Calendar.HOUR_OF_DAY, endHour);
            calendar.set(Calendar.MINUTE, endMin);
            calendar.set(Calendar.SECOND, 0);
            long endTime = calendar.getTimeInMillis();
            //结束时间小于起始时间，说明跨天了，结束时间需要跨天
            if (endTime < startTime) {
                endTime = endTime + 24 * 60 * 60 * 1000;
            }
            //如果结束时间小于现在，则需要往后推一天
            if (endTime < now) {
                startTime = startTime + 24 * 60 * 60 * 1000;
                endTime = endTime + 24 * 60 * 60 * 1000;
            }
            starts[i] = startTime;
            ends[i] = endTime;
        }
        //这时候拿到的数组里面的数据都是处理过的数据，从开始时间中取其中最小的数据
        int index = 0;
        for (int i = 0; i < starts.length; i++) {
            if (starts[i] < starts[index]) {
                index = i;
            }
        }
        //如果这个最小值结束时间还小于当前时间，说明时间数据有问题，不设置了
        if (ends[index] < now) {
            return;
        }
        //拿到了最小的时间的值，设置该值
        Schedule.ScheduleVolumeBean volume = volume_data.get(index);
        //发送定时音量开启消息
        android.os.Message message = mHandler.obtainMessage();
        message.what = HANDLER_MESSAGE_VOLUME_START;
        message.arg1 = volume.getValue();
        long startDelay = starts[index] - now;
        if (startDelay < 0) {
            startDelay = 100;
        }
        mHandler.sendMessageDelayed(message, startDelay);
        //发送定时音量关闭消息
        android.os.Message message2 = mHandler.obtainMessage();
        message2.what = HANDLER_MESSAGE_VOLUME_END;
        mHandler.sendMessageDelayed(message2, (ends[index] - now));
        //重置时间
        Calendar.getInstance().setTime(new Date());
        // TODO: 2019-06-15  log出设置信息
        if (Config.DEBUG) {
            Log.e("Time", "time: start:" + starts[index] + ", end:" + ends[index]);
            String log = "设置定时音量：音量：" + volume.getValue() + ", 开始时间：" + simpleDateFormat.format(new Date(starts[index]))
                    + ", 结束时间：" + simpleDateFormat.format(new Date(ends[index])) + ",恢复成音量：" + default_volume;
            mLogView.append(log);
            Log.e("ScheduleVolume", log);
        }
    }

    /**
     * 插播视频广告
     *
     * @param task
     */
    private void handleIntercutVideo(Task task) {
        //1表示取消，0/2表示新增或者更新
        if (task.getStatus() == 1) {
            mVideoView.pause();
            mVideoView.stopPlayback();
            mInterCuttingTask = null;
            mInterCutting = false;
            updateStatus();
            //更新当前的状态
            mStatus = Status.IDLE;
            //重新查询任务开始
            //重置一切状态
            mHandler.removeCallbacksAndMessages(null);
            mTaskIds.clear();
            //重新进行任务查询
//            queryTask();
            // TODO: 2019-05-22  停止插播，然后需要重新请求任务
            doAfterLogin();
            mHandler.removeMessages(HANDLER_MESSAGE_TIMING_LOGIN);
            mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_LOGIN, 600000L);
            mVideoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshStatus();
                }
            }, 500);
        } else if (task.getStatus() == 0 ||
                task.getStatus() == 2) {
            mInterCuttingTask = task;
            mInterCutting = true;
            updateStatus();
            //执行播放任务
            //先暂停图片播放
            mPicturePager.stop();
            //再变更状态为视频播放或者视频文字播放
            if (mStatus == Status.VIDEO_TEXT
                    || mStatus == Status.PICTURE_TEXT
                    || mStatus == Status.IDLE_TEXT) {
                mStatus = Status.VIDEO_TEXT;
            } else {
                mStatus = Status.VIDEO;
            }
            refreshStatus();
            playVideo(task);
        }
    }

    /**
     * @param task 开关机任务
     */
    private void handleOnOffTask(Task task) {
//        Toast.makeText(mContext, "task:" + task.getList().size(), Toast.LENGTH_SHORT).show();
        //先取消原有的定时开关
        PowerUtils.cancelPowerOnOff(mContext, mBrand, mMainforsettimezone);
        List<Schedule.ScheduleBean> scheduleList = task.getList();
        if (scheduleList != null) {
            DBUtils.getInstance().updateSchedule(scheduleList);
            handleOnOff(scheduleList);
        }
    }

    /**
     * 每次启动就检查开关机状态，并重新设置值
     */
    public void checkOnOff() {
        ArrayList<Schedule.ScheduleBean> list = (ArrayList<Schedule.ScheduleBean>) DBUtils.getInstance().findAllSchedule();
        handleOnOff(list);
    }

    /**
     * 处理开关机的任务
     * 1、保存当前的结果到缓存中
     * 2、清除之前的任务
     * 3、重新设置当前的设置
     * 4、每次设置都只是设置最近一次的关机和最近一次的开机
     * 5、开机或者关机重启后会重新设置
     *
     * @param list
     */
    public void handleOnOff(List<Schedule.ScheduleBean> list) {
        if (list == null || list.isEmpty()) {
            PowerUtils.cancelPowerOnOff(mContext, mBrand, mMainforsettimezone);
            return;
        }
        //day -0 星期天，1-星期一，。。。。
        long latestOff = 0L;
        long latestOn = 0L;
        //当前的时
        int hour = Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).get(Calendar.HOUR_OF_DAY);
        //当前的分
        int min = Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).get(Calendar.MINUTE);
        //当前周几
        int day = Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).get(Calendar.DAY_OF_WEEK);

        for (Schedule.ScheduleBean scheduleBean : list) {
            String[] offTimes = scheduleBean.getOffTime().split(":");
            String[] onTimes = scheduleBean.getStartTime().split(":");
            if (offTimes.length != 2) {
                return;
            }
            if (onTimes.length != 2) {
                return;
            }
            int offHour = Integer.parseInt(offTimes[0]);
            int offMin = Integer.parseInt(offTimes[1]);

            int onHour = Integer.parseInt(onTimes[0]);
            int onMin = Integer.parseInt(onTimes[1]);
            Log.e("Time", "周几：" + day + hour + min + "Off:H" + offHour + "Off:M" + offMin + "ON:H" + onHour + "ON:M" + onMin);
            //每天关机
            if ("0".equals(scheduleBean.getOffDay())) {
                //与现在星期差
                for (int i = 1; i <= 7; i++) {
                    long offTime = getOffsetTime(i, day, offHour, offMin);
                    if (latestOff == 0L || latestOff > offTime) {
                        latestOff = offTime;
                    }
                }
            } else if ("1".equals(scheduleBean.getOffDay())) { //星期一 对应2
                long offTime = getOffsetTime(2, day, offHour, offMin);
                if (latestOff == 0L || latestOff > offTime) {
                    latestOff = offTime;
                }
            } else if ("2".equals(scheduleBean.getOffDay())) {
                long offTime = getOffsetTime(3, day, offHour, offMin);
                if (latestOff == 0L || latestOff > offTime) {
                    latestOff = offTime;
                }
            } else if ("3".equals(scheduleBean.getOffDay())) {
                long offTime = getOffsetTime(4, day, offHour, offMin);
                if (latestOff == 0L || latestOff > offTime) {
                    latestOff = offTime;
                }
            } else if ("4".equals(scheduleBean.getOffDay())) {
                long offTime = getOffsetTime(5, day, offHour, offMin);
                if (latestOff == 0L || latestOff > offTime) {
                    latestOff = offTime;
                }
            } else if ("5".equals(scheduleBean.getOffDay())) {
                long offTime = getOffsetTime(6, day, offHour, offMin);
                if (latestOff == 0L || latestOff > offTime) {
                    latestOff = offTime;
                }
            } else if ("6".equals(scheduleBean.getOffDay())) {
                long offTime = getOffsetTime(7, day, offHour, offMin);
                if (latestOff == 0L || latestOff > offTime) {
                    latestOff = offTime;
                }
            } else if ("7".equals(scheduleBean.getOffDay())) {
                long offTime = getOffsetTime(1, day, offHour, offMin);
                if (latestOff == 0L || latestOff > offTime) {
                    latestOff = offTime;
                }
            } else if ("8".equals(scheduleBean.getOffDay())) {  //工作日
                //与现在星期差
                for (int i = 2; i <= 6; i++) {
                    long offTime = getOffsetTime(i, day, offHour, offMin);
                    if (latestOff == 0L || latestOff > offTime) {
                        latestOff = offTime;
                    }
                }
            } else if ("9".equals(scheduleBean.getOffDay())) {      //周末
                for (int i = 1; i <= 7; i = i + 6) {
                    long offTime = getOffsetTime(i, day, offHour, offMin);
                    if (latestOff == 0L || latestOff > offTime) {
                        latestOff = offTime;
                    }
                }
            }

            //每天开机
            if ("0".equals(scheduleBean.getStartDay())) {
                //与现在星期差
                for (int i = 1; i <= 7; i++) {
                    long onTime = getOffsetTime(i, day, onHour, onMin);
                    if (latestOn == 0L || latestOn > onTime && onTime > latestOff) {
                        latestOn = onTime;
                    }
                }
            } else if ("1".equals(scheduleBean.getStartDay())) { //星期一 对应2
                long onTime = getOffsetTime(2, day, onHour, onMin);
                if (latestOn == 0L || latestOn > onTime && onTime > latestOff) {
                    latestOn = onTime;
                }
            } else if ("2".equals(scheduleBean.getStartDay())) {
                long onTime = getOffsetTime(3, day, onHour, onMin);
                if (latestOn == 0L || latestOn > onTime && onTime > latestOff) {
                    latestOn = onTime;
                }
            } else if ("3".equals(scheduleBean.getStartDay())) {
                long onTime = getOffsetTime(4, day, onHour, onMin);
                if (latestOn == 0L || latestOn > onTime && onTime > latestOff) {
                    latestOn = onTime;
                }
            } else if ("4".equals(scheduleBean.getStartDay())) {
                long onTime = getOffsetTime(5, day, onHour, onMin);
                if (latestOn == 0L || latestOn > onTime && onTime > latestOff) {
                    latestOn = onTime;
                }
            } else if ("5".equals(scheduleBean.getStartDay())) {
                long onTime = getOffsetTime(6, day, onHour, onMin);
                if (latestOn == 0L || latestOn > onTime && onTime > latestOff) {
                    latestOn = onTime;
                }
            } else if ("6".equals(scheduleBean.getStartDay())) {
                long onTime = getOffsetTime(7, day, onHour, onMin);
                if (latestOn == 0L || latestOn > onTime && onTime > latestOff) {
                    latestOn = onTime;
                }
            } else if ("7".equals(scheduleBean.getStartDay())) {
                long onTime = getOffsetTime(1, day, onHour, onMin);
                if (latestOn == 0L || latestOn > onTime && onTime > latestOff) {
                    latestOn = onTime;
                }
            } else if ("8".equals(scheduleBean.getStartDay())) {  //工作日
                //与现在星期差
                for (int i = 2; i <= 6; i++) {
                    long onTime = getOffsetTime(i, day, onHour, onMin);
                    if (latestOn == 0L || latestOn > onTime && onTime > latestOff) {
                        latestOn = onTime;
                    }
                }
            } else if ("9".equals(scheduleBean.getStartDay())) {      //周末
                for (int i = 1; i <= 7; i = i + 6) {
                    long onTime = getOffsetTime(i, day, onHour, onMin);
                    if (latestOn == 0L || latestOn > onTime && onTime > latestOff) {
                        latestOn = onTime;
                    }
                }
            }
        }
        // 再次取消所设定的开关机
        PowerUtils.cancelPowerOnOff(mContext, mBrand, mMainforsettimezone);
        //开关机时间都 不为0
        if (latestOff != 0L && latestOn != 0L) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (Config.DEBUG) {
                Toast.makeText(mContext, "开关机时间:下一次关机：" + simpleDateFormat.format(latestOff)
                        + "后下一次开机：" + simpleDateFormat.format(latestOn) + "后", Toast.LENGTH_LONG).show();
            }
            String log = "开关机时间:下一次关机：" + latestOff + "__" + simpleDateFormat.format(latestOff)
                    + "后下一次开机：" + latestOn + "__" + simpleDateFormat.format(latestOn) + "后";
            LogUtils.e("开关机时间", log);
            if (Config.DEBUG) {
                mLogView.append(log);
            }
            PowerUtils.setPowerOnOff(mContext, mBrand, latestOff, latestOn, mMainforsettimezone);
        }
    }

    /**
     * @param week  Calendar 中的周X 表示的值，周一为2，周六为7
     * @param day   现在时间在Calendar中的周X
     * @param sHour
     * @param sMin
     * @return
     */
    private long getOffsetTime(int week, int day, int sHour, int sMin) {
        //与现在星期差
        int offset = week - day;
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.HOUR_OF_DAY, sHour);
        calendar.set(Calendar.MINUTE, sMin);
        long offsetTime = 0L;
        // 小于0 说明是周一后
        if (offset < 0) {
            offsetTime = calendar.getTimeInMillis() + Math.abs(offset) * 24L * 60L * 60L * 1000L;
        } else if (offset == 0) {
            if (System.currentTimeMillis() - calendar.getTimeInMillis() > 0) {  //今天的时间过了，下一次的周一
                offset = offset + 7;
                offsetTime = calendar.getTimeInMillis() + Math.abs(offset) * 24L * 60L * 60L * 1000L;
            } else {
                offsetTime = calendar.getTimeInMillis();
            }
        } else if (offset > 0) {       //大于0 ，说明是本周
            offsetTime = calendar.getTimeInMillis() + Math.abs(offset) * 24L * 60L * 60L * 1000L;
        }
        calendar.setTime(new Date());
        return offsetTime;
    }


    /**
     * @param task
     */
    private void handWeatherTask(Task task) {
        handleTask(task);
    }


    private void handleTask(Task task) {
        LogUtils.e("handleTask", "开始处理任务");
        if (task.getContent() != null && task.getContent().endsWith(".mp4")) {
            task.setType(6);
        }
        if (task.getFinish_time() < System.currentTimeMillis() / 1000L) {
            //Toast.makeText(mContext, "查询到任务id为" + task.getId() + "的任务已过期，自动跳过", //Toast.LENGTH_SHORT).show();
            return;
        }
        int status = task.getStatus();
        switch (status) {
            case AppConstant.TASK_STATUS_ADD:
                LogUtils.e("handleTask", "insertTask");
                DBUtils.getInstance().insertTask(task);
                addAlarmTask(task);
                break;
            case AppConstant.TASK_STATUS_DELETE:
                LogUtils.e("handleTask", "deleteTask");
                DBUtils.getInstance().deleteTask(task);
                deleteAlarmTask(task);
                break;
            case AppConstant.TASK_STATUS_UPDATE:
                LogUtils.e("handleTask", "updateTask");
                DBUtils.getInstance().updateTask(task);
                updateAlarmTask(task);
                break;
        }
        if (task.getType() == AppConstant.TASK_TYPE_VIDEO) {
            Downloader.getInstance().download(task);
        }

    }

    private void updateAlarmTask(Task task) {
        mHandler.removeMessages(task.getId());
        addAlarmTask(task);
    }


    private void deleteAlarmTask(Task task) {
        mHandler.removeMessages(task.getId());
    }

    /**
     * 结束的闹钟提前200毫秒执行
     *
     * @param task
     */
    private void setFinishAlarmTask(Task task) {
        mHandler.sendEmptyMessage(task.getId());
        android.os.Message message = mHandler.obtainMessage();
        message.what = task.getId();
        message.arg1 = HANDLER_MESSAGE_FINISH_ALARM;
        long time = task.getFinish_time() * 1000L - 200L - System.currentTimeMillis();
        mHandler.sendMessageDelayed(message, time);
        LogUtils.e("addFinishAlarmTask", "Task时间：" + task.getFinish_time() * 1000L);
    }

    /**
     * 根据任务ID，任务时间添加闹钟，到时间自动执行
     *
     * @param task
     */
    private void addAlarmTask(Task task) {
//            已经添加并正在执行的任务不再添加闹钟
        for (Integer key : mTaskIds.keySet()) {
            LogUtils.e("addAlarmTask", "遍历已经存在的任务：" + key + ", value:" + mTaskIds.get(key));
        }
        LogUtils.e("addAlarmTask", "添加任务判断：" + task.getId());
        if (Integer.valueOf(task.getId()).equals((Integer) mTaskIds.get(task.getType()))) {
            return;
        }
        mHandler.removeMessages(task.getId());
        android.os.Message message = mHandler.obtainMessage();
        message.what = task.getId();
        message.arg1 = HANDLER_MESSAGE_START_ALARM;
        long time = task.getStart_time() * 1000L - System.currentTimeMillis();
        if (time < 0) {
            time = 100L;
        }
        LogUtils.e("addAlarmTask", "DelayTime:" + time + "___ID:" + task.getId());
        mHandler.sendMessageDelayed(message, time);
        LogUtils.e("addAlarmTask", "Task时间：" + task.getStart_time() * 1000L);
        if (Config.DEBUG) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            String type = "";
            switch (task.getType()) {
                case AppConstant.TASK_TYPE_PICTURE:
                    type = "图片";
                    break;
                case AppConstant.TASK_TYPE_TEXT:
                    type = "文字";
                    break;
                case AppConstant.TASK_TYPE_VIDEO:
                    type = "视频";
                    break;
                case AppConstant.TASK_TYPE_WEATHER:
                    type = "天气";
                    break;
            }
//            Toast.makeText(mContext, "添加" + type + "任务结束闹钟\n任务Id:" + task.getId() + "\n开始时间：" + simpleDateFormat.format(new Date(task.getStart_time() * 1000L)) +
//                            "\n结束时间：" + simpleDateFormat.format(new Date(task.getFinish_time() * 1000L)),
//                    Toast.LENGTH_SHORT).show();
            String str = "添加" + type + "任务结束闹钟,任务Id:" + task.getId() + ",开始时间：" + simpleDateFormat.format(new Date(task.getStart_time() * 1000L)) +
                    ",结束时间：" + simpleDateFormat.format(new Date(task.getFinish_time() * 1000L));
            mLogView.append(str);
        }
    }


    private void textTask(Task task) {
        int status = task.getStatus();
        switch (status) {
            case AppConstant.TASK_STATUS_ADD:
                DBUtils.getInstance().insertTask(task);
                break;
            case AppConstant.TASK_STATUS_DELETE:
                DBUtils.getInstance().deleteTask(task);
                break;
            case AppConstant.TASK_STATUS_UPDATE:
                DBUtils.getInstance().updateTask(task);
                break;
        }
    }

    private void pictureTask(Task task) {
        int status = task.getStatus();
        switch (status) {
            case AppConstant.TASK_STATUS_ADD:
                DBUtils.getInstance().insertTask(task);
                break;
            case AppConstant.TASK_STATUS_DELETE:
                DBUtils.getInstance().deleteTask(task);
                break;
            case AppConstant.TASK_STATUS_UPDATE:
                DBUtils.getInstance().updateTask(task);
                break;
        }
    }


    /**
     * 休眠
     */
    private void sleep() {
        if (Config.DEBUG) {
            mLogView.append("执行休眠任务，任务时间：" + simpleDateFormat.format(new Date()));
        }
        Call<BaseResponse> sleepCall = Api.getDefault(HostType.VOM_HOST).setSleep(Api.getCacheControl(),
                String.valueOf(mDeviceId), mDeviceCode);
        sleepCall.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                LogUtils.e("sleep", response.body().getMsg() == null ? "休眠上报成功" : response.body().getMsg());
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                LogUtils.e("sleep", t.getMessage() == null ? "休眠上报失败" : t.getMessage());
            }
        });
        pauseTask();
        if (BRAND_QUANZHI.equals(mBrand)) {
            Intent intent = new Intent("android.intent.action.pubds_sleep");
            sendBroadcast(intent);
        } else if (BRAND_ROCKCHIP.equals(mBrand)) {

        } else {

        }
    }

    /**
     * 休眠时暂停播放
     */
    private void pauseTask() {
        mVideoView.pause();
        mPicturePager.stop();
    }

    /**
     * 唤醒后继续任务
     */
    private void resumeTask() {
//        queryTask();
        doAfterLogin();
    }

    /**
     * 唤醒
     */
    private void wakeUp() {
        if (Config.DEBUG) {
            mLogView.append("执行唤醒任务，任务时间：" + simpleDateFormat.format(new Date()));
        }
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        Call<BaseResponse> wakeupCall = Api.getDefault(HostType.VOM_HOST).setWakeUp(Api.getCacheControl(),
                String.valueOf(mDeviceId), mDeviceCode);
        wakeupCall.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                LogUtils.e("wakeUp", response.body().getMsg() == null ? "唤醒上报成功" : response.body().getMsg());
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                LogUtils.e("wakeUp", t.getMessage() == null ? "唤醒上报失败" : t.getMessage());
            }
        });
        resumeTask();
        // TODO: 2019-05-23  根据品牌进行判断
        if (BRAND_QUANZHI.equals(mBrand)) {
            //重启之前需要先关闭看门狗服务
            sendBroadcast(new Intent().setAction("android.intent.action.pubds_watchdogdisable"));
            sendBroadcast(new Intent().setAction("android.intent.action.pubds_reboot"));
        } else if (BRAND_ROCKCHIP.equals(mBrand)) {

        } else {

        }
    }

    /**
     * 调节音量
     */
    private void adjustVolume(Task task) {
        if (Config.DEBUG) {
            mLogView.append("执行调节音量任务，任务时间：" + simpleDateFormat.format(new Date()) + ",音量大小：" + task.getVolume());
        }
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {

            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        int volume = task.getVolume();
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int targetVolume = (int) Math.ceil(volume * maxVolume / 100D);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.abandonAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
            }
        });
        LogUtils.e("Volume", "Task:" + volume + "__Current:" + current + "__Target:" + targetVolume);
        updateStatus();
        Call<BaseResponse> volumeCall = Api.getDefault(HostType.VOM_HOST).setVolume(Api.getCacheControl(),
                String.valueOf(mDeviceId), mDeviceCode, String.valueOf(volume));
        volumeCall.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                BaseResponse baseResponse = response.body();
                LogUtils.e("adjustVolume", baseResponse.getMsg() == null ? "调节音量成功" : baseResponse.getMsg());
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                LogUtils.e("adjustVolume", t.getMessage() == null ? "调节音量失败" : t.getMessage());
            }
        });
    }

    /**
     * 调节音量
     */
    private void adjustScheduleVolume(int volume) {
        if (volume < 0) {
            volume = 0;
        }
        if (volume > 100) {
            volume = 100;
        }
        if (Config.DEBUG) {
            mLogView.append("执行调节音量任务，任务时间：" + simpleDateFormat.format(new Date()) + ",音量大小：" + volume);
        }
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {

            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int targetVolume = (int) Math.ceil(volume * maxVolume / 100D);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.abandonAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
            }
        });
        LogUtils.e("Volume", "Task:" + volume + "__Current:" + current + "__Target:" + targetVolume);
        updateStatus();
        Call<BaseResponse> volumeCall = Api.getDefault(HostType.VOM_HOST).setVolume(Api.getCacheControl(),
                String.valueOf(mDeviceId), mDeviceCode, String.valueOf(volume));
        volumeCall.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                BaseResponse baseResponse = response.body();
                LogUtils.e("adjustVolume", baseResponse.getMsg() == null ? "调节音量成功" : baseResponse.getMsg());
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                LogUtils.e("adjustVolume", t.getMessage() == null ? "调节音量失败" : t.getMessage());
            }
        });
    }

    private void mockPicture() {
        Task task = new Task();
        task.setId((int) (System.currentTimeMillis() / 1000L));
        task.setEquip_id(mDeviceId);
        task.setType(AppConstant.TASK_TYPE_PICTURE);
        task.setCreate_time((int) (System.currentTimeMillis() / 1000L));
        task.setLast_modify((int) (System.currentTimeMillis() / 1000L));
        task.setStatus(AppConstant.TASK_STATUS_ADD);
        task.setRunningStatus(AppConstant.TASK_RUNNING_STATUS_READY);
        task.setDate((int) ((System.currentTimeMillis() + 10000L) / 1000L));
        task.setStart_time((int) ((System.currentTimeMillis() + 10000L) / 1000L));
        task.setFinish_time((int) ((System.currentTimeMillis() + 10000L + 60000L) / 1000L));
        task.setDuration(60);
//        task.setContent("http://p1.wmpic.me/article/2015/04/10/1428655515_xYwBQLzs.jpg");
        task.setContent("http://p2.wmpic.me/article/2015/04/10/1428655516_cTGyxgAF.jpg");
        LogUtils.e("mockPicture", task.toString());
        //Toast.makeText(mContext, "模拟图片任务：" + task.toString(), //Toast.LENGTH_SHORT).show();
        handleTask(task);
    }

    private void mockText() {
        Task task = new Task();
        task.setId((int) (System.currentTimeMillis() / 1000L));
        task.setEquip_id(mDeviceId);
        task.setType(AppConstant.TASK_TYPE_TEXT);
        task.setCreate_time((int) (System.currentTimeMillis() / 1000L));
        task.setLast_modify((int) (System.currentTimeMillis() / 1000L));
        task.setStatus(AppConstant.TASK_STATUS_ADD);
        task.setRunningStatus(AppConstant.TASK_RUNNING_STATUS_READY);
        task.setDate((int) ((System.currentTimeMillis() + 10000L) / 1000L));
        task.setStart_time((int) ((System.currentTimeMillis() + 10000L) / 1000L));
        task.setFinish_time((int) ((System.currentTimeMillis() + 10000L + 60000L) / 1000L));
        task.setDuration(60);
        task.setContent("2017年11月05日发布下午天气预报 全省天气:今天晚上到明天赣州、萍乡两市和吉安市西部多云转阴，全省其他地区晴天转多云。风向：偏北，风力：2～3级。");
//        task.setContent("http://p2.wmpic.me/article/2015/04/10/1428655516_cTGyxgAF.jpg");
//        task.setContent("http://p1.wmpic.me/article/2015/04/10/1428655515_DCkMDAGY.jpg");
        LogUtils.e("mockText", task.toString());
        //Toast.makeText(mContext, "模拟文字任务：" + task.toString(), //Toast.LENGTH_SHORT).show();
        handleTask(task);
    }

    private void mockVideo() {
        Task task = new Task();
        task.setId((int) (System.currentTimeMillis() / 1000L));
        task.setEquip_id(mDeviceId);
        task.setType(AppConstant.TASK_TYPE_VIDEO);
        task.setCreate_time((int) (System.currentTimeMillis() / 1000L));
        task.setLast_modify((int) (System.currentTimeMillis() / 1000L));
        task.setStatus(AppConstant.TASK_STATUS_ADD);
        task.setRunningStatus(AppConstant.TASK_RUNNING_STATUS_READY);
        task.setDate((int) ((System.currentTimeMillis() + 15000L) / 1000L));
        task.setStart_time((int) ((System.currentTimeMillis() + 10000L) / 1000L));
        task.setFinish_time((int) ((System.currentTimeMillis() + 10000L + 60000L) / 1000L));
        task.setDuration(180);
        task.setContent("http://vf2.mtime.cn/Video/2017/03/31/mp4/170331093811717750.mp4");
        LogUtils.e("mockVideo", task.toString());
        //Toast.makeText(mContext, "模拟视频任务：" + task.toString(), //Toast.LENGTH_SHORT).show();
        Downloader.getInstance().download(task);
        handleTask(task);
    }


    /**
     * IM消息处理
     */
    private void handMessageEvent(String text) {
        if (String.valueOf(Event.SCREEN_ON).equals(text)) {
            screenOn();
        } else if (String.valueOf(Event.SCREEN_OFF).equals(text)) {
            screenOff();
        } else if (String.valueOf(Event.SNAPSHOT).equals(text)) {
        } else if (String.valueOf(Event.VOLUME_UP).equals(text)) {
        } else if (String.valueOf(Event.VOLUME_DOWN).equals(text)) {
        } else if (String.valueOf(Event.PAUSE_START).equals(text)) {
            pauseOrStartPlay();
        } else if (text.startsWith("http")) {
//            playVideo(text);
        } else {
            Log.e("taskMessage", "Message: " + text);
            Task task = GsonUtils.convertObj(text, Task.class);
            if (task == null) {
                return;
            }
            LogUtils.e("taskMessage", task.toString());
            handMessageEvent(task);
            //ToastUtil.short//Toast(mContext, "无法识别的指令");
        }
    }

    private void playVideo(Task task) {
        Download download = DBUtils.getInstance().findDownloadedDownload(task.getContent());
        String filePath = null;
        if (download != null) {
            filePath = download.getPath();
            Downloader.getInstance().setCurrentFilePath(filePath);
            if (!new File(filePath).exists()) {
                DBUtils.getInstance().deleteDownload(download);
                filePath = task.getContent();
                //Toast.makeText(mContext, "播放网络视频" + filePath, //Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText(mContext, "播放缓存视频" + filePath, //Toast.LENGTH_SHORT).show();
        } else {
            filePath = task.getContent();
            if (proxy != null) {
                filePath = proxy.getProxyUrl(filePath);
            }
            //Toast.makeText(mContext, "播放网络视频" + filePath, //Toast.LENGTH_SHORT).show();
        }
//        filePath = "http://ahwyx.com/images/attachment/20190219/15505628629557.mp4";
//        filePath = "http://180.163.159.6/huya-w6.huya.com/1816/28800281/yuanhua/845bcfe62ec9f58dda91bdf9614e7c5f.mp4";
        mVideoView.pause();
        mVideoView.stopPlayback();
        mVideoView.setVideoURI(Uri.parse(filePath));
        mVideoView.start();
        mVideoView.setTag(task.getId());
    }


    List<String> mList = new ArrayList<>();

    int index;

    private void mockPlayVideo() {
        mStatus = Status.VIDEO_TEXT;
        refreshStatus();
        index = index % 4;
        mList.clear();
        mVideoView.pause();
        mVideoView.stopPlayback();
        String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Projecting/1.wmv";
        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Projecting/2.avi";
        String path3 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Projecting/3.rmvb";
        String path4 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Projecting/4.mkv";
        mList.add(path1);
        mList.add(path2);
        mList.add(path3);
        mList.add(path4);
        mVideoView.setVideoURI(Uri.parse(mList.get(index)));
//        Toast.makeText(mContext, "播放" + mList.get(index), Toast.LENGTH_SHORT).show();
        mTextTv.setText("当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放当前正在播放");
        mTextTv.init(getWindowManager());
        mTextTv.startScroll();
        LogUtils.e("status", mStatus.getCode() + "" + mStatus.name());
        mTextTv.postDelayed(new Runnable() {
            @Override
            public void run() {
                mockPlayVideo();
            }
        }, 30L * 1000L);
        index++;
    }


    private void pauseOrStartPlay() {

    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

    /**
     * 直接截图发送
     *
     * @param task
     */
    private void snapshot(Task task) {
        if (Config.DEBUG) {
            mLogView.append("执行截屏任务，任务时间：" + simpleDateFormat.format(new Date()));
        }

        // TODO: 11/4/2017   需要验证视频
        //Toast.makeText(mContext, "当前状态：" + mStatus, //Toast.LENGTH_SHORT).show();
        mSnapFilePath = saveCurrentImage();
        String time = task.getCreate_time() + "";
        RequestBody timeRB = RequestBody.create(MediaType.parse("text/plain"), time);
        RequestBody mDeviceIdRB = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(mDeviceId));
        RequestBody snapshotRB = RequestBody.create(MediaType.parse("image/jpeg"), new File(mSnapFilePath));
        MultipartBody.Part snapshotPt = MultipartBody.Part.createFormData("attachment", mSnapFileName, snapshotRB);
        Call<BaseResponse> uploadCall = Api.getDefault(HostType.VOM_HOST).uploadSnapshoot(Api.getCacheControl(),
                timeRB, mDeviceIdRB, snapshotPt);
        LogUtils.e("snapshot", mSnapFilePath);
        uploadCall.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                LogUtils.e("snapshot", response.body().getMsg() == null ? "上传成功" : response.body().getMsg());
                try {
                    File file = new File(mSnapFilePath);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                try {
                    File file = new File(mSnapFilePath);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception e) {

                }
                LogUtils.e("snapshot", t.getMessage() == null ? "上传截图失败" : t.getMessage());
            }
        });
    }


    /**
     * 返回本地截图地址
     *
     * @return
     */
    private String saveCurrentImage() {
        //1.构建Bitmap
        //2.获取屏幕
        Bitmap Bmp = getScreenshot();
        String SavePath = getSDCardPath();
        mSnapFileName = "Snapshot_" + mDeviceId + "_" + System.currentTimeMillis() + ".jpeg";
        String filepath = null;
        //3.保存Bitmap
        try {
            File path = new File(SavePath);
            //文件
            filepath = SavePath + File.separator + mSnapFileName;
            File file = new File(filepath);
            if (!path.exists()) {
                path.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            if (null != fos) {
                Bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.flush();
                fos.close();
                //Toast.makeText(mContext, "截屏文件已保存", //Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return filepath;
        }
    }


    /**
     * 获取本地存储地址
     *
     * @return
     */
    private String getSDCardPath() {
        File sdcardDir = null;
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        } else {
            sdcardDir = Environment.getDownloadCacheDirectory();
        }
        return sdcardDir.toString();
    }


    /**
     * 生成截图Bitmap
     *
     * @return
     */
    private Bitmap getScreenshot() {
        if (mStatus == Status.VIDEO) {
            return mVideoView.getSnapShot();
        } else if (mStatus == Status.VIDEO_TEXT) {
            Bitmap videoBitmap = mVideoView.getSnapShot();
            return compositeBitmap2(videoBitmap, mTextTv);
        } else {
            WindowManager windowManager = getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            int w = display.getWidth();
            int h = display.getHeight();
            View decorview = this.getWindow().getDecorView();
            decorview.setDrawingCacheEnabled(true);
            decorview.buildDrawingCache();
            Bitmap decorBitmap = decorview.getDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(decorBitmap);
            decorview.setDrawingCacheEnabled(false);
//            Bitmap bitmap = loadBitmapFromView(decorview);
            return bitmap;
        }
    }


    /**
     * 合成视频+文字截图
     *
     * @param bottomBipmap
     * @param view
     * @return
     */
    private Bitmap compositeBitmap(Bitmap bottomBipmap, View view) {
        Bitmap screenshot = Bitmap.createBitmap(bottomBipmap.getWidth(), bottomBipmap.getHeight(), Bitmap.Config.ARGB_4444);
        // 把两部分拼起来，先把视频截图绘制到上下左右居中的位置，再把播放器的布局元素绘制上去。
        Canvas canvas = new Canvas(screenshot);
        canvas.drawBitmap(bottomBipmap, 0, 0, new Paint());
        Bitmap bitmap = null;
        //方案 一 一
        if (bitmap == null) {
            mTextTv.setDrawingCacheEnabled(true);
            Bitmap viewBitmap = mTextTv.getDrawingCache();
            bitmap = Bitmap.createBitmap(viewBitmap);
            mTextTv.setDrawingCacheEnabled(false);
            if (bitmap != null) {
                //Toast.makeText(mContext, "方案一成功", //Toast.LENGTH_SHORT).show();
                LogUtils.e("Bitmap", "方案一成功");
            }
        }
//        if (bitmap == null) {
//            bitmap = loadBitmapFromView(mTextTv);
//            mTextTv.setText(mTextTv.getText());
//            mTextTv.requestFocus();
//            if (bitmap != null) {
//                //Toast.makeText(mContext, "方案二失败", //Toast.LENGTH_SHORT).show();
//                LogUtils.e("Bitmap", "方案二失败");
//            }
//        }
        if (bitmap == null) {
            mTextLl.setDrawingCacheEnabled(true);
            Bitmap view2Bitmap = mTextLl.getDrawingCache();
            bitmap = Bitmap.createBitmap(view2Bitmap);
            mTextLl.setDrawingCacheEnabled(false);
            if (bitmap != null) {
                //Toast.makeText(mContext, "方案三成功", //Toast.LENGTH_SHORT).show();
                LogUtils.e("Bitmap", "方案三成功");
            }
        }
        if (bitmap == null) {
            bitmap = loadBitmapFromView(mTextLl);
            if (bitmap != null) {
                //Toast.makeText(mContext, "方案四成功", //Toast.LENGTH_SHORT).show();
                LogUtils.e("Bitmap", "方案四成功");
            }
        }
//        float top = mTextLl.getTop();
//        float left = mTextLl.getLeft();
        int[] location = new int[2];
        mTextTv.getLocationInWindow(location);
        float top = location[1];
        float left = location[0];
        canvas.drawBitmap(bitmap, left, top, new Paint());
        canvas.save();
        canvas.restore();
        return screenshot;
    }


    /**
     * 验证 方案2-1 是可行的
     *
     * @param bottomBipmap
     * @param view
     * @return
     */
    private Bitmap compositeBitmap2(Bitmap bottomBipmap, View view) {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int w = display.getWidth();
        int h = display.getHeight();
        Bitmap screenshot = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(screenshot);
        int bitW = bottomBipmap.getWidth();
        int bitH = bottomBipmap.getHeight();
        canvas.drawBitmap(bottomBipmap, (w - bitW) / 2, (h - bitH) / 2, new Paint());
//        canvas.drawBitmap(bottomBipmap, 0, 0, new Paint());
        Bitmap bitmap = null;
        if (bitmap == null) {
            mTextTvLl.setDrawingCacheEnabled(true);
            Bitmap viewBitmap = mTextTvLl.getDrawingCache();
            bitmap = Bitmap.createBitmap(viewBitmap);
            mTextTv.setDrawingCacheEnabled(false);
            if (bitmap != null) {
                //Toast.makeText(mContext, "方案一成功", //Toast.LENGTH_SHORT).show();
                LogUtils.e("Bitmap", "方案一成功");
            }
        }
        if (bitmap == null) {
            mTextLl.setDrawingCacheEnabled(true);
            Bitmap view2Bitmap = mTextLl.getDrawingCache();
            bitmap = Bitmap.createBitmap(view2Bitmap);
            mTextLl.setDrawingCacheEnabled(false);
            if (bitmap != null) {
                //Toast.makeText(mContext, "方案三成功", //Toast.LENGTH_SHORT).show();
                LogUtils.e("Bitmap", "方案三成功");
            }
        }
        if (bitmap == null) {
            bitmap = loadBitmapFromView(mTextLl);
            if (bitmap != null) {
                //Toast.makeText(mContext, "方案四成功", //Toast.LENGTH_SHORT).show();
                LogUtils.e("Bitmap", "方案四成功");
            }
        }
//        float top = mTextLl.getTop();
//        float left = mTextLl.getLeft();
        int[] location = new int[2];
        mTextTvLl.getLocationInWindow(location);
        float top = location[1];
        float left = location[0];
        canvas.drawBitmap(bitmap, left, top, new Paint());
        canvas.save();
        canvas.restore();
        return screenshot;
    }

    private Bitmap loadBitmapFromView(View view) {
        if (view == null) {
            return null;
        }
        Bitmap screenshot = null;
        screenshot = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredWidth(), Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(screenshot);
        view.draw(c);
        return screenshot;
    }


    /**
     * 休眠
     */
    private void screenOff() {

    }

    /**
     * 唤醒
     */
    private void screenOn() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.idle_about_iv:
                goToAbout();
                break;
            default:
                break;
        }
    }

    private void goToAbout() {
        if (SPUtils.getSharedIntData(mContext, BundleKey.DEVICE_ID) == 0) {
            String tip = "标识码：" + mUuid + "\n您的广告机后台还未注册,请先在后台注册后再打开";
            NoticeDialog noticeDialog = new NoticeDialog(mContext, null, tip, new NoticeDialog.OnDialogListener() {
                @Override
                public void clickPositive() {
                }
            });
            noticeDialog.show();
            return;
        } else {
            startActivity(new Intent(mContext, AboutActivity.class));
        }
    }


    @Override
    public void executeTask(int taskId) {
        LogUtils.e("executeTask", "taskId=" + taskId);
        Task task = DBUtils.getInstance().findTask(taskId);
        if (task == null) {
            LogUtils.e("executeTask", "task = null");
            return;
        }
        if (Integer.valueOf(taskId).equals((Integer) mTaskIds.get(task.getType()))) {
            LogUtils.e("执行中", "当前已存在执行中的任务:" + taskId);
            return;
        } else {
            mTaskIds.put(task.getType(), taskId);
        }
        LogUtils.e("executeTask", task.toString());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        if (Config.DEBUG) {
            String type = "";
            switch (task.getType()) {
                case AppConstant.TASK_TYPE_PICTURE:
                    type = "图片";
                    break;
                case AppConstant.TASK_TYPE_TEXT:
                    type = "文字";
                    break;
                case AppConstant.TASK_TYPE_VIDEO:
                    type = "视频";
                    break;
                case AppConstant.TASK_TYPE_WEATHER:
                    type = "天气";
                    break;
            }
            String log = "执行" + type + "任务\n任务Id:" + task.getId() + "\n开始时间：" + simpleDateFormat.format(new Date(task.getStart_time() * 1000L)) +
                    "\n结束时间：" + simpleDateFormat.format(new Date(task.getFinish_time() * 1000L));
//            Toast.makeText(mContext, log,
//                    Toast.LENGTH_LONG).show();
            String str = "执行" + type + "任务, 任务Id:" + task.getId() + ",开始时间：" + simpleDateFormat.format(new Date(task.getStart_time() * 1000L)) +
                    ", 结束时间：" + simpleDateFormat.format(new Date(task.getFinish_time() * 1000L)) + ",任务内容：" + task.getContent();
            mLogView.append(str);
        }
        switch (task.getType()) {
            case AppConstant.TASK_TYPE_PICTURE:
                //插播广告过程中，不执行，直接设置结束的任务
                if (mInterCutting) {
                    task.setRunningStatus(AppConstant.TASK_RUNNING_STATUS_GOING);
                    DBUtils.getInstance().updateTask(task);
                    setFinishAlarmTask(task);
                    return;
                }
                if (mStatus == Status.VIDEO_TEXT
                        || mStatus == Status.PICTURE_TEXT
                        || mStatus == Status.IDLE_TEXT) {
                    mStatus = Status.PICTURE_TEXT;
                } else {
                    mStatus = Status.PICTURE;
                }
                refreshStatus();
//                if (mPicturePager.getTag().equals(task.getId())) {
//                    return;
//                }
                mVideoView.pause();
                mPicturePager.stop();
                playPicture(task);
                break;
            case AppConstant.TASK_TYPE_TEXT:
                if (mStatus == Status.VIDEO) {
                    mStatus = Status.VIDEO_TEXT;
                    mPicturePager.stop();
                } else if (mStatus == Status.PICTURE) {
                    mStatus = Status.PICTURE_TEXT;
                    mVideoView.pause();
                } else if (mStatus == Status.IDLE) {
                    mVideoView.pause();
                    mPicturePager.stop();
                    mStatus = Status.IDLE_TEXT;
                }
                refreshStatus();
//                if (mTextTv.getTag().equals(task.getId())) {
//                    return;
//                }
                playText(task);
                break;
            case AppConstant.TASK_TYPE_VIDEO:
                //插播广告过程中，不执行，直接设置结束的任务
                if (mInterCutting) {
                    task.setRunningStatus(AppConstant.TASK_RUNNING_STATUS_GOING);
                    DBUtils.getInstance().updateTask(task);
                    setFinishAlarmTask(task);
                    return;
                }
                if (mStatus == Status.VIDEO_TEXT
                        || mStatus == Status.PICTURE_TEXT
                        || mStatus == Status.IDLE_TEXT) {
                    mStatus = Status.VIDEO_TEXT;
                } else {
                    mStatus = Status.VIDEO;
                }
                refreshStatus();
//                if (mVideoView.getTag().equals(task.getId())) {
//                    return;
//                }
                playVideo(task);
                break;
            case AppConstant.TASK_TYPE_WEATHER:
                if (mStatus == Status.VIDEO) {
                    mStatus = Status.VIDEO_TEXT;
                    mPicturePager.stop();
                } else if (mStatus == Status.PICTURE) {
                    mStatus = Status.PICTURE_TEXT;
                    mVideoView.pause();
                } else if (mStatus == Status.IDLE) {
                    mVideoView.pause();
                    mPicturePager.stop();
                    mStatus = Status.IDLE_TEXT;
                }
                refreshStatus();
//                if (mTextTv.getTag().equals(task.getId())) {
//                    return;
//                }
                playText(task);
//                handWeatherTask(task);
                break;
            default:
                break;
        }
        task.setRunningStatus(AppConstant.TASK_RUNNING_STATUS_GOING);
        DBUtils.getInstance().updateTask(task);
        setFinishAlarmTask(task);
    }

    private void playText(Task task) {
        mTextTv.setTag(task.getId());
        mTextTv.setText(task.getContent());
        mTextTv.init(getWindowManager());
        mTextTv.startScroll();
    }


    private List<Task> mRunningTaskList = new ArrayList<>();

    /**
     * 闹钟结束任务回调
     * 先假设为闲置状态，如5秒后仍然为闲置，则设置为闲置
     * 如2秒后不再是闲置，说明有新的任务，则不进行处理
     *
     * @param taskId
     */
    @Override
    public void finishTask(int taskId) {
        LogUtils.e("finishTask", "taskId=" + taskId);
        Task task = DBUtils.getInstance().findTask(taskId);
        if (task == null) {
            return;
        }
        try {
            mTaskIds.remove(task.getType());
            LogUtils.e("移除任务", "Map移除任务,Size:" + mTaskIds.size());
        } catch (Exception e) {
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        if (Config.DEBUG) {
            String type = "";
            switch (task.getType()) {
                case AppConstant.TASK_TYPE_PICTURE:
                    type = "图片";
                    break;
                case AppConstant.TASK_TYPE_TEXT:
                    type = "文字";
                    break;
                case AppConstant.TASK_TYPE_VIDEO:
                    type = "视频";
                    break;
                case AppConstant.TASK_TYPE_WEATHER:
                    type = "天气";
                    break;
            }
//            Toast.makeText(mContext, "结束" + type + "任务\n任务Id:" + task.getId() + "\n开始时间：" + simpleDateFormat.format(new Date(task.getStart_time() * 1000L)) +
//                            "\n结束时间：" + simpleDateFormat.format(new Date(task.getFinish_time() * 1000L)),
//                    Toast.LENGTH_LONG).show();
            String str = "结束" + type + "任务,任务Id:" + task.getId() + ",开始时间：" + simpleDateFormat.format(new Date(task.getStart_time() * 1000L)) +
                    ",结束时间：" + simpleDateFormat.format(new Date(task.getFinish_time() * 1000L)) + ",任务内容：" + task.getContent();
            mLogView.append(str);
        }
        //Toast.makeText(mContext, "结束：" + simpleDateFormat.format(new Date(task.getStart_time() * 1000L)), //Toast.LENGTH_SHORT).show();
        task.setRunningStatus(AppConstant.TASK_RUNNING_STATUS_FINISH);
        DBUtils.getInstance().updateTask(task);
        deleteAlarmTask(task);
        DBUtils.getInstance().deleteTask(task);
        LogUtils.e("切换状态", "结束前状态" + mStatus.name());
        //插播广告的状态下，结束之前的任务或者挂起的任务
        if (mInterCutting) {
            LogUtils.e("插播视频播放中", "当前状态：" + mStatus.name());
            if (mStatus == Status.VIDEO_TEXT &&
                    (task.getType() == AppConstant.TASK_TYPE_TEXT ||
                            task.getType() == AppConstant.TASK_TYPE_WEATHER)) {
                if (mTextTv.getTag().equals(task.getId())) {
                    mTextTv.setText("");
                    mStatus = Status.VIDEO;
                }
            }
            mIdleFl.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshStatus();
                }
            }, 2000);
            return;

        }
        switch (mStatus) {
            case VIDEO:
                if (mVideoView.getTag().equals(task.getId())) {
                    LogUtils.e("切换状态", "VIDEO->IDLE");
                    mVideoView.pause();
                    mStatus = Status.IDLE;
                } else {
                    LogUtils.e("切换状态", "VIDEO-不成功");
                }
                break;
            case VIDEO_TEXT:
                if (task.getType() == AppConstant.TASK_TYPE_VIDEO) {
                    if (mVideoView.getTag().equals(task.getId())) {
                        mVideoView.pause();
                        mStatus = Status.IDLE_TEXT;
                    }
                } else if (task.getType() == AppConstant.TASK_TYPE_TEXT ||
                        task.getType() == AppConstant.TASK_TYPE_WEATHER) {
                    if (mTextTv.getTag().equals(task.getId())) {
                        mTextTv.setText("");
                        mStatus = Status.VIDEO;
                    }
                }
                break;
            case PICTURE:
                if (mPicturePager.getTag().equals(task.getId())) {
                    mPicturePager.stop();
                    mStatus = Status.IDLE;
                }
                break;
            case PICTURE_TEXT:
                if (task.getType() == AppConstant.TASK_TYPE_PICTURE) {
                    if (mPicturePager.getTag().equals(task.getId())) {
                        mPicturePager.stop();
                        mStatus = Status.IDLE_TEXT;
                    }
                } else if (task.getType() == AppConstant.TASK_TYPE_TEXT ||
                        task.getType() == AppConstant.TASK_TYPE_WEATHER) {
                    if (mTextTv.getTag().equals(task.getId())) {
                        mTextTv.setText("");
                        mStatus = Status.PICTURE;
                    }
                }
                break;
            case TEXT:
                if (mTextTv.getTag().equals(task.getId())) {
                    mTextTv.setText("");
                    mStatus = Status.IDLE;
                }
                break;
            case IDLE_TEXT:
                if (mTextTv.getTag().equals(task.getId())) {
                    mTextTv.setText("");
                    mStatus = Status.IDLE;
                }
                break;
            case IDLE:
                mStatus = Status.IDLE;
                break;
        }
        LogUtils.e("切换状态", "结束后状态" + mStatus.name());
        mIdleFl.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshStatus();
            }
        }, 2000);
    }


    private void setCancelAlarm() {


    }

    private void playPicture(Task task) {
        mPicList = new ArrayList<>();
        mPicList.add(task.getContent());
        mPictureAdapter = new PictureAdapter(mContext, mPicList);
        mPicturePager.setAdapter(mPictureAdapter);
        mPicturePager.setTag(task.getId());
//        mPicturePager.start();
    }


    private void refreshStatus() {
        switch (mStatus) {
            case VIDEO_TEXT:
                mTextFl.setVisibility(View.VISIBLE);
                mTextServerHolderLl.setVisibility(View.GONE);
                mPictureFl.setVisibility(View.INVISIBLE);
                mVideoFl.setVisibility(View.VISIBLE);
                mIdleFl.setVisibility(View.VISIBLE);
                //Toast.makeText(mContext, "当前状态：视频+文字广告", //Toast.LENGTH_SHORT).show();
                break;
            case VIDEO:
                mTextFl.setVisibility(View.INVISIBLE);
                mPictureFl.setVisibility(View.INVISIBLE);
                mVideoFl.setVisibility(View.VISIBLE);
                mIdleFl.setVisibility(View.VISIBLE);
                //Toast.makeText(mContext, "当前状态：视频广告", //Toast.LENGTH_SHORT).show();
                break;
            case PICTURE:
                mTextFl.setVisibility(View.INVISIBLE);
                mPictureFl.setVisibility(View.VISIBLE);
                mVideoFl.setVisibility(View.INVISIBLE);
                mIdleFl.setVisibility(View.VISIBLE);
                //Toast.makeText(mContext, "当前状态：图片广告", //Toast.LENGTH_SHORT).show();
                break;
            case TEXT:
                mTextFl.setVisibility(View.VISIBLE);
                mTextServerHolderLl.setVisibility(View.INVISIBLE);
                mPictureFl.setVisibility(View.INVISIBLE);
                mVideoFl.setVisibility(View.INVISIBLE);
                mIdleFl.setVisibility(View.VISIBLE);
                //Toast.makeText(mContext, "当前状态：文字广告", //Toast.LENGTH_SHORT).show();
                break;
            case PICTURE_TEXT:
                mTextFl.setVisibility(View.VISIBLE);
                mTextServerHolderLl.setVisibility(View.GONE);
                mPictureFl.setVisibility(View.VISIBLE);
                mVideoFl.setVisibility(View.INVISIBLE);
                mIdleFl.setVisibility(View.VISIBLE);
                //Toast.makeText(mContext, "当前状态：图片+文字广告", //Toast.LENGTH_SHORT).show();
                break;
            case IDLE:
                mTextFl.setVisibility(View.INVISIBLE);
                mPictureFl.setVisibility(View.INVISIBLE);
                mVideoFl.setVisibility(View.INVISIBLE);
                mIdleFl.setVisibility(View.VISIBLE);
                //Toast.makeText(mContext, "当前状态：空闲状态", //Toast.LENGTH_SHORT).show();
                break;
            case IDLE_TEXT:
                mTextFl.setVisibility(View.VISIBLE);
                mTextServerHolderLl.setVisibility(View.INVISIBLE);
                mPictureFl.setVisibility(View.INVISIBLE);
                mVideoFl.setVisibility(View.INVISIBLE);
                mIdleFl.setVisibility(View.VISIBLE);
                //Toast.makeText(mContext, "当前状态：文字", //Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        //Toast.makeText(mContext, "按下了" + keyCode, //Toast.LENGTH_SHORT).show();
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_M) {
            goToAbout();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            mIdleFl.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateStatus();
                }
            }, 200);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onVideoSizeChanged() {

    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onCompletion() {
//            Toast.makeText(mContext, "本次视频播放完成!", Toast.LENGTH_SHORT).show();
        if (mVideoView.getType() == VideoView.TYPE_VLC) {
//            VLC播放器设定的是自动循环
            return;
        }
        //如果是插播的任务，则继续进行插播视频的播放，不需要停止
        if (mInterCutting && mInterCuttingTask != null) {
            playVideo(mInterCuttingTask);
            return;
        }
        if (mTaskIds.get(AppConstant.TASK_TYPE_VIDEO) == null) {
            LogUtils.e("onCompletion", "taskids get TASK_TYPE_VIDEO is null!");
            return;
        } else {
            int taskId = mTaskIds.get(AppConstant.TASK_TYPE_VIDEO);
            Task task = DBUtils.getInstance().findTask(taskId);
            LogUtils.e("onCompletion", "taskids getTask id is " + taskId);
            if (task != null) {
                LogUtils.e("onCompletion", "task  is not null!");
                playVideo(task);
            }
        }
    }

    @Override
    public void onError() {
        if (mVideoView.getType() == VideoView.TYPE_VLC) {
            //遇到错误后再尝试重新播放
            try {
                mVideoView.start();
            } catch (Exception e) {
            }
        }
    }
}
