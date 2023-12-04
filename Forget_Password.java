package com.example.payment_reminder_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class Forget_Password extends AppCompatActivity {

    ImageView back_btn;
    EditText email_ET;
    Button reset_pass;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    String strEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        back_btn = findViewById(R.id.back_btn);
        email_ET = findViewById(R.id.email_ET);
        reset_pass = findViewById(R.id.reset_pass);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        reset_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strEmail = email_ET.getText().toString().trim();
                if (!TextUtils.isEmpty(strEmail)) {
                    Reset_Password();
                } else {
                    email_ET.setError("Enter Your Email First");
                }
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Forget_Password.this, SignInActivity.class);
                startActivity(intent);

            }
        });

    }

    private void Reset_Password() {
        progressBar.setVisibility(View.VISIBLE);
        reset_pass.setVisibility(View.INVISIBLE);

        mAuth.sendPasswordResetEmail(strEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(Forget_Password.this, "Reset Password Link has been send to your Registered Email", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Forget_Password.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Forget_Password.this, "Error :-" + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                reset_pass.setVisibility(View.VISIBLE);
            }
        });
    }
}