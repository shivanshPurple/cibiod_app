package com.cibiod.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        final EditText emailText = findViewById(R.id.editEmail);
        final ImageView emailRect = findViewById(R.id.emailRect);

        final EditText passwordText = findViewById(R.id.editPassword);
        final ImageView passwordRect = findViewById(R.id.passwordRect);

        u.setOnFocusChange(LoginActivity.this,emailText,emailRect,"E MAIL",false);
        u.setOnFocusChange(LoginActivity.this,passwordText,passwordRect,"PASSWORD",true);
    }
}