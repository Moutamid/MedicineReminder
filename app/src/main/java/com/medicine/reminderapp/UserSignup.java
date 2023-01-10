package com.medicine.reminderapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserSignup extends AppCompatActivity {
    EditText email, password, nameFull, phone, confpassword,address;
    String emailStr, passwordStr, fullName, phoneSt,confpass,stAddress;
    Button signUpBt;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    StorageReference storageReference;
    private ProgressDialog mDialog;
    CheckBox checkBox;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    boolean shownpass=  false;
    boolean shownpass2=  false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        email = findViewById(R.id.ed_email);
        password = findViewById(R.id.ed_password);
        confpassword = findViewById(R.id.ed_confirm_password);
        nameFull = findViewById(R.id.ed_name);
        address = findViewById(R.id.ed_address);
        phone = findViewById(R.id.ed_phone);
        signUpBt = findViewById(R.id.bt_signup);
        checkBox=findViewById(R.id.checkBox);
        storageReference = FirebaseStorage.getInstance().getReference();




        nameFull.setFilters(new InputFilter[] {
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence cs, int start,
                                               int end, Spanned spanned, int dStart, int dEnd) {
                        // TODO Auto-generated method stub
                        if(cs.equals("")){ // for backspace
                            return cs;
                        }
                        if(cs.toString().matches("[a-zA-Z ]+")){
                            return cs;
                        }
                        return "";
                    }
                }
        });
        //  in this fragment get all the data from user and make a new account  for user.

        mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.setMessage("Signing you in...");
        mAuth = FirebaseAuth.getInstance();

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference();
        mDatabaseUsers.keepSynced(true);

        signUpBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStatusOfEditTexts();
            }
        });

    }

    private void checkStatusOfEditTexts() {

        // Getting strings from edit texts

        emailStr = email.getText().toString();
        passwordStr = password.getText().toString();
        fullName = nameFull.getText().toString();
        phoneSt = phone.getText().toString();
        stAddress = address.getText().toString();

        confpass = confpassword.getText().toString();



        // Checking if Fields are empty or not
        if (!TextUtils.isEmpty(fullName) && !TextUtils.isEmpty(emailStr) && !TextUtils.isEmpty(passwordStr) && passwordStr.length() >=8 && !TextUtils.isEmpty(phoneSt)  && !TextUtils.isEmpty(stAddress) && !TextUtils.isEmpty(confpass)) {


            // Signing up user
            if (passwordStr.equals(confpass)) {
                if (checkBox.isChecked()) {
                    signUpUserWithNameAndPassword();
                }
                else {
                    Toast.makeText(this, "Please agree terms and condition", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "Password not matched", Toast.LENGTH_SHORT).show();
            }

        } else if (TextUtils.isEmpty(fullName)) {


            nameFull.setError("Please provide your Name");
            nameFull.requestFocus();

        }  else if (TextUtils.isEmpty(phoneSt)) {


            phone.setError("Please provide a phone number");
            phone.requestFocus();


        }
        else if (TextUtils.isEmpty(emailStr)) {


            email.setError("Please provide a email");
            email.requestFocus();


        }

        else if (TextUtils.isEmpty(stAddress)) {


            address.setError("Please provide address");
            address.requestFocus();


        }


        else if (TextUtils.isEmpty(passwordStr)) {

            password.setError("Please provide a password");
            password.requestFocus();

        }
        else if (passwordStr.length() < 8) {

            password.setError("Minimum length should be 8");
            password.requestFocus();

        }
        else if (TextUtils.isEmpty(confpass)) {

            confpassword.setError("Please provide confirm password");
            confpassword.requestFocus();

        }

    }


    private void signUpUserWithNameAndPassword() {
        mDialog.show();
        if (!emailStr.matches(emailPattern)) {
            //if Email Address is Invalid..

            mDialog.dismiss();
            email.setError("Please enter a valid email ");
            email.requestFocus();
        } else {

            mAuth.createUserWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        addUserDetailsToDatabase();

                    } else {

                        mDialog.dismiss();
                        Toast.makeText(UserSignup.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void addUserDetailsToDatabase() {

        SharedPreferences pref = getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();

        DatabaseReference mRef = mDatabaseUsers.child("users").child(mAuth.getCurrentUser().getUid());
        mRef.child("Name").setValue(fullName);
        mRef.child("Email").setValue(emailStr);
        mRef.child("Password").setValue(passwordStr);
        mRef.child("Phone").setValue(phoneSt);
        mRef.child("address").setValue(stAddress);


        mDialog.dismiss();
        Intent intent = new Intent(UserSignup.this, RemindersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();


        editor.putString("login", "yes");
        editor.commit();
        Toast.makeText(

                        UserSignup.this, "You are signed up!", Toast.LENGTH_SHORT).

                show();
    }


    public void loginClick(View view) {
        Intent intent = new Intent(UserSignup.this, LoginActivity.class);
        startActivity(intent);
    }

    public void backButtonClick(View view) {
        finish();
    }

    public void shownPassFirst(View view) {
        if(!shownpass){
            password.setTransformationMethod(new HideReturnsTransformationMethod());
            shownpass=true;

        } else{
            password.setTransformationMethod(new PasswordTransformationMethod());

            shownpass=false;
        }
    }

    public void shownPassSecond(View view) {
        if(!shownpass2){
            confpassword.setTransformationMethod(new HideReturnsTransformationMethod());
            shownpass2=true;

        } else{
            confpassword.setTransformationMethod(new PasswordTransformationMethod());
            shownpass2=false;
        }
    }
}
