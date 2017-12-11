package com.xiao.nicevideoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XiaoJianjun on 2017/6/21.
 * 仿腾讯视频热点列表页播放器控制器.
 */
public class SimpleVideoPlayerController
        extends NiceVideoPlayerController
        implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        ChangeClarityDialog.OnClarityChangedListener {

    private Context mContext;

    private boolean topBottomVisible;
    private CountDownTimer mDismissTopBottomCountDownTimer;

    private List<Clarity> clarities;
    private int defaultClarityIndex;

    private ChangeClarityDialog mClarityDialog;

    private boolean hasRegisterBatteryReceiver; // 是否已经注册了电池广播

    public SimpleVideoPlayerController(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.simple_video_palyer_controller, this, true);
        this.setOnClickListener(this);
    }

    @Override
    public void setTitle(String title) {
    }

    @Override
    public ImageView imageView() {
        return null;
    }

    @Override
    public void setImage(@DrawableRes int resId) {

    }

    @Override
    public void setLenght(long length) {
    }

    @Override
    public void setNiceVideoPlayer(INiceVideoPlayer niceVideoPlayer) {
        super.setNiceVideoPlayer(niceVideoPlayer);
        // 给播放器配置视频链接地址
        if (clarities != null && clarities.size() > 1) {
            mNiceVideoPlayer.setUp(clarities.get(defaultClarityIndex).videoUrl, null);
        }
    }

    /**
     * 设置清晰度
     *
     * @param clarities 清晰度及链接
     */
    public void setClarity(List<Clarity> clarities, int defaultClarityIndex) {
        if (clarities != null && clarities.size() > 1) {
            this.clarities = clarities;
            this.defaultClarityIndex = defaultClarityIndex;

            List<String> clarityGrades = new ArrayList<>();
            for (Clarity clarity : clarities) {
                clarityGrades.add(clarity.grade + " " + clarity.p);
            }
            // 初始化切换清晰度对话框
            mClarityDialog = new ChangeClarityDialog(mContext);
            mClarityDialog.setClarityGrade(clarityGrades, defaultClarityIndex);
            mClarityDialog.setOnClarityCheckedListener(this);
            // 给播放器配置视频链接地址
            if (mNiceVideoPlayer != null) {
                mNiceVideoPlayer.setUp(clarities.get(defaultClarityIndex).videoUrl, null);
            }
        }
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        switch (playState) {
            case NiceVideoPlayer.STATE_IDLE:
                break;
            case NiceVideoPlayer.STATE_PREPARING:
                break;
            case NiceVideoPlayer.STATE_PREPARED:
                startUpdateProgressTimer();
                break;
            case NiceVideoPlayer.STATE_PLAYING:
                startDismissTopBottomTimer();
                break;
            case NiceVideoPlayer.STATE_PAUSED:
                cancelDismissTopBottomTimer();
                break;
            case NiceVideoPlayer.STATE_BUFFERING_PLAYING:
                startDismissTopBottomTimer();
                break;
            case NiceVideoPlayer.STATE_BUFFERING_PAUSED:
                cancelDismissTopBottomTimer();
                break;
            case NiceVideoPlayer.STATE_ERROR:
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                break;
            case NiceVideoPlayer.STATE_COMPLETED:
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                mNiceVideoPlayer.release();
                mNiceVideoPlayer.start();
                break;
        }
    }

    @Override
    protected void onPlayModeChanged(int playMode) {
        switch (playMode) {
            case NiceVideoPlayer.MODE_NORMAL:
                if (hasRegisterBatteryReceiver) {
                    mContext.unregisterReceiver(mBatterReceiver);
                    hasRegisterBatteryReceiver = false;
                }
                break;
            case NiceVideoPlayer.MODE_FULL_SCREEN:
                if (!hasRegisterBatteryReceiver) {
                    mContext.registerReceiver(mBatterReceiver,
                            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    hasRegisterBatteryReceiver = true;
                }
                break;
            case NiceVideoPlayer.MODE_TINY_WINDOW:
                break;
        }
    }

    /**
     * 电池状态即电量变化广播接收器
     */
    private BroadcastReceiver mBatterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };

    @Override
    protected void reset() {
        topBottomVisible = false;
        cancelUpdateProgressTimer();
        cancelDismissTopBottomTimer();
    }

    /**
     * 尽量不要在onClick中直接处理控件的隐藏、显示及各种UI逻辑。
     * UI相关的逻辑都尽量到{@link #onPlayStateChanged}和{@link #onPlayModeChanged}中处理.
     */
    @Override
    public void onClick(View v) {
    }

    @Override
    public void onClarityChanged(int clarityIndex) {
        // 根据切换后的清晰度索引值，设置对应的视频链接地址，并从当前播放位置接着播放
        Clarity clarity = clarities.get(clarityIndex);
        long currentPosition = mNiceVideoPlayer.getCurrentPosition();
        mNiceVideoPlayer.releasePlayer();
        mNiceVideoPlayer.setUp(clarity.videoUrl, null);
        mNiceVideoPlayer.start(currentPosition);
    }

    @Override
    public void onClarityNotChanged() {
        // 清晰度没有变化，对话框消失后，需要重新显示出top、bottom
        setTopBottomVisible(true);
    }

    /**
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    private void setTopBottomVisible(boolean visible) {
        topBottomVisible = visible;
        if (visible) {
            if (!mNiceVideoPlayer.isPaused() && !mNiceVideoPlayer.isBufferingPaused()) {
                startDismissTopBottomTimer();
            }
        } else {
            cancelDismissTopBottomTimer();
        }
    }

    /**
     * 开启top、bottom自动消失的timer
     */
    private void startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = new CountDownTimer(8000, 8000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomCountDownTimer.start();
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer.cancel();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mNiceVideoPlayer.isBufferingPaused() || mNiceVideoPlayer.isPaused()) {
            mNiceVideoPlayer.restart();
        }
        long position = (long) (mNiceVideoPlayer.getDuration() * seekBar.getProgress() / 100f);
        mNiceVideoPlayer.seekTo(position);
        startDismissTopBottomTimer();
    }

    @Override
    protected void updateProgress() {
    }

    @Override
    protected void showChangePosition(long duration, int newPositionProgress) {
    }

    @Override
    protected void hideChangePosition() {
    }

    @Override
    protected void showChangeVolume(int newVolumeProgress) {
    }

    @Override
    protected void hideChangeVolume() {
    }

    @Override
    protected void showChangeBrightness(int newBrightnessProgress) {
    }

    @Override
    protected void hideChangeBrightness() {
    }
}
