package com.cibiod.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class patientEntry extends AppCompatActivity {
    private DatabaseReference db;
    private ArrayList<patientDetails> mPatients = new ArrayList<patientDetails>();
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_entry);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        db = database.getReference("patients");

        FloatingActionButton fab = findViewById(R.id.newPatientFab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity("");
            }
        });

        Button button = findViewById(R.id.newPatientButton);
        button.setAlpha(0);
        button.setClickable(false);

        rv = findViewById(R.id.recyclerView);

        displayRecent();

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equals(""))
                    searchDB(newText);
                else
                {
                    Collections.reverse(mPatients);
                    changeAdapter(mPatients);
                }
                return true;
            }
        });
    }

    private void displayRecent()
    {
        Query lastEntries = db.limitToLast(8);

        lastEntries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    patientDetails temp = new patientDetails(dataSnapshot.child("name").getValue().toString(),
                            dataSnapshot.getKey(),
                            dataSnapshot.child("address").getValue().toString(),
                            dataSnapshot.child("gender").getValue().toString(),
                            dataSnapshot.child("age").getValue().toString());

                    mPatients.add(temp.getPatientDetails());
                }
                changeAdapter(mPatients);
                db.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Firebase Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void changeAdapter(ArrayList<patientDetails> mPatients) {
        Collections.reverse(mPatients);

        patientAdapter adapter = new patientAdapter(mPatients);

        rv.setAdapter(adapter);

        rv.setLayoutManager(new LinearLayoutManager(patientEntry.this));
    }

    private void searchDB(final String s)
    {
        final Query search = db.orderByChild("name").startAt(s).endAt(s+"\uf8ff");

        search.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                {
                    ArrayList<patientDetails> filter = new ArrayList<patientDetails>();
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                        patientDetails temp = new patientDetails(
                                postSnapshot.child("name").getValue().toString(),
                        postSnapshot.getKey(),
                        postSnapshot.child("address").getValue().toString(),
                        postSnapshot.child("gender").getValue().toString(),
                        postSnapshot.child("age").getValue().toString());
//
                        filter.add(temp);
                    }
                    changeAdapter(filter);
                    db.removeEventListener(this);
                }

                else
                    showButton(s);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"Firebase Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showButton(final String s)
    {
        changeAdapter(new ArrayList<patientDetails>());
        Button button = findViewById(R.id.newPatientButton);
        String temp = "No results\n Add new patient with name " + s;
        button.setText(temp);

        button.setAlpha(1);
        button.setClickable(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(s);
            }
        });
    }

    private void startActivity(String s)
    {
        Intent intent = new Intent(this,forumActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("name",s);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(intent, bundle);
        finish();
    }
}