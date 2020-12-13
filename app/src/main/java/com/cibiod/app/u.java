package com.cibiod.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class u {
    public static void print(Object o) {
        Log.d("purpleapp", String.valueOf(o));
    }

    public static void alert() {
        Log.d("purpleapp", "yes");
    }

    public static void setupEditText(final Context context, final EditText editText, final ImageView editRect, final String hint, final boolean isLast) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                onSelectEdit(context, editText, editRect, hasFocus, hint);
            }
        });

        if (isLast) {
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

    private static void onSelectEdit(Context context, TextView v, ImageView rect, boolean focus, String hint) {
        if (focus) {
            v.setTextColor(context.getColor(R.color.black));
            v.setBackgroundTintList(ContextCompat.getColorStateList(context.getApplicationContext(), R.color.black));
            v.setHint("");
            ObjectAnimator oa = ObjectAnimator.ofFloat(rect, "scaleX", 0, 1);
            oa.setDuration(300);
            oa.setInterpolator(new AccelerateInterpolator());
            oa.start();
        } else {
            v.setTextColor(context.getColor(R.color.white));
            v.setBackgroundTintList(ContextCompat.getColorStateList(context.getApplicationContext(), R.color.white));
            v.setHint(hint);
            ObjectAnimator oa = ObjectAnimator.ofFloat(rect, "scaleX", 1, 0);
            oa.setDuration(300);
            oa.setInterpolator(new AccelerateInterpolator());
            oa.start();
        }
    }

    public static void setupDropdown(final Context context, final CustomSpinner dropdown, int array, final ImageView dropdownPopupBg, final ImageView dropdownArrow) {
        final ArrayAdapter genderDropdownAdapter = new ArrayAdapter(context, R.layout.dropdown_adapter, context.getResources().getStringArray(array));
        dropdown.setAdapter(genderDropdownAdapter);

        Spinner.LayoutParams params = dropdownPopupBg.getLayoutParams();
        final int height = 100 * genderDropdownAdapter.getCount();
        params.height = height;
        dropdownPopupBg.setTranslationY(-height);

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
                if (hasFocus) {
                    dropdown.setBackgroundTintList(context.getColorStateList(R.color.black));
                    dropdownArrow.setColorFilter(context.getColor(R.color.black));
                    ObjectAnimator oa = ObjectAnimator.ofFloat(dropdownArrow, "rotation", dropdownArrow.getRotation(), 0);
                    oa.setDuration(300);
                    oa.setInterpolator(new DecelerateInterpolator());
                    ObjectAnimator oa2 = ObjectAnimator.ofFloat(dropdownPopupBg, "translationY", dropdownPopupBg.getTranslationY(), 0);
                    oa2.setDuration(300);
                    oa2.setInterpolator(new DecelerateInterpolator());
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(oa, oa2);
                    set.start();
                } else {
                    dropdown.setBackgroundTintList(context.getColorStateList(R.color.white));
                    dropdownArrow.setColorFilter(context.getColor(R.color.white));
                    ObjectAnimator oa = ObjectAnimator.ofFloat(dropdownArrow, "rotation", dropdownArrow.getRotation(), 90);
                    oa.setDuration(300);
                    oa.setInterpolator(new DecelerateInterpolator());
                    ObjectAnimator oa2 = ObjectAnimator.ofFloat(dropdownPopupBg, "translationY", dropdownPopupBg.getTranslationY(), -height);
                    oa2.setDuration(300);
                    oa2.setInterpolator(new DecelerateInterpolator());
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(oa, oa2);
                    set.start();
                }
            }
        });
    }

    public static void sharedTransFix(Window window, int color) {
        window.setBackgroundDrawableResource(color);
        Fade fade = new Fade();
        View decor = window.getDecorView();
        fade.excludeTarget(decor.findViewById(R.id.action_bar_container), true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        window.setEnterTransition(fade);
        window.setExitTransition(fade);
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void setupToolbar(Activity activity, DrawerLayout drawerLayout, NavigationView navigationView, Toolbar toolbar, String string, int padding) {
        TextView textView = activity.findViewById(R.id.actionBarTitle);
        textView.setText(string);
        textView.setPadding(0, 0, padding, 0);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.openNavDrawer, R.string.closeNavDrawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return false;
            }
        });
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

