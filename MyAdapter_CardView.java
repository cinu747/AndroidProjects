package com.example.Adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Model.NewBills;
import com.example.payment_reminder_app.MainActivity;
import com.example.payment_reminder_app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyAdapter_CardView extends RecyclerView.Adapter<MyAdapter_CardView.MyViewHolder> {

    Context context;

    ArrayList<NewBills> list;
    MainActivity activity;
    String type = "";

    public MyAdapter_CardView(Context context, ArrayList<NewBills> list, MainActivity activity, String type) {
        this.context = context;
        this.list = list;
        this.activity = activity;
        this.type = type;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        NewBills newBills = list.get(position);
        holder.title_card.setText(newBills.getTitle());

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM-dd-yyyy");
        try {
            Date date = inputFormat.parse(newBills.getDueDate());
            String formattedDate = outputFormat.format(date);
            holder.date_card.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle the ParseException appropriately
            holder.date_card.setText(newBills.getIssueDate());
        }

        if (newBills.getIspaid().equals("true") && !type.equals("upcoming")) {
            holder.check.setImageResource(R.drawable.checked);
        } else {
            holder.check.setImageResource(R.drawable.check);
        }
        holder.rl_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newBills.getIspaid().equals("false") && !type.equals("upcoming")) {
                    activity.ShowDialog(newBills.getKey(), holder.check);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title_card, date_card;
        RelativeLayout rl_main;
        public ImageView check;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            rl_main = itemView.findViewById(R.id.rl_main);
            title_card = itemView.findViewById(R.id.title_card);
            date_card = itemView.findViewById(R.id.date_card);
            check = itemView.findViewById(R.id.check);
        }
    }

    public void updateData(ArrayList<NewBills> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }
}