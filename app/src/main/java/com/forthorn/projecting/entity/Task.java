package com.forthorn.projecting.entity;

/**
 * Created by: Forthorn
 * Date: 11/4/2017.
 * Description:
 */

public class Task {


    /**
     * status : 2
     * id : 9
     * type : 0
     * last_modify : 1509761631
     * create_time : 1509761631
     * equip_id : 1
     * extras : {}
     */

    private int status;
    private int id;
    private int type;
    private int last_modify;
    private int create_time;
    private int equip_id;
    private int hour;
    private int duration;
    private int date;
    private String content;
    private int runningStatus;

    private Extras extras;

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * volume : 100
     */

    private int volume;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLast_modify() {
        return last_modify;
    }

    public void setLast_modify(int last_modify) {
        this.last_modify = last_modify;
    }

    public int getCreate_time() {
        return create_time;
    }

    public void setCreate_time(int create_time) {
        this.create_time = create_time;
    }

    public int getEquip_id() {
        return equip_id;
    }

    public void setEquip_id(int equip_id) {
        this.equip_id = equip_id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(int runningStatus) {
        this.runningStatus = runningStatus;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Extras getExtras() {
        return extras;
    }

    public void setExtras(Extras extras) {
        this.extras = extras;
    }

    public static class Extras {

    }

    @Override
    public String toString() {
        return "TaskMessage{" +
                "status=" + status +
                ", id=" + id +
                ", type=" + type +
                ", last_modify=" + last_modify +
                ", create_time=" + create_time +
                ", equip_id=" + equip_id +
                ", hour=" + hour +
                ", duration=" + duration +
                ", date=" + date +
                ", content='" + content + '\'' +
                ", extras=" + extras +
                ", volume=" + volume +
                '}';
    }
}
