package com.medicine.reminderapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;


public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.MyViewHolder> {
    public ArrayList<UsersListModel> childModelArrayList;
    Context cxt;

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView name,email,phone,address;
        ImageView imgPd;
        public  View viewItem;

        public MyViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.uname);
            email = itemView.findViewById(R.id.email);
            phone = itemView.findViewById(R.id.phone);
            address = itemView.findViewById(R.id.address);

            viewItem=itemView;

        }
    }

    public UsersListAdapter(ArrayList<UsersListModel> arrayList, Context mContext) {
        this.cxt = mContext;
        this.childModelArrayList = arrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        UsersListModel currentItem = childModelArrayList.get(position);


        holder.name.setText(currentItem.getName());
        holder.phone.setText(currentItem.getPhone());
        holder.email.setText(currentItem.getEmail());
        holder.address.setText(currentItem.getAddress());




    }

    @Override
    public int getItemCount() {
        return childModelArrayList.size();
    }
}
