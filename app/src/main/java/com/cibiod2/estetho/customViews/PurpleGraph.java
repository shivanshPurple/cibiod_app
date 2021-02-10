package com.cibiod2.estetho.customViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class PurpleGraph extends View {
    Paint p;
    final List<Float> listX = new ArrayList<>();
    final List<Float> listY = new ArrayList<>();
    float max;
    float originX, originY;

    public PurpleGraph(Context context) {
        super(context);
        intializeVariables();
    }

    public PurpleGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        intializeVariables();
    }

    public PurpleGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intializeVariables();
    }

    public void setMax(float max) {
        this.max = max;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (listX.size() == 0 && listY.size() == 0) {
            listX.add((float) getWidth());
            listY.add((float) getHeight() / 2);
            originX = getWidth();
            originY = (float) getHeight() / 2;
        }

        if (listX.size() >= 2 && listY.size() >= 2) {
            for (int i = 0; i < listX.size() - 1; i++) {
                canvas.drawLine(listX.get(i), listY.get(i), listX.get(i + 1), listY.get(i + 1), p);
            }
        }
    }

    private void intializeVariables() {
        p = new Paint();
        p.setColor(Color.CYAN);
        p.setStrokeWidth(4);
        p.setAntiAlias(true);
    }

    public void setColor(int c) {
        p.setColor(c);
    }

    @SuppressWarnings("UnusedAssignment")
    private void shiftX() {
        for (int i = 0; i < listX.size(); i++) {
            if (listX.get(i) < 0) {
                float temp = listX.remove(i);
                temp = listY.remove(i);
            }
            listX.set(i, listX.get(i) - 6);
        }
        invalidate();
    }

    public void addEntry(int s) {
        listX.add(originX);
        listY.add(originY - (s / max * originY));
        shiftX();
    }
}
