package com.cibiod.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText emailText = findViewById(R.id.editEmail);
        final ImageView emailRect = findViewById(R.id.emailRect);

        final EditText passwordText = findViewById(R.id.editPassword);
        final ImageView passwordRect = findViewById(R.id.passwordRect);

        final EditText confirmPasswordText = findViewById(R.id.editConfirmPassword);
        final ImageView confirmPasswordRect = findViewById(R.id.confirmPasswordRect);

        u.setOnFocusChange(RegisterActivity.this,emailText,emailRect,"E MAIL",false);
        u.setOnFocusChange(RegisterActivity.this,passwordText,passwordRect,"PASSWORD",false);
        u.setOnFocusChange(RegisterActivity.this,confirmPasswordText,confirmPasswordRect,"CONFIRM PASSWORD",true);


        final CustomSpinner genderDropdown = findViewById(R.id.genderSpinner);
        final ImageView genderPopupBg = findViewById(R.id.genderPopupBg);
        final ImageView genderDropdownArrow = findViewById(R.id.genderDropdownArrow);

        u.setupDropdown(this,genderDropdown,R.array.Gender,genderPopupBg,genderDropdownArrow);
    }
}