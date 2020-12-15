package com.cibiod.app.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cibiod.app.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class LogoActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private GestureDetectorCompat gestureDetectorCompat = null;
    private float startPosition = 0;
    private ObjectAnimator orangeAnimator;
    private boolean animationStarted;
    private ImageView orangeBg;
    private float screenDepth, swipeAmount;

    private float maxSwipeDist = 900, minSwipeAmt = 0.8f;

    private LinearLayout bottomLayout;
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        screenDepth = Resources.getSystem().getDisplayMetrics().heightPixels + 200;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_screen);

        ImageButton logo = findViewById(R.id.logo);
        TextView swipeUpText = findViewById(R.id.swipeUpText);
        orangeBg = findViewById(R.id.orangeBg);

        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);

        logo.startAnimation(fadeIn);
        swipeUpText.startAnimation(fadeIn);

        gestureDetectorCompat = new GestureDetectorCompat(this, this);

        orangeAnimator = ObjectAnimator.ofFloat(orangeBg, "translationY", screenDepth, 0);
        orangeAnimator.setDuration(1000);
        orangeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        orangeAnimator.setCurrentPlayTime(0);

        orangeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                startApp();
            }
        });

        if (ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) == PERMISSION_DENIED |
                ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) == PERMISSION_DENIED |
                ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PERMISSION_DENIED |
                ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET) == PERMISSION_DENIED |
                ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH) == PERMISSION_DENIED |
                ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH_ADMIN) == PERMISSION_DENIED)
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, INTERNET, BLUETOOTH, BLUETOOTH_ADMIN}, 12);
    }

    private void startApp() {
        Intent intent = new Intent(this, IntroActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Permissions required fpr app to work!", Toast.LENGTH_LONG).show();
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, INTERNET, BLUETOOTH, BLUETOOTH_ADMIN}, 12);
            }

            if (resultCode == RESULT_OK)
                ;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        if (startPosition != 0) {
            swipeAmount = startPosition - event.getY();
            swipeAmount = swipeAmount / maxSwipeDist;
            swipeAmount = Math.min(Math.max(0, swipeAmount), 1);
            if (!animationStarted) {
                if (swipeAmount > minSwipeAmt) {
                    animationStarted = true;
                    orangeAnimator.start();
                }
                orangeAnimator.setCurrentPlayTime((long) (orangeAnimator.getDuration() * swipeAmount));
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP & swipeAmount <= minSwipeAmt) {
            ObjectAnimator tempRev = ObjectAnimator.ofFloat(orangeBg, "translationY", orangeBg.getY(), screenDepth);
            tempRev.setDuration(600);
            tempRev.setInterpolator(new AnticipateInterpolator());
            tempRev.start();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        startPosition = e.getY();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

}