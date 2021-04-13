package com.example.instaclone.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private FirebaseUser firebaseUser;
    private DatabaseReference ref;
    private String id,type;



    public UserAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference();
        getId();
    }

    private void getId() {
        type = mContext.getSharedPreferences("USERLIST",Context.MODE_PRIVATE).getString("type",null);
        id = mContext.getSharedPreferences("USERLIST",Context.MODE_PRIVATE).getString("id",null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        if(user.getId().equals(firebaseUser.getUid())){
            holder.userButton.setVisibility(View.GONE);
        }
        if(type.equals("share")){
            holder.userButton.setText("send");
        } else {
            ref.child("Follow").child(firebaseUser.getUid()).child("following").child(user.getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        holder.userButton.setText("following");
                    } else {
                        holder.userButton.setText("follow");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getFullname());
        Picasso.get().load(user.getImageurl()).placeholder(R.drawable.ic_person).into(holder.imageProfile);


        holder.userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonTxt = holder.userButton.getText().toString();
                if(buttonTxt.equals("send")) {
                    String pushKey = ref.child("Notifications").child(user.getId()).push().getKey();
                    ref.child("Notifications").child(user.getId()).child(pushKey)
                            .setValue(new Notification(firebaseUser.getUid(), "Shared you post.", id));
                    holder.itemView.setTag(pushKey);
                    holder.userButton.setText("undo");
                    Toast.makeText(mContext, "Post sent successfully to "+user.getFullname(), Toast.LENGTH_SHORT).show();
                } else if(buttonTxt.equals("undo")){
                    ref.child("Notifications").child(user.getId()).child(holder.itemView.getTag().toString()).removeValue();
                    holder.userButton.setText("send");
                    Toast.makeText(mContext, "Undo Successfull", Toast.LENGTH_SHORT).show();
                } else if(buttonTxt.equals("follow")){
                    ref.child("Follow").child(user.getId()).child("followers").child(firebaseUser.getUid()).setValue(true);
                    ref.child("Follow").child(firebaseUser.getUid()).child("following").child(user.getId()).setValue(true);
                    holder.userButton.setText("following");

                } else if(buttonTxt.equals("following")){
                    ref.child("Follow").child(user.getId()).child("followers").child(firebaseUser.getUid()).removeValue();
                    ref.child("Follow").child(firebaseUser.getUid()).child("following").child(user.getId()).removeValue();
                    holder.userButton.setText("follow");
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
