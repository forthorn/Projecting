package com.forthorn.projecting.video;

public interface IVideoListener {


    void onVideoSizeChanged();

    void onBufferingUpdate(int percent);

    void onCompletion();

    void onError();
}
