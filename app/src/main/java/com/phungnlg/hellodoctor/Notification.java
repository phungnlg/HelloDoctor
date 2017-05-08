package com.phungnlg.hellodoctor;

/**
 * Created by Phil on 07/05/2017.
 */

public class Notification {
    private String time, notification;
    private Boolean isReaded;

    public Notification(){}

    public Notification(String _time, String _body, Boolean _isReaded){
        this.time = _time;
        this.notification = _body;
        this.isReaded = _isReaded;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

//    public String getIsReaded() {
//        return isReaded;
//    }
//
//    public void setIsReaded(String isReaded) {
//        this.isReaded = isReaded;
//    }

    public Boolean getReaded() {
        return isReaded;
    }

    public void setReaded(Boolean readed) {
        isReaded = readed;
    }
}
