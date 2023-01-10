package com.medicine.reminderapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView listRecyclerView1;
    private LinearLayoutManager listLayoutManager;
    ArrayList<UsersListModel> arrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot

                        for (DataSnapshot child: dataSnapshot.getChildren()){
                            String name = child.getKey();
                            String uname= dataSnapshot.child(name).child("Name").getValue().toString();
                            String email= dataSnapshot.child(name).child("Email").getValue().toString();
                            String phone= dataSnapshot.child(name).child("Phone").getValue().toString();
                            String address= dataSnapshot.child(name).child("address").getValue().toString();

                            arrayList.add(new UsersListModel(name,uname,phone,email,address));



                        }



                        listRecyclerView1 = findViewById(R.id.recyclerview_list_users);
                        listRecyclerView1.setHasFixedSize(true);
                        listLayoutManager = new LinearLayoutManager(MainActivity.this);
                        UsersListAdapter usersListAdapter = new UsersListAdapter(arrayList,MainActivity.this);
                        listRecyclerView1.setAdapter(usersListAdapter);
                        listRecyclerView1.setLayoutManager(listLayoutManager);
                        usersListAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

    }
}