package com.example.payment_reminder_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class NotificationActivity extends AppCompatActivity {

    TextView textViewData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        textViewData = findViewById(R.id.textViewData);
        String data = getIntent().getStringExtra("data");
        textViewData.setText(data);


    }
}