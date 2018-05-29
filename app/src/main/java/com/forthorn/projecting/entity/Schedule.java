package com.forthorn.projecting.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by: Forthorn
 * Date: 5/7/2018.
 * Description:
 * 定时开关机任务
 */
public class Schedule {


    /**
     * code : 200
     * msg : 获取成功
     * data : [{"start_time":"19:37","start_day":"1","off_time":"13:14","off_day":"8"}]
     */

    private int code;
    private String msg;
    private List<ScheduleBean> data;

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

    public List<ScheduleBean> getData() {
        return data;
    }

    public void setData(List<ScheduleBean> data) {
        this.data = data;
    }

    public static class ScheduleBean {
        /**
         * start_time : 19:37
         * start_day : 1
         * off_time : 13:14
         * off_day : 8
         */

        @SerializedName("start_time")
        private String startTime;
        @SerializedName("start_day")
        private String startDay;
        @SerializedName("off_time")
        private String offTime;
        @SerializedName("off_day")
        private String offDay;

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getStartDay() {
            return startDay;
        }

        public void setStartDay(String startDay) {
            this.startDay = startDay;
        }

        public String getOffTime() {
            return offTime;
        }

        public void setOffTime(String offTime) {
            this.offTime = offTime;
        }

        public String getOffDay() {
            return offDay;
        }

        public void setOffDay(String offDay) {
            this.offDay = offDay;
        }
    }
}