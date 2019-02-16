package com.dannextech.apps.nab_a_cab.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.dannextech.apps.nab_a_cab.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class Maps {

    private static final int LOCATION_REQUEST = 5000;

    private Context context;
    private GoogleMap mMaps;

    private DirectionsResult result;

    public Maps(Context context, GoogleMap mMaps) {
        this.context = context;
        this.mMaps = mMaps;
    }

    public void addMarker(LatLng latLng, String title, String type){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);

        if (type.equals("Driver")){
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.vehicle));
        }else if (type.equals("Destination")){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        mMaps.addMarker(markerOptions);
        mMaps.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17.0f));
        //mMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));

    }

    public void calculateDirections(final LatLng orig, final LatLng dest, final boolean drawPolyLine) {

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

                SharedPreferences.Editor editor = context.getSharedPreferences("request", MODE_PRIVATE).edit();
                editor.putString("destination", result.routes[0].legs[0].endAddress);
                editor.putString("origlatt", String.valueOf(orig.latitude));
                editor.putString("origlong", String.valueOf(orig.longitude));
                editor.putString("destlatt", String.valueOf(dest.latitude));
                editor.putString("destlong", String.valueOf(dest.longitude));
                editor.putString("origin", String.valueOf(result.routes[0].legs[0].startAddress));
                editor.putString("distance",String.valueOf(result.routes[0].legs[0].distance));
                editor.putString("duration",String.valueOf(result.routes[0].legs[0].duration));
                editor.apply();

                if (drawPolyLine)
                    addPolylinesToMap(result, mMaps);

                setDirectionResult(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("DannexDaniels ", "calculateDirections: Failed to get directions: " + e.getMessage());

            }
        });
    }

    public void setDirectionResult(DirectionsResult result) {
        Log.e("Dannex Daniels", "setDirectionResult: "+result);
        this.result = result;
    }

    public DirectionsResult getDirectionResult(){
        return result;
    }
    private GeoApiContext getGeoContext() {
        return new GeoApiContext.Builder()
                .apiKey("AIzaSyC5rzT4swvklbLSaGmPfFw-qVGl_efN1dQ")
                .build();
    }

    private void addPolylinesToMap(final DirectionsResult result, final GoogleMap mMap) {
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
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    polyline.setClickable(true);
                    polyline.setTag(route.legs[0].distance.toString());

                    poly.put(polyline.getId(),route.legs[0].distance.toString());

                    mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                        @Override
                        public void onPolylineClick(Polyline polyline) {
                            Toast.makeText(context,"polyline clicked"+poly.get(polyline.getId()), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                //selectVehicleType(findViewById(android.R.id.content),distance,duration,destination);


                // int approximateCost = calculateCost(selectedInfo.get("type").toString(), distance);

            }
        });
    }


}
