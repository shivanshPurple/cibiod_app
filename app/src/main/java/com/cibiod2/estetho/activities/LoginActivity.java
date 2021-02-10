package com.cibiod2.estetho.activities;

import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cibiod2.estetho.R;
import com.cibiod2.estetho.utils.u;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        u.sharedTransFix(getWindow(), R.color.blue);

        final EditText emailText = findViewById(R.id.editEmail);
        final ImageView emailRect = findViewById(R.id.emailRect);

        final EditText passwordText = findViewById(R.id.editPassword);
        final ImageView passwordRect = findViewById(R.id.passwordRect);

        u.setupEditText(LoginActivity.this, emailText, emailRect, "E MAIL", false);
        u.setupEditText(LoginActivity.this, passwordText, passwordRect, "PASSWORD", true);

        final Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setClickable(false);
                ObjectAnimator.ofFloat(loginButton, "alpha", 1, 0).setDuration(300).start();
                final String emailVal = emailText.getText().toString();
                final String emailKey = emailVal.replace(".", "");
                final String passwordVal = passwordText.getText().toString();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference dbRef = database.getReference("users");
                final Query search = dbRef.orderByKey().equalTo(emailKey);

                search.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (passwordVal.equals(Objects.requireNonNull(snapshot.child("password").getValue()).toString())) {
                                    u.setPref(LoginActivity.this, "id", emailKey);
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    passwordText.setError("Wrong password");
                                    loginButton.setClickable(true);
                                    ObjectAnimator.ofFloat(loginButton, "alpha", 0, 1).setDuration(300).start();
                                }
                            }
                        } else {
                            emailText.setError("Email not registered");
                            loginButton.setClickable(true);
                            ObjectAnimator.ofFloat(loginButton, "alpha", 0, 1).setDuration(300).start();
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

    public void startRegActivity(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(findViewById(R.id.emailGroup), "emailShared"),
                Pair.create(findViewById(R.id.passwordGroup), "passwordShared"),
                Pair.create(findViewById(R.id.loginButton), "sharedLoginRegisterButton"));
        startActivity(intent, options.toBundle());
    }
}