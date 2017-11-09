package com.forthorn.projecting.player;

import android.view.SurfaceHolder;
import android.view.View;

/**
 * Created by: Forthorn
 * Date: 11/9/2017.
 * Description:
 */

public interface IMDisplay extends IMPlayListener {
    View getDisplayView();

    SurfaceHolder getHolder();

}