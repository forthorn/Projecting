package com.forthorn.projecting.entity;

/**
 * Created by: Forthorn
 * Date: 8/19/2017.
 * Description:
 */
public class Task {

    private int id;
    private int type;
    private int status;
    private int hour;
    private int duration;
    private int date;
    private String content;

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


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", type=" + type +
                ", status=" + status +
                ", hour=" + hour +
                ", duration=" + duration +
                ", date=" + date +
                ", content='" + content + '\'' +
                '}';
    }
}
