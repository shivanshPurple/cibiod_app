package com.cibiod.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class nearestTowers extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap m;
    private FusedLocationProviderClient fusedClient;

    private Marker[] ms = new Marker[5];
    private Marker mainMarker;

    private LatLng loc;

    private boolean circleAlready = false;

    private CardView popup;

    private TextView propertiesText;

    private int k = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_towers);

        SupportMapFragment fm = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.nearestTowerMap);

        assert fm != null;
        fm.getMapAsync(nearestTowers.this);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        final Button getLocButton = findViewById(R.id.locGetter);
        Button getNearestPointsButtons = findViewById(R.id.getNearestPoints);
        Button showCoverageButton = findViewById(R.id.showCoverage);

        popup = findViewById(R.id.popupCard);
        propertiesText = findViewById(R.id.markerProperties);

        popup.setAlpha(0);
        popup.setClickable(false);

        getLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m.clear();
                getCurrentLocation();
                findViewById(R.id.cardView).setAlpha(0);
            }
        });

        getNearestPointsButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject locJson = new JSONObject();
                try {
                    locJson.put("Latitude", loc.latitude);
                    locJson.put("Longitude", loc.longitude);
                }
                catch (JSONException e){
                    e.printStackTrace();
                }

                findViewById(R.id.cardView2).setAlpha(0);

                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    public void run() { ms = addMarkers();;
                    }
                }, 5000);

            }
        });

        final Circle[] cs = new Circle[5];


        showCoverageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(circleAlready){
                    m.clear();
                    ms = addMarkers();
                    circleAlready = false;
                    return;
                }
                circleAlready = true;

                LatLng temp = new LatLng(ms[0].getPosition().latitude,ms[0].getPosition().longitude);
                CircleOptions c = new CircleOptions().fillColor(Color.argb(50,150,0,0)).center(temp).radius(2000d).strokeWidth(0);
                cs[0] = m.addCircle(c);

                LatLng temp1 = new LatLng(ms[1].getPosition().latitude,ms[1].getPosition().longitude);
                CircleOptions c1 = new CircleOptions().fillColor(Color.argb(50,150,0,0)).center(temp1).radius(3000d).strokeWidth(0);
                cs[1] = m.addCircle(c1);

                LatLng temp2 = new LatLng(ms[2].getPosition().latitude,ms[2].getPosition().longitude);
                CircleOptions c2 = new CircleOptions().fillColor(Color.argb(50,150,0,0)).center(temp2).radius(2000d).strokeWidth(0);
                cs[2] = m.addCircle(c2);

                LatLng temp3 = new LatLng(ms[3].getPosition().latitude,ms[3].getPosition().longitude);
                CircleOptions c3 = new CircleOptions().fillColor(Color.argb(50,150,0,0)).center(temp3).radius(2000d).strokeWidth(0);
                cs[3] = m.addCircle(c3);

                LatLng temp4 = new LatLng(ms[4].getPosition().latitude,ms[4].getPosition().longitude);
                CircleOptions c4 = new CircleOptions().fillColor(Color.argb(50,150,0,0)).center(temp4).radius(1000d).strokeWidth(0);
                cs[4] = m.addCircle(c4);
            }
        });
    }

    private Bitmap createStoreMarker(String s) {
        View markerLayout = getLayoutInflater().inflate(R.layout.store_marker_layout, null);

        ImageView markerImage = (ImageView) markerLayout.findViewById(R.id.marker_image);
        TextView markerInfo = (TextView) markerLayout.findViewById(R.id.marker_text);
        markerImage.setImageResource(R.drawable.pin);
        markerInfo.setText(s);

        markerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        final Bitmap bitmap = Bitmap.createBitmap(markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerLayout.draw(canvas);
        return bitmap;
    }

    private void moveCamToLoc(LatLng currentLocation)
    {
        m.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        m = map;
//        start();
    }

    //33.2 77.25
    private void start(){
        m.clear();
//        addMarkers();

        final CardView popup = findViewById(R.id.popupCard);

        popup.setAlpha(0);
        popup.setClickable(false);

        m.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker1) {
                //marker on click

                popup.setAlpha(1);
                popup.setClickable(true);

                popup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.setAlpha(0);
                        popup.setClickable(false);

                        start();
                    }
                });

                return false;
            }
        });
    }

    private Marker[] addMarkers()
    {
        LatLng[] l = new LatLng[5];
        l[0] = new LatLng(30.74561,76.84673);
        l[1] = new LatLng(30.74352,76.84737);
        l[2] = new LatLng(30.74252,76.84927);
        l[3] = new LatLng(30.69974,76.83361);
        l[4] = new LatLng(30.69959,76.83372);

        double[] dist = new double[5];
        dist[0] = 8.54642;
        dist[1] = 8.61868;
        dist[2] = 8.80761;
        dist[3] = 9.05517;
        dist[4] = 9.07353;

        final String[] props = {"Tower Type : RTP\nID : 18437","Tower Type : RTT\nID : 18484","Tower Type : RTP\nID : 18452","Tower Type : COW(GBT)\nID : 18319","Tower Type : GBM\nID : 18520"};

        for(int i = 0; i < 5; i++)
        {
            String temp = dist[i] + " km away!";
            ms[i] = m.addMarker(new MarkerOptions()
                        .position(l[i])
                        .draggable(false)
                        .icon(BitmapDescriptorFactory.fromBitmap(createStoreMarker(temp))));

            m.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker1) {
                    propertiesText.setText(props[k]);
                    k++;
                    if(k>4)
                        k=0;

                    popup.setAlpha(1);
                    popup.setClickable(true);

                    popup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            popup.setAlpha(0);
                            popup.setClickable(false);

                            addMarkers();
                        }
                    });

                    return false;
                }
            });
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : ms) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,300);
        m.animateCamera(cu);



        return ms;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(nearestTowers.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            int locationPermissionCode = 51;
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, locationPermissionCode);
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == 51) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();
            } else {
                Toast.makeText(nearestTowers.this,
                        "Permission Denied. Please Allow",
                        Toast.LENGTH_SHORT)
                        .show();
                getCurrentLocation();
            }
        }
    }

    private void handleLoc(Location location)
    {
        if(location==null)
        {
            Toast.makeText(nearestTowers.this,"Can not get the location",Toast.LENGTH_LONG).show();
            return;
        }

        LatLng currentLocation = new LatLng(location.getLatitude(),location.getLongitude());

        mainMarker = m.addMarker(new MarkerOptions().position(currentLocation).draggable(true));
        mainMarker.setTitle(currentLocation.latitude + "," + currentLocation.longitude);
        mainMarker.showInfoWindow();

        m.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

                                      @Override
                                      public void onMarkerDragStart(Marker mainMarker) {

                                      }

                                      @Override
                                      public void onMarkerDrag(Marker mainMarker) {
                                          mainMarker.setTitle(mainMarker.getPosition().latitude + ", " + mainMarker.getPosition().longitude);
                                          mainMarker.showInfoWindow();
                                      }

                                      @Override
                                      public void onMarkerDragEnd(Marker mainMarker) {
                                          mainMarker.setTitle(mainMarker.getPosition().latitude + ", " + mainMarker.getPosition().longitude);
                                          mainMarker.showInfoWindow();
                                          moveCamToLoc(mainMarker.getPosition());
                                          finish();
                                      }
                                  });

        moveCamToLoc(currentLocation);

        Toast.makeText(getApplicationContext(),
                currentLocation.latitude + "," + currentLocation.longitude,
                Toast.LENGTH_SHORT).show();

        loc = currentLocation;
    }

}