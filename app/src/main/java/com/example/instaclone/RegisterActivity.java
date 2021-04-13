package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instaclone.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    private EditText username,name,email,password;
    private Button register;
    private TextView goToLoginPage;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.register_username);
        name = findViewById(R.id.register_name);
        email = findViewById(R.id.register_email);
        password = findViewById(R.id.register_password);
        register = findViewById(R.id.register_to_db);
        goToLoginPage = findViewById(R.id.go_to_login_page);
        pd = new ProgressDialog(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        goToLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtUsername = username.getText().toString();
                String txtName = name.getText().toString();
                String txtEmail = email.getText().toString();
                String txtPassword = password.getText().toString();

                if(TextUtils.isEmpty(txtUsername) || TextUtils.isEmpty(txtName) || TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)){
                    Toast.makeText(getApplicationContext(),"Empty Credentials",Toast.LENGTH_SHORT).show();
                }  else if(txtPassword.length()<6){
                    Toast.makeText(getApplicationContext(),"Password too short",Toast.LENGTH_SHORT).show();
                } else{
                    checkUsernameAvailability(txtUsername,txtName,txtEmail,txtPassword);
                }
            }
        });
    }

    private void checkUsernameAvailability(String txtUsername,String txtName,String txtEmail,String txtPassword) {
        databaseReference.child("Username").child(txtUsername.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast.makeText(RegisterActivity.this, "Username already taken, Choose another", Toast.LENGTH_SHORT).show();
                } else{
                    registerUser(txtUsername,txtName,txtEmail,txtPassword);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void registerUser(String txtUsername, String txtName, String txtEmail, String txtPassword) {

        pd.setTitle("Please wait...");
        pd.show();
        mAuth.createUserWithEmailAndPassword(txtEmail,txtPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                User user = new User("",txtEmail,mAuth.getCurrentUser().getUid(),"default",txtName,txtUsername);
                databaseReference.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            pd.dismiss();
                            databaseReference.child("Username").child(txtUsername.toLowerCase()).setValue(true);
                            Toast.makeText(getApplicationContext(),"Registration successful,Update the profile for better experience",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this,StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            finish();
                        } else{
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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