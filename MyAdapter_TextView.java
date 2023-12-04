package com.example.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Model.NewBills;
import com.example.payment_reminder_app.R;

import java.util.ArrayList;

public class MyAdapter_TextView extends RecyclerView.Adapter<MyAdapter_TextView.MyViewHolder> {

    Context context;

    ArrayList<NewBills> list;

    public MyAdapter_TextView(Context context, ArrayList<NewBills> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.items_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        NewBills newBills = list.get(position);
        holder.title_adp.setText(newBills.getTitle());

        holder.view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title_adp;
        View view2;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view2 = itemView;
            title_adp = itemView.findViewById(R.id.title_txt);
        }
    }
}