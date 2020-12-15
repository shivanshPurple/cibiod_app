package com.cibiod.app.CustomViews;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.cibiod.app.Utils.u;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomSpinner extends androidx.appcompat.widget.AppCompatSpinner{

    public CustomSpinner(@NonNull Context context) {
        super(context);
    }

    public CustomSpinner(@NonNull Context context, int mode) {
        super(context, mode);
    }

    public CustomSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public CustomSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
    }

    public interface OnSpinnerEventsListener {
        void onSpinnerFocusChange(boolean hasFocus);
    }

    private OnSpinnerEventsListener mListener;

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        mListener.onSpinnerFocusChange(!hasWindowFocus);
    }

    public void setOnSpinnerEventsListener(OnSpinnerEventsListener onSpinnerEventsListener)
    {
        mListener=onSpinnerEventsListener;
    }
}
