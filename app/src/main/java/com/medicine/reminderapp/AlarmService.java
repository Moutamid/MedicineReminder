package com.medicine.reminderapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    private static final String TAG = "ABC AlarmService";

    Vibrator vibrator;
    Ringtone ringtone;
    private static final int JOB_ID = 1000;
    private int currentNotificationId = 1;
    String CHANNEL_ID = "ra_c_id";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
//        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setLooping(true);
//        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        ringtone= RingtoneManager.getActualDefaultRingtoneUri(this.getBaseContext(), RingtoneManager.TYPE_ALARM);
    }

    boolean isStock;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        String tit = intent.getStringExtra("notify");
        String tit2 = intent.getStringExtra("note2");
        int code = intent.getIntExtra("code", 200);
        isStock = intent.getBooleanExtra("isStock", false);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(getString(R.string.channel_description));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.new_logo)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(getString(R.string.notification_ctitle))
                .setContentText(tit);

        Intent notifyIntent = new Intent(this, RemindersActivity.class);
        notifyIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, code, notifyIntent,
                PendingIntent.FLAG_IMMUTABLE);//FLAG_UPDATE_CURRENT
        notificationBuilder.setContentIntent(pendingIntent);

        notificationManager.notify(currentNotificationId, notificationBuilder.build());

        startForeground(code, notificationBuilder.build());

        // we will use vibrator first
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(15000);

        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        // setting default ringtone
        ringtone = RingtoneManager.getRingtone(this, alarmUri);

        // play ringtone
        ringtone.play();

        new Handler().postDelayed(() -> {
            Log.d(TAG, "onStartCommand: new Handler().postDelayed(() -> {");
            stopSelf();
        }, 15000);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        ringtone.stop();
        vibrator.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
