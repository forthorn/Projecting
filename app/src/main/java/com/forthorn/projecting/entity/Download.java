package com.forthorn.projecting.entity;

/**
 * Created by: Forthorn
 * Date: 11/1/2017.
 * Description:
 */

public class Download {

    private int id;
    private int taskId;
    private int status;
    private String url;
    private String path;
    private int time;
    private int fileSize;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Download{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", status=" + status +
                ", url='" + url + '\'' +
                ", path='" + path + '\'' +
                ", time=" + time +
                ", fileSize=" + fileSize +
                '}';
    }
}
