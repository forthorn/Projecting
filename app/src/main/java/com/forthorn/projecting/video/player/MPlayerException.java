package com.forthorn.projecting.video.player;

/**
 * Created by: Forthorn
 * Date: 11/9/2017.
 * Description:
 */

public class MPlayerException extends Exception {

    public MPlayerException(String detailMessage) {
        super(detailMessage);
    }

    public MPlayerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MPlayerException(Throwable throwable) {
        super(throwable);
    }
}