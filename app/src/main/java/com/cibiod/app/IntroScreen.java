package com.cibiod.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class IntroScreen extends AppCompatActivity {

    private IntroAdapter introAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen);

        setupIntroItems();

        ViewPager2 introViewpager = findViewById(R.id.viewpager);
        introViewpager.setPageTransformer(new IntroPageTransformer());
        View child = introViewpager.getChildAt(0);

        if (child instanceof RecyclerView) {
            child.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        introViewpager.setAdapter(introAdapter);
    }

    public void setupIntroItems()
    {
        List<IntroItem> introItems = new ArrayList<>();

        IntroItem itemBluetooth = new IntroItem();
        itemBluetooth.setTitle("WIRELESS TECHNOLOGY");
        itemBluetooth.setDesc("Use our device to reduce  the hassle of wires or getting in contact with someone. Your phone will connect automatically to the device while you are examining someone. " +
                "No need to worry about pairing and connecting to the device.");
        itemBluetooth.setImage(R.drawable.vector_bluetooth);
        introItems.add(itemBluetooth);

        IntroItem itemCloud = new IntroItem();
        itemCloud.setTitle("CLOUD\nSTORAGE");
        itemCloud.setDesc("Every test you take will automatically save itself on our cloud services , so you can manage them for later. These tests may also be used to train algorithms which " +
                "can help you get more out of every analysis.");
        itemCloud.setImage(R.drawable.vector_cloud);
        introItems.add(itemCloud);

        IntroItem itemProc = new IntroItem();
        itemProc.setTitle("IN-APP\nPROCESSING");
        itemProc.setDesc("Our app provides in-app processing features like filtering to pick up heart or lung sounds individually. It can also export every PCG file as a WAV file for sharing purposes.");
        itemProc.setImage(R.drawable.vector_inapp_processing);
        introItems.add(itemProc);

        introAdapter = new IntroAdapter(introItems);
    }

    private static class IntroPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.2f;
        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -100) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 100) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }
}