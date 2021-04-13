package com.example.instaclone.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instaclone.Adapter.CommentAdapter;
import com.example.instaclone.Model.Comment;
import com.example.instaclone.Model.Notification;
import com.example.instaclone.Model.Post;
import com.example.instaclone.Model.User;
import com.example.instaclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class CommentFragment extends Fragment {

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private EditText addComment;
    private CircleImageView imageProfile;
    private TextView postComment;

    private Post post;
    FirebaseUser fUser;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        post = (Post)getArguments().getSerializable("post");


        addComment = view.findViewById(R.id.add_comment);
        imageProfile = view.findViewById(R.id.image_profile);
        postComment = view.findViewById(R.id.post);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentList = new ArrayList<>();
        getComment();
        commentAdapter = new CommentAdapter(getContext(),commentList,post);
        recyclerView.setAdapter(commentAdapter);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        getUserImage();

        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(addComment.getText().toString())){
                    Toast.makeText(getContext(), "Comment can't be empty", Toast.LENGTH_SHORT).show();
                } else{
                    putComment();
                }
            }
        });

        return view;
    }
    private void getComment() {
        FirebaseDatabase.getInstance().getReference().child("Comments").child(post.getPostid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentList.clear();
                        for(DataSnapshot val : snapshot.getChildren()){
                            Comment comment = val.getValue(Comment.class);
                            commentList.add(comment);
                        }
                        commentAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void putComment() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments").child(post.getPostid());
        String pushId = ref.push().getKey();
        Comment comment = new Comment(addComment.getText().toString(),fUser.getUid(),pushId);


        ref.child(pushId).setValue(comment)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            addNotification(pushId);
                            Toast.makeText(getContext(), "Comment added", Toast.LENGTH_SHORT).show();
                            addComment.setText("");

                        } else{
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addNotification(String pushId) {
        if(post.getPublisher().equals(fUser.getUid())){
            return;
        }
        FirebaseDatabase.getInstance().getReference().child("Notifications").child(post.getPublisher()).child(pushId)
                .setValue(new Notification(fUser.getUid(),"Commented on your post.",post.getPostid()));
    }

    private void getUserImage() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if(user.getImageurl().equals("default")){
                            imageProfile.setImageResource(R.drawable.ic_person);
                        } else {
                            Picasso.get().load(user.getImageurl()).into(imageProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}