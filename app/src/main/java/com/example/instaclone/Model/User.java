package com.example.instaclone.Model;

public class User {
    private String bio,email,id,imageurl,fullname,username;

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getFullname() {
        return fullname;
    }

    public void setName(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User() {
    }

    public User(String bio, String email, String id, String imageurl, String fullname, String username) {
        this.bio = bio;
        this.email = email;
        this.id = id;
        this.imageurl = imageurl;
        this.fullname = fullname;
        this.username = username;
    }
}
