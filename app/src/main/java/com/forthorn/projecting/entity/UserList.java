package com.forthorn.projecting.entity;

import java.util.List;

/**
 * Created by: Forthorn
 * Date: 8/19/2017.
 * Description:
 */

public class UserList {

    /**
     * total : 12580
     * start : 1100
     * count : 100
     * users : [{"username":"cai","nickname":"hello","mtime":"2015-01-01 00:00:00","ctime":"2015-01-01 00:00:00"},{"username":"yi","nickname":"hello","mtime":"2015-01-01 00:00:00","ctime":"2015-01-01 00:00:00"},{"username":"huang","nickname":"hello","mtime":"2015-01-01 00:00:00","ctime":"2015-01-01 00:00:00"}]
     */

    private int total;
    private int start;
    private int count;
    private List<Users> users;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Users> getUsers() {
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }
}
