package com.forthorn.projecting.video.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.forthorn.projecting.video.IVideoListener;
import com.forthorn.projecting.video.IVideoView;

/**
 * Created by: Forthorn
 * Date: 2018/11/5.
 * Description:
 */
public class MVideoView extends FrameLayout implements IVideoView, IMPlayListener {

    private String TAG = "MVideoView";
    private MPlayer mMPlayer;
    private SurfaceView mSurfaceView;
    IVideoListener mVideoListener;

    public MVideoView(@NonNull Context context) {
        this(context, null);
    }

    public MVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mSurfaceView = new SurfaceView(context);
        LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mSurfaceView, layoutParams);
        mMPlayer = new MPlayer();
        MinimalDisplay minimalDisplay = new MinimalDisplay(mSurfaceView);
        mMPlayer.setDisplay(minimalDisplay);
        mMPlayer.setPlayListener(this);
    }


    @Override
    public void start() {
        try {
            mMPlayer.play();
        } catch (MPlayerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        mMPlayer.pause();

    }

    @Override
    public void stopPlayback() {
        mMPlayer.pause();
    }

    @Override
    public void setVideoURI(Uri uri) {
        try {
            mMPlayer.setSource(uri.toString());
        } catch (MPlayerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bitmap getSnapShot() {
        return null;
    }

    @Override
    public void setVideoListener(IVideoListener iVideoListener) {
        mVideoListener = iVideoListener;
    }

    @Override
    public void onStart(IMPlayer player) {

    }

    @Override
    public void onPause(IMPlayer player) {

    }

    @Override
    public void onResume(IMPlayer player) {

    }

    @Override
    public void onComplete(IMPlayer player) {
        if (mVideoListener != null) {
            mVideoListener.onCompletion();
        }
    }
}
