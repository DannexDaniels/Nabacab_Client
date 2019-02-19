package com.dannextech.apps.nab_a_cab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class DriveToDestination extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    private static final int LOCATION_REQUEST = 5000;

    private Maps maps;

    private TextView tvPhone, tvDestination, tvName;
    private Button btnStartJourney, btnFinishJourney,btnShowClientDetails;
    private LinearLayout llDriverDetails;
    private Toolbar tbDriveToDestination;


    private SharedPreferences locDet, client, request;

    private LatLng dest = null;
    private JSONObject jsonObject1 = null;

    private Boolean journeyStarted = false;

    private LatLng myLocation = null;

    private Marker origin, destination;
    private Polyline polyline;

    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_to_destination);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!new Errors().isNetworkAvailable(getApplicationContext())){
            Snackbar.make(findViewById(R.id.rlDriveToDestination),Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }

        counter = 0;

        try {
            jsonObject1 = new JSONObject(getIntent().getStringExtra("client"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tvDestination = findViewById(R.id.tvClientDestination);
        tvName = findViewById(R.id.tvClientName);
        tvPhone = findViewById(R.id.tvClientPhone);

        btnFinishJourney = findViewById(R.id.btnFinishJourney);
        btnStartJourney = findViewById(R.id.btnStartJourney);
        btnShowClientDetails = findViewById(R.id.btnShowClient);

        llDriverDetails = findViewById(R.id.llClientDetails);

        tbDriveToDestination = findViewById(R.id.toolbarDriveToDestination);

        client = getSharedPreferences("customer", MODE_PRIVATE);
        locDet = getSharedPreferences("request", MODE_PRIVATE);
        request = getSharedPreferences("req", MODE_PRIVATE);

        tvPhone.setText(client.getString("phone",""));
        tvName.setText(client.getString("name",""));
        tvDestination.setText(client.getString("destination",""));

        btnStartJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                try {
                    dest = new LatLng(Double.parseDouble(jsonObject1.getString("destlatt")),Double.parseDouble(jsonObject1.getString("destlong")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Add marker of user's position
                MarkerOptions userIndicator = new MarkerOptions()
                        .position(dest)
                        .title("Client Location");

                destination = mMap.addMarker(userIndicator);
                journeyStatus("start");
            }
        });

        btnFinishJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPayment();
                journeyStatus("stop");
            }
        });


        startLocationUpdates();
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            dest = new LatLng(Double.parseDouble(jsonObject1.getString("origlatt")),Double.parseDouble(jsonObject1.getString("origlong")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Add marker of user's position
        MarkerOptions userIndicator = new MarkerOptions()
                .position(dest)
                .title("Client Location");

        destination = mMap.addMarker(userIndicator);

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);


    }


    private void updateLocation(final LatLng latLng) {
        //post my location
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/update_location";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        JSONObject jsonObject1 = null;
                        Log.e("ResponseResult", response);
                        try {
                            jsonObject1 = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            if (jsonObject1.getString("status").equals("OK")){
                                Log.e("Dannex Daniels", "onResponse: location updated" );
                            }else{
                                Log.e("Dannex Daniels", "onResponse: error while updating location" );
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.ResponseResult", error.toString());
                        Log.d("Error.ResponseResult", error.toString());
                        Snackbar.make(findViewById(R.id.rlDriveToDestination), new Errors().volleyErrors(error), Snackbar.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                SharedPreferences client = getSharedPreferences("driver", MODE_PRIVATE);
                params.put("phone", client.getString("phone",""));
                params.put("latt", String.valueOf(latLng.latitude));
                params.put("long", String.valueOf(latLng.longitude));

                return params;
            }
        };
        queue.add(postRequest);
    }


    private void journeyStatus(final String status) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/update_journey_status";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Dannex Daniels", "journey status: "+response);
                if (status.equals("start")){
                    journeyStarted = true;
                    llDriverDetails.setVisibility(View.GONE);
                    btnShowClientDetails.setVisibility(View.GONE);
                    tbDriveToDestination.setVisibility(View.GONE);
                    btnFinishJourney.setVisibility(View.VISIBLE);
                }else if (status.equals("stop")){
                    startActivity(new Intent(DriveToDestination.this,WaitClientPayment.class));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Dannex Daniels", "journey status: "+error);
                Snackbar.make(findViewById(R.id.rlDriveToDestination),Html.fromHtml("<font color=\"#ffffff\">"+new Errors().volleyErrors(error)+"</font>"),Snackbar.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
            Map<String, String>  params = new HashMap<String, String>();
                try {
                    if (status.equals("start")){
                        params.put("id", jsonObject1.getString("id"));
                        params.put("phone",jsonObject1.getString("driver_phone"));
                        params.put("driver_latt",String.valueOf(myLocation.latitude));
                        params.put("driver_long",String.valueOf(myLocation.longitude));
                        params.put("status", "started");
                    }
                    else{
                        params.put("id", jsonObject1.getString("id"));
                        params.put("phone",jsonObject1.getString("driver_phone"));
                        params.put("driver_latt",String.valueOf(myLocation.latitude));
                        params.put("driver_long",String.valueOf(myLocation.longitude));
                        params.put("status", "finished");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }



                return params;
            }
        };
        queue.add(postRequest);
    }

    public void callClient(View v){
        Uri number = Uri.parse("tel:"+tvPhone.getText().toString());
        startActivity(new Intent(Intent.ACTION_DIAL,number));
    }

    public void showClientDetails(View v){
        if (counter%2 == 0) {
            llDriverDetails.setVisibility(View.VISIBLE);
            btnShowClientDetails.setText("Hide Client Details");
        }else {
            llDriverDetails.setVisibility(View.GONE);
            btnShowClientDetails.setText("Show Client Details");
        }

        counter++;

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

        // Add marker of user's position
        MarkerOptions userIndicator = new MarkerOptions()
                .position(myLocation)
                .title("My Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.vehicle));
        if(origin != null && polyline != null){
            origin.remove();
            polyline.remove();
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,17.0f));
        origin = mMap.addMarker(userIndicator);

        calculateDirections(myLocation,dest);

        updateLocation(myLocation);

        if (!journeyStarted)
            Snackbar.make(findViewById(R.id.rlDriveToDestination),Html.fromHtml("<font color=\"#ffffff\">Client is "+locDet.getString("duration","")+" away</font>"),Snackbar.LENGTH_SHORT).show();
    }

    public void calculateDirections(final LatLng orig, final LatLng dest) {

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                dest.latitude,
                dest.longitude
        );

        DirectionsApiRequest directions = new DirectionsApiRequest(getGeoContext());

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        orig.latitude,
                        orig.longitude
                )
        );
        Log.d("DannexDaniels", "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d("DannexDaniels", "calculateDirections: routes: " + result.routes[0].toString());
                Log.d("DannexDaniels", "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d("DannexDaniels", "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d("DannexDaniels", "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                SharedPreferences.Editor editor = getSharedPreferences("request", MODE_PRIVATE).edit();
                editor.putString("destination", result.routes[0].legs[0].endAddress);
                editor.putString("origlatt", String.valueOf(orig.latitude));
                editor.putString("origlong", String.valueOf(orig.longitude));
                editor.putString("destlatt", String.valueOf(dest.latitude));
                editor.putString("destlong", String.valueOf(dest.longitude));
                editor.putString("origin", String.valueOf(result.routes[0].legs[0].startAddress));
                editor.putString("distance",String.valueOf(result.routes[0].legs[0].distance));
                editor.putString("duration",String.valueOf(result.routes[0].legs[0].duration));
                editor.apply();

                addPolylinesToMap(result);

            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("DannexDaniels ", "calculateDirections: Failed to get directions: " + e.getMessage());

            }
        });
    }

    private GeoApiContext getGeoContext() {
        return new GeoApiContext.Builder()
                .apiKey("AIzaSyC5rzT4swvklbLSaGmPfFw-qVGl_efN1dQ")
                .build();
    }

    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d("DannexDaniels", "run: result routes: " + result.routes.length);

                String distance = result.routes[0].legs[0].distance.toString();
                String duration = result.routes[0].legs[0].duration.toString();
                String destination = result.routes[0].legs[0].endAddress;

                final HashMap<String, String> poly = new HashMap<>();

                for (DirectionsRoute route : result.routes) {
                    Log.d("DannexDaniels", "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (com.google.maps.model.LatLng latLng : decodedPath) {

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(DriveToDestination.this, R.color.colorPrimary));
                    polyline.setClickable(true);
                    polyline.setTag(route.legs[0].distance.toString());

                    poly.put(polyline.getId(),route.legs[0].distance.toString());

                    mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                        @Override
                        public void onPolylineClick(Polyline polyline) {
                            Toast.makeText(DriveToDestination.this,"polyline clicked"+poly.get(polyline.getId()), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    public void addPayment(){
        //post my user details
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/Payments/make_payment";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
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

                        if (jsonObject == null){
                            Snackbar.make(findViewById(R.id.clMakePayment),Html.fromHtml("<font color=\"#ffffff\">An error has occurred. Please try again</font>"),Snackbar.LENGTH_SHORT).show();
                        }else {
                            try {

                                String status = jsonObject.getString("status");
                                if (status.equals("OK")){
                                    startActivity(new Intent(DriveToDestination.this, WaitClientPayment.class));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Snackbar.make(findViewById(R.id.clSignUpUser), Html.fromHtml("<font color=\"#ffffff\">An error has occurred. Please try again</font>"),Snackbar.LENGTH_SHORT).show();
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
                        Snackbar.make(findViewById(R.id.clMakePayment), Html.fromHtml("<font color=\"#ffffff\">"+new Errors().volleyErrors(error)+"</font>"),Snackbar.LENGTH_LONG).show();

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("amount", request.getString("charges",""));
                params.put("amount","1");
                params.put("mode", "not set");
                params.put("client", request.getString("client_phone",""));
                params.put("driver", request.getString("vehicle",""));
                params.put("id", request.getString("reqid",""));
                return params;
            }
        };
        queue.add(postRequest);

    }


}
