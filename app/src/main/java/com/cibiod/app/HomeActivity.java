package com.cibiod.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements PatientRecyclerCallback {
    private DatabaseReference db;
    private ArrayList<PatientObject> mPatients = new ArrayList<>();

    private RecyclerView rv;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        u.sharedTransFix(getWindow(), R.color.blue);
        toolbar = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        appBarLayout = findViewById(R.id.appBarLayout);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayoutHome);
        NavigationView navigationView = findViewById((R.id.navViewHome));

        u.setupToolbar(this, drawerLayout, navigationView, toolbar, "Home", 55);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        db = database.getReference("users");

        rv = findViewById(R.id.recyclerViewHome);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        displayRecent();


    }

    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) menuItem.getActionView();

        ImageView closeButton = searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.clearFocus();
                toolbar.collapseActionView();
                appBarLayout.setExpanded(true);
                displayRecent();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                appBarLayout.setExpanded(false);
                if (!newText.equals(""))
                    searchDB(newText);
                else {
                    Collections.reverse(mPatients);
                    changeAdapter(mPatients);
                }
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.searchView) {
            TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.toolbarHome));
            MenuItemCompat.expandActionView(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchDB(String s) {
        String lowercaseS = s.toLowerCase();
        final Query search = db.child("q@w/patients").orderByChild("lowercase").startAt(lowercaseS).endAt(lowercaseS + "\uf8ff").limitToLast(10);

        search.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPatients.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        PatientObject temp = new PatientObject(postSnapshot.child("name").getValue().toString(),
                                postSnapshot.getKey(),
                                postSnapshot.child("gender").getValue().toString(),
                                postSnapshot.child("age").getValue().toString());
                        mPatients.add(temp);
                    }
                } else {
                    mPatients.clear();
                }
                changeAdapter(mPatients);
                db.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Firebase Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayRecent() {
        Query lastEntries = db.child("q@w").child("patients").limitToLast(10);

        lastEntries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mPatients.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PatientObject temp = new PatientObject(Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString(),
                            dataSnapshot.getKey(),
                            Objects.requireNonNull(dataSnapshot.child("gender").getValue()).toString(),
                            Objects.requireNonNull(dataSnapshot.child("age").getValue()).toString());
                    mPatients.add(temp);
                }
                changeAdapter(mPatients);
                db.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Firebase Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void changeAdapter(ArrayList<PatientObject> mPatients) {
        Collections.reverse(mPatients);
        PatientAdapter adapter = new PatientAdapter(mPatients, this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    public void startAddActivity(View v) {
        Intent intent = new Intent(this, ForumActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(findViewById(R.id.uploadTestButton), "orangeBg"),
                Pair.create(findViewById(R.id.uploadTestIcon), "plusIcon"));
        startActivity(intent, options.toBundle());
    }

    @Override
    public void OnPatientClickListener(int pos) {
        Intent intent = new Intent(this, PatientActivity.class);
        intent.putExtra("patientObject", mPatients.get(pos));

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(findViewById(R.id.homeBottomBarLayout), "patientBottomBar"),
                Pair.create((View) rv, "dataRecyclerView"));
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        u.alert();
        displayRecent();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
//        u.alert();
//        searchView.clearFocus();
//        toolbar.collapseActionView();
//        appBarLayout.setExpanded(true);
//        displayRecent();
    }
}