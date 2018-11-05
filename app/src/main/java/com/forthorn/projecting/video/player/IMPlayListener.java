package com.forthorn.projecting.video.player;

/**
 * Created by: Forthorn
 * Date: 11/9/2017.
 * Description:
 */

public interface IMPlayListener {
    void onStart(IMPlayer player);
    void onPause(IMPlayer player);
    void onResume(IMPlayer player);
    void onComplete(IMPlayer player);
}
