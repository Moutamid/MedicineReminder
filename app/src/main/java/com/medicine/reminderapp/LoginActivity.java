package com.medicine.reminderapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    String who;
    private EditText emailEditText, passwordEditText;
    private Button loginBtn;
    private ProgressDialog progressDialog;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    boolean shownpass = false;

    String select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        select = intent.getStringExtra("select");


        emailEditText = findViewById(R.id.ed_email);
        passwordEditText = findViewById(R.id.ed_password);
        loginBtn = findViewById(R.id.t_login);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseUsers.keepSynced(true);

        // here is the login fragment in which get user email and password and match them with the sotred email amd passwords
        // in firebase and if its matched then user login successfully. and open main screen if login successfully


        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Signing you in...");

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

        progressDialog.show();

        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            //if Email Address is Invalid..

            progressDialog.dismiss();
            emailEditText.setError("Email is not valid. Make sure no spaces and special characters are included");
            emailEditText.requestFocus();
        } else {

            mAuth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {


                    if (task.isSuccessful()) {


                        progressDialog.dismiss();


                        Intent intent = new Intent(LoginActivity.this, RemindersActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        Toast.makeText(LoginActivity.this, "Log in Successfully", Toast.LENGTH_SHORT).show();

                        SharedPreferences pref = getSharedPreferences("MyPref", 0);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("login", "yes");
                        editor.commit();


//
//
//
//
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(intent);
//                        finish();


                    } else {

                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }


    }

    public void registerClick(View view) {
        Intent intent = new Intent(LoginActivity.this, UserSignup.class);
        startActivity(intent);

    }

    public void forgotClick(View view) {


        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailAddress = emailEditText.getText().toString();

        if (!emailAddress.isEmpty()) {

            auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Email send for Reset Password", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Please Enter Mail First", Toast.LENGTH_SHORT).show();
        }


    }

    public void shownPassFirst(View view) {
        if (!shownpass) {
            passwordEditText.setTransformationMethod(new HideReturnsTransformationMethod());
            shownpass = true;

        } else {
            passwordEditText.setTransformationMethod(new PasswordTransformationMethod());

            shownpass = false;
        }
    }
}