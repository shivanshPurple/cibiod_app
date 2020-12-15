package com.cibiod.app.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cibiod.app.R;
import com.cibiod.app.Utils.u;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

        u.setupEditText(LoginActivity.this,emailText,emailRect,"E MAIL",false);
        u.setupEditText(LoginActivity.this,passwordText,passwordRect,"PASSWORD",true);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailVal = emailText.getText().toString();
                final String passwordVal = passwordText.getText().toString();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference dbRef = database.getReference("users");
                final Query search = dbRef.orderByKey().equalTo(emailVal);

                search.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChildren())
                        {
                            for(DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                if(passwordVal.equals(snapshot.child("password").getValue().toString()))
                                {
                                    SharedPreferences prefs = getSharedPreferences("applicationVariables", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("id",emailVal);
                                    editor.apply();
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                }
                                else
                                    passwordText.setError("Wrong password");
                            }
                        }
                        else
                            emailText.setError("Email not registered");
                        dbRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(),"Database Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void startRegActivity(View v)
    {
        Intent intent = new Intent(this, RegisterActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(findViewById(R.id.emailGroup), "emailShared"),
                Pair.create(findViewById(R.id.passwordGroup), "passwordShared"),
                Pair.create(findViewById(R.id.loginButton), "sharedLoginRegisterButton"));
        startActivity(intent, options.toBundle());
    }
}