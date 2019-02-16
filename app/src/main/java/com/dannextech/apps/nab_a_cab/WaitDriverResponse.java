package com.dannextech.apps.nab_a_cab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WaitDriverResponse extends AppCompatActivity {

    private CircularProgressBar circularProgressBar;
    private TextView tvTimeCounter;
    private boolean responded = false;

    private Alerts alerts;

    private CountDownTimer count;

    private int cc = 0;
    private String id = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_driver_response);

        circularProgressBar = findViewById(R.id.cpTimeWait);
        tvTimeCounter = findViewById(R.id.tvTimeCounterWait);

        alerts = new Alerts(WaitDriverResponse.this);

        if (!new Errors().isNetworkAvailable(getApplicationContext())){
            Snackbar.make(findViewById(R.id.clSignUpUser),Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }

        int animationDuration = 60000; // 3000ms = 3s
        circularProgressBar.setProgressWithAnimation(100, animationDuration);

        count = new CountDownTimer(animationDuration,1000){
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimeCounter.setText(millisUntilFinished/1000 +"s");
                if (cc == 0 && id == null)
                    getRequestResult();
                else
                    getRequestDetails(id);
            }

            @Override
            public void onFinish() {
                if (!responded){
                    alerts.showMessage("Sorry","We could not find a driver near you",new ClientHome());
                }
            }
        }.start();
    }

    public void getRequestResult(){
        final SharedPreferences.Editor editor = getSharedPreferences("request", MODE_PRIVATE).edit();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/get_request_id";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Dannex Daniels", "onResponseId: "+response);
                        if (!response.equals("null")){
                            id = response;
                            editor.putString("id",response);
                            editor.apply();
                            cc++;
                            getRequestDetails(response);
                        }else{
                            Log.e("Dannex Daniels", "onResponse: waiting for driver to respond"+response);
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.ResponseResult", error.toString());
                        Snackbar.make(findViewById(R.id.clWaitDriverResponse), new Errors().volleyErrors(error), Snackbar.LENGTH_LONG).show();
                        getRequestResult();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                SharedPreferences client = getSharedPreferences("client", MODE_PRIVATE);
                params.put("phone", client.getString("phone",""));
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
                                final JSONObject jsonObject = new JSONObject(response);
                                responded = true;
                                if (jsonObject.getString("status").equals("accepted")){
                                    count.cancel();
                                    new android.support.v7.app.AlertDialog.Builder(WaitDriverResponse.this)
                                            .setTitle("Request Accepted")
                                            .setMessage("The driver is coming to pick you up shortly")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    startActivity(new Intent(WaitDriverResponse.this,DriverApproaching.class).putExtra("client",jsonObject.toString()));
                                                    dialog.dismiss();
                                                }
                                            })
                                            .create()
                                            .show();
                                    //alerts.showMessage("Request Accepted","The driver is coming to pick you up shortly",new DriverApproaching());
                                }else if(jsonObject.getString("status").equals("rejected")){
                                    count.cancel();
                                    alerts.showMessage("Request Rejected","The driver has rejected your request. Try getting another ride",new ClientHome());
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
                        Snackbar.make(findViewById(R.id.clWaitDriverResponse), new Errors().volleyErrors(error), Snackbar.LENGTH_LONG).show();
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
}
