package com.phungnlg.hellodoctor;

import android.support.v4.app.Fragment;

/**
 * Created by Phil on 07/05/2017.
 */

public class Comment {
    private String name, uid, comment, time;

    public Comment(){}

    public Comment(String _name, String _uid, String _comment){
        this.comment = _comment;
        this.name = _name;
        this.uid = _uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
