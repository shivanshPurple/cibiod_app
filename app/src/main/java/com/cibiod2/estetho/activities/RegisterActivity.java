package com.cibiod2.estetho.activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cibiod2.estetho.customViews.CustomSpinner;
import com.cibiod2.estetho.R;
import com.cibiod2.estetho.utils.u;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailText, passwordText, confirmPasswordText;
    private CustomSpinner genderDropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);
        u.sharedTransFix(getWindow(), R.color.blue);

        emailText = findViewById(R.id.editEmail);
        final ImageView emailRect = findViewById(R.id.emailRect);

        passwordText = findViewById(R.id.editPassword);
        final ImageView passwordRect = findViewById(R.id.passwordRect);

        confirmPasswordText = findViewById(R.id.editConfirmPassword);
        final ImageView confirmPasswordRect = findViewById(R.id.confirmPasswordRect);

        u.setupEditText(this, emailText, emailRect, "E MAIL", false);
        u.setupEditText(this, passwordText, passwordRect, "PASSWORD", false);
        u.setupEditText(this, confirmPasswordText, confirmPasswordRect, "CONFIRM PASSWORD", true);

        genderDropdown = findViewById(R.id.genderSpinner);
        final ImageView genderPopupBg = findViewById(R.id.genderPopupBg);
        final ImageView genderDropdownArrow = findViewById(R.id.genderDropdownArrow);

        u.setupDropdown(this, genderDropdown, R.array.Gender, genderPopupBg, genderDropdownArrow);

        final Button registerFinalButton = findViewById(R.id.registerButtonFinal);
        registerFinalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerFinalButton.setClickable(false);
                ObjectAnimator.ofFloat(registerFinalButton, "alpha", 1, 0).setDuration(300).start();
                final String emailVal = emailText.getText().toString().toLowerCase();
                final String passwordVal = passwordText.getText().toString();
                String confirmPasswordVal = confirmPasswordText.getText().toString();
                final String genderVal = genderDropdown.getSelectedItem().toString();
                if (!emailVal.contains("@") | !emailVal.contains(".") |
                        emailVal.contains("#") |
                        emailVal.contains("$") |
                        emailVal.contains("[") |
                        emailVal.contains("]")) {
                    emailText.setError("Invalid Email");
                    registerFinalButton.setClickable(true);
                    ObjectAnimator.ofFloat(registerFinalButton, "alpha", 0, 1).setDuration(300).start();
                    return;
                }
                if (passwordVal.isEmpty() || !confirmPasswordVal.equals(passwordVal)) {
                    confirmPasswordText.setError("Passwords don't match");
                    passwordText.setError("Passwords don't match");
                    registerFinalButton.setClickable(true);
                    ObjectAnimator.ofFloat(registerFinalButton, "alpha", 0, 1).setDuration(300).start();
                    return;
                }
                if (genderVal.equals("GENDER")) {
                    Toast.makeText(RegisterActivity.this, "Select a gender", Toast.LENGTH_LONG).show();
                    registerFinalButton.setClickable(true);
                    ObjectAnimator.ofFloat(registerFinalButton, "alpha", 0, 1).setDuration(300).start();
                    return;
                }

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference dbRef = database.getReference("users");
                final Query search = dbRef.orderByKey().equalTo(emailVal);

                search.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            emailText.setError("Email Already Exists");
                            registerFinalButton.setClickable(true);
                            ObjectAnimator.ofFloat(registerFinalButton, "alpha", 0, 1).setDuration(300).start();
                        } else {
                            String emailKey = emailVal.replace(".", "");
                            dbRef.child(emailKey).child("password").setValue(passwordVal);
                            dbRef.child(emailKey).child("email").setValue(emailVal);
                            dbRef.child(emailKey).child("gender").setValue(genderVal).addOnSuccessListener(RegisterActivity.this, aVoid -> {
                                Toast.makeText(getApplicationContext(), "New user registered!", Toast.LENGTH_LONG).show();
                                RegisterActivity.this.onBackPressed();
                            });
                        }
                        dbRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Database Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}