package com.forthorn.projecting.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

//import com.forthorn.projecting.video.exo.ExoVideoView;
import com.forthorn.projecting.BuildConfig;
import com.forthorn.projecting.video.ijk.IJKVideoView;
import com.forthorn.projecting.video.player.MVideoView;

public class VideoView extends FrameLayout {

    private static final String TAG = "VideoView";
    private IVideoView mIVideoView;
    private Context context;
    private int type;

    public static final int TYPE_VLC = 0;
    public static final int TYPE_PL = 1;
    public static final int TYPE_ML = 2;
    public static final int TYPE_EXO = 3;

    public VideoView(@NonNull Context context) {
        this(context, null);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setUp(IVideoListener iVideoListener, int type) {
        switch (type) {
            case TYPE_VLC:
                mIVideoView = new IJKVideoView(context);
                if (BuildConfig.DEBUG) {
//                    Toast.makeText(context, "IJKEXO播放器", Toast.LENGTH_SHORT).show();
                }
                break;
            case TYPE_PL:
                mIVideoView = new IJKVideoView(context);
                if (BuildConfig.DEBUG) {
//                    Toast.makeText(context, "IJKEXO播放器", Toast.LENGTH_SHORT).show();
                }
                break;
            case TYPE_ML:
                mIVideoView = new MVideoView(context);
                if (BuildConfig.DEBUG) {
//                    Toast.makeText(context, "MVideoView", Toast.LENGTH_SHORT).show();
                }
                break;
            case TYPE_EXO:
//                mIVideoView = new ExoVideoView(context);
                if (BuildConfig.DEBUG) {
//                    Toast.makeText(context, "ExoVideoView", Toast.LENGTH_SHORT).show();
                }
        }
        LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView((View) mIVideoView, layoutParams);
        mIVideoView.setVideoListener(iVideoListener);
    }


    public int getType() {
        return type;
    }


    public void setVideoURI(Uri uri) {
        if (mIVideoView != null) {
            mIVideoView.setVideoURI(uri);
        }
    }

    public void start() {
        if (mIVideoView != null) {
            mIVideoView.start();
        }
    }

    public void pause() {
        if (mIVideoView != null) {
            mIVideoView.pause();
        }
    }

    public void stopPlayback() {
        if (mIVideoView != null) {
            mIVideoView.stopPlayback();
        }
    }

    public Bitmap getSnapShot() {
        if (mIVideoView != null) {
            return mIVideoView.getSnapShot();
        }
        return null;
    }

}
