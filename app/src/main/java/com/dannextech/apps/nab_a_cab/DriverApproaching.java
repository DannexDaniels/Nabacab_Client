package com.dannextech.apps.nab_a_cab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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

public class DriverApproaching extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int LOCATION_REQUEST = 5000;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private LatLng myLocation;

    private JSONObject jsonObject = null;
    private Maps maps;

    private Alerts alerts;

    private SharedPreferences locDet;

    private Button btnCancelJourney, btnShowDriverDetails;
    private TextView tvPhone, tvName, tvVehicle, tvPlate;
    private LinearLayout llDriverDetails;

    private Handler handler1;

    private Marker origin, destination;
    private Polyline polyline;


    int counter, finCounter;

    private LatLng driverLoc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_approaching);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!new Errors().isNetworkAvailable(getApplicationContext())){
            Snackbar.make(findViewById(R.id.clSignUpUser),Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }

        handler1 = new Handler();

        try {
            jsonObject = new JSONObject(getIntent().getStringExtra("client"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        counter = 0;
        finCounter = 0;

        alerts = new Alerts(DriverApproaching.this);
        alerts.showProgressDialog("","Loading...");

        btnCancelJourney = findViewById(R.id.btnCancelJourney);
        btnShowDriverDetails = findViewById(R.id.btnShowDriver);

        tvName = findViewById(R.id.tvDriverName);
        tvPhone = findViewById(R.id.tvDriverPhone);
        tvPlate = findViewById(R.id.tvPlateNo);
        tvVehicle = findViewById(R.id.tvVehicleType);

        llDriverDetails = findViewById(R.id.llDriverDetails);

        locDet = getSharedPreferences("request", MODE_PRIVATE);

        SharedPreferences.Editor editor = locDet.edit();

        /*try {
            jsonObject = new JSONObject(getIntent().getStringExtra("client"));
            editor.putString("client",jsonObject.getString("phone"));
            editor.putString("driver",jsonObject.getString("driver_phone"));
            editor.apply();
            Log.e("Dannex Daniels", "onCreate: "+jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/


        btnCancelJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(DriverApproaching.this)
                        .setTitle("Confirm")
                        .setMessage("Do you wish to Cancel Journey?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();            }
        });
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

        maps = new Maps(DriverApproaching.this, mMap);



       // maps.addMarker(myLocation,"My Location","Client");
        initMethod();

        try {
            myLocation = new LatLng(Double.parseDouble(jsonObject.getString("origlatt")),Double.parseDouble(jsonObject.getString("origlong")));
            // Add marker of user's position
            MarkerOptions userIndicator = new MarkerOptions()
                    .position(myLocation)
                    .title("Driver's Location");
            destination = mMap.addMarker(userIndicator);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void initMethod() {
        handler1.postDelayed(new Runnable(){
            public void run(){
                getLocation();
                try {
                    getRequestDetails(jsonObject.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler1.postDelayed(this, 5000);
            }
        }, 5000);

    }

    private void getLocation() {
        //post my location
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/get_location";
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

                            driverLoc = new LatLng(Double.parseDouble(jsonObject1.getString("user_latt")),Double.parseDouble(jsonObject1.getString("user_long")));
                            // Add marker of user's position
                            MarkerOptions userIndicator = new MarkerOptions()
                                    .position(driverLoc)
                                    .title("Driver's Location")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.vehicle));
                            if(origin != null && polyline != null){
                                origin.remove();
                                polyline.remove();
                            }

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLoc));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLoc,17.0f));
                            origin = mMap.addMarker(userIndicator);

                            if (finCounter == 0){
                                calculateDirections(driverLoc,myLocation);
                            }


                            Snackbar.make(findViewById(R.id.rlDriverApproaching),Html.fromHtml("<font color=\"#ffffff\">Driver is "+locDet.getString("duration","")+" away</font>"),Snackbar.LENGTH_SHORT).show();

                            alerts.hideProgressDialog();
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
                        Snackbar.make(findViewById(R.id.rlDriverApproaching), Html.fromHtml("<font color=\"#ffffff\">"+new Errors().volleyErrors(error)+"</font>"), Snackbar.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();


                try {
                    params.put("phone", jsonObject.getString("driver_phone"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return params;
            }
        };
        queue.add(postRequest);
    }

    private void getRequestDetails(final String requestId) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/check_client_request";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Dannex Daniels", "onResponseAll: "+response);
                        if (!response.equals("null")){
                            try {
                                final JSONObject jsonObject1 = new JSONObject(response);

                                tvName.setText(jsonObject1.getString("driver"));
                                tvPhone.setText(jsonObject1.getString("driver_phone"));
                                tvPlate.setText(jsonObject1.getString("vehicle_no"));
                                tvVehicle.setText(jsonObject1.getString("vehicle_type"));

                                if (jsonObject1.getString("journey_status").equals("finished")){
                                    handler1.removeCallbacksAndMessages(null);
                                    startActivity(new Intent(DriverApproaching.this,MakePayment.class));
                                }else if(jsonObject1.getString("journey_status").equals("started")){
                                    btnCancelJourney.setVisibility(View.GONE);

                                    LatLng dest = new LatLng(Double.parseDouble(jsonObject.getString("destlatt")),Double.parseDouble(jsonObject.getString("destlong")));
                                    if (finCounter == 0){
                                        mMap.clear();
                                        // Add marker of user's position
                                        MarkerOptions userIndicator = new MarkerOptions()
                                                .position(myLocation)
                                                .title("Driver's Location");
                                        destination = mMap.addMarker(userIndicator);
                                    }
                                    calculateDirections(driverLoc,dest);
                                }
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
                        Snackbar.make(findViewById(R.id.rlDriverApproaching), new Errors().volleyErrors(error), Snackbar.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("requestId", requestId);
                return params;
            }
        };
        queue.add(postRequest);
    }

    public void showDriverDetails(View v){
        if (counter%2 == 0) {
            llDriverDetails.setVisibility(View.VISIBLE);
            btnShowDriverDetails.setText("Hide Driver Details");
        }else {
            llDriverDetails.setVisibility(View.GONE);
            btnShowDriverDetails.setText("Show Driver Details");
        }

        counter++;

    }

    public void callDriver(View v){
        Uri number = Uri.parse("tel:"+tvPhone.getText().toString());
        startActivity(new Intent(Intent.ACTION_DIAL,number));
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
                    polyline.setColor(ContextCompat.getColor(DriverApproaching.this, R.color.colorPrimary));
                    polyline.setClickable(true);
                    polyline.setTag(route.legs[0].distance.toString());

                    poly.put(polyline.getId(),route.legs[0].distance.toString());

                    mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                        @Override
                        public void onPolylineClick(Polyline polyline) {
                            Toast.makeText(DriverApproaching.this,"polyline clicked"+poly.get(polyline.getId()), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }




}
