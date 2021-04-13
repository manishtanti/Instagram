package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instaclone.Model.Post;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {
    private Uri imageUri;
    private String imageUrl;
    private ImageView close,imageAdded;
    private TextView post;
    SocialAutoCompleteTextView description;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this,StartActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
        CropImage.activity().start(PostActivity.this);
    }

    private void upload() {
        if(imageUri != null){
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Uploading");
            pd.show();
            StorageReference filePath = FirebaseStorage.getInstance().getReference("Posts")
                    .child(firebaseUser.getUid()+System.currentTimeMillis());
            StorageTask uploadtask = filePath.putFile(imageUri);
            uploadtask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Uri downloadUrl = task.getResult();
                    imageUrl = downloadUrl.toString();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    String uniqueId = databaseReference.child("Posts").push().getKey();

                    Post post = new Post(description.getText().toString(),imageUrl,uniqueId,firebaseUser.getUid());

                    databaseReference.child("Posts").child(uniqueId).setValue(post);
                    databaseReference.child("Userposts").child(firebaseUser.getUid()).child(uniqueId).setValue(true);

                    DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                    List<String> hashTags = description.getHashtags();
                    if(!hashTags.isEmpty()){
                        for(String tag:hashTags){
                            mHashTagRef.child(tag.toLowerCase()).child(uniqueId).setValue(true);
                        }
                    }
                    pd.dismiss();
                    startActivity(new Intent(PostActivity.this,StartActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            imageAdded.setImageURI(imageUri);
        } else{
            Toast.makeText(getApplicationContext(),"Try Again...",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this,StartActivity.class));
            finish();
        }
    }

}