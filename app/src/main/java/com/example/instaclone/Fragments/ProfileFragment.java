package com.example.instaclone.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.instaclone.Adapter.PhotoAdapter;

import com.example.instaclone.EditProfileActivity;
import com.example.instaclone.Model.Notification;
import com.example.instaclone.Model.Post;
import com.example.instaclone.Model.User;
import com.example.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerViewSaves;
    private PhotoAdapter postAdapterSaves;
    private ArrayList<Post> mySavedPosts;

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private ArrayList<Post> myPhotoList;

    private CircleImageView imageProfile;
    private ImageView options,myPictures,savedPictures;
    private Button editProfile;

    private TextView posts,followers,following,username,fullname,bio;
    private LinearLayout followersLayout,followingLayout;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    String profileId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        String data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId",null);

        if(data==null){
            profileId = firebaseUser.getUid();
        } else{
            profileId = data;
        }




        imageProfile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        myPictures = view.findViewById(R.id.my_pictures);
        savedPictures = view.findViewById(R.id.saved_pictures);

        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        username = view.findViewById(R.id.username);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        editProfile = view.findViewById(R.id.edit_profile);
        followersLayout = view.findViewById(R.id.followers_layout);
        followingLayout = view.findViewById(R.id.following_layout);

        recyclerView = view.findViewById(R.id.recycler_view_pictures);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        myPhotoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(),myPhotoList);
        recyclerView.setAdapter(photoAdapter);

        recyclerViewSaves = view.findViewById(R.id.recycler_view_saved);
        recyclerViewSaves.setHasFixedSize(true);
        recyclerViewSaves.setLayoutManager(new GridLayoutManager(getContext(),3));
        mySavedPosts = new ArrayList<>();
        postAdapterSaves = new PhotoAdapter(getContext(),mySavedPosts);
        recyclerViewSaves.setAdapter(postAdapterSaves);



        
        userInfo();
        getFollwersAndFollowingCount();
        getPostCount();
        myPhotos();


        if(profileId.equals(firebaseUser.getUid())){
            editProfile.setText("Edit Profile");
            getSavedPosts();
        } else{
            checkFollowingStatus();
            savedPictures.setVisibility(View.GONE);
        }


        followersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().getSharedPreferences("USERLIST",Context.MODE_PRIVATE).edit().putString("type","followers").putString("id",profileId).apply();
                FragmentTransaction fragmentTransaction = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new UserFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        followingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().getSharedPreferences("USERLIST",Context.MODE_PRIVATE).edit().putString("type","following").putString("id",profileId).apply();
                FragmentTransaction fragmentTransaction = ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new UserFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnTxt = editProfile.getText().toString();
                if(btnTxt.equals("Edit Profile")){
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else{
                    if(btnTxt.equals("Follow")){
                        String pushId = databaseReference.child("Notifications").child(profileId)
                                .push().getKey();
                        databaseReference.child("Notifications").child(profileId).child(pushId).setValue(new Notification(firebaseUser.getUid(),"Started following you.","null"));
                        databaseReference.child("Follow").child(firebaseUser.getUid()).child("following")
                                .child(profileId).setValue(pushId);


                        databaseReference.child("Follow").child(profileId).child("followers")
                                .child(firebaseUser.getUid()).setValue(pushId);
                        editProfile.setText("Following");


                    } else{
                        databaseReference.child("Follow").child(firebaseUser.getUid()).child("following")
                                .child(profileId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String pushId = snapshot.getValue(String.class);
                                editProfile.setText("Follow");

                                databaseReference.child("Follow").child(firebaseUser.getUid()).child("following")
                                        .child(profileId).removeValue();

                                databaseReference.child("Follow").child(profileId).child("followers")
                                        .child(firebaseUser.getUid()).removeValue();


                                //Remove notification
                                databaseReference.child("Notifications").child(profileId).child(pushId).removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        databaseReference.child("Follow").child(profileId).child("followers")
                                .child(firebaseUser.getUid()).removeValue();
                        editProfile.setText("Follow");
                    }
                }
            }
        });
        recyclerView.setVisibility(View.VISIBLE);
        recyclerViewSaves.setVisibility(View.GONE);

        myPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerViewSaves.setVisibility(View.GONE);
            }
        });

        savedPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerViewSaves.setVisibility(View.VISIBLE);
            }
        });



        return view;
    }

    private void getSavedPosts() {
        List<String> savedIds = new ArrayList<>();
        DatabaseReference ref = databaseReference.child("Posts");
        databaseReference.child("Saves").child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot val : snapshot.getChildren()){
                    savedIds.add(val.getKey());
                }
                postAdapterSaves.notifyDataSetChanged();

                for(String ids : savedIds){
                    ref.child(ids).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Post post = snapshot.getValue(Post.class);
                                mySavedPosts.add(post);
                            } else{
                                databaseReference.child("Saves").child(firebaseUser.getUid()).child(ids).removeValue();
                                
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myPhotos() {
        List<String> myPostsIds = new ArrayList<>();
        DatabaseReference ref = databaseReference.child("Posts");
        databaseReference.child("Userposts").child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot val : snapshot.getChildren()){
                    myPostsIds.add(val.getKey());
                }

                for(String ids : myPostsIds){
                    ref.child(ids).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Post post = snapshot.getValue(Post.class);
                                myPhotoList.add(post);
                            } else{
                                databaseReference.child("Userposts").child(profileId).child(ids).removeValue();
                            }
                            photoAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollowingStatus(){
        databaseReference.child("Follow").child(firebaseUser.getUid()).child("following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(profileId).exists()){
                            editProfile.setText("Following");
                        } else{
                            editProfile.setText("Follow");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getPostCount() {
        databaseReference.child("Userposts").child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.setText(String.valueOf(snapshot.getChildrenCount()+""));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollwersAndFollowingCount() {
        DatabaseReference ref = databaseReference.child("Follow").child(profileId);
        ref.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText(snapshot.getChildrenCount()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userInfo() {
        databaseReference.child("Users").child(profileId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        username.setText(user.getUsername());
                        fullname.setText(user.getFullname());
                        bio.setText(user.getBio());
                        if(user.getImageurl().equals("default")){
                            imageProfile.setImageResource(R.drawable.ic_person);
                        } else{
                            Picasso.get().load(user.getImageurl()).into(imageProfile);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}