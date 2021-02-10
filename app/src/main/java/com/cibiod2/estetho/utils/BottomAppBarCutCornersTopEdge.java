package com.cibiod2.estetho.utils;

import android.annotation.SuppressLint;

import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.shape.ShapePath;

import androidx.annotation.NonNull;

@SuppressLint("RestrictedApi")
public class BottomAppBarCutCornersTopEdge extends BottomAppBarTopEdgeTreatment {

    private final float fabMargin;
    private final float cradleVerticalOffset;

    public BottomAppBarCutCornersTopEdge(
            float fabMargin, float roundedCornerRadius, float cradleVerticalOffset) {
        super(fabMargin, roundedCornerRadius, cradleVerticalOffset);
        this.fabMargin = fabMargin;
        this.cradleVerticalOffset = cradleVerticalOffset;
    }

    @Override
    @SuppressWarnings("RestrictTo")
    public void getEdgePath(float length, float center, float interpolation, @NonNull ShapePath shapePath) {
        float fabDiameter = getFabDiameter();
        if (fabDiameter == 0) {
            shapePath.lineTo(length, 0);
            return;
        }

        float diamondSize = fabDiameter / 2f;
        float middle = center + getHorizontalOffset();

        float verticalOffsetRatio = cradleVerticalOffset / diamondSize;
        if (verticalOffsetRatio >= 1.0f) {
            shapePath.lineTo(length, 0);
            return;
        }

        float barLeftVertex = middle - (fabMargin + diamondSize - cradleVerticalOffset);
        float barRightVertex = middle + (fabMargin + diamondSize - cradleVerticalOffset);
        float depth = (diamondSize - cradleVerticalOffset + fabMargin) * interpolation;

        float heightArc = 25;
        float widthArc = 25;

        shapePath.lineTo(barLeftVertex, 0);

        shapePath.lineTo(middle - widthArc, depth - heightArc);

        shapePath.addArc(middle - widthArc - 10, 35, middle + widthArc + 10, depth - 15, 135, -83);

        shapePath.lineTo(middle + widthArc, depth - heightArc);
        shapePath.lineTo(barRightVertex, 0);

        shapePath.lineTo(length, 0);
    }
}