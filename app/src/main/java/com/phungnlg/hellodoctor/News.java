package com.phungnlg.hellodoctor;

/**
 * Created by Phil on 6/21/2017.
 */

public class News {
    private String title;
    private String photoUrl;
    private String contentUrl;

    public News() {

    }

    public News(String title, String photoUrl, String contentUrl) {
        this.title = title;
        this.photoUrl = photoUrl;
        this.contentUrl = contentUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }
}
