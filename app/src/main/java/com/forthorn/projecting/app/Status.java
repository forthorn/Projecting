package com.forthorn.projecting.app;

/**
 * Created by: Forthorn
 * Date: 10/28/2017.
 * Description:
 */

public enum Status {
    VIDEO(0x1),
    PICTURE(0X2),
    IDLE(0X3),
    TEXT(0X4),
    VIDEO_TEXT(0X5),
    PICTURE_TEXT(0X6),
    IDLE_TEXT(0X7);

    final int code;

    Status(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }


}
