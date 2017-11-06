package com.forthorn.projecting.entity;

import java.util.List;

/**
 * Created by: Forthorn
 * Date: 11/6/2017.
 * Description:
 */

public class TaskList {


    /**
     * code : 200
     * msg : 请求成功
     * data : [{"id":47,"date":1509984000,"duration":30,"type":6,"equip_id":1,"volume":null,"status":0,"content":"http://advert.chindor.com/images/attachment/20171103/15097073544403.mp4","create_time":1509778131,"last_modify":1509778131,"hour":0,"start_time":1509984000,"finish_time":1509984030},{"id":178,"date":1509984000,"duration":300,"type":4,"equip_id":1,"volume":"","status":0,"content":"http://advert.chindor.com/images/image/20171106/15099796023638.png","create_time":1509778143,"last_modify":1509778143,"hour":0,"start_time":1509984030,"finish_time":1509984330},{"id":176,"date":1509984000,"duration":300,"type":6,"equip_id":1,"volume":"","status":0,"content":"http://advert.chindor.com/images/attachment/20171103/15097069333940.mp4","create_time":1509778143,"last_modify":1509778143,"hour":0,"start_time":1509984330,"finish_time":1509984630},{"id":177,"date":1509984000,"duration":300,"type":7,"equip_id":1,"volume":"","status":0,"content":"天气广告测试","create_time":1509778143,"last_modify":1509778143,"hour":0,"start_time":1509984000,"finish_time":1509984300},{"id":174,"date":1509984000,"duration":300,"type":4,"equip_id":1,"volume":"","status":0,"content":"http://advert.chindor.com/images/image/20171106/15099796023638.png","create_time":1509778143,"last_modify":1509778143,"hour":0,"start_time":1509984630,"finish_time":1509984930},{"id":175,"date":1509984000,"duration":300,"type":5,"equip_id":1,"volume":"","status":0,"content":"文字广告内容测试显示","create_time":1509778143,"last_modify":1509778143,"hour":0,"start_time":1509984300,"finish_time":1509984600}]
     */

    private int code;
    private String msg;
    private List<Data> data;

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

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public static class Data {
        /**
         * id : 47
         * date : 1509984000
         * duration : 30
         * type : 6
         * equip_id : 1
         * volume : null
         * status : 0
         * content : http://advert.chindor.com/images/attachment/20171103/15097073544403.mp4
         * create_time : 1509778131
         * last_modify : 1509778131
         * hour : 0
         * start_time : 1509984000
         * finish_time : 1509984030
         */

        private int id;
        private int date;
        private int duration;
        private int type;
        private int equip_id;
        private Object volume;
        private int status;
        private String content;
        private int create_time;
        private int last_modify;
        private int hour;
        private int start_time;
        private int finish_time;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getDate() {
            return date;
        }

        public void setDate(int date) {
            this.date = date;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getEquip_id() {
            return equip_id;
        }

        public void setEquip_id(int equip_id) {
            this.equip_id = equip_id;
        }

        public Object getVolume() {
            return volume;
        }

        public void setVolume(Object volume) {
            this.volume = volume;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getCreate_time() {
            return create_time;
        }

        public void setCreate_time(int create_time) {
            this.create_time = create_time;
        }

        public int getLast_modify() {
            return last_modify;
        }

        public void setLast_modify(int last_modify) {
            this.last_modify = last_modify;
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getStart_time() {
            return start_time;
        }

        public void setStart_time(int start_time) {
            this.start_time = start_time;
        }

        public int getFinish_time() {
            return finish_time;
        }

        public void setFinish_time(int finish_time) {
            this.finish_time = finish_time;
        }
    }
}
