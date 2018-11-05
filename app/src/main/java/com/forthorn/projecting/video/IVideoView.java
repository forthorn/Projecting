package com.forthorn.projecting.video;

import android.graphics.Bitmap;
import android.net.Uri;

public interface IVideoView {


    void start();

    void pause();

    void stopPlayback();

    void setVideoURI(Uri uri);

    Bitmap getSnapShot();

    void setVideoListener(IVideoListener iVideoListener);

}
