package com.example.instaclone.Singleton;

import com.example.instaclone.Model.Post;

import java.util.ArrayList;

public class SingletonPostList {
    private static SingletonPostList instance;
    private ArrayList<Post> postList;
    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ArrayList<Post> getPostList() {
        return postList;
    }

    public void setPostList(ArrayList<Post> postList) {
        this.postList = postList;
    }

    private SingletonPostList(){
        this.postList = new ArrayList<>();
        this.position = 0;
    };
    public static SingletonPostList getInstance(){
        if(instance==null){
            instance = new SingletonPostList();
        }
        return instance;
    }
}
