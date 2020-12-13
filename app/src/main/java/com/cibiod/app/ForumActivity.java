package com.cibiod.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ForumActivity extends AppCompatActivity {

    private EditText nameText,ageText;
    private CustomSpinner genderDropdown;
    private ImageView closeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        u.sharedTransFix(getWindow(), R.color.orange);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.orangeDark));

        nameText = findViewById(R.id.addNameEdit);
        final ImageView nameRect = findViewById(R.id.addNameRect);

        ageText = findViewById(R.id.addAgeEdit);
        final ImageView ageRect = findViewById(R.id.addAgeRect);

        u.setupEditText(this,nameText,nameRect,"NAME",false);
        u.setupEditText(this,ageText,ageRect,"AGE",false);

        genderDropdown = findViewById(R.id.addGenderSpinner);
        final ImageView genderPopupBg = findViewById(R.id.addGenderPopupBg);
        final ImageView genderDropdownArrow = findViewById(R.id.addGenderDropdownArrow);

        u.setupDropdown(this,genderDropdown,R.array.Gender,genderPopupBg,genderDropdownArrow);

        closeIcon = findViewById(R.id.closeAddButton);
        final RotateAnimation aRotate = new RotateAnimation(0, 135,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        aRotate.setStartOffset(0);
        aRotate.setDuration(600);
        aRotate.setFillAfter(true);
        aRotate.setInterpolator(new AnticipateOvershootInterpolator());
        closeIcon.startAnimation(aRotate);

        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nameVal = nameText.getText().toString().toLowerCase();
                final String ageVal = ageText.getText().toString();
                final String genderVal = genderDropdown.getSelectedItem().toString();
                if(nameVal.isEmpty())
                {
                    nameText.setError("Name is Empty");
                    return;
                }
                if(ageVal.isEmpty())
                {
                    ageText.setError("Age is Empty");
                    return;
                }
                if(genderVal.equals("GENDER"))
                {
                    Toast.makeText(ForumActivity.this,"Select a gender",Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences prefs = getSharedPreferences("applicationVariables", Context.MODE_PRIVATE);
                String id = prefs.getString("id","users");

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference dbRef = database.getReference("users");
                final Query idGetter = dbRef.child("q@w").child("patients").limitToLast(1).orderByKey();

                idGetter.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        idGetter.removeEventListener(this);
                        dbRef.removeEventListener(this);
                        for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                            String id;
                            if(postSnapshot.hasChildren()){
                                int temp = Integer.parseInt(Objects.requireNonNull(postSnapshot.getKey()));
                                id = Integer.toString(++temp);
                            }
                            else {
                                id = Integer.toString(1);
                            }
                            addToDb(dbRef,"q@w",nameVal,ageVal,genderVal,id);
                            goBack();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(),"Firebase Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    private void goBack() {
        RotateAnimation aRotate = new RotateAnimation(135, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        aRotate.setStartOffset(0);
        aRotate.setDuration(600);
        aRotate.setFillAfter(true);
        aRotate.setInterpolator(new AnticipateOvershootInterpolator());
        closeIcon.startAnimation(aRotate);
        Runnable r = new Runnable() {
            @Override
            public void run() {

//                startActivity(intent, options.toBundle());
                ForumActivity.this.onBackPressed();
            }
        };
        new Handler().postDelayed(r,600);
    }

    void addToDb(DatabaseReference dbRef,String userId, String name, String age, String gender, String id)
    {
        dbRef.child(userId).child("patients").child(id).child("name").setValue(name);
        dbRef.child(userId).child("patients").child(id).child("age").setValue(age);
        dbRef.child(userId).child("patients").child(id).child("gender").setValue(gender);
        dbRef.child(userId).child("patients").child(id).child("lowercase").setValue(name.toLowerCase());
    }
}