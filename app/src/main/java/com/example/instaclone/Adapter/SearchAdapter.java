package com.example.instaclone.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instaclone.Fragments.ProfileFragment;
import com.example.instaclone.Model.Notification;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<User> mUsers;
    private  Context mContext;
    private boolean isFragment;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    public SearchAdapter(List<User> mUsers, Context mContext, boolean isFragment) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.isFragment = isFragment;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = mUsers.get(position);

        holder.userButton.setVisibility(View.VISIBLE);
        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getFullname());
        Picasso.get().load(user.getImageurl()).placeholder(R.mipmap.ic_launcher).into(holder.imageProfile);

        isFollowed(user.getId(),holder.userButton);

        if(user.getId().equals(firebaseUser.getUid())){
            holder.userButton.setVisibility(View.GONE);
        }

        holder.userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.userButton.getText().toString().equals("follow")){

                    Notification notification = new Notification(firebaseUser.getUid(),"Started following you.","null");
                    String pushId = databaseReference.child("Notifications").child(user.getId()).push().getKey();
                    databaseReference.child("Notifications").child(user.getId()).child(pushId).setValue(notification);

                    databaseReference.child("Follow").child(firebaseUser.getUid()).child("following").child(user.getId()).setValue(pushId);

                    databaseReference.child("Follow").child(user.getId()).child("followers").child(firebaseUser.getUid()).setValue(pushId);

                } else{

                    databaseReference.child("Follow").child(firebaseUser.getUid()).child("following")
                            .child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String pushId = snapshot.getValue(String.class);
                            databaseReference.child("Follow").child(firebaseUser.getUid()).child("following").child(user.getId()).removeValue();

                            databaseReference.child("Follow").child(user.getId()).child("followers").child(firebaseUser.getUid()).removeValue();

                            databaseReference.child("Notifications").child(user.getId()).child(pushId).removeValue();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    databaseReference.child("Follow")
                            .child(firebaseUser.getUid()).child("following")
                            .child(user.getId()).removeValue();

                    databaseReference.child("Follow")
                            .child(user.getId()).child("followers")
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });
        holder.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId",user.getId()).apply();
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new ProfileFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.getSharedPreferences("PROFILE",Context.MODE_PRIVATE).edit().putString("profileId",user.getId()).apply();
                FragmentTransaction fragmentTransaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container,new ProfileFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    private void isFollowed(final String id,final Button btnFollow) {
        DatabaseReference reference = databaseReference.child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(id).exists()){
                    btnFollow.setText("following");
                } else{
                    btnFollow.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView imageProfile;
        TextView username,fullname;
        Button userButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            userButton = itemView.findViewById(R.id.user_btn);
        }
    }
}
