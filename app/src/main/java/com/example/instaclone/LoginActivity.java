package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText email,password;
    private TextView goToRegisterPage;
    private Button login;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        goToRegisterPage = findViewById(R.id.go_to_register_page);
        login = findViewById(R.id.login_with_db);
        mAuth = FirebaseAuth.getInstance();

        goToRegisterPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtEmail = email.getText().toString();
                String txtPassword = password.getText().toString();

                if(TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)){
                    Toast.makeText(getApplicationContext(),"Empty Credentials!",Toast.LENGTH_SHORT).show();
                } else{
                    loginUser(txtEmail,txtPassword);
                }
            }
        });
    }

    private void loginUser(String txtEmail, String txtPassword) {
        ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setTitle("Please wait...");
        pd.show();
        mAuth.signInWithEmailAndPassword(txtEmail,txtPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    pd.dismiss();
                    startActivity(new Intent(LoginActivity.this,StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                }
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