package com.dannextech.apps.nab_a_cab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dannextech.apps.nab_a_cab.utils.Alerts;
import com.dannextech.apps.nab_a_cab.utils.Errors;
import com.dannextech.apps.nab_a_cab.utils.Maps;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class DriverHome extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST = 5000;
    private FusedLocationProviderClient mFusedLocationClient;

    private LatLng myLocation;

    private Handler handler1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!new Errors().isNetworkAvailable(getApplicationContext())){
            Snackbar.make(findViewById(R.id.clSignUpUser),Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }
        initMethod();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //set be able to get my location
        mMap.getUiSettings().setZoomControlsEnabled(true);

        getLastLocation();
    }

    private void initMethod() {
        handler1 = new Handler();
        final int delay = 3000; //milliseconds

        handler1.postDelayed(new Runnable(){
            public void run(){
                checkRequest();
                handler1.postDelayed(this, delay);
            }
        }, delay);

    }

    private void checkRequest() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/check_drive_request";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        JSONObject jsonObject = null;
                        Log.e("ResponseResult", "res = "+response);

                        if (!response.equals("null")){
                            try {
                                jsonObject = new JSONObject(response);
                                handler1.removeCallbacksAndMessages(null);

                                SharedPreferences.Editor editor = getSharedPreferences("req", MODE_PRIVATE).edit();
                                editor.putString("reqid", jsonObject.getString("id"));
                                editor.putString("charges", jsonObject.getString("charges"));
                                editor.putString("client", jsonObject.getString("client"));
                                editor.putString("client_phone", jsonObject.getString("phone"));
                                editor.putString("vehicle", jsonObject.getString("vehicle_no"));
                                editor.putString("pickup", jsonObject.getString("pickup_loc"));
                                editor.putString("destination", jsonObject.getString("destination"));
                                editor.putString("origlatt", jsonObject.getString("origlatt"));
                                editor.putString("origlong", jsonObject.getString("origlong"));
                                editor.putString("destlatt", jsonObject.getString("destlatt"));
                                editor.putString("destlong", jsonObject.getString("destlong"));
                                editor.apply();

                                startActivity(new Intent(DriverHome.this,NewRequestClient.class).putExtra("client",jsonObject.toString()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.ResponseResult", error.toString());
                        Toast.makeText(getApplicationContext(),"Error: "+error.toString()+". Please Try Again",Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                SharedPreferences driver = getSharedPreferences("driver", MODE_PRIVATE);

                params.put("license", driver.getString("license",""));
                return params;
            }
        };
        queue.add(postRequest);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            mMap.setMyLocationEnabled(true);
                            onLocationChanged(location);
                        }else
                            new Alerts(DriverHome.this).showMessage("Error", "Unable to get your location. Ensure your GPS is turned on.", new MainActivity());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                        new Alerts(DriverHome.this).showMessage("Error", e.getMessage(), new MainActivity());
                    }
                });
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.e("Dannex Daniels", "onLocationChanged: "+msg);
        // You can now create a LatLng Object for use with maps
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Maps maps = new Maps(DriverHome.this,mMap);
        maps.addMarker(myLocation,"My Location","Driver");
    }

}
