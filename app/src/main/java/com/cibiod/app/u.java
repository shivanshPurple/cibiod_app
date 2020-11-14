package com.cibiod.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class u {
    public static void print(Object o)
    {
        Log.d("customm",String.valueOf(o));
    }

    public static void alert()
    {
        Log.d("customm","yes");
    }

    public static void setOnFocusChange(final Context context, final EditText editText, final ImageView editRect, final String hint, final boolean isLast)
    {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                onSelectEdit(context,editText,editRect,hasFocus,hint);
            }
        });

        if(isLast)
        {
            editText.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == 66) {
                        editText.clearFocus();
                        InputMethodManager manager = (InputMethodManager) v.getContext()
                                .getSystemService(INPUT_METHOD_SERVICE);
                        if (manager != null)
                            manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private static void onSelectEdit(Context context, TextView v, ImageView rect, boolean focus, String hint)
    {
        if(focus)
        {
            v.setTextColor(context.getColor(R.color.black));
            v.setBackgroundTintList(ContextCompat.getColorStateList(context.getApplicationContext(), R.color.black));
            v.setHint("");
            ObjectAnimator oa = ObjectAnimator.ofFloat(rect,"scaleX",0,1);
            oa.setDuration(300);
            oa.setInterpolator(new AccelerateInterpolator());
            oa.start();
        }
        else
        {
            v.setTextColor(context.getColor(R.color.white));
            v.setBackgroundTintList(ContextCompat.getColorStateList(context.getApplicationContext(), R.color.white));
            v.setHint(hint);
            ObjectAnimator oa = ObjectAnimator.ofFloat(rect,"scaleX",1,0);
            oa.setDuration(300);
            oa.setInterpolator(new AccelerateInterpolator());
            oa.start();
        }
    }

    public static void setupDropdown(final Context context, final CustomSpinner dropdown, int array, final ImageView dropdownPopupBg, final ImageView dropdownArrow)
    {
        final ArrayAdapter genderDropdownAdapter = new ArrayAdapter(context,R.layout.dropdown_adapter, context.getResources().getStringArray(array));
        dropdown.setAdapter(genderDropdownAdapter);

        Spinner.LayoutParams params = dropdownPopupBg.getLayoutParams();
        final int height = 100*genderDropdownAdapter.getCount();
        params.height = height;

        dropdownPopupBg.setPadding(0,0,0,params.height);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dropdown.setOnSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerFocusChange(boolean hasFocus) {
                if(hasFocus)
                {
                    dropdown.setBackgroundTintList(context.getColorStateList(R.color.black));
                    dropdownArrow.setColorFilter(context.getColor(R.color.black));
                    ObjectAnimator oa = ObjectAnimator.ofFloat(dropdownArrow,"rotation",dropdownArrow.getRotation(),0);
                    oa.setDuration(300);
                    oa.setInterpolator(new DecelerateInterpolator());
                    oa.start();
                    ValueAnimator va = ValueAnimator.ofInt(dropdownPopupBg.getPaddingBottom(),0);
                    va.setDuration(300);
                    va.setInterpolator(new DecelerateInterpolator());
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            dropdownPopupBg.setPadding(0,0,0, (int) animation.getAnimatedValue());
                        }
                    });
                    va.start();
                }
                else
                {
                    dropdown.setBackgroundTintList(context.getColorStateList(R.color.white));
                    dropdownArrow.setColorFilter(context.getColor(R.color.white));
                    ObjectAnimator oa = ObjectAnimator.ofFloat(dropdownArrow,"rotation",dropdownArrow.getRotation(),90);
                    oa.setDuration(300);
                    oa.setInterpolator(new DecelerateInterpolator());
                    oa.start();
                    ValueAnimator va = ValueAnimator.ofInt(dropdownPopupBg.getPaddingBottom(),height);
                    va.setDuration(300);
                    va.setInterpolator(new DecelerateInterpolator());
                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            dropdownPopupBg.setPadding(0,0,0, (int) animation.getAnimatedValue());
                        }
                    });
                    va.start();
                }
            }
        });
    }
}
