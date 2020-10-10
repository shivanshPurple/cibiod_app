package com.cibiod.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class LogoScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_screen);

        ImageButton logo = findViewById(R.id.logo);

        final Animation fadeIn = AnimationUtils.loadAnimation(this,R.anim.fadein);

        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);

        logo.startAnimation(fadeIn);
        startApp();
    }

    private void startApp()
    {
        Intent intent = new Intent(this,IntroScreen.class);

        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(intent, bundle);
    }
}