package com.example.instaclone.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instaclone.Fragments.PostDetailFragment;
import com.example.instaclone.Fragments.ProfileFragment;
import com.example.instaclone.Fragments.UserFragment;
import com.example.instaclone.Model.Notification;
import com.example.instaclone.Model.Post;
import com.example.instaclone.Model.User;
import com.example.instaclone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    private Context mContext;
    private List<Notification> mNotification;

    public NotificationAdapter(Context mContext, List<Notification> mNotification) {
        this.mContext = mContext;
        this.mNotification = mNotification;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.notification_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = mNotification.get(position);

        getUser(holder.imageProfile,holder.username,notification.getUserid());

        if(!notification.getPostid().equals("null")){
            holder.postImage.setVisibility(View.VISIBLE);
            getPostImage(holder.postImage,notification.getPostid());
        } else{
            holder.postImage.setVisibility(View.GONE);
        }

        holder.comment.setText(notification.getText());


        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId",notification.getUserid()).apply();

                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new ProfileFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId",notification.getUserid()).apply();

                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new ProfileFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPost(notification.getPostid());


            }
        });

    }

    private void goToPost(String postid){
        FirebaseDatabase.getInstance().getReference().child("Posts").child(postid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                PostDetailFragment postDetailFragment = new PostDetailFragment();
                ArrayList<Post> singlePost = new ArrayList<>();
                singlePost.add(post);
                postDetailFragment.setPostList(singlePost,0);
                fragmentTransaction.add(R.id.fragment_container,postDetailFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getPostImage(ImageView postImage, String postid) {
        FirebaseDatabase.getInstance().getReference().child("Posts").child(postid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                Picasso.get().load(post.getImageurl()).into(postImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUser(CircleImageView imageProfile, TextView username, String userId) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if(user.getImageurl().equals("default")){
                    imageProfile.setImageResource(R.drawable.ic_person);
                } else{
                    Picasso.get().load(user.getImageurl()).into(imageProfile);
                }
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView postImage;
        public CircleImageView imageProfile;
        public TextView username,comment;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            postImage = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }
}
