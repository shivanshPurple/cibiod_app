package com.cibiod.app.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.transition.Fade;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
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

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.cibiod.app.Activities.BluetoothActivity;
import com.cibiod.app.Activities.HomeActivity;
import com.cibiod.app.Activities.SettingsActivity;
import com.cibiod.app.CustomViews.CustomSpinner;
import com.cibiod.app.R;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
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
        editRect.setTranslationY(-8);
        editText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
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

    public static void setupDropdown(final Context context, final CustomSpinner dropdown, final int array, final ImageView dropdownPopupBg, final ImageView dropdownArrow) {
        final ArrayAdapter genderDropdownAdapter = new ArrayAdapter(context, R.layout.custom_dropdown, context.getResources().getStringArray(array));
        dropdown.setAdapter(genderDropdownAdapter);

        Spinner.LayoutParams params = dropdownPopupBg.getLayoutParams();
        final int height = 100 * genderDropdownAdapter.getCount();
        params.height = height;
        dropdownPopupBg.setTranslationY(-height);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView selectedItem = (TextView) parent.getChildAt(0);
                ObjectAnimator oa = ObjectAnimator.ofArgb(selectedItem, "textColor", Color.TRANSPARENT, Color.WHITE).setDuration(300);
                oa.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dropdown.setOnSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerFocusChange(boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dropdown.getWindowToken(), 0);
                if (hasFocus) {
                    dropdown.setBackgroundTintList(context.getColorStateList(R.color.black));
                    dropdownArrow.setColorFilter(context.getColor(R.color.black));
                    ObjectAnimator oa = ObjectAnimator.ofFloat(dropdownArrow, "rotation", dropdownArrow.getRotation(), -90);
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
                    ObjectAnimator oa = ObjectAnimator.ofFloat(dropdownArrow, "rotation", dropdownArrow.getRotation(), 0);
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

    public static void setupToolbar(final Activity activity, final DrawerLayout drawerLayout, NavigationView navigationView, Toolbar toolbar, String string, int padding) {
        TextView textView = activity.findViewById(R.id.toolbarTitle);
        textView.setText(string);
        textView.setPadding(0, 0, padding, 0);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.openNavDrawer, R.string.closeNavDrawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuHome:
                        if (!activity.getLocalClassName().equals("Activities.HomeActivity"))
                            activity.startActivity(new Intent(activity, HomeActivity.class));
                        break;
                    case R.id.menuQuickTest:
                        if (!activity.getLocalClassName().equals("Activities.BluetoothActivity"))
                            activity.startActivity(new Intent(activity, BluetoothActivity.class).putExtra("from", "quickTest"));
                        break;
                    case R.id.menuSettings:
                        if (!activity.getLocalClassName().equals("Activities.SettingsActivity"))
                            activity.startActivity(new Intent(activity, SettingsActivity.class));
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
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

    public static void setViewStub(Activity activity, ViewStub viewStub, String action) {
        if (viewStub.getLayoutResource() != R.layout.layout_animation_view) {
            viewStub.setLayoutResource(R.layout.layout_animation_view);
            viewStub.inflate();
        }

        TextView tv = activity.findViewById(R.id.animationText);
        LottieAnimationView lav = activity.findViewById(R.id.animationView);
        viewStub.setVisibility(View.VISIBLE);

        if (action.equals("loading")) {
            ArrayList<String> funnies = new ArrayList<>();
            funnies.add("An apple day keeps everyone\naway, if thrown hard enough");
            funnies.add("The six best doctors are sunshine,\nwater, rest, exercise and diet");
            funnies.add("Dentist is only the half the\ndoctor he claims to be!");
            funnies.add("Never go to a doctor whose\noffice plants have died!");
            funnies.add("Beware of dogs, young doctors,\nand old barbers");
            tv.setText(funnies.get(new Random().nextInt(funnies.size())));
        }

        if (action.equals("empty")) {
            lav.setAnimation("empty_anim.json");
            lav.setRepeatMode(LottieDrawable.RESTART);
            tv.setPadding(0, 16, 0, 0);
            tv.setText("Check internet or\nClick + icon to add new");
        }

        lav.playAnimation();

        if (action.equals("clear")) {
            viewStub.setVisibility(View.GONE);
        }
    }

    public static String getPref(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("applicationVariables", Context.MODE_PRIVATE);
        return prefs.getString(key, "NA");
    }

    public static void setPref(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences("applicationVariables", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.apply();
    }

    public static void logout(Context context){
        SharedPreferences prefs = context.getSharedPreferences("applicationVariables", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("id");
        editor.apply();
    }
}

