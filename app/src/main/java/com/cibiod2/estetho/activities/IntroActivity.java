package com.cibiod2.estetho.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import com.cibiod2.estetho.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GestureDetectorCompat;


public class IntroActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private ConstraintLayout bluetoothGroup, cloudGroup, inAppGroup;
    private ImageView circle1white, circle2white, circle3white, circle1black, circle2black, circle3black;
    private float startPosition, startCirclePosition, endCirclePosition;
    private AnimatorSet toLeftSet, fromRightSet, toLeftSetRev, fromRightSetRev;
    private AnimatorSet circleUpSet, circleDownSet, circleUpSetRev;
    private GestureDetectorCompat gestureDetectorCompat;
    private String current = "bluetooth";
    private boolean animating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro_screen);

        final Button button = findViewById(R.id.introButton);
        button.setOnClickListener(v -> startApp());


        gestureDetectorCompat = new GestureDetectorCompat(this, this);

        bluetoothGroup = findViewById(R.id.bluetoothGroup);
        ObjectAnimator oa = ObjectAnimator.ofFloat(bluetoothGroup, "alpha", 0, 1);
        oa.setDuration(1000).setInterpolator(new AccelerateInterpolator());
        oa.start();
        cloudGroup = findViewById(R.id.cloudGroup);
        inAppGroup = findViewById(R.id.inAppGroup);

        float centerPos = bluetoothGroup.getX();
        float offset = 600;
        float rightPos = centerPos + offset;
        float leftPos = centerPos - offset;

        cloudGroup.setX(rightPos);
        inAppGroup.setX(rightPos);

        ObjectAnimator toLeftAnimator = ObjectAnimator.ofFloat(bluetoothGroup, "translationX", centerPos, leftPos);
        toLeftAnimator.setDuration(600).setInterpolator(new AccelerateInterpolator());
        ObjectAnimator alphaToZeroAnimator = ObjectAnimator.ofFloat(bluetoothGroup, "alpha", 1, 0);
        alphaToZeroAnimator.setDuration(600).setInterpolator(new AccelerateInterpolator());

        toLeftSet = new AnimatorSet();
        toLeftSet.playTogether(alphaToZeroAnimator, toLeftAnimator);
        toLeftSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (current.equals("bluetooth")) {
                    fromRightSet.setTarget(inAppGroup);
                    toLeftSet.setTarget(cloudGroup);
                    current = "cloud";
                    setCircleUpSet(circle3black, circle3white);
                    setCircleUpSetRev(circle1black, circle1white);
                    setCircleDownSet(circle2black, circle2white);
                } else if (current.equals("cloud")) {
                    toLeftSetRev.setTarget(cloudGroup);
                    fromRightSetRev.setTarget(inAppGroup);
                    current = "inApp";
                    circleUpSet = null;
                    setCircleUpSetRev(circle2black, circle2white);
                    setCircleDownSet(circle3black, circle3white);
                    button.setAlpha(1);
                }
                super.onAnimationEnd(animation);
            }
        });

        ObjectAnimator fromRightAnimator = ObjectAnimator.ofFloat(cloudGroup, "translationX", rightPos, centerPos);
        fromRightAnimator.setDuration(600).setInterpolator(new AccelerateInterpolator());
        ObjectAnimator alphaToOneAnimator = ObjectAnimator.ofFloat(cloudGroup, "alpha", 0, 1);
        alphaToOneAnimator.setDuration(600).setInterpolator(new AccelerateInterpolator());
        fromRightSet = new AnimatorSet();
        fromRightSet.playTogether(alphaToOneAnimator, fromRightAnimator);

        //reverse animations

        ObjectAnimator toLeftAnimatorRev = ObjectAnimator.ofFloat(bluetoothGroup, "translationX", leftPos, centerPos);
        toLeftAnimatorRev.setDuration(600).setInterpolator(new AccelerateInterpolator());
        ObjectAnimator alphaToZeroAnimatorRev = ObjectAnimator.ofFloat(bluetoothGroup, "alpha", 0, 1);
        alphaToZeroAnimatorRev.setDuration(600).setInterpolator(new AccelerateInterpolator());

        toLeftSetRev = new AnimatorSet();
        toLeftSetRev.playTogether(alphaToZeroAnimatorRev, toLeftAnimatorRev);
        toLeftSetRev.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (current.equals("cloud")) {
                    fromRightSet.setTarget(cloudGroup);
                    toLeftSet.setTarget(bluetoothGroup);
                    current = "bluetooth";
                    setCircleUpSet(circle2black, circle2white);
                    circleUpSetRev = null;
                    setCircleDownSet(circle1black, circle1white);
                } else if (current.equals("inApp")) {
                    toLeftSetRev.setTarget(bluetoothGroup);
                    fromRightSetRev.setTarget(cloudGroup);
                    current = "cloud";
                    setCircleUpSet(circle3black, circle3white);
                    setCircleUpSetRev(circle1black, circle1white);
                    setCircleDownSet(circle2black, circle2white);
                }
                super.onAnimationEnd(animation);
            }
        });

        ObjectAnimator fromRightAnimatorRev = ObjectAnimator.ofFloat(cloudGroup, "translationX", centerPos, rightPos);
        fromRightAnimatorRev.setDuration(600).setInterpolator(new AccelerateInterpolator());
        ObjectAnimator alphaToOneAnimatorRev = ObjectAnimator.ofFloat(cloudGroup, "alpha", 1, 0);
        alphaToOneAnimatorRev.setDuration(600).setInterpolator(new AccelerateInterpolator());
        fromRightSetRev = new AnimatorSet();
        fromRightSetRev.playTogether(alphaToOneAnimatorRev, fromRightAnimatorRev);

        //start circle animation

        circle1white = findViewById(R.id.circle1white);
        circle2white = findViewById(R.id.circle2white);
        circle3white = findViewById(R.id.circle3white);
        circle1black = findViewById(R.id.circle1black);
        circle2black = findViewById(R.id.circle2black);
        circle3black = findViewById(R.id.circle3black);

        startCirclePosition = circle1black.getY();
        endCirclePosition = startCirclePosition - 32;

        setCircleUpSet(circle1black, circle1white);
        circleUpSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animation.removeAllListeners();
                setCircleUpSet(circle2black, circle2white);
            }
        });
        setCircleDownSet(circle1black, circle1white);
        circleUpSet.start();
    }

    private void setCircleUpSetRev(ImageView cBlack, ImageView cWhite) {
        ObjectAnimator circleUpWhite = ObjectAnimator.ofFloat(cWhite, "translationY", startCirclePosition, endCirclePosition);
        circleUpWhite.setDuration(600);
        circleUpWhite.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator circleUpBlack = ObjectAnimator.ofFloat(cBlack, "translationY", startCirclePosition, endCirclePosition);
        circleUpBlack.setDuration(600);
        circleUpBlack.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator circleBlackAlpha = ObjectAnimator.ofFloat(cBlack, "alpha", 0, 1);
        circleBlackAlpha.setDuration(600);
        circleBlackAlpha.setInterpolator(new AccelerateInterpolator());

        circleUpSetRev = new AnimatorSet();
        circleUpSetRev.playTogether(circleUpWhite, circleUpBlack, circleBlackAlpha);
    }

    private void setCircleDownSet(ImageView cBlack, ImageView cWhite) {
        ObjectAnimator circleDownWhite = ObjectAnimator.ofFloat(cWhite, "translationY", endCirclePosition, startCirclePosition);
        circleDownWhite.setDuration(600);
        circleDownWhite.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator circleDownBlack = ObjectAnimator.ofFloat(cBlack, "translationY", endCirclePosition, startCirclePosition);
        circleDownBlack.setDuration(600);
        circleDownBlack.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator circleBlackAlpha = ObjectAnimator.ofFloat(cBlack, "alpha", 1, 0);
        circleBlackAlpha.setDuration(600);
        circleBlackAlpha.setInterpolator(new AccelerateInterpolator());

        circleDownSet = new AnimatorSet();
        circleDownSet.playTogether(circleDownWhite, circleDownBlack, circleBlackAlpha);
    }

    private void setCircleUpSet(ImageView cBlack, ImageView cWhite) {
        ObjectAnimator circleUpWhite = ObjectAnimator.ofFloat(cWhite, "translationY", startCirclePosition, endCirclePosition);
        circleUpWhite.setDuration(600);
        circleUpWhite.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator circleUpBlack = ObjectAnimator.ofFloat(cBlack, "translationY", startCirclePosition, endCirclePosition);
        circleUpBlack.setDuration(600);
        circleUpBlack.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator circleBlackAlpha = ObjectAnimator.ofFloat(cBlack, "alpha", 0, 1);
        circleBlackAlpha.setDuration(600);
        circleBlackAlpha.setInterpolator(new AccelerateInterpolator());

        circleUpSet = new AnimatorSet();
        circleUpSet.playTogether(circleUpWhite, circleUpBlack, circleBlackAlpha);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        String dir;
        if (startPosition > event.getX())
            dir = "toLeft";
        else
            dir = "toRight";
        float swipeAmount = Math.abs(startPosition - event.getX());
        float maxSwipeDist = 500;
        swipeAmount = swipeAmount / maxSwipeDist;
        swipeAmount = Math.min(Math.max(0, swipeAmount), 1);

        float minSwipeAmt = 0.8f;
        if (dir.equals("toLeft") & !animating & !current.equals("inApp")) {
            if (swipeAmount > minSwipeAmt) {
                fromRightSet.start();
                toLeftSet.start();
                circleDownSet.start();
                circleUpSet.start();
                animating = true;
            }
            fromRightSet.setCurrentPlayTime((long) (600 * swipeAmount));
            circleDownSet.setCurrentPlayTime((long) (600 * swipeAmount));
            circleUpSet.setCurrentPlayTime((long) (600 * swipeAmount));
            toLeftSet.setCurrentPlayTime((long) (600 * swipeAmount));
        } else if (dir.equals("toRight") & !animating & !current.equals("bluetooth")) {
            if (swipeAmount > minSwipeAmt) {
                fromRightSetRev.start();
                toLeftSetRev.start();
                circleDownSet.start();
                circleUpSetRev.start();
                animating = true;
            }
            fromRightSetRev.setCurrentPlayTime((long) (600 * swipeAmount));
            toLeftSetRev.setCurrentPlayTime((long) (600 * swipeAmount));
            circleDownSet.setCurrentPlayTime((long) (600 * swipeAmount));
            circleUpSetRev.setCurrentPlayTime((long) (600 * swipeAmount));
        }

        if (event.getAction() == MotionEvent.ACTION_UP & swipeAmount <= minSwipeAmt) {
            ValueAnimator va = ValueAnimator.ofFloat(swipeAmount, 0f);
            va.setDuration(300);
            va.setInterpolator(new AccelerateInterpolator());
            final String dirTemp = dir;
            va.addUpdateListener(animation -> {
                if (dirTemp.equals("toLeft")) {
                    if (!current.equals("inApp")) {
                        fromRightSet.setCurrentPlayTime((long) (600f * (float) animation.getAnimatedValue()));
                        circleDownSet.setCurrentPlayTime((long) (600f * (float) animation.getAnimatedValue()));
                        if (circleUpSet != null)
                            circleUpSet.setCurrentPlayTime((long) (600f * (float) animation.getAnimatedValue()));
                        toLeftSet.setCurrentPlayTime((long) (600f * (float) animation.getAnimatedValue()));
                    }
                } else {
                    if (!current.equals("bluetooth")) {
                        fromRightSetRev.setCurrentPlayTime((long) (600f * (float) animation.getAnimatedValue()));
                        circleDownSet.setCurrentPlayTime((long) (600f * (float) animation.getAnimatedValue()));
                        if (circleUpSetRev != null)
                            circleUpSetRev.setCurrentPlayTime((long) (600f * (float) animation.getAnimatedValue()));
                        toLeftSetRev.setCurrentPlayTime((long) (600f * (float) animation.getAnimatedValue()));
                    }
                }
            });
            va.start();
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        animating = false;
        startPosition = e.getX();
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

    private void startApp() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slideupfromdown, 0);
    }
}