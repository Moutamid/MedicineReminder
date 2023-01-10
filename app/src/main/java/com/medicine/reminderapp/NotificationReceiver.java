package com.medicine.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.fxn.stash.Stash;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "ABC NotificationRecever";

    private ReminderDatabaseAdapter remindersDatabaseAdapter;
    private StockReminderDatabaseAdapter stockReminderDatabaseAdapter;
    private List<Reminder> reminderList = new ArrayList<>();
    private List<Reminder> stockreminderList = new ArrayList<>();
    String titlenotify, note;
    boolean isStock;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: started");

        String rRTitle = intent.getStringExtra("lReminderTitle");
        String rRTitle2 = intent.getStringExtra("note");
        isStock = intent.getBooleanExtra("isStock", false);
//        if(extras != null){

        if (rRTitle != null) {
            Log.d(TAG, "onReceive: if (rRTitle != null) {");
            if (rRTitle.length() > 0) {
                Log.d(TAG, "onReceive: if (rRTitle.length() > 0) {");
                Log.d(TAG, "onReceive: isStock: "+isStock);
                if (isStock) {
                    Log.d(TAG, "onReceive: if (isStock) {");
                    startAlarmService(context, rRTitle+" stock finished", rRTitle2);
                }else {
                    startAlarmService(context, "Take this drug: "+rRTitle, rRTitle2);
                }

                if (!isStock) {
                    Log.d(TAG, "onReceive: if (!isStock) {");
                    ArrayList<Reminder> reminderArrayList =
                            Stash.getArrayList(Constants.STOCKS_LIST, Reminder.class);

                    for (Reminder reminder : reminderArrayList) {
                        if (reminder.getReminderTitle().equals(rRTitle)) {
                            int stockValue = Integer.parseInt(reminder.getStock());
                            stockValue--;

                            reminder.setStock(String.valueOf(stockValue));
                        }
                    }

                    Stash.put(Constants.STOCKS_LIST, reminderArrayList);

                    int newCode = new Random().nextInt(9999);
                    int oldCode = Stash.getInt(rRTitle, newCode);

                    if (oldCode == newCode){
                        Stash.put(rRTitle, newCode);
                        oldCode = newCode;
                    }

                    Intent rARIntent = new Intent(context, NotificationReceiver.class);
                    rARIntent.putExtra("lReminderTitle", rRTitle);
                    rARIntent.putExtra("isStock", false);
                    rARIntent.putExtra("code", oldCode);
                    PendingIntent rARPIntent = PendingIntent.getBroadcast(context, oldCode, rARIntent, PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager rAAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(System.currentTimeMillis());
                    c.add(Calendar.DATE, 1);

                    if (Build.VERSION.SDK_INT >= 23) {
                        rAAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), rARPIntent);
                    } else if (Build.VERSION.SDK_INT >= 19) {
                        rAAlarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), rARPIntent);
                    } else {
                        rAAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), rARPIntent);
                    }

                }
            }
        }

        //TODO: COMMENTED
        // setNextReminderAlarmStock(context);
    }

    private int getNextReminderAPosition() {
        Log.d(TAG, "getNextReminderAPosition: ");
        int nRAPosition = 0;

        int i = 0;
        while (i >= 0 && i <= (reminderList.size() - 1)) {
            Reminder bReminder = reminderList.get(i);
            long nowTIM = getNowTIM();
            long bReminderTIM = bReminder.getReminderTIM();

            if (bReminderTIM > nowTIM) {
                nRAPosition = i;
                i = reminderList.size();
            }

            ++i;
        }

        return nRAPosition;
    }

    private int getNextReminderAPositionStock() {
        Log.d(TAG, "getNextReminderAPositionStock: ");
        int nRAPosition = 0;

        int i = 0;
        while (i >= 0 && i <= (stockreminderList.size() - 1)) {
            Reminder bReminder = stockreminderList.get(i);
            long nowTIM = getNowTIM();
            long bReminderTIM = bReminder.getReminderTIM();

            if (bReminderTIM > nowTIM) {
                nRAPosition = i;
                i = stockreminderList.size();
            }

            ++i;
        }

        return nRAPosition;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setNextReminderAlarm(Context context) {
        Log.d(TAG, "setNextReminderAlarm: ");
        remindersDatabaseAdapter = new ReminderDatabaseAdapter(context);
        remindersDatabaseAdapter.open();

        Runnable initializeReminderListRunnable = new Runnable() {
            @Override
            public void run() {
                reminderList = remindersDatabaseAdapter.fetchReminders();

            }
        };
        Thread initializeReminderListThread = new Thread(initializeReminderListRunnable);
        initializeReminderListThread.start();
        try {
            initializeReminderListThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (reminderList != null) {

            if (reminderList.size() != 0) {

                int nRAPosition = getNextReminderAPosition();
                Reminder latestReminder = reminderList.get(nRAPosition);

                long nowTIM = getNowTIM();
                long lReminderTIM = latestReminder.getReminderTIM();

                if (lReminderTIM >= nowTIM) {

                    titlenotify = latestReminder.getReminderTitle();
                    note = "medicine";
                    Intent rARIntent = new Intent(context, NotificationReceiver.class);
                    rARIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    rARIntent.putExtra("lReminderTitle", titlenotify);
                    rARIntent.putExtra("isStock", false);
                    rARIntent.putExtra("note", note);
                    PendingIntent rARPIntent = PendingIntent.getBroadcast(context, 100, rARIntent, PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager rAAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


                    int SDK_INT = Build.VERSION.SDK_INT;
                    if (SDK_INT < Build.VERSION_CODES.KITKAT) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.set(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);
                    } else if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.setExact(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);

                    } else if (SDK_INT >= Build.VERSION_CODES.M) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.setExact(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);
                    }

                }

            }

        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setNextReminderAlarmStock(Context context) {
        Log.d(TAG, "setNextReminderAlarmStock: ");
        stockReminderDatabaseAdapter = new StockReminderDatabaseAdapter(context);
        stockReminderDatabaseAdapter.open();

        Runnable initializeReminderListRunnable = new Runnable() {
            @Override
            public void run() {
                stockreminderList = stockReminderDatabaseAdapter.fetchReminders();

            }
        };
        Thread initializeReminderListThread = new Thread(initializeReminderListRunnable);
        initializeReminderListThread.start();
        try {
            initializeReminderListThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (stockreminderList != null) {

            if (stockreminderList.size() != 0) {

                int nRAPosition = getNextReminderAPositionStock();
                Reminder latestReminder = stockreminderList.get(nRAPosition);

                long nowTIM = getNowTIM();
                long lReminderTIM = latestReminder.getReminderTIM();

                if (lReminderTIM >= nowTIM) {

                    titlenotify = latestReminder.getReminderTitle();
                    note = "stock";
                    Intent rARIntent = new Intent(context, NotificationReceiver.class);
                    rARIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    rARIntent.putExtra("lReminderTitle", titlenotify);
                    rARIntent.putExtra("isStock", true);
                    rARIntent.putExtra("note", note);
                    PendingIntent rARPIntent = PendingIntent.getBroadcast(context, 200, rARIntent, PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager rAAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


                    int SDK_INT = Build.VERSION.SDK_INT;
                    if (SDK_INT < Build.VERSION_CODES.KITKAT) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.set(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);

                    } else if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.setExact(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);

                    } else if (SDK_INT >= Build.VERSION_CODES.M) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.setExact(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);
                    }

                }

            }

        }

    }


    protected long getNowTIM() {
        Log.d(TAG, "getNowTIM: ");
        Date nowDate = new Date();
        long nowTIM = nowDate.getTime();
        return nowTIM;
    }


    private void startAlarmService(Context context, String title, String title2) {
        Log.d(TAG, "startAlarmService: ");
        Intent intentService = new Intent(context, AlarmService.class);
        intentService.putExtra("notify", title);
        intentService.putExtra("note2", title2);
        intentService.putExtra("isStock", isStock);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService);
        } else {
            context.startService(intentService);
        }
    }

}