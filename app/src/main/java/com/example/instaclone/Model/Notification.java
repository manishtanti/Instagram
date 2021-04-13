package com.example.instaclone.Model;

public class Notification {
    private String userid,text,postid;

    public Notification() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }



    public Notification(String userid, String text, String postid) {
        this.userid = userid;
        this.text = text;
        this.postid = postid;

    }
}
