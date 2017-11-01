package com.forthorn.projecting.entity;

/**
 * Created by: Forthorn
 * Date: 8/19/2017.
 * Description:
 */
public class Users {
    /**
     * username : cai
     * nickname : hello
     * mtime : 2015-01-01 00:00:00
     * ctime : 2015-01-01 00:00:00
     */

    private String username;
    private String nickname;
    private String mtime;
    private String ctime;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }
}