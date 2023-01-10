package com.medicine.reminderapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StockReminderAdapter extends RecyclerView.Adapter<StockReminderAdapter.ReminderViewHolder>{

    private Context reminderAdapterContext;
    private List<Reminder> reminderList;
    private List<Integer> selectedPositions = new ArrayList<>();
    private ReminderInterface reminderInterface;

    public StockReminderAdapter(Context reminderAdapterContext, List<Reminder> reminderList, ReminderInterface reminderInterface) {
        this.reminderAdapterContext = reminderAdapterContext;
        this.reminderInterface= reminderInterface;
        this.reminderList = reminderList;
    }

    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_reminder_layout_item, parent, false);

        return new ReminderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReminderViewHolder holder, int position) {

        holder.rTitleTextView.setText(reminderList.get(position).getReminderTitle());
        holder.rTOFTextView.setText(reminderList.get(position).getReminderTOF());
        holder.stock.setText(reminderList.get(position).getStock());


        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                openEditDialog(holder.getAdapterPosition());

                reminderInterface.editClick(holder.getAdapterPosition());
            }
        });


        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reminderInterface.deleteClick(holder.getAdapterPosition());
            }
        });
        holder.rDOFTextView.setText(reminderList.get(position).getReminderDOF());


        holder.whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openWhatsApp();

            }
        });

//        if (selectedPositions.contains(position)){
//            holder.reminderListRowFrameLayout.setForeground(new ColorDrawable(ContextCompat.getColor(reminderAdapterContext,R.color.text1)));
//        }
//        else {
//            holder.reminderListRowFrameLayout.setForeground(new ColorDrawable(ContextCompat.getColor(reminderAdapterContext,android.R.color.transparent)));
//        }

    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    protected class ReminderViewHolder extends RecyclerView.ViewHolder {
        protected TextView rTitleTextView;
        protected TextView rTOFTextView;
        protected TextView stock;
        protected ImageView edit, delete, whatsapp;

        protected TextView rDOFTextView;
//        protected FrameLayout reminderListRowFrameLayout;

        public ReminderViewHolder(View view) {
            super(view);
            rTitleTextView = (TextView) view.findViewById(R.id.reminder_title_tvw);
            rTOFTextView = (TextView) view.findViewById(R.id.reminder_tof_tvw);
            edit =  view.findViewById(R.id.edit_reminder);
            delete =  view.findViewById(R.id.delete_reminder);
            whatsapp =  view.findViewById(R.id.whatsapp);
            rDOFTextView = view.findViewById(R.id.reminder_dof_tvw);
            stock = view.findViewById(R.id.reminder_stock);
//            reminderListRowFrameLayout = view.findViewById(R.id.reminder_list_row_frame_layout);
        }
    }


    public void setSelectedPositions(int previousPosition, List<Integer> selectedPositions) {
        this.selectedPositions = selectedPositions;

        if(previousPosition!=-1){
            notifyItemChanged(previousPosition);
        }

        if(this.selectedPositions.size()>0){
            notifyItemChanged(this.selectedPositions.get(0));
        }

    }


    public Reminder getItem(int position){
        return reminderList.get(position);
    }


    private void openEditDialog(int reminderPosition){

        Reminder selectedReminder = reminderList.get(reminderPosition);
        int reminderId = selectedReminder.getReminderId();
        String reminderTitle = selectedReminder.getReminderTitle();
        String reminderDOF = selectedReminder.getReminderDOF();
        String reminderTOF = selectedReminder.getReminderTOF();
        long reminderTIM = selectedReminder.getReminderTIM();

        FragmentManager openERFSD = ((AppCompatActivity)reminderAdapterContext).getSupportFragmentManager();
        ReminderFSD editReminderFSD =  ReminderFSD.newInstance(reminderId,reminderTitle,reminderDOF,reminderTOF,reminderTIM,reminderPosition,true);
        FragmentTransaction oEditRFSDTransaction = openERFSD.beginTransaction();
        oEditRFSDTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        oEditRFSDTransaction.add(android.R.id.content, editReminderFSD).addToBackStack(null).commit();
    }


    public void openWhatsApp(){
        try {
            String text = "Hi, I need new Stock.";// Replace with your message.

//            String toNumber = "+923407958573";
            String toNumber = "+201092234564";
            // Replace with mobile phone number without +Sign or leading zeros, but with country code
            //Suppose your country is India and your phone number is “xxxxxxxxxx”, then you need to send “91xxxxxxxxxx”.


            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
            reminderAdapterContext.startActivity(intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}