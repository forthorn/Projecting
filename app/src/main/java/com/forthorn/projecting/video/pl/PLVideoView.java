package com.forthorn.projecting.video.pl;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.forthorn.projecting.util.LogUtils;
import com.forthorn.projecting.video.IVideoListener;
import com.forthorn.projecting.video.IVideoView;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoTextureView;

public class PLVideoView extends FrameLayout implements IVideoView {

    private String TAG = "PLVideoView";
    PLVideoTextureView mVideoView;
    IVideoListener mVideoListener;

    public PLVideoView(@NonNull Context context) {
        this(context, null);
    }

    public PLVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PLVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mVideoView = new PLVideoTextureView(context);
        LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mVideoView, layoutParams);
        int codec = AVOptions.MEDIA_CODEC_HW_DECODE;
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

    public void setVideoListener(IVideoListener iVideoListener) {
        mVideoListener = iVideoListener;
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


    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer mp, int errorCode) {
            LogUtils.e(TAG, "Error happened, errorCode = " + errorCode);
            if (mVideoListener != null) {
                mVideoListener.onError();
            }
            switch (errorCode) {
                case PLMediaPlayer.ERROR_CODE_IO_ERROR:
//                    Toast.makeText(mContext, "IO Error !", Toast.LENGTH_SHORT).show();
                    return false;
                default:
                    break;
            }
            //遇到错误后再尝试重新播放
            try {
                mVideoView.start();
            } catch (Exception e) {
            }
            return true;
        }
    };


    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer plMediaPlayer) {
            LogUtils.i(TAG, "Play Completed !");
            if (mVideoListener != null) {
                mVideoListener.onCompletion();
            }
        }
    };

    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int precent) {
            LogUtils.i(TAG, "onBufferingUpdate: " + precent);
            if (mVideoListener != null) {
                mVideoListener.onBufferingUpdate(precent);
            }
        }
    };

    private PLMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(PLMediaPlayer plMediaPlayer, int i, int i1, int i2, int i3) {
            LogUtils.i(TAG, "onVideoSizeChanged: width = " + i + ", height = " + i1 + "I2" + i2 + "I3" + i3);
            if (mVideoListener != null) {
                mVideoListener.onVideoSizeChanged();
            }
        }
    };


    @Override
    public void start() {
        mVideoView.start();
    }

    @Override
    public void pause() {
        mVideoView.pause();
    }

    @Override
    public void stopPlayback() {
        mVideoView.stopPlayback();
    }

    @Override
    public void setVideoURI(Uri uri) {
        mVideoView.setVideoURI(uri);
    }

    @Override
    public Bitmap getSnapShot() {
        return mVideoView.getTextureView().getBitmap();
    }
}
