package com.example.instaclone.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.instaclone.Adapter.UserAdapter;
import com.example.instaclone.Model.User;
import com.example.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList,userListCopy;
    private EditText searchBar;
    private String type;

    private List<String> followerList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        userListCopy = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(),userList);
        recyclerView.setAdapter(userAdapter);


        type = getContext().getSharedPreferences("USERLIST", Context.MODE_PRIVATE).getString("type",null);

        getUsersIds(type);

        searchBar = view.findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;
    }

    private void getUsersIds(String type) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        if(type.equals("share")) {
            ref.child("Follow").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("following")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            followerList.clear();
                            for (DataSnapshot val : snapshot.getChildren()) {
                                followerList.add(val.getKey());
                            }
                            getUsersDetails();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        else if(type.equals("following")){
            String profileId  = getContext().getSharedPreferences("USERLIST", Context.MODE_PRIVATE).getString("id",null);
            ref.child("Follow").child(profileId).child("following")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            followerList.clear();
                            for (DataSnapshot val : snapshot.getChildren()) {
                                followerList.add(val.getKey());
                            }
                            getUsersDetails();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
        else if(type.equals("followers")){
            String profileId = getContext().getSharedPreferences("USERLIST", Context.MODE_PRIVATE).getString("id",null);
            ref.child("Follow").child(profileId).child("followers")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            followerList.clear();
                            for (DataSnapshot val : snapshot.getChildren()) {
                                followerList.add(val.getKey());
                            }
                            getUsersDetails();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else if(type.equals("like")){
            String postId = getContext().getSharedPreferences("USERLIST", Context.MODE_PRIVATE).getString("id",null);
            ref.child("Likes").child(postId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            followerList.clear();
                            for (DataSnapshot val : snapshot.getChildren()) {
                                followerList.add(val.getKey());
                            }
                            getUsersDetails();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void getUsersDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        userList.clear();

        for(String follower : followerList){
            ref.child(follower).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    userList.add(user);
                    userListCopy.add(user);
                    userAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void searchUsers(String s){
        userList.clear();
        for(User user : userListCopy){
            if(user.getUsername().contains(s)){
                userList.add(user);
            }
        }
        userAdapter.notifyDataSetChanged();
    }
}