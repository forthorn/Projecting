package com.forthorn.projecting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.forthorn.projecting.api.Api;
import com.forthorn.projecting.api.HostType;
import com.forthorn.projecting.app.BundleKey;
import com.forthorn.projecting.app.DeviceUuidFactory;
import com.forthorn.projecting.app.Status;
import com.forthorn.projecting.baserx.BaseResponse;
import com.forthorn.projecting.entity.Event;
import com.forthorn.projecting.entity.IMAccount;
import com.forthorn.projecting.func.picture.AutoViewPager;
import com.forthorn.projecting.func.picture.PictureAdapter;
import com.forthorn.projecting.util.SPUtils;
import com.forthorn.projecting.util.ToastUtil;
import com.forthorn.projecting.widget.NoticeDialog;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.IntegerCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.forthorn.projecting.app.Status.*;


public class HomeActivity extends Activity implements View.OnClickListener {
    //视频
    private FrameLayout mVideoFl;
    private PLVideoTextureView mVideoView;
    //图片
    private FrameLayout mPictureFl;
    private AutoViewPager mPicturePager;
    private PictureAdapter mPictureAdapter;
    //待机
    private FrameLayout mIdleFl;
    private ImageView mIdleAboutIv;
    private ImageView mIdleQrcodeIv;
    private TextView mIdleQrcodeTv;
    private LinearLayout mIdleServerStatusLl;
    private TextView mIdleServerStatusTv;
    //文本
    private FrameLayout mTextFl;
    private TextView mTextTv;
    private LinearLayout mTextServerHolderLl;

    private Context mContext;
    private List<String> mPicList = new ArrayList<>();
    private Status mStatus;

    private String mUuid;
    private String mIMUsername;
    private String mIMPassword;
    private String mDeviceId;
    private String mDeviceCode;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = HomeActivity.this;
        setContentView(R.layout.activity_home);
        initView();
        initData();
        initEvent();
        initIM();
        initPlayer();
    }

    private void initPlayer() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        int codec = getIntent().getIntExtra("mediaCodec", AVOptions.MEDIA_CODEC_SW_DECODE);
        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);
        mVideoView.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_FIT_PARENT);
        mVideoView.setAVOptions(options);
        mVideoView.setDebugLoggingEnabled(true);
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
    }

    private void initIM() {
//        mIMUsername = SPUtils.getSharedStringData(mContext, BundleKey.IM_ACCOUNT);
//        mIMPassword = SPUtils.getSharedStringData(mContext, BundleKey.IM_PASSWORD);
        JMessageClient.registerEventReceiver(this);
        requestIMAccount();
//        if (TextUtils.isEmpty(mDeviceId)) {
//            registerIM();
//        } else {
//            login();
//        }
    }

    private void registerIM() {
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
        mIdleAboutIv = (ImageView) findViewById(R.id.idle_about_iv);
        mIdleQrcodeIv = (ImageView) findViewById(R.id.idle_qrcode_iv);
        mIdleQrcodeTv = (TextView) findViewById(R.id.idle_qrcode_tv);
        mIdleServerStatusLl = (LinearLayout) findViewById(R.id.idle_server_status_ll);
        mIdleServerStatusTv = (TextView) findViewById(R.id.idle_server_status_tv);
        //文字
        mTextFl = (FrameLayout) findViewById(R.id.text_fl);
        mTextTv = (TextView) findViewById(R.id.text_tv);
        mTextServerHolderLl = (LinearLayout) findViewById(R.id.text_server_holder_ll);
        mIdleAboutIv.setOnClickListener(this);
    }

    private void initData() {
        DeviceUuidFactory uuidFactory = new DeviceUuidFactory(mContext);
        mUuid = uuidFactory.getDeviceUuid().toString();
        // TODO: 2017/11/3
        mUuid = "11211";
        SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_CODE, mUuid);
        mDeviceCode = mUuid;

        mDeviceId = SPUtils.getSharedStringData(mContext, BundleKey.DEVICE_ID);
        mIdleAboutIv.setImageResource(TextUtils.isEmpty(mDeviceCode) ? R.drawable.ic_info_offline : R.drawable.ic_info_online);

        mPicList.add("https://img11.360buyimg.com/da/jfs/t9595/285/2471111611/183642/3aad4810/59f7e3afN583ea737.jpg");
        mPictureAdapter = new PictureAdapter(mContext, mPicList);
        mPicturePager.setAdapter(mPictureAdapter);
//        mPicturePager.start();
        mStatus = IDLE;


    }

    private void initEvent() {

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
                mIMUsername = imAccount.getData().getEquipment_im_account();
                mIMPassword = imAccount.getData().getEquipment_im_password();
                mDeviceId = imAccount.getData().getEquipment_id();
                mDeviceCode = imAccount.getData().getEquipment_code();
                SPUtils.setSharedStringData(mContext, BundleKey.IM_ACCOUNT, mIMUsername);
                SPUtils.setSharedStringData(mContext, BundleKey.IM_PASSWORD, mIMPassword);
                SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_ID, mDeviceId);
                SPUtils.setSharedStringData(mContext, BundleKey.DEVICE_CODE, mDeviceCode);
                login();
            }

            @Override
            public void onFailure(Call<IMAccount> call, Throwable t) {
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 登陆IM
     */
    private void login() {
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
                    ToastUtil.shortToast(mContext, "登陆成功" + myInfo.getUserName());
                    // TODO: 10/31/2017  请求数据
                    mIdleServerStatusTv.setText("在线");
                    mIdleServerStatusTv.setEnabled(true);
                } else {
                    ToastUtil.shortToast(mContext, "Code:" + i + "   Reason:" + s);
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
        switch (reason) {
            case user_password_change:
                logout();
                break;
            case user_logout:
                logout();
                break;
            case user_deleted:
                logout();
                break;
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
                ToastUtil.shortToast(mContext, "消息：" + textContent.getText());
                Log.d("MessageEvent", "消息：" + textContent.getText());
                handMessageEvent(textContent.getText());
                break;
            case image:
                break;
            case voice:
                break;
            case custom:
                break;
            case eventNotification:
                break;
            default:
                break;
        }
    }


    /**
     * IM消息处理
     *
     * @param text
     */
    private void handMessageEvent(String text) {
        if (String.valueOf(Event.SCREEN_ON).equals(text)) {
            screenOn();
        } else if (String.valueOf(Event.SCREEN_OFF).equals(text)) {
            screenOff();
        } else if (String.valueOf(Event.SNAPSHOTS).equals(text)) {
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
            ToastUtil.shortToast(mContext, "无法识别的指令");
        }
    }

    private void playVideo(String text) {

    }

    private void pauseOrStartPlay() {

    }

    private void volumeDown() {
        // TODO: 10/31/2017  先降音量，再通知服务器
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Double v = volume * 14.28571428571429D;
        Call<BaseResponse> volumeCall = Api.getDefault(HostType.VOM_HOST).setVolume(Api.getCacheControl(),
                mDeviceId, mDeviceCode, String.valueOf(Math.ceil(v)));
        volumeCall.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                BaseResponse baseResponse = response.body();

            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {

            }
        });
    }


    private void volumeUp() {
        // TODO: 10/31/2017  先升音量，再通知服务器
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Double v = volume * 14.28571428571429D;
        Call<BaseResponse> volumeCall = Api.getDefault(HostType.VOM_HOST).setVolume(Api.getCacheControl(),
                mDeviceId, mDeviceCode, String.valueOf(Math.ceil(v)));
        volumeCall.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                BaseResponse baseResponse = response.body();

            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {

            }
        });
    }

    private void snapshot() {

    }

    private void screenOff() {

    }

    private void screenOn() {

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


    private String TAG = "";


    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer mp, int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLMediaPlayer.ERROR_CODE_IO_ERROR:
                    Toast.makeText(mContext, "IO Error !", Toast.LENGTH_SHORT).show();
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
            Log.i(TAG, "Play Completed !");
            // TODO: 10/31/2017  播放完成
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
        public void onVideoSizeChanged(PLMediaPlayer plMediaPlayer, int i, int i1, int i2, int i3) {
            Log.i(TAG, "onVideoSizeChanged: width = " + i + ", height = " + i1 + "I2" + i2 + "I3" + i3);
        }
    };


    @Override
    protected void onDestroy() {
        mVideoView.stopPlayback();
        JMessageClient.unRegisterEventReceiver(this);
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
        if (TextUtils.isEmpty(SPUtils.getSharedStringData(mContext, BundleKey.DEVICE_ID))) {
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
}
