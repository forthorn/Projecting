package com.forthorn.projecting.baserx;

import java.io.Serializable;

/**
 * Created by: Forthorn
 * Date: 7/17/2017.
 * Description:
 */

public class BaseResponse<T> implements Serializable {
    public String msg;
    public String code;
    public T data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean success() {
        return "200".equals(code);
    }
}
