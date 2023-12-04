package com.example.payment_reminder_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Utils.PrefrenceManager;
import com.example.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText emailET, passwordET, comPasswordET;
    ProgressBar progressBar;
    TextView signInText;
    Button continueBtn;
    PrefrenceManager prefrenceManager;
    Boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signInText = findViewById(R.id.sign_in_txt);
        emailET = findViewById(R.id.email_ET);
        passwordET = findViewById(R.id.password_ET);
        comPasswordET = findViewById(R.id.cm_password_ET);
        progressBar = findViewById(R.id.progressBar);
        continueBtn = findViewById(R.id.continue_btn);

        mAuth = FirebaseAuth.getInstance();
        prefrenceManager = new PrefrenceManager(this);

        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                finish();

            }
        });

        passwordET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int Right = 2;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getRawX() >= passwordET.getRight() - passwordET.getCompoundDrawables()[Right].getBounds().width()) {
                        int selection = passwordET.getSelectionEnd();
                        if (passwordVisible) {
                            passwordET.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.hide, 0);
                            passwordET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        } else {
                            passwordET.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.view, 0);
                            passwordET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisible = true;
                        }
                        passwordET.setSelection(selection);
                        return true;
                    }
                }

                return false;
            }
        });

        comPasswordET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int Right = 2;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if (motionEvent.getRawX()>=comPasswordET.getRight()-comPasswordET.getCompoundDrawables()[Right].getBounds().width()){
                        int selection = comPasswordET.getSelectionEnd();
                        if (passwordVisible){
                            comPasswordET.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.hide,0);
                            comPasswordET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        }else
                        {
                            comPasswordET.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.view,0);
                            comPasswordET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisible = true;
                        }
                        comPasswordET.setSelection(selection);
                        return true;
                    }
                }

                return false;
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, c_password;

                email = String.valueOf(emailET.getText());
                password = String.valueOf(passwordET.getText());
                c_password = String.valueOf(comPasswordET.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(SignUpActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignUpActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(c_password)) {
                    Toast.makeText(SignUpActivity.this, "Enter Password Again", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    prefrenceManager.setData(prefrenceManager.EMAIL, email);
                                    firebaseStoreEmailUser(email);
                                    Toast.makeText(SignUpActivity.this, "Account Created.", Toast.LENGTH_SHORT).show();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                        });
            }
        });
    }

    private void firebaseStoreEmailUser(String email) {
        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("email")
                .equalTo(email)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            User newBills = new User();
                            newBills.email = email;
                            FirebaseDatabase.getInstance().getReference("users").push()
                                    .setValue(newBills).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(SignUpActivity.this, "Please try again Later", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SignUpActivity.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.d("App Crashed", "onFailure: " + e.getMessage());
                                        }
                                    });
                        } else {
                            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error fetching data from Firebase: " + error.getMessage());
                    }
                });
    }
}