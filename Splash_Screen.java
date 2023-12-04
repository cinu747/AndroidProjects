package com.example.payment_reminder_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.Utils.PrefrenceManager;

public class Splash_Screen extends AppCompatActivity {
    PrefrenceManager prefrenceManager;
    String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        prefrenceManager = new PrefrenceManager(this);
        email = prefrenceManager.getData(prefrenceManager.EMAIL);

        new Handler().postDelayed(() -> {
            if (!email.isEmpty()) {
                Intent intent = new Intent(Splash_Screen.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(Splash_Screen.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000); // 3000 milliseconds (2 seconds)
    }
}