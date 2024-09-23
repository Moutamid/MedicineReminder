package com.medicine.reminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


public class SelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        Constants.checkApp(this);

    }


    public void adminClick(View view) {
        Intent intent = new Intent(SelectionActivity.this, AdminLoginActivity.class);
        startActivity(intent);
    }

    public void userClick(View view) {
        Intent intent = new Intent(SelectionActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}