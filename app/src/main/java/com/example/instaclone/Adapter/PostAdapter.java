package com.example.instaclone.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instaclone.Fragments.CommentFragment;
import com.example.instaclone.Fragments.ProfileFragment;
import com.example.instaclone.Fragments.UserFragment;
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
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.squareup.picasso.Picasso;

import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context mContext;
    private List<Post> mPosts;

    private FirebaseUser firebaseUser;

    private DatabaseReference databaseReference;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.post_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);
        Picasso.get().load(post.getImageurl()).into(holder.postImage);
        holder.postImage.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.description.setText(post.getDescription());

        databaseReference.child("Users").child(post.getPublisher()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if(user.getImageurl().equals("default")){
                    holder.imageProfile.setImageResource(R.drawable.ic_person);
                } else{
                    Picasso.get().load(user.getImageurl()).into(holder.imageProfile);
                }
                holder.username.setText(user.getUsername());
                holder.author.setText(user.getFullname());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        isLiked(post.getPostid(),holder.like);
        noOfLikes(post.getPostid(),holder.noOfLikes);
        noOfComments(post.getPostid(),holder.noOfComments);
        isSaved(post.getPostid(),holder.save);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(holder.like.getTag().equals("like")){
                    String notificationKeyId = addNotification(post.getPostid(), post.getPublisher());
                    databaseReference.child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).setValue(notificationKeyId);



                } else{
                    databaseReference.child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String id = snapshot.getValue(String.class);
                            databaseReference.child("Notifications").child(post.getPublisher()).child(id).removeValue();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    databaseReference.child("Likes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();

                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentFragment commentFragment = new CommentFragment();
                Bundle args = new Bundle();
                args.putSerializable("post",post);
                commentFragment.setArguments(args);
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,commentFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("USERLIST",Context.MODE_PRIVATE).edit().putString("type","share").putString("id",post.getPostid()).apply();
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new UserFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        holder.noOfLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("USERLIST",Context.MODE_PRIVATE).edit().putString("type","like").putString("id",post.getPostid()).apply();
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new UserFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        holder.noOfComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentFragment commentFragment = new CommentFragment();
                Bundle args = new Bundle();
                args.putString("postId",post.getPostid());
                args.putString("authorId",post.getPublisher());
                commentFragment.setArguments(args);
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,commentFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId",post.getPublisher()).apply();
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new ProfileFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId",post.getPublisher()).apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
            }
        });

        holder.author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId",post.getPublisher()).apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
            }
        });


        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.save.getTag().equals("save")){
                    databaseReference.child("Saves").child(firebaseUser.getUid()).child(post.getPostid()).setValue(true);
                } else{
                    databaseReference.child("Saves").child(firebaseUser.getUid()).child(post.getPostid()).removeValue();
                }
            }
        });
    }

    private String addNotification(String postid, String publisher) {
        if(firebaseUser.getUid().equals(publisher)){
            return "null";
        }

        Notification notification = new Notification(firebaseUser.getUid(),"Liked your post",postid);


        String notificationKeyId = databaseReference.child("Notifications").child(publisher).push().getKey();
        databaseReference.child("Notifications").child(publisher).child(notificationKeyId).setValue(notification);
        return notificationKeyId;
    }



    private void isSaved(String postid, ImageView imageView) {
        databaseReference.child("Saves").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.ic_save_black);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.ic_save);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageProfile,postImage,like,comment,save,more,share;
        public TextView username,author,noOfLikes,noOfComments;
        SocialTextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.image_profile);
            postImage = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            more = itemView.findViewById(R.id.more);
            share = itemView.findViewById(R.id.share);

            username = itemView.findViewById(R.id.username);
            author = itemView.findViewById(R.id.author);
            noOfLikes = itemView.findViewById(R.id.no_of_likes);
            noOfComments = itemView.findViewById(R.id.no_of_comments);
            description = itemView.findViewById(R.id.description);
        }
    }
    private void isLiked(String postId,ImageView imageView){
        databaseReference.child("Likes").child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(firebaseUser.getUid()).exists()){
                            imageView.setImageResource(R.drawable.ic_liked);
                            imageView.setTag("liked");
                        } else{
                            imageView.setImageResource(R.drawable.ic_like);
                            imageView.setTag("like");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void noOfLikes(String postId,TextView textView){
        databaseReference.child("Likes").child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        textView.setText(snapshot.getChildrenCount()+" likes");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void noOfComments(String postId,TextView textView){
        databaseReference.child("Comments").child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        textView.setText( "View all " +snapshot.getChildrenCount()+" comments");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
