package com.example.instaclone.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instaclone.Fragments.PostDetailFragment;
import com.example.instaclone.Fragments.ProfileFragment;
import com.example.instaclone.Model.Post;
import com.example.instaclone.R;
import com.example.instaclone.Singleton.SingletonPostList;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Post> mPosts;

    public PhotoAdapter(Context mContext, ArrayList<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.photo_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);
        Picasso.get().load(post.getImageurl()).placeholder(R.drawable.ic_person).into(holder.postImage);
        holder.postImage.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                PostDetailFragment postDetailFragment = new PostDetailFragment();
                postDetailFragment.setPostList(mPosts,position);
                fragmentTransaction.add(R.id.fragment_container,postDetailFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView postImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_image);
        }
    }
}
