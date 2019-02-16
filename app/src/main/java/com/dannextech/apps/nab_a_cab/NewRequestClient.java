package com.dannextech.apps.nab_a_cab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dannextech.apps.nab_a_cab.utils.Alerts;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class NewRequestClient extends AppCompatActivity {

    private ImageView ivcircle1, ivcircle2, ivcircle3;
    private Button btAccept,btReject;
    private TextView tvName,tvPhone,tvDestination,tvCost;

    private Ringtone ringtone;
    private Vibrator vibe;

    private Handler handler;

    private LatLng myLocation;

    private JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final int[] counter = {0};
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request_client);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        vibe  = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        ringtone = RingtoneManager.getRingtone(NewRequestClient.this,uri);
        ringtone.play();

        ivcircle1  = findViewById(R.id.ivCircle1);
        ivcircle2 = findViewById(R.id.ivCircle2);
        ivcircle3 = findViewById(R.id.ivCircle3);
        btAccept = findViewById(R.id.btAcceptClient);
        btReject = findViewById(R.id.btRejectClient);
        tvName = findViewById(R.id.tvClientNameRequest);
        tvPhone = findViewById(R.id.tvClientPhoneRequest);
        tvDestination = findViewById(R.id.tvClientDestinationRequest);
        tvCost = findViewById(R.id.tvClientCharges);

        getLastLocation();



        try {
            jsonObject = new JSONObject(getIntent().getStringExtra("client"));
            tvName.setText(jsonObject.getString("client"));
            tvPhone.setText(jsonObject.getString("phone"));
            tvDestination.setText(jsonObject.getString("destination"));
            tvCost.setText(jsonObject.getString("charges"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("Dannex", "run: "+ counter[0] );

                vibe.vibrate(800);

                if (counter[0] %3==0){
                    ivcircle1.setVisibility(View.VISIBLE);
                    ivcircle2.setVisibility(View.INVISIBLE);
                    ivcircle3.setVisibility(View.INVISIBLE);
                }else if (counter[0] %3==1){
                    ivcircle1.setVisibility(View.VISIBLE);
                    ivcircle2.setVisibility(View.VISIBLE);
                    ivcircle3.setVisibility(View.INVISIBLE);
                }else {
                    ivcircle1.setVisibility(View.VISIBLE);
                    ivcircle2.setVisibility(View.VISIBLE);
                    ivcircle3.setVisibility(View.VISIBLE);
                }
                counter[0]++;


                handler.postDelayed(this,1000);
            }
        },1000);

        btAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                handler.removeCallbacksAndMessages(null);
                ringtone.stop();
                acceptRide();
            }
        });

        btReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacksAndMessages(null);
                ringtone.stop();
                rejectRide();
            }
        });

    }

    private void rejectRide() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/update_drive_request";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.e("ResponseResult", response);
                        handler.removeCallbacksAndMessages(null);
                        startActivity(new Intent(NewRequestClient.this, DriverHome.class));
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
                try {
                    params.put("id", jsonObject.getString("id"));
                    params.put("phone",jsonObject.getString("driver_phone"));
                    params.put("status", "rejected");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };
        queue.add(postRequest);
    }

    private void acceptRide() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/update_drive_request";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.e("ResponseResult", response);



                        if (response != null){
                            try {
                                SharedPreferences.Editor editor = getSharedPreferences("customer", MODE_PRIVATE).edit();
                                editor.putString("name", jsonObject.getString("client"));
                                editor.putString("phone", jsonObject.getString("phone"));
                                editor.putString("destination", jsonObject.getString("destination"));
                                editor.apply();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            handler.removeCallbacksAndMessages(null);
                            startActivity(new Intent(NewRequestClient.this, DriveToDestination.class).putExtra("client",jsonObject.toString()).putExtra("location",myLocation));
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
                try {
                    params.put("id", jsonObject.getString("id"));
                    params.put("phone",jsonObject.getString("driver_phone"));
                    params.put("status", "accepted");
                    params.put("driver_latt",String.valueOf(myLocation.latitude));
                    params.put("driver_long",String.valueOf(myLocation.longitude));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                            onLocationChanged(location);
                        }else
                            new Alerts(NewRequestClient.this).showMessage("Error", "Unable to get your location. Ensure your GPS is turned on.", new MainActivity());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                        new Alerts(NewRequestClient.this).showMessage("Error", e.getMessage(), new MainActivity());
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

    }

}
