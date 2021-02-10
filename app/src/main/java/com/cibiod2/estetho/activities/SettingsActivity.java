package com.cibiod2.estetho.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.cibiod2.estetho.fragments.BottomSheetBluetoothDevice;
import com.cibiod2.estetho.R;
import com.cibiod2.estetho.utils.BottomAppBarCutCornersTopEdge;
import com.cibiod2.estetho.utils.VolumeObserver;
import com.cibiod2.estetho.utils.u;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.rey.material.widget.Slider;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

public class SettingsActivity extends AppCompatActivity {
    private VolumeObserver volumeObserver;
    private BluetoothAdapter bluetoothAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        u.sharedTransFix(getWindow(), R.color.blue);
        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
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

        slider.setOnPositionChangeListener((view, fromUser, oldPos, newPos, oldValue, newValue) -> audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (newPos * maxVolume), 0));

        volumeObserver = new VolumeObserver(this, slider, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, volumeObserver);
//        endregion

        String deviceName = u.getPref(this, "pairedStethoName");
        if (deviceName.equals("NA"))
            ((TextView) findViewById(R.id.deviceName)).setText("No Device");
        else
            ((TextView) findViewById(R.id.deviceName)).setText(deviceName);

        findViewById(R.id.cibiodLink).setOnClickListener(v -> {
            AnimatorSet as = new AnimatorSet();
            as.playSequentially(ObjectAnimator.ofArgb(v, "backgroundColor", Color.WHITE, Color.LTGRAY).setDuration(200)
                    , ObjectAnimator.ofArgb(v, "backgroundColor", Color.LTGRAY, Color.WHITE).setDuration(200));
            as.setInterpolator(new DecelerateInterpolator());
            as.start();
            String url = "http://cibiod.in/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        findViewById(R.id.purpleLink).setOnClickListener(v -> {
            AnimatorSet as = new AnimatorSet();
            as.playSequentially(ObjectAnimator.ofArgb(v, "backgroundColor", Color.WHITE, Color.LTGRAY).setDuration(200)
                    , ObjectAnimator.ofArgb(v, "backgroundColor", Color.LTGRAY, Color.WHITE).setDuration(200));
            as.setInterpolator(new DecelerateInterpolator());
            as.start();
            Uri uri = Uri.parse("http://instagram.com/_u/shivansh.purple");
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
            likeIng.setPackage("com.instagram.android");
            startActivity(likeIng);
        });

        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            u.logout(SettingsActivity.this);
            Intent intent = new Intent(SettingsActivity.this, LogoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slideupfromdown, 0);
        });

        findViewById(R.id.fabSettings).setOnClickListener(v -> SettingsActivity.this.finish());

        findViewById(R.id.changeDeviceGroup).setOnClickListener(v -> {
            AnimatorSet as = new AnimatorSet();
            as.playSequentially(ObjectAnimator.ofArgb(v, "backgroundColor", Color.WHITE, Color.LTGRAY).setDuration(200),
                    ObjectAnimator.ofArgb(v, "backgroundColor", Color.LTGRAY, Color.WHITE).setDuration(200));
            as.setInterpolator(new DecelerateInterpolator());
            as.start();

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null)
                Toast.makeText(SettingsActivity.this, "Bluetooth Required", Toast.LENGTH_LONG).show();

            else if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 97);
            } else {
                BottomSheetBluetoothDevice bottomSheet = new BottomSheetBluetoothDevice(bluetoothAdapter, true);
                bottomSheet.show(getSupportFragmentManager(), "bottomSheet");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 97) {
            if (resultCode == RESULT_OK) {
                BottomSheetBluetoothDevice bottomSheet = new BottomSheetBluetoothDevice(bluetoothAdapter, true);
                bottomSheet.show(getSupportFragmentManager(), "bottomSheet");
            } else {
                Toast.makeText(this, "Bluetooth is required to connect", Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 11);
            }
        }
    }

    @Override
    protected void onDestroy() {
        getApplicationContext().getContentResolver().unregisterContentObserver(volumeObserver);
        super.onDestroy();
    }
}