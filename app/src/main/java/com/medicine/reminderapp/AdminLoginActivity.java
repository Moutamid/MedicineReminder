package com.medicine.reminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);


        emailEditText = findViewById(R.id.ed_email);
        passwordEditText = findViewById(R.id.ed_password);
        loginBtn = findViewById(R.id.bt_login);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailStr = emailEditText.getText().toString();
                String passwordStr = passwordEditText.getText().toString();

                if (!TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr)) {

                    signInUserWithNameAndPassword(emailStr, passwordStr);

                } else if (TextUtils.isEmpty(emailStr)) {

                    emailEditText.setError("Please enter emailStr");
                    emailEditText.requestFocus();

                } else if (TextUtils.isEmpty(passwordStr)) {

                    passwordEditText.setError("Please enter password");
                    passwordEditText.requestFocus();

                }

            }
        });

    }




    private void signInUserWithNameAndPassword(final String emailStr, final String passwordStr) {



        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            //if Email Address is Invalid..


            emailEditText.setError("Email is not valid. Make sure no spaces and special characters are included");
            emailEditText.requestFocus();
        } else {


                    if (emailStr.equals("admin@gmail.com") && passwordStr.equals("admin123")) {


                            Intent intent = new Intent(AdminLoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();


                        Toast.makeText(

                                        AdminLoginActivity.this, "Log in Successfully", Toast.LENGTH_SHORT).

                                show();


                    } else {


                        Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show();                    }


        }


    }



}