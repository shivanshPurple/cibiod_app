package com.cibiod.app.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cibiod.app.Objects.PatientObject;
import com.cibiod.app.R;
import com.cibiod.app.Callbacks.RecyclerCallback;
import com.cibiod.app.Adapters.TestAdapter;
import com.cibiod.app.Objects.TestObject;
import com.cibiod.app.Utils.u;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class PatientActivity extends AppCompatActivity implements RecyclerCallback {
    private DatabaseReference db;
    private ArrayList<TestObject> mTests = new ArrayList<>();

    private PatientObject patient;
    private RecyclerView rv;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);
        u.sharedTransFix(getWindow(), R.color.blue);
        Toolbar toolbar = findViewById(R.id.toolbarPatient);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.cutom_toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayoutPatient);
        NavigationView navigationView = findViewById(R.id.navViewPatient);

        u.setupToolbar(this, drawerLayout, navigationView, toolbar, "Patient", 170);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        db = database.getReference("users");

        patient = (PatientObject) getIntent().getSerializableExtra("patientObject");

        rv = findViewById(R.id.recyclerViewHome);
        displayRecentTests(patient);

        TextView patientName = findViewById(R.id.patientName);
        patientName.setText(patient.getName());
        TextView patientId = findViewById(R.id.patientId);
        patientId.setText("#" + patient.getId());
        TextView patientAge = findViewById(R.id.patientAge);
        patientAge.setText(patient.getAge());
        TextView patientGender = findViewById(R.id.patientGender);
        patientGender.setText(patient.getGender());
    }

    private void displayRecentTests(PatientObject patient) {

        Query lastEntries = db.child("q@w").child("patients").child(patient.getId()).limitToLast(10);

        lastEntries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mTests.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (u.isNumeric(dataSnapshot.getKey())) {
                        TestObject temp = new TestObject(dataSnapshot.getKey(),
                                dataSnapshot.child("date").getValue().toString(),
                                dataSnapshot.child("time").getValue().toString(),
                                dataSnapshot.child("data").getValue().toString(),
                                false);
                        mTests.add(temp);
                    }
                }
                changeTestAdapter(mTests);
                db.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Firebase Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void changeTestAdapter(ArrayList<TestObject> mTests) {
        Collections.reverse(mTests);
        TestAdapter adapter = new TestAdapter(mTests, this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onItemClick(int pos) {
        Intent intent = new Intent(this, BluetoothActivity.class);
        intent.putExtra("testObject", mTests.get(pos));
        intent.putExtra("from", "cloud");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(findViewById(R.id.patientBottomBar), "bluetoothBottomBar"));
        startActivity(intent, options.toBundle());
    }

    public void startNewTest(View v)
    {
        Intent intent = new Intent(this, BluetoothActivity.class);
        intent.putExtra("patientObject", patient);
        intent.putExtra("from", "local");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(findViewById(R.id.patientBottomBar), "bluetoothBottomBar"));
        startActivity(intent, options.toBundle());
    }
}