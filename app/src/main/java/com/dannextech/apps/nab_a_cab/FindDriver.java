package com.dannextech.apps.nab_a_cab;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
import com.dannextech.apps.nab_a_cab.utils.Errors;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FindDriver extends AppCompatActivity {

    private static final int LOCATION_REQUEST = 5000;
    private FusedLocationProviderClient mFusedLocationClient;

    private LatLng myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);

        if (!new Errors().isNetworkAvailable(getApplicationContext())){
            Snackbar.make(findViewById(R.id.clFindDriver),Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }

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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.e("DannexDaniels", "onSucess: success"+ myLocation.latitude);

                    getDriver(myLocation);
                }
            }
        });
    }


    private void getDriver(final LatLng myLoc) {
        //post my location
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/get_closest_driver";
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

                        try {
                            if (jsonObject.getString("driveTime").equals("null") || jsonObject.get("driveTime").toString().equals("null")){
                                AlertDialog alertDialog = new AlertDialog.Builder(FindDriver.this).create();
                                alertDialog.setTitle("Sorry");
                                alertDialog.setMessage("The isn't any driver available at the moment");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        startActivity(new Intent(FindDriver.this, ClientHome.class));
                                    }
                                });
                                alertDialog.show();
                            }else{
                                Log.e("ResponseResult", jsonObject.getString("driver"));
                                Log.e("ResponseResult",jsonObject.getString("phone"));
                                sendDriveRequest(jsonObject);
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
                        Snackbar.make(findViewById(R.id.clFindDriver), new Errors().volleyErrors(error), Snackbar.LENGTH_LONG).show();
                        getDriver(myLoc);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("latlng", myLoc.latitude+",%20"+myLoc.longitude);
                return params;
            }
        };
        queue.add(postRequest);
    }


    private void sendDriveRequest(final JSONObject jsonObject) {
        //post my location
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/add_drive_request";
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
                                Toast.makeText(FindDriver.this, "Request has been Sent",Toast.LENGTH_LONG).show();
                                startActivity(new Intent(FindDriver.this, WaitDriverResponse.class));
                            }else{
                                Toast.makeText(FindDriver.this, "Request Failed to be Sent",Toast.LENGTH_LONG).show();
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
                        Snackbar.make(findViewById(R.id.clFindDriver), new Errors().volleyErrors(error), Snackbar.LENGTH_LONG).show();
                        sendDriveRequest(jsonObject);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                SharedPreferences client = getSharedPreferences("client", MODE_PRIVATE);
                SharedPreferences request = getSharedPreferences("request", MODE_PRIVATE);
                SharedPreferences pickup = getSharedPreferences("pickup", MODE_PRIVATE);

                params.put("client", client.getString("phone",""));
                try {
                    params.put("driver",jsonObject.getString("license"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                params.put("destination", request.getString("destination",""));
                params.put("origlatt", request.getString("origlatt",""));
                params.put("origlong", request.getString("origlong",""));
                params.put("destlatt", request.getString("destlatt",""));
                params.put("destlong", request.getString("destlong",""));
                params.put("charges", pickup.getString("cost",""));
                return params;
            }
        };
        queue.add(postRequest);
    }

    public void goBackConfirmPickup(View v){
        finish();
    }

    /*private void showDriver(final JSONObject jsonObject) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.driver_detail_pop_up, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.NO_GRAVITY, 0, 0);

        TextView tvname = popupView.findViewById(R.id.tvNamePopup);
        TextView tvgender = popupView.findViewById(R.id.tvGenderPopup);
        TextView tvphone = popupView.findViewById(R.id.tvPhonePopup);
        TextView tvtime = popupView.findViewById(R.id.tvtimePopup);
        TextView tvtype = popupView.findViewById(R.id.tvtypePopup);
        TextView tvplate = popupView.findViewById(R.id.tvPlatePopup);
        TextView tvsize = popupView.findViewById(R.id.tvSizePopup);

        try {
            tvname.setText(jsonObject.getString("driver"));
            tvgender.setText(jsonObject.getString("gender"));
            tvphone.setText(jsonObject.getString("phone"));
            tvtime.setText(jsonObject.getString("driveTime"));
            tvtype.setText(jsonObject.getString("type"));
            tvplate.setText(jsonObject.getString("vehicle"));
            tvsize.setText(jsonObject.getString("capacity")+" seater");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        popupView.findViewById(R.id.btAcceptDriver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

            }
        });

        popupView.findViewById(R.id.btRejectDriver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FindDriver.this,ClientHome.class));
            }
        });
    }*/

}
