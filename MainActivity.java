/*
Name: Cini Kolath Abraham
Final Project
12/03/2023
Brief program description: This is a payment reminder application for the users to set reminders. All bills can be
added to this application and the app will categorise into three sections according to the due dates.*/

package com.example.payment_reminder_app;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.Adapter.MyAdapter_CardView;
import com.example.Model.NewBills;
import com.example.Utils.PrefrenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button btnNext, logout;
    RecyclerView recyclerViewOverdue, recyclerViewAvailable, recyclerViewUpcoming;
    ArrayList<NewBills> list;
    PrefrenceManager prefrenceManager;
    String email;
    Date currentDate;
    ArrayList<NewBills> overdueList = new ArrayList<>();
    ArrayList<NewBills> availableList = new ArrayList<>();
    ArrayList<NewBills> upcomingList = new ArrayList<>();
    private MyAdapter_CardView adapterOverdue;
    private MyAdapter_CardView adapterAvailable;
    private MyAdapter_CardView adapterUpcoming;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNext = findViewById(R.id.list_btn);
        logout = findViewById(R.id.logout);
        Permission_Notification();
        prefrenceManager = new PrefrenceManager(this);
        currentDate = new Date();

        list = new ArrayList<>();
        adapterOverdue = new MyAdapter_CardView(this, overdueList, MainActivity.this, "due");
        adapterAvailable = new MyAdapter_CardView(this, availableList, MainActivity.this, "available");
        adapterUpcoming = new MyAdapter_CardView(this, upcomingList, MainActivity.this, "upcoming");

        // recycle view for OverDue Bills
        recyclerViewOverdue = findViewById(R.id.recycler_view_overdue);
        recyclerViewOverdue.setHasFixedSize(true);
        recyclerViewOverdue.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOverdue.setAdapter(adapterOverdue);

        // recycle view for Available bills
        recyclerViewAvailable = findViewById(R.id.recycler_view_available);
        recyclerViewAvailable.setHasFixedSize(true);
        recyclerViewAvailable.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAvailable.setAdapter(adapterAvailable);

        // recycle view for Upcoming Bills
        recyclerViewUpcoming = findViewById(R.id.recycler_view_upcoming);
        recyclerViewUpcoming.setHasFixedSize(true);
        recyclerViewUpcoming.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUpcoming.setAdapter(adapterUpcoming);
        email = prefrenceManager.getData(prefrenceManager.EMAIL);

        // Fetch Data form FireBase From Specifics Table
        FirebaseDatabase.getInstance().getReference("new bill")
                .orderByChild("email")
                .equalTo(email)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        overdueList.clear();
                        availableList.clear();
                        upcomingList.clear();


                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            NewBills newBills = dataSnapshot.getValue(NewBills.class);

                            if (newBills != null && newBills.getEmail() != null && newBills.getEmail().equals(email)) {
                                newBills.key = dataSnapshot.getKey();
                                list.add(newBills);
                            }
                        }
                        if (!list.isEmpty()) {
                            lists();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error fetching data from Firebase: " + error.getMessage());
                    }
                });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BillActivity.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout_account();
            }
        });
    }

    void lists() {

        for (int i = 0; i < list.size(); i++) {
            if (isOverdue(list.get(i))) {
                if (list.get(i).getIspaid().equals("false")) {
                    overdueList.add(list.get(i));
                }
            } else if (isAvailable(list.get(i))) {
                availableList.add(list.get(i));
            } else if (isUpcoming(list.get(i))) {
                upcomingList.add(list.get(i));
            }
        }
        adapterOverdue.notifyDataSetChanged();
        adapterAvailable.notifyDataSetChanged();
        adapterUpcoming.notifyDataSetChanged();
    }

    public void ShowDialog(String keyvalue, ImageView img) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        View dialogView = getLayoutInflater().inflate(R.layout.show_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        Button dialog_button_wait = dialogView.findViewById(R.id.wait);
        Button dialog_button_paid = dialogView.findViewById(R.id.paid);

        dialog_button_wait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog_button_paid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateBillStatus(keyvalue, img);
                dialog.dismiss();
            }
        });
    }

    private void logout_account() {
        prefrenceManager.clearPreferences();
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private Date convertStringToDate(String dateString) {
        try {
            if (dateString != null && !dateString.isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                return dateFormat.parse(dateString);
            } else {
                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isOverdue(NewBills newBills) {
        Date dueDate = convertStringToDate(newBills.getDueDate());
        Log.d("check_date", newBills.getDueDate() + "isOverdue: " + dueDate);
        Boolean isOverdue = dueDate != null && currentDate.after(dueDate);

        if (isOverdue) {
            makeNotification("overdue");
        }

        return isOverdue;
    }

    private boolean isAvailable(NewBills newBills) {
        Date issueDate = convertStringToDate(newBills.getIssueDate());
        Date dueDate = convertStringToDate(newBills.getDueDate());
        boolean isAvailable = issueDate != null && dueDate != null && currentDate.after(issueDate) && currentDate.before(dueDate);

        if (isAvailable) {
            makeNotification("available");
        }

        return isAvailable;
    }

    private boolean isUpcoming(NewBills newBills) {
        Date issueDate = convertStringToDate(newBills.getIssueDate());
        if (issueDate == null) {
            return false;
        }
        long timeDifference = issueDate.getTime() - currentDate.getTime();
        long daysDifference = timeDifference / (24 * 60 * 60 * 1000);
        boolean checkUpcoming = daysDifference >= 0 && daysDifference <= 7;
        if (checkUpcoming && newBills.getIspaid().equals("true")) {
            updateBillStatusChange(newBills.getKey());
        }
        if (checkUpcoming) {
            makeNotification("upcoming");
        }
        return checkUpcoming;
    }

    // update Bill Status Change in FireBase
    private void updateBillStatusChange(String billId) {
        DatabaseReference billReference = FirebaseDatabase.getInstance().getReference("new bill").child(billId);

        billReference.child("ispaid").setValue("false")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    // update Bill Status in FireBase
    private void updateBillStatus(String billId, ImageView img) {
        DatabaseReference billReference = FirebaseDatabase.getInstance().getReference("new bill").child(billId);

        billReference.child("ispaid").setValue("true")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            img.setImageResource(R.drawable.checked);
                            Toast.makeText(MainActivity.this, "Bill status updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to update bill status", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void makeNotification(String billStatus) {
        String channel_id = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id);
        builder.setSmallIcon(R.drawable.logo)
                .setContentTitle("Pay Vista")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Add a large icon to the notification
        Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        builder.setLargeIcon(largeIconBitmap);

        // Set notification text based on bill status
        switch (billStatus) {
            case "overdue":
                builder.setContentText("Your Bill is Overdue! Quick Pay");
                break;
            case "available":
                builder.setContentText("Your Bill is Available To Pay");
                break;
            case "upcoming":
                builder.setContentText("Your Bill is Upcoming To Pay");
                break;
            default:
                builder.setContentText("New Notification");
                break;
        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data", "Some Value to be Passed Here");

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channel_id);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channel_id, "Some Description", importance);
                notificationChannel.setLightColor(Color.BLUE);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        notificationManager.notify(0, builder.build());
    }

    private void Permission_Notification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }


}