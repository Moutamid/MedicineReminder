package com.medicine.reminderapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

public class NotificationService extends JobIntentService {

    private static final int JOB_ID = 1000;
    private int currentNotificationId = 1;
    String CHANNEL_ID = "ra_c_id";
    public static int REQUEST_CODE = 100;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, NotificationService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String reminderTitle = intent.getExtras().getString("rRTitle");
        setNotification(reminderTitle);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setNotification(String tit) {


//        SharedPreferences pref = getSharedPreferences("MyPref", 0);
//        String tit= pref.getString("notifytitle",null);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(getString(R.string.channel_description));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[] {0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.new_logo)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(getString(R.string.notification_ctitle))
                .setContentText(tit);

        Intent notifyIntent = new Intent(this, RemindersActivity.class);
//        notifyIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, notifyIntent, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent intent = PendingIntent.getActivity(this, 0,
                notifyIntent, PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder.setContentIntent(intent);

        notificationManager.notify(currentNotificationId, notificationBuilder.build());
    }
}
