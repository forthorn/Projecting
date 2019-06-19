package com.forthorn.projecting.video.ijk;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.forthorn.libijk.widget.media.IjkVideoView;
import com.forthorn.projecting.util.LogUtils;
import com.forthorn.projecting.video.IVideoListener;
import com.forthorn.projecting.video.IVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IJKVideoView extends FrameLayout implements IVideoView, IMediaPlayer.OnCompletionListener {

    private String TAG = "IjkVideoView";
    private Uri mUri;

    IVideoListener mVideoListener;
    private Context mContext;
    IjkVideoView ijkVideoView;

    public IJKVideoView(@NonNull Context context) {
        this(context, null);
    }

    public IJKVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IJKVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        ijkVideoView = new IjkVideoView(context);
        LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(ijkVideoView, layoutParams);
        ijkVideoView.setOnCompletionListener(this);
    }


    @Override
    public void start() {
        ijkVideoView.start();
    }

    @Override
    public void pause() {
        ijkVideoView.pause();
    }

    @Override
    public void stopPlayback() {
        ijkVideoView.stopPlayback();
    }

    @Override
    public void setVideoURI(Uri uri) {
        if (uri == null) {
            LogUtils.e(TAG, "URI is null");
            return;
        }
        mUri = uri;
        ijkVideoView.setVideoURI(uri);
    }

    @Override
    public Bitmap getSnapShot() {
        // 截屏
        return ijkVideoView.getShortcut();
    }

    @Override
    public void setVideoListener(IVideoListener iVideoListener) {
        mVideoListener = iVideoListener;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        setVideoURI(mUri);
        ijkVideoView.start();
    }
}
