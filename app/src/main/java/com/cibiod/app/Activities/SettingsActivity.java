package com.cibiod.app.Activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.cibiod.app.R;
import com.cibiod.app.Utils.BottomAppBarCutCornersTopEdge;
import com.cibiod.app.Utils.VolumeObserver;
import com.cibiod.app.Utils.u;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.rey.material.widget.Slider;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

public class SettingsActivity extends AppCompatActivity {
    private VolumeObserver volumeObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        u.sharedTransFix(getWindow(), R.color.blue);
        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.cutom_toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayoutSettings);
        NavigationView navigationView = findViewById((R.id.navViewSettings));
        u.setupToolbar(this, drawerLayout, navigationView, toolbar, "Settings", 170);

        BottomAppBar bar = findViewById(R.id.bottomBarSettings);

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
        //        region slider setup
        Slider slider = findViewById(R.id.slider);
        final AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        final int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        slider.setPosition((float) currentVolume / maxVolume, true);

        slider.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (newPos * maxVolume), AudioManager.FLAG_PLAY_SOUND + AudioManager.FLAG_SHOW_UI);
            }
        });

        volumeObserver = new VolumeObserver(this, slider, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, volumeObserver);
//        endregion

        findViewById(R.id.cibiodLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimatorSet as = new AnimatorSet();
                as.playSequentially(ObjectAnimator.ofArgb(v, "backgroundColor", Color.WHITE, Color.LTGRAY).setDuration(200)
                        , ObjectAnimator.ofArgb(v, "backgroundColor", Color.LTGRAY, Color.WHITE).setDuration(200));
                as.setInterpolator(new DecelerateInterpolator());
                as.start();
                String url = "http://cibiod.in/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        findViewById(R.id.purpleLink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimatorSet as = new AnimatorSet();
                as.playSequentially(ObjectAnimator.ofArgb(v, "backgroundColor", Color.WHITE, Color.LTGRAY).setDuration(200)
                        , ObjectAnimator.ofArgb(v, "backgroundColor", Color.LTGRAY, Color.WHITE).setDuration(200));
                as.setInterpolator(new DecelerateInterpolator());
                as.start();
                Uri uri = Uri.parse("http://instagram.com/_u/shivansh.purple");
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                likeIng.setPackage("com.instagram.android");
                startActivity(likeIng);
            }
        });

        findViewById(R.id.logoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                unregister from shared prefs
            }
        });

        findViewById(R.id.fabSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.finish();
            }
        });
    }



    @Override
    protected void onDestroy() {
        getApplicationContext().getContentResolver().unregisterContentObserver(volumeObserver);
        super.onDestroy();
    }
}