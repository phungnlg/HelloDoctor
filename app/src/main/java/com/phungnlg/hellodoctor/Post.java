package com.phungnlg.hellodoctor;

/**
 * Created by Phil on 07/05/2017.
 */

public class Post {
    private int vote;
    private int answer;
    private String uid;
    private String body;
    private String title;
    private String username;
    private String tag;
    private String time;
    private String photoUrl;

    public Post() {
    }

    public Post(String _uid, String _tittle, String _body, int _vote, int _answer, String _username, String _tag,
                String _time) {
        this.uid = _uid;
        this.title = _tittle;
        this.body = _body;
        this.vote = _vote;
        this.answer = _answer;
        this.username = _username;
        this.tag = _tag;
        this.time = _time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTittle() {
        return title;
    }

    public void setTittle(String tittle) {
        this.title = tittle;
    }
}
