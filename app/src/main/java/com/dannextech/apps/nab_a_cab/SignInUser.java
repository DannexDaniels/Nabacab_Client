package com.dannextech.apps.nab_a_cab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dannextech.apps.nab_a_cab.utils.Alerts;
import com.dannextech.apps.nab_a_cab.utils.Errors;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class SignInUser extends AppCompatActivity {

    private EditText etPhone, etPassword;
    private ProgressBar pbSignInUser;

    private LatLng myLocation;

    private Errors errors;

    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_user);

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        pbSignInUser = findViewById(R.id.pbSignInUser);

        errors = new Errors();

        if (!errors.isNetworkAvailable(getApplicationContext())) {
            Snackbar.make(findViewById(R.id.clSignIn), Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }

        startLocationUpdates();

    }

    public void signUpUser(View v) {
        startActivity(new Intent(SignInUser.this, SignUpUser.class));
    }

    public void signInUser(View v) {
        pbSignInUser.setVisibility(View.VISIBLE);

        getLastLocation();

        //post my user details
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/authenticate_user";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        JSONObject jsonObject = null;
                        Log.e("ResponseResult", response);
                        try {
                            jsonObject = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (jsonObject == null) {
                            pbSignInUser.setVisibility(View.GONE);
                            Snackbar.make(findViewById(R.id.clSignIn), Html.fromHtml("<font color=\"#ffffff\">Wrong User Name or Password</font>"), Snackbar.LENGTH_SHORT).show();
                        } else {
                            try {

                                String resp = null;
                                if (jsonObject.has("status")) {
                                    resp = jsonObject.getString("status");
                                } else {
                                    resp = jsonObject.getString("user_category");
                                }

                                if (resp.equals("Client")) {
                                    SharedPreferences.Editor editor = getSharedPreferences("client", MODE_PRIVATE).edit();
                                    editor.putString("name", jsonObject.getString("client_name"));
                                    editor.putString("phone", jsonObject.getString("client_phone"));
                                    editor.putString("email", jsonObject.getString("client_email"));
                                    editor.apply();
                                    startActivity(new Intent(SignInUser.this, ClientHome.class));
                                } else if (resp.equals("Driver")) {
                                    Toast.makeText(getApplicationContext(), "Login Successfull", Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = getSharedPreferences("driver", MODE_PRIVATE).edit();
                                    editor.putString("name", jsonObject.getString("driver_name"));
                                    editor.putString("phone", jsonObject.getString("driver_phone"));
                                    editor.putString("email", jsonObject.getString("driver_email"));
                                    editor.putString("gender", jsonObject.getString("driver_gender"));
                                    editor.putString("id", jsonObject.getString("driver_id"));
                                    editor.putString("license", jsonObject.getString("license_no"));
                                    editor.putString("plate", jsonObject.getString("vehicle_no"));
                                    editor.putString("type", jsonObject.getString("vehicle_type"));
                                    editor.putString("vcategory", jsonObject.getString("category"));
                                    editor.putString("capacity", jsonObject.getString("capacity"));
                                    editor.putString("id", jsonObject.getString("driver_id"));
                                    editor.putString("category", resp);
                                    editor.apply();
                                    startActivity(new Intent(SignInUser.this, DriverHome.class));
                                } else if (resp.equals("pass_err1")) {
                                    pbSignInUser.setVisibility(View.GONE);
                                    etPassword.setVisibility(View.VISIBLE);
                                    etPassword.setAlpha(0.0f);

                                    // Start the animation
                                    etPassword.animate()
                                            .translationY(etPassword.getHeight())
                                            .alpha(1.0f)
                                            .setDuration(500)
                                            .setListener(null);

                                } else if (resp.equals("pass_err2")) {
                                    pbSignInUser.setVisibility(View.GONE);
                                    Snackbar.make(findViewById(R.id.clSignIn), Html.fromHtml("<font color=\"#ffffff\">Password is incorrect</font>"), Snackbar.LENGTH_LONG).show();
                                } else if (resp.equals("null")) {
                                    pbSignInUser.setVisibility(View.GONE);
                                    new AlertDialog.Builder(SignInUser.this)
                                            .setTitle("Alert")
                                            .setMessage("We don't have an account registered to this number. Do you wish to create a new one?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    startActivity(new Intent(SignInUser.this, SignUpUser.class));
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    startActivity(new Intent(SignInUser.this, MainActivity.class));
                                                }
                                            })
                                            .create()
                                            .show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pbSignInUser.setVisibility(View.GONE);
                        Log.d("Error.ResponseResult", error.toString());
                        Snackbar.make(findViewById(R.id.clSignIn), Html.fromHtml("<font color=\"#ffffff\">" + new Errors().volleyErrors(error) + "</font>"), Snackbar.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("phone", etPhone.getText().toString().trim());
                params.put("password", etPassword.getText().toString().trim());
                params.put("latt", String.valueOf(myLocation.latitude));
                params.put("long", String.valueOf(myLocation.longitude));
                return params;
            }
        };
        queue.add(postRequest);
    }

    @Override
    public void onBackPressed() {
        pbSignInUser.setVisibility(View.GONE);
        new Alerts(SignInUser.this).exitApp();
    }


    // Trigger new location updates at interval
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);


        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.e("Dannex Daniels", "onLocationChanged: "+msg);
        // You can now create a LatLng Object for use with maps
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
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
                            onLocationChanged(location);
                        }else
                            new Alerts(SignInUser.this).showMessage("Error", "Unable to get your location. Ensure your GPS is turned on.", new MainActivity());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                        new Alerts(SignInUser.this).showMessage("Error", e.getMessage(), new MainActivity());
                    }
                });
    }
}