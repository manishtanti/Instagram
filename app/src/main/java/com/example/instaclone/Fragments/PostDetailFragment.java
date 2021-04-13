package com.example.instaclone.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instaclone.Adapter.PostAdapter;
import com.example.instaclone.Model.Post;
import com.example.instaclone.R;
import com.example.instaclone.Singleton.SingletonPostList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PostDetailFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ArrayList<Post> postList;
    private int position;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(getContext(),postList);
        recyclerView.setAdapter(postAdapter);
        recyclerView.scrollToPosition(position);

        return view;
    }

    public void setPostList(ArrayList<Post> postList,int position){
        this.postList = postList;
        this.position = position;
    }
}