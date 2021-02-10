package com.cibiod2.estetho.customViews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomSpinner extends androidx.appcompat.widget.AppCompatSpinner {

    private OnSpinnerEventsListener mListener;

    public CustomSpinner(@NonNull Context context) {
        super(context);
    }

    public CustomSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        mListener.onSpinnerFocusChange(!hasWindowFocus);
    }

    public void setOnSpinnerEventsListener(OnSpinnerEventsListener onSpinnerEventsListener) {
        mListener = onSpinnerEventsListener;
    }

    public interface OnSpinnerEventsListener {
        void onSpinnerFocusChange(boolean hasFocus);
    }
}
