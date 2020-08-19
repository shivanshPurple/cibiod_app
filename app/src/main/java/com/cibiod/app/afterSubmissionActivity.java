package com.cibiod.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class afterSubmissionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap m;
    private FusedLocationProviderClient fusedClient;
    private TextView latLongText;
    private Marker marker;
    private LatLng currentLocation;

    private boolean ifPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        Objects.requireNonNull(getSupportActionBar()).hide(); //hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_submission);

        SupportMapFragment fm = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        assert fm != null;
        fm.getMapAsync(this);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        //region Declarations of views and animations
        TextView nameText = findViewById(R.id.nameFinal);
        TextView idText = findViewById(R.id.PatientIdFinal);
        latLongText = findViewById(R.id.LatLongFinal);

        TextView towerDetails = findViewById(R.id.towerDetails);
        final TextView confirmAndUpload = findViewById(R.id.confirmAndUpload);
        final CardView confirmButtonCard = findViewById(R.id.buttonCard);

        final Button confirmButton = findViewById(R.id.confirm);

        final ImageView circleButton = findViewById(R.id.circleButton);

        Animation fadeUpAnim = AnimationUtils.loadAnimation(this,R.anim.fadeinup);
        Animation fadeDownAnim = AnimationUtils.loadAnimation(this,R.anim.fadeindown);
        Animation fadeIn = AnimationUtils.loadAnimation(this,R.anim.fadein);
        Animation fadeIn2 = AnimationUtils.loadAnimation(this,R.anim.fadein2);
        final Animation fadeOut = AnimationUtils.loadAnimation(this,R.anim.fadeout);
        final Animation zoomIn = AnimationUtils.loadAnimation(this,R.anim.zoomin);

        //endregion

        findViewById(R.id.mapCard).startAnimation(fadeIn);
        circleButton.startAnimation(fadeIn2);

        towerDetails.startAnimation(fadeDownAnim);

        confirmButtonCard.startAnimation(fadeUpAnim);
        confirmAndUpload.startAnimation(fadeUpAnim);

        final ArrayList<String> data = (ArrayList<String>) getIntent().getSerializableExtra("data");

        nameText.setText(data.get(0));
        if(data.get(4)==null)
            idText.setText("Error, Try Again");
        else
            idText.setText(data.get(4));

        turnGpsOn();

        ImageButton getLocButton = findViewById(R.id.getCurrLoc);

        getLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        final Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ifPressed) {
                    ifPressed = true;
                    circleButton.startAnimation(zoomIn);
                    confirmAndUpload.startAnimation(fadeOut);
                    confirmButtonCard.startAnimation(fadeOut);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference dbRef = database.getReference("patients");

                    dbRef.child(data.get(4)).child("address").setValue(data.get(2));
                    dbRef.child(data.get(4)).child("gender").setValue(data.get(3));
                    dbRef.child(data.get(4)).child("age").setValue(data.get(1));
                    dbRef.child(data.get(4)).child("name").setValue(data.get(0));
                    dbRef.child(data.get(4)).child("location").child("latitude").setValue(currentLocation.latitude);
                    dbRef.child(data.get(4)).child("location").child("longitude").setValue(currentLocation.longitude);

                    Toast.makeText(getApplicationContext(),"Added to Firebase",Toast.LENGTH_SHORT).show();

                    Handler h = new Handler();
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(afterSubmissionActivity.this,patientEntry.class);

                            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(afterSubmissionActivity.this,android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                            startActivity(i, bundle);
                        }
                    };

                    h.postDelayed(r,1000);
                }
            }
        });
    }


    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(afterSubmissionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(afterSubmissionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        fusedClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                if (location != null)
                    handleLoc(location);
                }
            });
    }

    private void turnGpsOn()
    {
        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(afterSubmissionActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(afterSubmissionActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            }
        });

        task.addOnFailureListener(afterSubmissionActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(afterSubmissionActivity.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        getCurrentLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_CANCELED) {
                turnGpsOn();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        m = map;
    }

    private void moveCamToLoc(LatLng currentLocation)
    {
        m.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
    }

    private void handleLoc(Location loc)
    {
        if(loc==null)
        {
            Toast.makeText(afterSubmissionActivity.this,"Can not get the location",Toast.LENGTH_LONG).show();
            return;
        }

        currentLocation = new LatLng(loc.getLatitude(),loc.getLongitude());

        moveCamToLoc(currentLocation);

        if(marker==null) {
            marker = m.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .draggable(true));
        }

        else
            marker.setPosition(currentLocation);

        updateLatLongText(currentLocation);

        m.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                updateLatLongText(marker.getPosition());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                updateLatLongText(marker.getPosition());
                moveCamToLoc(marker.getPosition());
            }
        });
    }

    private void updateLatLongText(LatLng position)
    {
        String tempLat = Math.round(position.latitude * 10000.0) / 10000.0 + ", " + Math.round(position.longitude * 10000.0) / 10000.0;
        latLongText.setText(tempLat);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}