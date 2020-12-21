package com.cibiod.app.Activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.cibiod.app.Adapters.PatientAdapter;
import com.cibiod.app.Callbacks.RecyclerCallback;
import com.cibiod.app.Objects.PatientObject;
import com.cibiod.app.R;
import com.cibiod.app.Utils.BottomAppBarCutCornersTopEdge;
import com.cibiod.app.Utils.u;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeActivity extends AppCompatActivity implements RecyclerCallback {
    private DatabaseReference db;
    private ArrayList<PatientObject> mPatients = new ArrayList<>();
    private PatientAdapter recyclerAdapter;

    private RecyclerView rv;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementsUseOverlay(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        u.sharedTransFix(getWindow(), R.color.blue);
        toolbar = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.cutom_toolbar);

        appBarLayout = findViewById(R.id.appBarLayout);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayoutHome);
        NavigationView navigationView = findViewById((R.id.navViewHome));
        u.setupToolbar(this, drawerLayout, navigationView, toolbar, "Home", 55);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        db = database.getReference("users");

        BottomAppBar bar = findViewById(R.id.bottomBarHome);

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

        rv = findViewById(R.id.recyclerViewHome);

        recyclerAdapter = new PatientAdapter(this, mPatients, this);
        rv.setAdapter(recyclerAdapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        displayRecent();
    }

    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.searchView);
        searchView = (SearchView) searchMenuItem.getActionView();

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
                else
                    displayRecent();
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.clearFocus();
                appBarLayout.setExpanded(true);
                displayRecent();
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
                recyclerAdapter.notifyItemRangeRemoved(0, mPatients.size());
                mPatients.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        PatientObject temp = new PatientObject(postSnapshot.child("name").getValue().toString(),
                                postSnapshot.getKey(),
                                postSnapshot.child("gender").getValue().toString(),
                                postSnapshot.child("age").getValue().toString());
                        updateAdapter(temp);
                    }
                } else {
                    mPatients.clear();
                }
                db.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Firebase Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayRecent() {
        Query lastEntries = db.child("q@w").child("patients").limitToLast(25);
        lastEntries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recyclerAdapter.notifyItemRangeRemoved(0, mPatients.size());
                mPatients.clear();
                int k = 1;
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                List<DataSnapshot> dataSnapshotList = new ArrayList<>();
                while (iterator.hasNext()) {
                    dataSnapshotList.add(iterator.next());
                }
                Collections.reverse(dataSnapshotList);
                for (DataSnapshot dataSnapshot : dataSnapshotList) {
                    final PatientObject temp = new PatientObject(dataSnapshot.child("name").getValue().toString(),
                            dataSnapshot.getKey(),
                            dataSnapshot.child("gender").getValue().toString(),
                            dataSnapshot.child("age").getValue().toString());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateAdapter(temp);
                        }
                    }, 300 * k);
                    k++;
                }
                db.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Firebase Connection Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateAdapter(PatientObject temp) {
        mPatients.add(temp);
        recyclerAdapter.notifyItemInserted(mPatients.size() - 1);
    }

    public void startForumActivity(View v) {
        Intent intent = new Intent(this, ForumActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, findViewById(R.id.fabBg), "containerTransform");
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onItemClick(int pos, View card) {
        Intent intent = new Intent(this, PatientActivity.class);
        intent.putExtra("patientObject", mPatients.get(pos));
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,card, "patientContainer");
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        displayRecent();
        super.onConfigurationChanged(newConfig);
    }
}