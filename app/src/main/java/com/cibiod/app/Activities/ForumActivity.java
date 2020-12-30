package com.cibiod.app.Activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cibiod.app.CustomViews.CustomSpinner;
import com.cibiod.app.R;
import com.cibiod.app.Utils.u;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ForumActivity extends AppCompatActivity {

    private EditText nameText, ageText;
    private CustomSpinner genderDropdown;
    private ImageView closeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        u.sharedTransFix(getWindow(), R.color.transparent);
        Window window = getWindow();
        window.setSharedElementEnterTransition(enterTransition());
        window.setSharedElementReturnTransition(returnTransition());
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.orangeDark));

        nameText = findViewById(R.id.addNameEdit);
        final ImageView nameRect = findViewById(R.id.addNameRect);

        ageText = findViewById(R.id.addAgeEdit);
        final ImageView ageRect = findViewById(R.id.addAgeRect);

        u.setupEditText(this, nameText, nameRect, "NAME", false);
        u.setupEditText(this, ageText, ageRect, "AGE", false);

        genderDropdown = findViewById(R.id.addGenderSpinner);
        final ImageView genderPopupBg = findViewById(R.id.addGenderPopupBg);
        final ImageView genderDropdownArrow = findViewById(R.id.addGenderDropdownArrow);

        u.setupDropdown(this, genderDropdown, R.array.Gender, genderPopupBg, genderDropdownArrow);

        ObjectAnimator oa = ObjectAnimator.ofFloat(findViewById(R.id.addRoot), "alpha", 0, 1).setDuration(300);
        oa.setStartDelay(1000);
        oa.setInterpolator(new AccelerateInterpolator());
        oa.start();

        closeIcon = findViewById(R.id.closeAddButton);
        final RotateAnimation aRotate = new RotateAnimation(0, 135,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        aRotate.setStartOffset(1300);
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
                if (nameVal.isEmpty()) {
                    nameText.setError("Name is Empty");
                    return;
                }
                if (ageVal.isEmpty()) {
                    ageText.setError("Age is Empty");
                    return;
                }
                if (genderVal.equals("GENDER")) {
                    Toast.makeText(ForumActivity.this, "Select a gender", Toast.LENGTH_LONG).show();
                    return;
                }

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference dbRef = database.getReference("users");
                final Query idGetter = dbRef.child(u.getPref(ForumActivity.this, "id")).child("patients").limitToLast(1).orderByKey();
                idGetter.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        idGetter.removeEventListener(this);
                        dbRef.removeEventListener(this);
                        String id = null;
                        if (snapshot.hasChildren()) {
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                int temp = Integer.parseInt(Objects.requireNonNull(postSnapshot.getKey()));
                                id = Integer.toString(++temp);
                            }
                        } else
                            id = Integer.toString(1);

                        addToDb(dbRef, nameVal, ageVal, genderVal, id);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Firebase Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    private void goBack() {
        RotateAnimation aRotate = new RotateAnimation(135, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        aRotate.setDuration(600);
        aRotate.setFillAfter(true);
        aRotate.setInterpolator(new AnticipateOvershootInterpolator());
        closeIcon.startAnimation(aRotate);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ForumActivity.this.onBackPressed();
            }
        };
        new Handler().postDelayed(r, 800);
    }

    void addToDb(DatabaseReference dbRef, String name, String age, String gender, String id) {
        String[] split = name.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap).append(" ");
        }
        builder.deleteCharAt(builder.length() - 1);
        name = builder.toString();
        final String userId = u.getPref(this, "id");
        dbRef.child(userId).child("patients").child(id).child("name").setValue(name);
        dbRef.child(userId).child("patients").child(id).child("age").setValue(age);
        dbRef.child(userId).child("patients").child(id).child("gender").setValue(gender);
        final String finalName = name;
        dbRef.child(userId).child("patients").child(id).child("lowercase").setValue(name.toLowerCase()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(ForumActivity.this);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, userId);
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, finalName);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                Toast.makeText(ForumActivity.this, "Added to Database", Toast.LENGTH_LONG).show();
                goBack();
            }
        });
    }

    private Transition enterTransition() {
        ChangeBounds bounds = new ChangeBounds();
        bounds.setInterpolator(new AccelerateDecelerateInterpolator());
        bounds.setDuration(600);

        return bounds;
    }

    private Transition returnTransition() {
        ChangeBounds bounds = new ChangeBounds();
        bounds.setInterpolator(new AccelerateDecelerateInterpolator());
        bounds.setDuration(600);

        return bounds;
    }
}