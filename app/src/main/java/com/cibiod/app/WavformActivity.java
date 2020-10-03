package com.cibiod.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.anand.brose.graphviewlibrary.GraphView;
import com.anand.brose.graphviewlibrary.WaveSample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WavformActivity extends AppCompatActivity {

    private List<WaveSample> points = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wavform);

        final GraphView graphView = findViewById(R.id.graphView);
        graphView.setMaxAmplitude(Short.MAX_VALUE);
        graphView.setMasterList(points);
        final int min = Short.MIN_VALUE;
        final int max = Short.MAX_VALUE;

        new Thread()
        {
            @Override
            public void run() {
                graphView.startPlotting();
                Random r = new Random();
                while(true)
                {
                    points.add(new WaveSample(100,r.nextInt((max - min) + 1)+min));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}