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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Utils.PrefrenceManager;
import com.example.Model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    EditText emailET, passwordET;
    ProgressBar progressBar;
    Button signInBtn;
    ImageView googleAuth;
    TextView register, last_txt;
    PrefrenceManager prefrenceManager;
    Boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        emailET = findViewById(R.id.email_ET);
        passwordET = findViewById(R.id.password_ET);
        signInBtn = findViewById(R.id.sign_in_btn);
        register = findViewById(R.id.not_account_txt); // don't have an account object
        googleAuth = findViewById(R.id.googleAuth);
        progressBar = findViewById(R.id.progressBar);
        last_txt = findViewById(R.id.last_txt);

        mAuth = FirebaseAuth.getInstance(); //firebase authentication object
        firebaseDatabase = FirebaseDatabase.getInstance(); //firebase database object
        prefrenceManager = new PrefrenceManager(this); //To set the app context

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class); //new intent created, navigate to sign up
                startActivity(intent);
                finish();
            }
        });

        passwordET.setOnTouchListener(new View.OnTouchListener() { //password mask or unmask
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int Right = 2;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    if (motionEvent.getRawX()>=passwordET.getRight()-passwordET.getCompoundDrawables()[Right].getBounds().width()){
                        int selection = passwordET.getSelectionEnd();
                        if (passwordVisible){
                            passwordET.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.hide,0);
                            passwordET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        }else
                        {
                            passwordET.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.view,0);
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

        last_txt.setOnClickListener(new View.OnClickListener() { //navigate to "forgot password? intent
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Forget_Password.class);
                startActivity(intent);
                finish();
            }
        });

        googleAuth.setOnClickListener(new View.OnClickListener() { //Google button to sign in using google
            @Override
            public void onClick(View view) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient signInClient = GoogleSignIn.getClient(SignInActivity.this, gso);
                if (signInClient != null) {
                    signInClient.signOut();
                }
                googleAuthSignIn();
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(emailET.getText());
                password = String.valueOf(passwordET.getText());
                if (TextUtils.isEmpty(email)) { //toast if empty
                    Toast.makeText(SignInActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignInActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password) //mAuth created in firebase. If not empty, function is called
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    prefrenceManager.setData(prefrenceManager.EMAIL, email); //To save the email to the phone, another login is not required
                                    Toast.makeText(SignInActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void googleAuthSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient signInClient = GoogleSignIn.getClient(SignInActivity.this, gso);

        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) { //after signed in successfully, check if request code is successful with google. Then the google email will
            //be used to login with firebase
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getEmail());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String email) {
        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("email")
                .equalTo(email)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            prefrenceManager.setData(prefrenceManager.EMAIL, email);
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            User newBills = new User();

                            newBills.email = email;
                            FirebaseDatabase.getInstance().getReference("users").push()
                                    .setValue(newBills).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                prefrenceManager.setData(prefrenceManager.EMAIL, email);
                                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(SignInActivity.this, "Please try again Later", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SignInActivity.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.d("App Crashed", "onFailure: " + e.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error fetching data from Firebase: " + error.getMessage());
                    }
                });
    }



}