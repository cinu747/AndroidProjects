package com.example.payment_reminder_app;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.Adapter.MyAdapter_TextView;
import com.example.Model.NewBills;
import com.example.Utils.PrefrenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;


public class BillActivity extends AppCompatActivity {
    Button backBtn, addBillBtn;
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    MyAdapter_TextView adapter;
    ArrayList<NewBills> list,listComplete;
    PrefrenceManager prefrenceManager;
    String email;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        addBillBtn = findViewById(R.id.add_circle);
        backBtn = findViewById(R.id.back_btn);
        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();

        prefrenceManager = new PrefrenceManager(this);

        recyclerView = findViewById(R.id.recycler_view);
        databaseReference = FirebaseDatabase.getInstance().getReference("new bill");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        listComplete = new ArrayList<>();

        adapter = new MyAdapter_TextView(this, list);
        recyclerView.setAdapter(adapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                listComplete.clear();
                email = prefrenceManager.getData(prefrenceManager.EMAIL);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NewBills newBills = dataSnapshot.getValue(NewBills.class);

                    Log.d("Data Debug", "onDataChange: " + newBills.getEmail());

                    if (newBills != null && newBills.getEmail().equals(email)) {

                        Log.d("Data_get", "onDataChange: " + newBills.getEmail());
                        list.add(newBills);
                        listComplete.add(newBills);
                    }
                }
                if (list.isEmpty()) {
                    Log.d("Data Debug", "No data found for the logged-in user.");
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching data from Firebase: " + error.getMessage());
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        addBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddBill.class);
                startActivity(intent);
            }
        });
    }

    public void searchList(String text){
        if (listComplete.size()>0) {
            list.clear();
            for (NewBills searchDataFirebase : listComplete) {
                if (searchDataFirebase.getTitle().toLowerCase().contains(text.toLowerCase())) {
                    list.add(searchDataFirebase);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }
}