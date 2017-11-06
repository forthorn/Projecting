package com.forthorn.projecting.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by: Forthorn
 * Date: 11/6/2017.
 * Description:
 */

public class TaskRes {

    /**
     * code : 200
     * msg : 请求成功
     * data : [{"id":22,"date":1508774400,"duration":90,"type":5,"equip_id":1,"volume":null,"status":0,"content":"/images/attachment/20171023/15087387314335.jpg","create_time":null,"last_modify":null,"hour":2,"start_time":1508774400,"finish_time":1508774490},{"id":23,"date":1508774400,"duration":90,"type":5,"equip_id":1,"volume":null,"status":0,"content":"/images/attachment/20171023/15087387314335.jpg","create_time":null,"last_modify":null,"hour":3,"start_time":1508774490,"finish_time":1508774580},{"id":28,"date":1508774400,"duration":90,"type":4,"equip_id":2,"volume":null,"status":0,"content":"/images/attachment/20171023/15087387314335.jpg","create_time":null,"last_modify":null,"hour":2,"start_time":1508774400,"finish_time":1508774490},{"id":29,"date":1508774400,"duration":90,"type":4,"equip_id":2,"volume":null,"status":0,"content":"/images/attachment/20171023/15087387314335.jpg","create_time":null,"last_modify":null,"hour":3,"start_time":1508774490,"finish_time":1508774580}]
     */

    private int code;
    private String msg;
    @SerializedName("data")
    private List<Task> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Task> getData() {
        return data;
    }

    public void setData(List<Task> data) {
        this.data = data;
    }

}
