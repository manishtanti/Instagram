package com.example.instaclone.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instaclone.Fragments.PostDetailFragment;
import com.example.instaclone.Model.Post;
import com.example.instaclone.R;
import com.example.instaclone.Singleton.SingletonPostList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {
    private Context mContext;
    private List<String> mTags,mTagsCount;

    public TagAdapter(Context mContext, List<String> mTags, List<String> mTagsCount) {
        this.mContext = mContext;
        this.mTags = mTags;
        this.mTagsCount = mTagsCount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.tag_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tag.setText("# "+mTags.get(position));
        holder.noOfPosts.setText(mTagsCount.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPosts();
            }

            private void getPosts() {
                    ArrayList<Post> hashtagPosts = new ArrayList<>();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    List<String> myPostsIds = new ArrayList<>();
                    DatabaseReference ref = databaseReference.child("Posts");
                    databaseReference.child("HashTags").child(mTags.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot val : snapshot.getChildren()){
                                myPostsIds.add(val.getKey());
                            }

                            for(String ids : myPostsIds){
                                ref.child(ids).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            Post post = snapshot.getValue(Post.class);
                                            hashtagPosts.add(post);
                                        }
                                        notifyDataSetChanged();
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                            PostDetailFragment postDetailFragment = new PostDetailFragment();
                            postDetailFragment.setPostList(hashtagPosts,0);
                            fragmentTransaction.add(R.id.fragment_container,postDetailFragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

        });
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tag,noOfPosts;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.hash_tag);
            noOfPosts = itemView.findViewById(R.id.no_of_posts);

        }
    }

    public void filter(List<String> filterTags,List<String> filterTagsCount){
        this.mTags = filterTags;
        this.mTagsCount = filterTagsCount;
        notifyDataSetChanged();
    }
}
