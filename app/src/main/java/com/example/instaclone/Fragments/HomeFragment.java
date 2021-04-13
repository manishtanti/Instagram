package com.example.instaclone.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.instaclone.Adapter.PostAdapter;
import com.example.instaclone.LoginActivity;
import com.example.instaclone.Model.Post;
import com.example.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewPost;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private ImageView logout;

    private List<String> followingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewPost = view.findViewById(R.id.recycler_view_posts);
        recyclerViewPost.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPost.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),postList);
        recyclerViewPost.setAdapter(postAdapter);

        followingList = new ArrayList<>();

        getFollowingUsers();


        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });


        return view;
    }

    private void getFollowingUsers() {

        FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followingList.clear();
                        for(DataSnapshot val : snapshot.getChildren()){
                            followingList.add(val.getKey());
                        }
                        followingList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        readPosts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void readPosts() {
        System.out.println(System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot val : snapshot.getChildren()){
                    Post post = val.getValue(Post.class);

                    for(String id : followingList){
                        if(post.getPublisher().equals(id)){
                            postList.add(post);
                        }
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}