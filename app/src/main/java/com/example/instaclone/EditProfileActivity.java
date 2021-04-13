 package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import com.example.instaclone.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView close;
    private CircleImageView imageProfile;
    private TextView save,changePhoto;
    private MaterialEditText fullname,username,bio;

    private Uri mImageUri = null;
    private StorageTask uploadTask;
    private StorageReference storageRef;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private String lastUsername;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        close = findViewById(R.id.close);
        imageProfile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        changePhoto = findViewById(R.id.change_photo);
        fullname = findViewById(R.id.fullname);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        storageRef = FirebaseStorage.getInstance().getReference().child("Dp");

        databaseReference.child("Users").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                lastUsername = user.getUsername();
                fullname.setText(user.getFullname());
                username.setText(user.getUsername());
                bio.setText(user.getBio());
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

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setCropShape(CropImageView.CropShape.RECTANGLE).start(EditProfileActivity.this);
            }
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setCropShape(CropImageView.CropShape.RECTANGLE).start(EditProfileActivity.this);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setUsername(username.getText().toString());
                user.setName(fullname.getText().toString());
                user.setBio(bio.getText().toString());

                if(TextUtils.isEmpty(user.getFullname())){
                    Toast.makeText(EditProfileActivity.this, "Fullname can't be empty", Toast.LENGTH_SHORT).show();
                } else {
                    checkUsernameAvailability();
                }
            }
        });
    }

    private void updateProfile(){

        databaseReference.child("Users").child(firebaseUser.getUid()).setValue(user);

        if(!user.getUsername().toLowerCase().equals(lastUsername.toLowerCase())){
            databaseReference.child("Username").child(lastUsername).removeValue();
            databaseReference.child("Username").child(user.getUsername()).setValue(true);
        }
        Toast.makeText(this, "Profile Updated successfully", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();
            imageProfile.setImageURI(mImageUri);
        } else{
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkUsernameAvailability() {
        if(TextUtils.isEmpty(user.getUsername())){
            Toast.makeText(this, "Username can't be empty", Toast.LENGTH_SHORT).show();
        } else if(!user.getUsername().toLowerCase().equals(lastUsername.toLowerCase())) {
            databaseReference.child("Username").child(user.getUsername().toLowerCase()).
            addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(EditProfileActivity.this, "Username not available", Toast.LENGTH_SHORT).show();
                    } else {
                        uploadImage();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if(user.getUsername().toLowerCase().equals(lastUsername.toLowerCase())){
            uploadImage();
        }
    }

    private void uploadImage() {
        if(mImageUri != null){
            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Uploading");
            pd.show();
            StorageReference fileRef = storageRef.child(firebaseUser.getUid()+".jpeg");
            uploadTask = fileRef.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                     if(!task.isSuccessful()){
                         throw task.getException();
                     }
                     return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        user.setImageurl(downloadUri.toString());
                        updateProfile();
                        pd.dismiss();
                    } else{
                        Toast.makeText(EditProfileActivity.this, "Image upload Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Updating profile without image", Toast.LENGTH_SHORT).show();
            updateProfile();
        }

    }
}