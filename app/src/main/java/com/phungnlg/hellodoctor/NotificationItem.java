package com.phungnlg.hellodoctor;

/**
 * Created by Phil on 07/05/2017.
 */

public class NotificationItem {
    private String time;
    private String notification;
    private Boolean isReaded;

    public NotificationItem() {
    }

    public NotificationItem(String _time, String _body, Boolean _isReaded) {
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

    public Boolean getReaded() {
        return isReaded;
    }

    public void setReaded(Boolean readed) {
        isReaded = readed;
    }
}
