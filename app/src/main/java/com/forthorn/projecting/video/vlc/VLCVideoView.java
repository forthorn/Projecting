package com.forthorn.projecting.video.vlc;

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

import org.videolan.vlc.VlcVideoView;
import org.videolan.vlc.listener.MediaListenerEvent;

public class VLCVideoView extends FrameLayout implements IVideoView, MediaListenerEvent {

    private String TAG = "VLCVideoView";
    private VlcVideoView vlcVideoView;
    IVideoListener mVideoListener;

    public VLCVideoView(@NonNull Context context) {
        this(context, null);
    }

    public VLCVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VLCVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        vlcVideoView = new VlcVideoView(context);
        LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(vlcVideoView, layoutParams);
        vlcVideoView.setMediaListenerEvent(this);
    }


    @Override
    public void start() {
        vlcVideoView.startPlay();
    }

    @Override
    public void pause() {
        vlcVideoView.pause();
    }

    @Override
    public void stopPlayback() {
        vlcVideoView.onStop();
    }

    @Override
    public void setVideoURI(Uri uri) {
        if (uri == null) {
            LogUtils.e(TAG, "URI is null");
            return;
        }
//        LibVLC libVLC = VLCInstance.get(getContext());
//        Media media = new Media(libVLC, uri.getPath());
        //  VLCOptions.setMediaOptions(media,getActivity(),VLCOptions.HW_ACCELERATION_AUTOMATIC);
//        media.setHWDecoderEnabled(true, true);
//        media.setHWDecoderEnabled(false, false);
//        vlcVideoView.setMedia(new MediaPlayer(media));
        vlcVideoView.setPath(uri.getPath());
    }

    @Override
    public Bitmap getSnapShot() {
        return vlcVideoView.getBitmap();
    }

    @Override
    public void setVideoListener(IVideoListener iVideoListener) {
        mVideoListener = iVideoListener;
    }

    @Override
    public void eventBuffing(int event, float buffing) {
        LogUtils.e(TAG, "eventBuffing:" + buffing);
    }

    @Override
    public void eventStop(boolean isPlayError) {
        if (isPlayError) {
            LogUtils.e(TAG, "eventStop 播放出现错误");
        }
        if (mVideoListener != null) {
            mVideoListener.onCompletion();
        }
    }

    @Override
    public void eventError(int event, boolean show) {
        LogUtils.e(TAG, "Error happened, errorCode = " + event);
        if (mVideoListener != null) {
            mVideoListener.onError();
        }
    }

    @Override
    public void eventPlay(boolean isPlaying) {
        LogUtils.e(TAG, "eventPlay");
    }

    @Override
    public void eventPlayInit(boolean openClose) {

    }
}
