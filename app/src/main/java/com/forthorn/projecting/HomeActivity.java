package com.forthorn.projecting;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.forthorn.projecting.api.Api;
import com.forthorn.projecting.api.HostType;
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
import com.forthorn.projecting.entity.Task;
import com.forthorn.projecting.entity.TaskRes;
import com.forthorn.projecting.func.picture.AutoViewPager;
import com.forthorn.projecting.func.picture.PictureAdapter;
import com.forthorn.projecting.receiver.AlarmReceiver;
import com.forthorn.projecting.receiver.DeviceReceiver;
import com.forthorn.projecting.util.GsonUtils;
import com.forthorn.projecting.util.LogUtils;
import com.forthorn.projecting.util.SPUtils;
import com.forthorn.projecting.widget.AutoScrollTextView;
import com.forthorn.projecting.widget.NoticeDialog;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


public class HomeActivity extends Activity implements View.OnClickListener, AlarmReceiver.AlarmListener {
    //视频
    private FrameLayout mVideoFl;
    private PLVideoTextureView mVideoView;
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

    private Context mContext;
    private List<String> mPicList = new ArrayList<>();
    private Status mStatus;

    private String mUuid;
    private String mIMUsername;
    private String mIMPassword;
    private int mDeviceId;
    private String mDeviceCode;

    private AudioManager mAudioManager;
    private DevicePolicyManager mPolicyManager;
    private ComponentName mComponentName;
    private AlarmManager mAlarmManager;
    private AlarmReceiver mAlarmReceiver;

    private RxManager mRxManager;

    private String mSnapFileName;
    private String mSnapFilePath;

    private boolean mEnableLock;
    private static final int REQUEST_CODE_ADMIN = 0x1;
    private static final int HANDLER_MESSAGE_TIMING_LOGIN = 0X2;
    private static final int HANDLER_MESSAGE_TIMING_REQUESR_ACCOUNT = 0X3;
    private static final int HANDLER_MESSAGE_TIMING_REQUESR_MESSAGE = 0X4;
    private static final int HANDLER_MESSAGE_START_ALARM = 0X5;
    private static final int HANDLER_MESSAGE_FINISH_ALARM = 0X6;

    private long nextHourTimeStamp = 0L;
    private List<Integer> mTaskIdList = new ArrayList<>();

    private Map<Integer, Integer> mTaskIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRxManager = new RxManager();
        mContext = HomeActivity.this;
        SQLiteStudioService.instance().start(mContext);
        setContentView(R.layout.activity_home);
        initView();
        initData();
        initEvent();
        initPlayer();
        initManager();
        initIM();
        queryTask();
        //每十分钟登录一下
        mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_LOGIN, 600000);
        setRequestAlarm();
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
                    mDeviceId, AppConstant.STATUS_WAKE_UP, targetVolume);
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
        Calendar cal = Calendar.getInstance();
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
                    requestIMAccount();
                    mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE_TIMING_LOGIN, 600000L);
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
            }
            if (msg.what != HANDLER_MESSAGE_TIMING_LOGIN && msg.what != HANDLER_MESSAGE_TIMING_REQUESR_ACCOUNT
                    && msg.what != HANDLER_MESSAGE_TIMING_REQUESR_MESSAGE) {
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        LogUtils.e("request", "请求：" + sdf.format(new Date(timeStamp * 1000L)) + "的任务");
        //Toast.makeText(mContext, "查询" + sdf.format(new Date(timeStamp * 1000L)) + "的任务", //Toast.LENGTH_SHORT).show();
        Call<TaskRes> taskResCall = Api.getDefault(HostType.VOM_HOST).getTaskList(Api.getCacheControl(),
                String.valueOf(mDeviceId), String.valueOf(timeStamp));
        taskResCall.enqueue(new Callback<TaskRes>() {
            @Override
            public void onResponse(Call<TaskRes> call, Response<TaskRes> response) {
                LogUtils.e("查询任务", "结果：" + response.toString());
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

    private void initManager() {
        mPolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, DeviceReceiver.class);
        if (!mPolicyManager.isAdminActive(mComponentName)) {
            goToSetting();
        } else {
            mEnableLock = true;
        }
    }

    private void initPlayer() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        int codec = getIntent().getIntExtra("mediaCodec", AVOptions.MEDIA_CODEC_HW_DECODE);
        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);
        mVideoView.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_FIT_PARENT);
        mVideoView.setAVOptions(options);
        mVideoView.setDebugLoggingEnabled(false);
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);

    }

    private void initIM() {
        JMessageClient.registerEventReceiver(this);
        requestIMAccount();
    }

    private void initView() {
        //视频
        mVideoFl = (FrameLayout) findViewById(R.id.video_fl);
        mVideoView = (PLVideoTextureView) findViewById(R.id.video_view);
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
    }

    private void initData() {
        DeviceUuidFactory uuidFactory = new DeviceUuidFactory(mContext);
        mUuid = uuidFactory.getDeviceUuid().toString();
//        mUuid = "c3d30ab2-1139-300a-830f-bc4e6900c015";
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
                login();
                updateStatus();
                nextHourTimeStamp = getNextHourStamp();
                requestTasks(nextHourTimeStamp - 3600L);
            }

            @Override
            public void onFailure(Call<IMAccount> call, Throwable t) {
                //Toast.makeText(mContext, t.getMessage(), //Toast.LENGTH_SHORT).show();
                login();
            }
        });
    }

    /**
     * 登陆IM
     */
    private void login() {
        mIMUsername = SPUtils.getSharedStringData(mContext, BundleKey.IM_ACCOUNT);
        mIMPassword = SPUtils.getSharedStringData(mContext, BundleKey.IM_PASSWORD);
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
                    //ToastUtil.short//Toast(mContext, "登陆成功" + myInfo.getUserName());
                    mIdleServerStatusTv.setText("在线");
                    mIdleServerStatusTv.setEnabled(true);
                } else {
                    //ToastUtil.short//Toast(mContext, "Code:" + i + "   Reason:" + s);
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
        Log.d("MessageEvent", "收到消息：" + msg.getFromUser().getUserName());
        switch (msg.getContentType()) {
            case text:  //处理文字消息
                TextContent textContent = (TextContent) msg.getContent();
                Log.d("MessageEvent", "消息：" + textContent.getText());
                handMessageEvent(textContent.getText());
                break;
            case image:
                break;
            case voice:
                break;
            case custom:
                CustomContent customContent = (CustomContent) msg.getContent();
                Task task = GsonUtils.convertObj(customContent.toJson(), Task.class);
                LogUtils.e("CustomContent", customContent.toJson());
                if (task == null) {
                    //Toast.makeText(mContext, "消息格式错误", //Toast.LENGTH_SHORT).show();
                    return;
                }
                LogUtils.e("taskMessage", task.toString());
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
            default:
                break;
        }
    }


    /**
     * @param task
     */
    private void handWeatherTask(Task task) {
        handleTask(task);
    }


    private void handleTask(Task task) {
        LogUtils.e("handleTask", "开始处理任务");
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
        if (new Integer(task.getId()).equals((Integer) mTaskIds.get(task.getType()))) {
            return;
        }
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
        if (!mEnableLock) {
            return;
        }
        if (mPolicyManager.isAdminActive(mComponentName)) {
//            Window localWindow = getWindow();
//            WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
//            localLayoutParams.screenBrightness = 0.05F;
//            localWindow.setAttributes(localLayoutParams);
            mPolicyManager.lockNow();
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
        queryTask();
    }

    /**
     * 唤醒
     */
    private void wakeUp() {
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
    }

    /**
     * 调节音量
     */
    private void adjustVolume(Task task) {
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
            //Toast.makeText(mContext, "播放网络视频" + filePath, //Toast.LENGTH_SHORT).show();
        }
        mVideoView.pause();
        mVideoView.stopPlayback();
        mVideoView.setVideoPath(filePath);
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
        mVideoView.setVideoPath(mList.get(index));
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

    /**
     * 直接截图发送
     *
     * @param task
     */
    private void snapshot(Task task) {
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
            return mVideoView.getTextureView().getBitmap();
        } else if (mStatus == Status.VIDEO_TEXT) {
            Bitmap videoBitmap = mVideoView.getTextureView().getBitmap();
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

    private PLMediaPlayer.OnInfoListener mOnInfoListener = new PLMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
            LogUtils.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    break;
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    LogUtils.i(TAG, "First video render time: " + extra + "ms");
                    break;
                case PLMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                    LogUtils.i(TAG, "First audio render time: " + extra + "ms");
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    LogUtils.i(TAG, "video frame rendering, ts = " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    LogUtils.i(TAG, "audio frame rendering, ts = " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_GOP_TIME:
                    LogUtils.i(TAG, "Gop Time: " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_SWITCHING_SW_DECODE:
                    LogUtils.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private String TAG = "Player";

    @Override
    protected void onDestroy() {
        mVideoView.stopPlayback();
        SQLiteStudioService.instance().stop();
        unregisterReceiver(mAlarmReceiver);
        JMessageClient.unRegisterEventReceiver(this);
        mRxManager.clear();
        super.onDestroy();
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

    private void goToSetting() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
        startActivityForResult(intent, REQUEST_CODE_ADMIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_ADMIN == requestCode) {
            if (RESULT_OK == resultCode) {
                mEnableLock = true;
            } else if (RESULT_CANCELED == resultCode) {
                mEnableLock = false;
            }
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss:SSS");
        //Toast.makeText(mContext, "执行：" + simpleDateFormat.format(new Date(task.getStart_time() * 1000L)), //Toast.LENGTH_SHORT).show();
        switch (task.getType()) {
            case AppConstant.TASK_TYPE_PICTURE:
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss:SSS");
        //Toast.makeText(mContext, "结束：" + simpleDateFormat.format(new Date(task.getStart_time() * 1000L)), //Toast.LENGTH_SHORT).show();
        task.setRunningStatus(AppConstant.TASK_RUNNING_STATUS_FINISH);
        DBUtils.getInstance().updateTask(task);
        deleteAlarmTask(task);
        DBUtils.getInstance().deleteTask(task);
        LogUtils.e("切换状态", "结束前状态" + mStatus.name());
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
    protected void onStop() {
        super.onStop();
    }


    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer mp, int errorCode) {
            LogUtils.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLMediaPlayer.ERROR_CODE_IO_ERROR:
//                    Toast.makeText(mContext, "IO Error !", Toast.LENGTH_SHORT).show();
                    return false;
                default:
                    break;
            }
            return true;
        }
    };


    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer plMediaPlayer) {
            LogUtils.i(TAG, "Play Completed !");
//            Toast.makeText(mContext, "本次视频播放完成!", Toast.LENGTH_SHORT).show();
            if (mTaskIds.get(AppConstant.TASK_TYPE_VIDEO) == null) {
                LogUtils.e("onCompletion", "taskids get TASK_TYPE_VIDEO is null!");
                return;
            } else {
                int taskId = mTaskIds.get(AppConstant.TASK_TYPE_VIDEO);
                Task task = DBUtils.getInstance().findTask(taskId);
                LogUtils.e("onCompletion", "taskids getTask id is " + taskId);
                if (task != null)
                    LogUtils.e("onCompletion", "task  is not null!");
                playVideo(task);
            }
        }
    };


    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int precent) {
            LogUtils.i(TAG, "onBufferingUpdate: " + precent);
        }
    };

    private PLMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(PLMediaPlayer plMediaPlayer, int i, int i1, int i2, int i3) {
            LogUtils.i(TAG, "onVideoSizeChanged: width = " + i + ", height = " + i1 + "I2" + i2 + "I3" + i3);
        }
    };

}
