package com.forthorn.projecting;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.forthorn.projecting.api.Api;
import com.forthorn.projecting.api.HostType;
import com.forthorn.projecting.app.DeviceUuidFactory;
import com.forthorn.projecting.entity.Event;
import com.forthorn.projecting.entity.UserList;
import com.forthorn.projecting.entity.Users;
import com.forthorn.projecting.receiver.DeviceReceiver;
import com.forthorn.projecting.util.ToastUtil;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.upyun.jpush.api.utils.Base64Coder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.IntegerCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView mUserNameTv, mStatusTv, mMessageTv;
    TextView mRegsiterBtn, mLoginBtn, mLogoutBtn, mAdminLoginBtn;
    ImageView mSnapshotIv;
    LinearLayout mAdminLl;
    FrameLayout mFrameLayout;
    LinearLayout mController;
    EditText mMessageEt;
    TextView mChooseContactTv;
    TextView mContactTv;
    TextView mSendTv;

    DevicePolicyManager policyManager;
    ComponentName componentName;
    private boolean canLock;
    private PLVideoTextureView mVideoView;
    private TextView mStatInfoTextView;
    private Toast mToast = null;
    private static final String TAG = MainActivity.class.getSimpleName();

    private String mCurrentPath;
    private String mNextPath;
    private boolean admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        mRegsiterBtn.setOnClickListener(this);
        mLoginBtn.setOnClickListener(this);
        mLogoutBtn.setOnClickListener(this);
        mAdminLoginBtn.setOnClickListener(this);

        mChooseContactTv.setOnClickListener(this);
        mSendTv.setOnClickListener(this);
        mFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toogleControllerVisiable(mController.getVisibility() == View.GONE);
            }
        });
        JMessageClient.registerEventReceiver(this);
        policyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, DeviceReceiver.class);
        if (!policyManager.isAdminActive(componentName)) {
            goSetActivity();
        } else {
            canLock = true;
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        int codec = getIntent().getIntExtra("mediaCodec", AVOptions.MEDIA_CODEC_SW_DECODE);
        AVOptions options = new AVOptions();
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        // 1 -> hw codec enable, 0 -> disable [recommended]
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);
        //        boolean cache = getIntent().getBooleanExtra("cache", false);
//        if (!isLiveStreaming && cache) {
//            options.setString(AVOptions.KEY_CACHE_DIR, Config.DEFAULT_CACHE_DIR);
//        }
        mVideoView.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_FIT_PARENT);
        mVideoView.setAVOptions(options);
        mVideoView.setDebugLoggingEnabled(true);
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);

        if (JMessageClient.getMyInfo() == null) {
            register();
        } else {
            login();
        }
    }

    private void toogleControllerVisiable(boolean flag) {
        if (flag) {
            mController.setVisibility(View.VISIBLE);
            startDismissControlViewTimer();
        } else {
            mController.setVisibility(View.GONE);
        }
    }


    protected static Timer DISMISS_CONTROL_VIEW_TIMER;
    protected DismissControlViewTimerTask mDismissControlViewTimerTask;

    public void startDismissControlViewTimer() {
        if (admin) {
            return;
        }
        cancelDismissControlViewTimer();
        DISMISS_CONTROL_VIEW_TIMER = new Timer();
        mDismissControlViewTimerTask = new DismissControlViewTimerTask();
        DISMISS_CONTROL_VIEW_TIMER.schedule(mDismissControlViewTimerTask, 10000);
    }

    public void cancelDismissControlViewTimer() {
        if (DISMISS_CONTROL_VIEW_TIMER != null) {
            DISMISS_CONTROL_VIEW_TIMER.cancel();
        }
        if (mDismissControlViewTimerTask != null) {
            mDismissControlViewTimerTask.cancel();
        }
    }

    public class DismissControlViewTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toogleControllerVisiable(false);
                }
            });
        }
    }

    private void initData() {

    }

    private void initView() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mFrameLayout = (FrameLayout) findViewById(R.id.fl);
        mController = (LinearLayout) findViewById(R.id.controller_ll);
        mRegsiterBtn = (TextView) findViewById(R.id.register_btn);
        mLoginBtn = (TextView) findViewById(R.id.login_btn);
        mLogoutBtn = (TextView) findViewById(R.id.logout_btn);
        mAdminLoginBtn = (TextView) findViewById(R.id.admin_login_btn);

        mAdminLl = (LinearLayout) findViewById(R.id.admin_ll);
        mMessageEt = (EditText) findViewById(R.id.message_et);
        mChooseContactTv = (TextView) findViewById(R.id.choose_contact_tv);
        mContactTv = (TextView) findViewById(R.id.contact_tv);
        mSendTv = (TextView) findViewById(R.id.send_tv);

        mSnapshotIv = (ImageView) findViewById(R.id.snapshot_iv);
        mUserNameTv = (TextView) findViewById(R.id.username_tv);
        mStatusTv = (TextView) findViewById(R.id.status_tv);
        mMessageTv = (TextView) findViewById(R.id.message_tv);
        mVideoView = (PLVideoTextureView) findViewById(R.id.VideoView);
        View loadingView = findViewById(R.id.LoadingView);
        loadingView.setVisibility(View.INVISIBLE);
        mVideoView.setBufferingIndicator(loadingView);
        View coverView = findViewById(R.id.CoverView);
        mVideoView.setCoverView(coverView);
        mStatInfoTextView = (TextView) findViewById(R.id.StatInfoTextView);
    }

    private void register() {
        DeviceUuidFactory uuidFactory = new DeviceUuidFactory(MainActivity.this);
        String username = uuidFactory.getDeviceUuid().toString();
        String password = uuidFactory.getDeviceUuid().toString();
        BasicCallback callback = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                mMessageTv.setText(s);
                if (i == 0) {
                    UserInfo myInfo = JMessageClient.getMyInfo();
                    ToastUtil.shortToast(MainActivity.this, "注册成功");
                } else if (898001 == i) {   //已经注册了，直接登陆
                    login();
                } else {
                    ToastUtil.shortToast(MainActivity.this, "Code:" + i + "   Reason:" + s);
                }
            }
        };
        JMessageClient.register(username, password, callback);
    }


    @Override
    protected void onResume() {
        startDismissControlViewTimer();
        super.onResume();
    }

    private void goSetActivity() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_btn:
                register();
                break;
            case R.id.login_btn:
                login();
                break;
            case R.id.logout_btn:
                logout();
                break;
            case R.id.admin_login_btn:
                adminLogin();
                break;
            case R.id.choose_contact_tv:
                chooseContact();
                break;
            case R.id.send_tv:
                sendMessage();
                break;
            default:
                break;
        }
    }

    private void adminLogin() {
        JMessageClient.logout();
        String username = "admin";
        String password = "admin";
        BasicCallback callback = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                mMessageTv.setText(s);
                if (i == 0) {
                    UserInfo myInfo = JMessageClient.getMyInfo();
                    ToastUtil.shortToast(MainActivity.this, "登陆成功" + myInfo.getUserName());
                    mUserNameTv.setText(myInfo.getUserName());
                    admin = true;
                    mStatusTv.setText("在线");
                    mAdminLl.setVisibility(View.VISIBLE);
                    mMessageTv.setText("输入：\n" +
                            "1--打开屏幕  2--关闭屏幕\n" +
                            "3--截屏  4--增大音量\n" +
                            "5--调小音量 6--暂停/开始播放\n" +
                            "视频地址--播放视频  其他--无响应");
                } else {
                    ToastUtil.shortToast(MainActivity.this, "Code:" + i + "   Reason:" + s);
                }
            }
        };
        JMessageClient.login(username, password, callback);
    }

    private void sendMessage() {
        if (TextUtils.isEmpty(mContactTv.getText())) {
            ToastUtil.shortToast(MainActivity.this, "联系人不能为空");
            return;
        }
        if (TextUtils.isEmpty(mMessageEt.getText())) {
            ToastUtil.shortToast(MainActivity.this, "消息不能为空");
            return;
        }

        Message message = JMessageClient.createSingleTextMessage(mContactTv.getText().toString(),
                "", mMessageEt.getText().toString().trim());
        message.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if (i == 0) {
                    //消息发送成功
                    ToastUtil.shortToast(MainActivity.this, "消息发送成功");
                } else {
                    //消息发送失败
                    ToastUtil.shortToast(MainActivity.this, "消息发送失败");
                }
            }
        });
        JMessageClient.sendMessage(message);
    }

    private void chooseContact() {
        String auth = "Basic " + Base64Coder.encodeString("6aecbbb0036c315f0d49f8f3:3c5cd35f01d2382479ae65a1");
        Call<UserList> call = Api.getDefault(HostType.JPUSH_HOST)
                .getContactList(Api.getCacheControl(), auth, "0", "10");
        call.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(Call<UserList> call, Response<UserList> response) {
                UserList userList = response.body();

                InfoDialog companyDialog = new InfoDialog(MainActivity.this, "选择设备", userList.getUsers(), new InfoDialog.OnDialogListener() {
                    @Override
                    public void selectInfo(Users users) {
                        mContactTv.setText(users.getUsername());
                    }
                });
                companyDialog.show();
            }

            @Override
            public void onFailure(Call<UserList> call, Throwable t) {

            }
        });

    }


    private void logout() {
        if ("admin".equals(JMessageClient.getMyInfo().getUserName())) {
            mAdminLl.setVisibility(View.GONE);
        }
        JMessageClient.logout();
        mStatusTv.setText("离线");
    }

    private void login() {
        DeviceUuidFactory uuidFactory = new DeviceUuidFactory(MainActivity.this);
        String username = uuidFactory.getDeviceUuid().toString();
        String password = uuidFactory.getDeviceUuid().toString();
        BasicCallback callback = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                mMessageTv.setText(s);
                if (i == 0) {
                    UserInfo myInfo = JMessageClient.getMyInfo();
                    JMessageClient.getNoDisturbGlobal(new IntegerCallback() {
                        @Override
                        public void gotResult(int i, String s, Integer integer) {
                        }
                    });
                    mAdminLl.setVisibility(View.GONE);
                    admin = false;
                    ToastUtil.shortToast(MainActivity.this, "登陆成功" + myInfo.getUserName());
                    mUserNameTv.setText(myInfo.getUserName());
                    mStatusTv.setText("在线");
                } else {
                    ToastUtil.shortToast(MainActivity.this, "Code:" + i + "   Reason:" + s);
                }
            }
        };
        JMessageClient.login(username, password, callback);
    }


    @Override
    protected void onDestroy() {
        mVideoView.stopPlayback();
        JMessageClient.unRegisterEventReceiver(this);
        super.onDestroy();
    }

    public void onEventMainThread(MessageEvent event) {
        Message msg = event.getMessage();
        ToastUtil.shortToast(MainActivity.this, "收到消息：" + msg.getFromUser().getUserName());
        switch (msg.getContentType()) {
            case text:
                //处理文字消息
                TextContent textContent = (TextContent) msg.getContent();
                ToastUtil.shortToast(MainActivity.this, "消息：" + textContent.getText());
                mMessageTv.setText(textContent.getText());
                textContent.getText();
                handMessageEvent(textContent.getText());
                break;
            case image:
                //处理图片消息
                ImageContent imageContent = (ImageContent) msg.getContent();
                imageContent.getLocalPath();//图片本地地址
                imageContent.getLocalThumbnailPath();//图片对应缩略图的本地地址
                break;
            case voice:
                //处理语音消息
                VoiceContent voiceContent = (VoiceContent) msg.getContent();
                voiceContent.getLocalPath();//语音文件本地地址
                voiceContent.getDuration();//语音文件时长
                break;
            case custom:
                //处理自定义消息
                CustomContent customContent = (CustomContent) msg.getContent();
                customContent.getNumberValue("custom_num"); //获取自定义的值
                customContent.getBooleanValue("custom_boolean");
                customContent.getStringValue("custom_string");
                break;
            case eventNotification:
                //处理事件提醒消息
                EventNotificationContent eventNotificationContent = (EventNotificationContent) msg.getContent();
                switch (eventNotificationContent.getEventNotificationType()) {
                    case group_member_added:
                        //群成员加群事件
                        break;
                    case group_member_removed:
                        //群成员被踢事件
                        break;
                    case group_member_exit:
                        //群成员退群事件
                        break;
                    case group_info_updated://since 2.2.1
                        //群信息变更事件
                        break;
                }
                break;
        }
    }

    private void handMessageEvent(String text) {
        if (String.valueOf(Event.SCREEN_ON).equals(text)) {
            screenOn();
        } else if (String.valueOf(Event.SCREEN_OFF).equals(text)) {
            screenOff();
        } else if (String.valueOf(Event.SNAPSHOT).equals(text)) {
            snapshot();
        } else if (String.valueOf(Event.VOLUME_UP).equals(text)) {
            volumeUp();
        } else if (String.valueOf(Event.VOLUME_DOWN).equals(text)) {
            volumeDown();
        } else if (String.valueOf(Event.PAUSE_START).equals(text)) {
            pauseOrStartPlay();
        } else if (text.startsWith("http")) {
            playVideo(text);
        } else {
            ToastUtil.shortToast(MainActivity.this, "无法识别的指令");
        }
    }

    private void pauseOrStartPlay() {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        } else if (mCurrentPath != null) {
            mVideoView.start();
        }
    }

    private void playVideo(String text) {
//        if (mVideoView.isPlaying()) {
//            mVideoView.stopPlayback();
//        }
        mCurrentPath = text;
        mVideoView.setVideoPath(text);
        mVideoView.start();
    }

    private void volumeUp() {
        //初始化音频管理器
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //获取系统最大音量
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 获取设备当前音量
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        ToastUtil.shortToast(MainActivity.this, "当前音量：" + currentVolume);

        //增大音量
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);

        ToastUtil.shortToast(MainActivity.this, "增大后音量：" + mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    private void volumeDown() {
        //初始化音频管理器
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //获取系统最大音量
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 获取设备当前音量
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        ToastUtil.shortToast(MainActivity.this, "当前音量：" + currentVolume);

        //减少音量
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
        ToastUtil.shortToast(MainActivity.this, "减少后音量：" + mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
//        mVideoView.pause();
        super.onPause();
    }

    private void snapshot() {
        String filePath = saveCurrentImage();
        ToastUtil.shortToast(MainActivity.this, "屏幕已截取，等待上传");
        Glide.with(MainActivity.this).load(filePath).into(mSnapshotIv);
    }

    private String saveCurrentImage() {
        //1.构建Bitmap
        //2.获取屏幕
        Bitmap Bmp = getScreenshot();
        String SavePath = getSDCardPath();
        String filepath = null;
        //3.保存Bitmap
        try {
            File path = new File(SavePath);
            //文件
            filepath = SavePath + File.separator + System.currentTimeMillis() + ".png";
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
                Bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
                Toast.makeText(MainActivity.this, "截屏文件已保存", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return filepath;
        }
    }


    private Bitmap getScreenshot() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int w = display.getWidth();
        int h = display.getHeight();
        View decorview = this.getWindow().getDecorView();
        decorview.setDrawingCacheEnabled(true);
        decorview.buildDrawingCache();
        Bitmap content = mVideoView.getTextureView().getBitmap();
        Bitmap layout = decorview.getDrawingCache();
        Bitmap screenshot = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        // 把两部分拼起来，先把视频截图绘制到上下左右居中的位置，再把播放器的布局元素绘制上去。
        Canvas canvas = new Canvas(screenshot);
        canvas.drawBitmap(layout, 0, 0, new Paint());
        canvas.drawBitmap(content, (layout.getWidth() - content.getWidth()) / 2, (layout.getHeight() - content.getHeight()) / 2, new Paint());
        canvas.save();
        canvas.restore();
        return screenshot;
    }

    private String getSDCardPath() {
        File sdcardDir = null;
        //判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        return sdcardDir.toString();
    }

    private void screenOff() {
        if (!canLock) {
            return;
        }
        if (policyManager.isAdminActive(componentName)) {
            Window localWindow = getWindow();
            WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
            localLayoutParams.screenBrightness = 0.05F;
            localWindow.setAttributes(localLayoutParams);
            policyManager.lockNow();
        }
    }

    private void screenOn() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
    }


    public void onEventMainThread(LoginStateChangeEvent event) {
        LoginStateChangeEvent.Reason reason = event.getReason();//获取变更的原因
        UserInfo myInfo = event.getMyInfo();//获取当前被登出账号的信息
        switch (reason) {
            case user_password_change:
                //用户密码在服务器端被修改
                break;
            case user_logout:
                mStatusTv.setText("离线");
                //用户换设备登录
                break;
            case user_deleted:
                //用户被删除
                break;
        }
    }

    public void onEventMainThread(ContactNotifyEvent event) {
        String reason = event.getReason();
        String fromUsername = event.getFromUsername();
        String appkey = event.getfromUserAppKey();
        mMessageTv.setText(reason);
        switch (event.getType()) {
            case invite_received://收到好友邀请
                //...
                break;
            case invite_accepted://对方接收了你的好友邀请
                ToastUtil.shortToast(MainActivity.this, "绑定管理账户成功" + fromUsername);
                Conversation.createSingleConversation(fromUsername);
                //...
                break;
            case invite_declined://对方拒绝了你的好友邀请
                //...
                break;
            case contact_deleted://对方将你从好友中删除
                //...
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (1 == requestCode) {
            if (RESULT_OK == resultCode) {
                canLock = true;
            } else if (RESULT_CANCELED == resultCode) {
                canLock = false;
            }
        }
    }

    private PLMediaPlayer.OnInfoListener mOnInfoListener = new PLMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
            Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    break;
                case PLMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Log.i(TAG, "First video render time: " + extra + "ms");
                    break;
                case PLMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                    Log.i(TAG, "First audio render time: " + extra + "ms");
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    Log.i(TAG, "video frame rendering, ts = " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    Log.i(TAG, "audio frame rendering, ts = " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_VIDEO_GOP_TIME:
                    Log.i(TAG, "Gop Time: " + extra);
                    break;
                case PLMediaPlayer.MEDIA_INFO_SWITCHING_SW_DECODE:
                    Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                default:
                    break;
            }
            return true;
        }
    };


    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer mp, int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLMediaPlayer.ERROR_CODE_IO_ERROR:
                    /**
                     * SDK will do reconnecting automatically
                     */
                    showToastTips("IO Error !");
                    return false;
                default:
                    showToastTips("unknown error !");
                    break;
            }
//            finish();
            // TODO: 2017/8/14
            playNext();
            return true;
        }
    };


    private void playNext() {
        if (mNextPath == null || TextUtils.isEmpty(mNextPath)) {
            playVideo(mCurrentPath);
        }
    }

    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer plMediaPlayer) {
            Log.i(TAG, "Play Completed !");
            // TODO: 2017/8/14  播放下一个视频
            playNext();
        }
    };

    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int precent) {
            Log.i(TAG, "onBufferingUpdate: " + precent);
        }
    };

    private PLMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(PLMediaPlayer plMediaPlayer, int i, int i1) {
        }

    };

    private void showToastTips(final String tips) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(MainActivity.this, tips, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }

    private void updateStatInfo() {
        long bitrate = mVideoView.getVideoBitrate() / 1024;
        final String stat = bitrate + "kbps, " + mVideoView.getVideoFps() + "fps";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatInfoTextView.setText(stat);
            }
        });
    }
}
