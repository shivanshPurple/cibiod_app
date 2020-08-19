package com.cibiod.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

public class LogoScreen extends AppCompatActivity {
//    private ImageView circle;
    private boolean isPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_screen);

        ImageButton logo = findViewById(R.id.logo);

        final Animation fadeIn = AnimationUtils.loadAnimation(this,R.anim.fadein);

        logo.startAnimation(fadeIn);
//        circle.startAnimation(fadeIn2);

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                circle.clearAnimation();
//                circle.startAnimation(zoomIn);
                isPressed = true;

                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        startApp();
                    }
                }, 1000);
            }
        });

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            if(!isPressed)
            {
                heartbeatAnim();
                handler.postDelayed(this, 1500);
            }
            }
        };

        handler.postDelayed(runnable, 1000);
    }

    private void heartbeatAnim()
    {
        Animation heartBeat = AnimationUtils.loadAnimation(this,R.anim.blink_anim);
//        circle.startAnimation(heartBeat);

    }

    private void startApp()
    {
        Intent intent = new Intent(this,patientEntry.class);

        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(intent, bundle);
    }
}