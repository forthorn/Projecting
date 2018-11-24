package com.forthorn.projecting.video.exo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.forthorn.projecting.util.LogUtils;
import com.forthorn.projecting.video.IVideoListener;
import com.forthorn.projecting.video.IVideoView;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import java.io.IOException;

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
        mPlayer.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int i, int i1, int i2, float v) {

            }

            @Override
            public void onRenderedFirstFrame() {

            }
        });
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object o, int i) {
                LogUtils.e(TAG, "onTimelineChanged:" + i + ", Timeline:" + timeline.getPeriodCount());
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroupArray, TrackSelectionArray trackSelectionArray) {

            }

            @Override
            public void onLoadingChanged(boolean b) {
                LogUtils.e(TAG, "onLoadingChanged:" + b);

            }

            @Override
            public void onPlayerStateChanged(boolean b, int i) {
                LogUtils.e(TAG, "onPlayerStateChanged:" + b + ", " + i);
            }

            @Override
            public void onRepeatModeChanged(int i) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean b) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException e) {
                LogUtils.e(TAG, "onPlayerError:" + e.getMessage());
            }

            @Override
            public void onPositionDiscontinuity(int i) {
                LogUtils.e(TAG, "onPositionDiscontinuity:" + i);
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });

        mPlayer.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onPlayerStateChanged(EventTime eventTime, boolean b, int i) {

            }

            @Override
            public void onTimelineChanged(EventTime eventTime, int i) {

            }

            @Override
            public void onPositionDiscontinuity(EventTime eventTime, int i) {

            }

            @Override
            public void onSeekStarted(EventTime eventTime) {

            }

            @Override
            public void onSeekProcessed(EventTime eventTime) {

            }

            @Override
            public void onPlaybackParametersChanged(EventTime eventTime, PlaybackParameters playbackParameters) {

            }

            @Override
            public void onRepeatModeChanged(EventTime eventTime, int i) {

            }

            @Override
            public void onShuffleModeChanged(EventTime eventTime, boolean b) {

            }

            @Override
            public void onLoadingChanged(EventTime eventTime, boolean b) {

            }

            @Override
            public void onPlayerError(EventTime eventTime, ExoPlaybackException e) {

            }

            @Override
            public void onTracksChanged(EventTime eventTime, TrackGroupArray trackGroupArray, TrackSelectionArray trackSelectionArray) {

            }

            @Override
            public void onLoadStarted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {

            }

            @Override
            public void onLoadCompleted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {

            }

            @Override
            public void onLoadCanceled(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {

            }

            @Override
            public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException e, boolean b) {

            }

            @Override
            public void onDownstreamFormatChanged(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

            }

            @Override
            public void onUpstreamDiscarded(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

            }

            @Override
            public void onMediaPeriodCreated(EventTime eventTime) {

            }

            @Override
            public void onMediaPeriodReleased(EventTime eventTime) {

            }

            @Override
            public void onReadingStarted(EventTime eventTime) {

            }

            @Override
            public void onBandwidthEstimate(EventTime eventTime, int i, long l, long l1) {

            }

            @Override
            public void onViewportSizeChange(EventTime eventTime, int i, int i1) {

            }

            @Override
            public void onNetworkTypeChanged(EventTime eventTime, @Nullable NetworkInfo networkInfo) {

            }

            @Override
            public void onMetadata(EventTime eventTime, Metadata metadata) {

            }

            @Override
            public void onDecoderEnabled(EventTime eventTime, int i, DecoderCounters decoderCounters) {

            }

            @Override
            public void onDecoderInitialized(EventTime eventTime, int i, String s, long l) {

            }

            @Override
            public void onDecoderInputFormatChanged(EventTime eventTime, int i, Format format) {

            }

            @Override
            public void onDecoderDisabled(EventTime eventTime, int i, DecoderCounters decoderCounters) {

            }

            @Override
            public void onAudioSessionId(EventTime eventTime, int i) {

            }

            @Override
            public void onAudioUnderrun(EventTime eventTime, int i, long l, long l1) {

            }

            @Override
            public void onDroppedVideoFrames(EventTime eventTime, int i, long l) {

            }

            @Override
            public void onVideoSizeChanged(EventTime eventTime, int i, int i1, int i2, float v) {

            }

            @Override
            public void onRenderedFirstFrame(EventTime eventTime, Surface surface) {

            }

            @Override
            public void onDrmKeysLoaded(EventTime eventTime) {

            }

            @Override
            public void onDrmSessionManagerError(EventTime eventTime, Exception e) {

            }

            @Override
            public void onDrmKeysRestored(EventTime eventTime) {

            }

            @Override
            public void onDrmKeysRemoved(EventTime eventTime) {

            }
        });
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
        Toast.makeText(mContext, "开始播放：" + uri.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public Bitmap getSnapShot() {
        // TODO: 2018/11/22  截屏
        Bitmap bitmap = Bitmap.createBitmap(mSurfaceView.getWidth(), mSurfaceView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mSurfaceView.draw(canvas);
        return bitmap;
    }

    @Override
    public void setVideoListener(IVideoListener iVideoListener) {
        mVideoListener = iVideoListener;
    }
}
