package com.cibiod.app.Activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.transition.Transition;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cibiod.app.Adapters.TestAdapter;
import com.cibiod.app.Callbacks.RecyclerCallback;
import com.cibiod.app.Objects.PatientObject;
import com.cibiod.app.Objects.TestObject;
import com.cibiod.app.R;
import com.cibiod.app.Utils.BottomAppBarCutCornersTopEdge;
import com.cibiod.app.Utils.u;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PatientActivity extends AppCompatActivity implements RecyclerCallback {
    private DatabaseReference db;
    private final ArrayList<TestObject> mTests = new ArrayList<>();

    private PatientObject patient;
    private RecyclerView rv;
    private String id;
    private ViewStub viewStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementEnterTransition(customTransition());
        getWindow().setSharedElementReturnTransition(customTransition());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);
        Toolbar toolbar = findViewById(R.id.toolbarPatient);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.cutom_toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayoutPatient);
        NavigationView navigationView = findViewById(R.id.navViewPatient);

        u.setupToolbar(this, drawerLayout, navigationView, toolbar, "Patient", 170);
        viewStub = findViewById(R.id.viewStubPatient);
        u.setViewStub(this, viewStub, "loading");

        BottomAppBar bar = findViewById(R.id.bottomBarPatient);

        BottomAppBarTopEdgeTreatment topEdge = new BottomAppBarCutCornersTopEdge(
                bar.getFabCradleMargin(),
                bar.getFabCradleRoundedCornerRadius(),
                bar.getCradleVerticalOffset());

        MaterialShapeDrawable bottomBarBackground = (MaterialShapeDrawable) bar.getBackground();
        bottomBarBackground.setShapeAppearanceModel(
                bottomBarBackground.getShapeAppearanceModel()
                        .toBuilder()
                        .setTopRightCorner(CornerFamily.ROUNDED, 75)
                        .setTopLeftCorner(CornerFamily.ROUNDED, 75)
                        .setTopEdge(topEdge)
                        .build());

        id = u.getPref(this, "id");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        db = database.getReference("users");

        patient = (PatientObject) getIntent().getSerializableExtra("patientObject");

        rv = findViewById(R.id.recyclerViewHome);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                displayRecentTests(patient);
            }
        }, 600);

        TextView patientName = findViewById(R.id.patientName);
        patientName.setText(patient.getName());
        TextView patientId = findViewById(R.id.patientId);
        patientId.setText("#" + patient.getId());
        TextView patientAge = findViewById(R.id.patientAge);
        patientAge.setText(patient.getAge());
        TextView patientGender = findViewById(R.id.patientGender);
        patientGender.setText(patient.getGender());
        ImageView patientImg = findViewById(R.id.patientImg);
        setPatientImage(patientImg);
    }

    private void setPatientImage(final ImageView patientImg) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference dataRef = storage.getReferenceFromUrl(patient.getPhotoUrl());

        String imageFileName = patient.getName() + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        final File file = new File(pictureImagePath);

        dataRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap imageBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                patientImg.setImageBitmap(imageBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(PatientActivity.this, exception.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private Transition customTransition() {
        MaterialContainerTransform mct = new MaterialContainerTransform();
        mct.setFadeMode(MaterialContainerTransform.FADE_MODE_OUT);
        mct.setScrimColor(Color.DKGRAY);
        mct.setAllContainerColors(Color.WHITE);
        mct.setElevationShadowEnabled(true);
        mct.setStartElevation(8);
        mct.setEndElevation(16);
        return mct.addTarget(R.id.drawerLayoutPatient).setDuration(600).setInterpolator(new AccelerateDecelerateInterpolator());
    }

    private void displayRecentTests(PatientObject patient) {
        final Query lastEntries = db.child(id).child("patients").child(patient.getId()).limitToLast(10);

        lastEntries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                u.setViewStub(PatientActivity.this, viewStub, "clear");
                db.removeEventListener(this);
                lastEntries.removeEventListener(this);
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
                if (mTests.size() == 0)
                    u.setViewStub(PatientActivity.this, viewStub, "empty");

                changeTestAdapter(mTests);
                db.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Firebase Connection Error", Toast.LENGTH_LONG).show();
                u.setViewStub(PatientActivity.this, viewStub, "empty");
            }
        });
    }

    private void changeTestAdapter(ArrayList<TestObject> mTests) {
        Collections.reverse(mTests);
        TestAdapter adapter = new TestAdapter(this, mTests, this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onItemClick(int pos, View card) {
        Intent intent = new Intent(this, BluetoothActivity.class);
        intent.putExtra("testObject", mTests.get(pos));
        intent.putExtra("from", "cloud");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, card, "testContainer");
        startActivity(intent, options.toBundle());
    }

    public void startNewTest(View v) {
        Intent intent = new Intent(this, BluetoothActivity.class);
        intent.putExtra("patientObject", patient);
        intent.putExtra("from", "local");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        startActivity(intent, options.toBundle());
    }

    @Override
    protected void onResume() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                displayRecentTests(patient);
            }
        }, 600);
        super.onResume();
    }
}