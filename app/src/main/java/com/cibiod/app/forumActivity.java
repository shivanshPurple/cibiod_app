package com.cibiod.app;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class forumActivity extends AppCompatActivity{

    private boolean ifPressed = false;
    private DatabaseReference dbRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        final EditText addressEditText = findViewById(R.id.newPatientAddress);
        final EditText nameEditText = findViewById(R.id.newPatientName);
        final EditText ageEditText = findViewById(R.id.newPatientAge);
        final Spinner genderSpinner = findViewById(R.id.newPatientGender);

        Intent intent = getIntent();
        String str = intent.getStringExtra("name");
        if(!str.equals(""))
            nameEditText.setText(str);

        final Button submitButton = findViewById(R.id.addTower);
        final CardView buttonCard = findViewById(R.id.buttonCard);

        final ImageView circle = findViewById(R.id.circleButton2);
        final Animation zoomIn = AnimationUtils.loadAnimation(this,R.anim.zoomin);
        final Animation fadeOut = AnimationUtils.loadAnimation(this,R.anim.fadeout);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ifErrors = false;

                EditText[] notEmptyEditText = {ageEditText,nameEditText};

                for(EditText t : notEmptyEditText)
                {
                    if (TextUtils.isEmpty(t.getText().toString().trim()))
                    {
                        t.setError("This can't be left empty");
                        ifErrors = true;
                    }
                }

                if(!ifErrors & !ifPressed)
                {
                    ifPressed = true;
                    buttonCard.startAnimation(fadeOut);
                    circle.startAnimation(zoomIn);

                    ArrayList<String> d = new ArrayList<>();
                    d.add(nameEditText.getText().toString());
                    d.add(ageEditText.getText().toString());
                    d.add(addressEditText.getText().toString());
                    d.add(genderSpinner.getSelectedItem().toString());
                    submit(d);
                }

            }
        });
    }

    private void submit(final ArrayList<String> data)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("patients");

        Query last = dbRef.limitToLast(1).orderByChild("value");

        last.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    String id;
                    if(postSnapshot.hasChildren()){
                        int temp = Integer.parseInt(Objects.requireNonNull(postSnapshot.getKey()));
                        temp++;
                        id = Integer.toString(temp);
                        dbRef.removeEventListener(this);
                    }

                    else
                        id = Integer.toString(1);

                    addLocActivity(data,id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Firebase Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addLocActivity(final ArrayList<String> data, String patientKey) {
        data.add(patientKey);

        final ArrayList<String> d = data;

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(forumActivity.this, afterSubmissionActivity.class);
                intent.putExtra("data", d);

                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(forumActivity.this,android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                startActivity(intent, bundle);
            }
        };

        handler.postDelayed(r,800);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
