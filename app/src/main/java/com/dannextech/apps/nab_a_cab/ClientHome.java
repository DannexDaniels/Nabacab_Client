package com.dannextech.apps.nab_a_cab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dannextech.apps.nab_a_cab.utils.Alerts;
import com.dannextech.apps.nab_a_cab.utils.Errors;
import com.dannextech.apps.nab_a_cab.utils.Maps;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class ClientHome extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LatLng myLocation;
    private PlaceAutocompleteFragment autocompleteFragment, myPlaceAutocompleteFragment;

    private View mMapView;

    private Maps maps;
    private Alerts alerts;

    private ImageView ivSetDestination, ivOpenMenu, ivSetMyLocation;
    private LinearLayout llMyLocation, ll2w3p, ll4w3p, ll2w6p, ll4w6p;
    private Button btproceed;
    private RelativeLayout rlSelectVehicle;

    private int selectedV = 0;

    private SharedPreferences.Editor editor;

    private static final int LOCATION_REQUEST = 5000;

    private Handler handler;

    private LatLng destination,origin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMapView = mapFragment.getView();

        alerts = new Alerts(ClientHome.this);

        if (!(new Errors().isNetworkAvailable(ClientHome.this))){
            Snackbar.make(findViewById(R.id.rlClientHome),Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }

        handler = new Handler();

        ivOpenMenu = findViewById(R.id.ivOpenMenu);
        ivSetDestination = findViewById(R.id.ivSetDestination);
        ivSetMyLocation = findViewById(R.id.ivSetLocation);
        llMyLocation = findViewById(R.id.llMyLocation);
        ll2w3p = findViewById(R.id.ll2w3p);
        ll2w6p = findViewById(R.id.ll2w6p);
        ll4w3p = findViewById(R.id.ll4w3p);
        ll4w6p = findViewById(R.id.ll4w6p);
        btproceed = findViewById(R.id.btSelectVehicle);
        rlSelectVehicle = findViewById(R.id.rlSelectVehicle);

        btproceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedV == 0){
                    Snackbar.make(v,Html.fromHtml("<font color=\"#ffffff\">You need to select a vehicle to proceed</font>"),Snackbar.LENGTH_LONG).show();
                }else {
                    rlSelectVehicle.setVisibility(View.GONE);

                    handler.removeCallbacksAndMessages(null);

                    switch (selectedV){
                        case 1:
                            editor.putString("vehicle","2 Wheel");
                            editor.putString("pass","3 Passengers");
                            break;
                        case 2:
                            editor.putString("vehicle","4 Wheel");
                            editor.putString("pass","3 Passengers");
                            break;
                        case 3:
                            editor.putString("vehicle","2 Wheel");
                            editor.putString("pass","6 Passengers");
                            break;
                        case 4:
                            editor.putString("vehicle","4 Wheel");
                            editor.putString("pass","6 Passengers");
                            break;
                    }
                    editor.putString("disttime",maps.getDirectionResult().routes[0].legs[0].distance +" ("+maps.getDirectionResult().routes[0].legs[0].duration.toString()+")" );
                    editor.putString("cost", calculatedCost(maps.getDirectionResult().routes[0].legs[0].distance.toString())+" sh");
                    editor.apply();
                    startActivity(new Intent(ClientHome.this, ConfirmRequestDetails.class));
                }
            }
        });


        editor = getSharedPreferences("pickup", MODE_PRIVATE).edit();

        myPlaceAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.myplace_autocomplete_fragment);
        myPlaceAutocompleteFragment.setHint("Enter Pickup Location");
        myPlaceAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        myPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.e("Dannex Daniels", "onPlaceSelected: ");

                mMap.clear();
                maps.addMarker(place.getLatLng(),place.getName().toString(),"Client");

                myLocation = place.getLatLng();

                editor.putString("origin", place.getName().toString());
                editor.putFloat("origLatt", (float) place.getLatLng().latitude);
                editor.putFloat("origLong", (float) place.getLatLng().longitude);
                editor.apply();
                maps.calculateDirections(myLocation, destination,true);
                maps.addMarker(destination,"Destination","Destination");
            }

            @Override
            public void onError(Status status) {
                Log.e("Dannex Daniels", "onError: searching" + status.getStatusMessage());
            }
        });


        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("Enter Destination?");
        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                maps.addMarker(place.getLatLng(),place.getName().toString(),"Destination");
                maps.calculateDirections(myLocation,place.getLatLng(),true);

                destination = place.getLatLng();
                editor.putString("destination", place.getName().toString());
                editor.putFloat("destLatt", (float) place.getLatLng().latitude);
                editor.putFloat("destLong", (float) place.getLatLng().longitude);

                getDistance(place);
            }

            @Override
            public void onError(Status status) {
                Log.e("Dannex Daniels", "onError: searching" + status.getStatusMessage());
            }
        });
    }

    int counter = 0;
    private void getDistance(final Place place) {

        llMyLocation.setVisibility(View.VISIBLE);
        ivOpenMenu.setVisibility(View.INVISIBLE);
        ivSetDestination.setVisibility(View.VISIBLE);

        maps.calculateDirections(myLocation,place.getLatLng(),false);
        if (counter == 0)
            alerts.showProgressDialog(null,"Loading...");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (maps.getDirectionResult() == null){
                    alerts.hideProgressDialog();
                    counter++;
                    alerts.showProgressDialog(null,"Your internet connection is slow. Please wait...");
                    getDistance(place);
                }else {
                    alerts.hideProgressDialog();
                    rlSelectVehicle.setVisibility(View.VISIBLE);
                    Log.e("Dannex Daniels", "onPlaceSelected: "+maps.getDirectionResult());
                }
            }
        },5000);

    }

    private int calculatedCost(String distance) {
            //get the float value of the distance
        double dist = Double.parseDouble(distance.replace(" km",""));
        double cost = 0;

        if (dist<= 5.0){
            cost = 400;
        }else {
            cost = dist * 80;
        }

        return (int) cost;

    }


    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String address = "Not Set";
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();

            address = obj.getAddressLine(0);
            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Log.e("IGA", "getAddress: "+address);
        return address;
    }

    public void w2p3Selected(View v){
        ll2w3p.setBackgroundResource(R.color.colorPrimary);
        ll2w6p.setBackgroundResource(R.color.textColor);
        ll4w3p.setBackgroundResource(R.color.textColor);
        ll4w6p.setBackgroundResource(R.color.textColor);
        selectedV = 1;
    }

    public void w4p3Selected(View v){
        ll2w3p.setBackgroundResource(R.color.textColor);
        ll2w6p.setBackgroundResource(R.color.textColor);
        ll4w3p.setBackgroundResource(R.color.colorPrimary);
        ll4w6p.setBackgroundResource(R.color.textColor);
        selectedV = 2;
    }

    public void w2p6Selected(View v){
        ll2w3p.setBackgroundResource(R.color.textColor);
        ll2w6p.setBackgroundResource(R.color.colorPrimary);
        ll4w3p.setBackgroundResource(R.color.textColor);
        ll4w6p.setBackgroundResource(R.color.textColor);
        selectedV = 3;
    }


    public void w4p6Selected(View v){
        ll2w3p.setBackgroundResource(R.color.textColor);
        ll2w6p.setBackgroundResource(R.color.textColor);
        ll4w3p.setBackgroundResource(R.color.textColor);
        ll4w6p.setBackgroundResource(R.color.colorPrimary);
        selectedV = 4;
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

        maps = new Maps(ClientHome.this, mMap);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST);
            return;
        }

        //set be able to get my location
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);



        /*View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 180, 180, 0);
*/
        getLastLocation();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mMap.setMyLocationEnabled(true);
                }
                break;

        }
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.e("Dannex Daniels", "onLocationChanged: "+msg);
        // You can now create a LatLng Object for use with maps
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());

        //maps.addMarker(myLocation, "My Location","Client");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17.0f));

        //myPlaceAutocompleteFragment.setText(getAddress(myLocation.latitude, myLocation.longitude));
        editor.putString("origin",getAddress(myLocation.latitude, myLocation.longitude));
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
                            new Alerts(ClientHome.this).showMessage("Error", "Unable to get your location. Ensure your GPS is turned on.", new MainActivity());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                        new Alerts(ClientHome.this).showMessage("Error", e.getMessage(), new MainActivity());
                    }
                });
    }
}
