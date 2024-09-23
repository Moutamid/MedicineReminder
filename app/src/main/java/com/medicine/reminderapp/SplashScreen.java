package com.medicine.reminderapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;


public class SplashScreen extends AppCompatActivity {
    private static int SPLASH_SCREEN = 2000;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        SharedPreferences pref = getSharedPreferences("MyPref", 0);
        login = pref.getString("login", "no");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAuth.getCurrentUser() == null || login.equals("no")) {
                    Intent intent = new Intent(SplashScreen.this, SelectionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashScreen.this, RemindersActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_SCREEN);
    }
}