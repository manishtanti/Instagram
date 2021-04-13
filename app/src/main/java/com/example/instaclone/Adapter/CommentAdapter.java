package com.example.instaclone.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instaclone.Fragments.ProfileFragment;
import com.example.instaclone.MainActivity;
import com.example.instaclone.Model.Comment;
import com.example.instaclone.Model.Post;
import com.example.instaclone.Model.User;
import com.example.instaclone.R;
import com.example.instaclone.StartActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class    CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context mContext;
    private List<Comment> mComments;
    private Post post;

    private FirebaseUser fUser;

    public CommentAdapter(Context mContext, List<Comment> mComments,Post post) {
        this.mContext = mContext;
        this.mComments = mComments;
        this.post = post;
        fUser = FirebaseAuth.getInstance().getCurrentUser();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.comment_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = mComments.get(position);
        holder.comment.setText(comment.getComment());

        FirebaseDatabase.getInstance().getReference().child("Users").child(comment.getPublisher())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if(user.getImageurl().equals("default")){
                            holder.imageProfile.setImageResource(R.drawable.ic_person);
                        } else{
                            Picasso.get().load(user.getImageurl()).into(holder.imageProfile);
                        }

                        holder.username.setText(user.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId",comment.getPublisher()).apply();
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new ProfileFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId",comment.getPublisher()).apply();
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new ProfileFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(comment.getPublisher().equals(fUser.getUid())){
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("Do you want to delete?");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String notificationId = FirebaseDatabase.getInstance().getReference().child("Comments").child(post.getPostid()).child(comment.getId()).getKey();
                            FirebaseDatabase.getInstance().getReference().child("Comments").child(post.getPostid()).child(comment.getId()).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                FirebaseDatabase.getInstance().getReference().child("Notifications").child(post.getPublisher())
                                                        .child(notificationId).removeValue();
                                                Toast.makeText(mContext, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                        }
                    });
                    alertDialog.show();
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView imageProfile;
        public TextView username,comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }
}
