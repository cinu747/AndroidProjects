package com.example.payment_reminder_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.Model.NewBills;
import com.example.Utils.PrefrenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class AddBill extends AppCompatActivity {
    protected Button cancelBtn, submitBtn;
    private EditText titleET, descriptionET;
    PrefrenceManager prefrenceManager;
    private Button issueDateBtn, dueDateBtn;
    private int year, month, day;
    private DatePickerDialog datePickerDialogIssue, datePickerDialogDue;
    String issue_date, due_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        cancelBtn = findViewById(R.id.cancel_btn);
        submitBtn = findViewById(R.id.submit_btn);
        issueDateBtn = findViewById(R.id.datePickerButton);
        dueDateBtn = findViewById(R.id.datePickerButton2);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-dd-yyyy");
        String currentDate = simpleDateFormat.format(new Date());
        issueDateBtn.setText(currentDate);
        dueDateBtn.setText(currentDate);

        prefrenceManager = new PrefrenceManager(this);
        titleET = findViewById(R.id.title_ET);
        descriptionET = findViewById(R.id.description_ET);

        // Get the current date
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        year = calendar.get(java.util.Calendar.YEAR);
        month = calendar.get(java.util.Calendar.MONTH);
        day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        // Create separate DatePickerDialogs for each button
        datePickerDialogIssue = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                year = selectedYear;
                month = selectedMonth;
                day = selectedDay;
                // Update your UI with the selected date
                updateDateDisplay();
            }
        }, year, month, day);

        datePickerDialogDue = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                // Update your UI with the selected date for datePickerButton2
                updateDateDisplay2(selectedYear, selectedMonth, selectedDay);
            }
        }, year, month, day);

        issueDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the date picker dialog for datePickerButton
                datePickerDialogIssue.show();
            }
        });

        dueDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the date picker dialog for datePickerButton2
                datePickerDialogDue.show();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBills();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BillActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateDateDisplay() {
        issue_date = String.format("%d-%02d-%02d", year, month + 1, day);
        issueDateBtn.setText(issue_date);

        // Automatically calculate and update the due date with a default value
        updateDueDateDisplayDefault();
    }

    private void updateDueDateDisplayDefault() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, month, day);
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        updateDateDisplay2(
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
        );
    }

    private void updateDateDisplay2(int selectedYear, int selectedMonth, int selectedDay) {
        java.util.Calendar selectedCalendar = java.util.Calendar.getInstance();
        selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
        java.util.Calendar currentCalendar = java.util.Calendar.getInstance();
        if (selectedCalendar.before(currentCalendar)) {
            due_date = String.format("%d-%02d-%02d",
                    currentCalendar.get(java.util.Calendar.YEAR),
                    currentCalendar.get(java.util.Calendar.MONTH) + 1,
                    currentCalendar.get(java.util.Calendar.DAY_OF_MONTH));
            Toast.makeText(this, "Due date must be greater than issue date.", Toast.LENGTH_SHORT).show();
        } else {
            due_date = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
        }
        dueDateBtn.setText(due_date);
    }

    private void saveBills() {
        String title;
        String description;
        String email = prefrenceManager.getData(prefrenceManager.EMAIL);

        title = titleET.getText().toString();
        description = descriptionET.getText().toString();
        if (title.isEmpty()) {
            titleET.setError("Enter the title of your bill");
        }
        if (issue_date == null || issue_date.isEmpty()) {
            Toast.makeText(this, "Please enter the issue date.", Toast.LENGTH_SHORT).show();
            return;
        }
        NewBills newBills = new NewBills();

        newBills.email = email;
        newBills.title = title;
        newBills.description = description;
        newBills.issueDate = issue_date;
        newBills.dueDate = due_date;

        FirebaseDatabase.getInstance().getReference("new bill").push()
                .setValue(newBills).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddBill.this, "Bill Added", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddBill.this, "Bill Not Added", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddBill.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("App Crashed", "onFailure: " + e.getMessage());
                    }
                });
    }
}