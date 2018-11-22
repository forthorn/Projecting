package com.forthorn.projecting.video.exo;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.forthorn.projecting.util.LogUtils;
import com.forthorn.projecting.video.IVideoListener;
import com.forthorn.projecting.video.IVideoView;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

public class ExoVideoView extends FrameLayout implements IVideoView {

    private String TAG = "ExoVideoView";

    IVideoListener mVideoListener;
    private SurfaceView mSurfaceView;
    private SimpleExoPlayer mPlayer;
    private MediaSource mMediaSource;
    private Context mContext;

    private DataSource.Factory dataSourceFactory;
    private BandwidthMeter bandwidthMeter;
    private ExtractorsFactory extractorsFactory;
    private LoopingMediaSource loopingSource;

    public ExoVideoView(@NonNull Context context) {
        this(context, null);
    }

    public ExoVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        mSurfaceView = new SurfaceView(context);
        LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mSurfaceView, layoutParams);
        // step1. 创建一个默认的TrackSelector
        Handler mainHandler = new Handler();
        // 创建带宽
        bandwidthMeter = new DefaultBandwidthMeter();
        // 创建轨道选择工厂
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        // 创建轨道选择器实例
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        //step2. 创建播放器
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        // 创建加载数据的工厂
        dataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, "Projecting"),
                (TransferListener<? super DataSource>) bandwidthMeter);
        // 创建解析数据的工厂
        extractorsFactory = new DefaultExtractorsFactory();
        mPlayer.setVideoSurfaceView(mSurfaceView);
        mPlayer.setPlayWhenReady(true);
    }


    @Override
    public void start() {
        if (mPlayer.isLoading()) {
            return;
        } else {
            mPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void pause() {
        mPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stopPlayback() {
        mPlayer.stop();
    }

    @Override
    public void setVideoURI(Uri uri) {
        if (uri == null) {
            LogUtils.e(TAG, "URI is null");
            return;
        }
        // 传入Uri、加载数据的工厂、解析数据的工厂，就能创建出MediaSource
        mMediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        loopingSource = new LoopingMediaSource(mMediaSource);
        mPlayer.prepare(loopingSource);
        mPlayer.setPlayWhenReady(true);
    }

    @Override
    public Bitmap getSnapShot() {
        // TODO: 2018/11/22  截屏
        return null;
    }

    @Override
    public void setVideoListener(IVideoListener iVideoListener) {
        mVideoListener = iVideoListener;
    }
}
