package com.cibiod.app.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;

import com.cibiod.app.R;
import com.cibiod.app.Utils.u;
import com.google.android.material.navigation.NavigationView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        u.sharedTransFix(getWindow(), R.color.blue);
        toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.cutom_toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayoutSettings);
        NavigationView navigationView = findViewById((R.id.navViewSettings));
        u.setupToolbar(this, drawerLayout, navigationView, toolbar, "Settings", 170);
    }
}