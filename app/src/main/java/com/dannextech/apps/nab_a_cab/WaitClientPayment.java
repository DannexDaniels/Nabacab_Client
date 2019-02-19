package com.dannextech.apps.nab_a_cab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dannextech.apps.nab_a_cab.utils.Alerts;
import com.dannextech.apps.nab_a_cab.utils.Errors;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WaitClientPayment extends AppCompatActivity {

    private Alerts alert;

    private TextView tvPaymentMsg;

    private SharedPreferences request;

    private Button btConfirmCashPayment, btConfirmPayment;

    private Handler handler;

    private int counter = 0;

    private String mPesaStatus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_client_payment);

        if (!new Errors().isNetworkAvailable(getApplicationContext())){
            Snackbar.make(findViewById(R.id.clFindDriver),Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }
        alert = new Alerts(WaitClientPayment.this);
        alert.showProgressDialog("","Loading...");

        tvPaymentMsg = findViewById(R.id.tvPaymentMsg);

        btConfirmCashPayment = findViewById(R.id.btConfirmCashPayment);
        btConfirmPayment = findViewById(R.id.btConfirmPayment);

        request = getSharedPreferences("req", MODE_PRIVATE);

        handler = new Handler();


        checkPayments();

        btConfirmCashPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePayment("paid");
            }
        });

        btConfirmPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPesaStatus.equals("success")){

                }
            }
        });
    }

    private void checkPayments() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/Payments/check_payment";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JSONObject jsonObject = null;
                Log.e("ResponseResult", "res = "+response);
                if (!response.equals("null")){
                    try {

                        jsonObject = new JSONObject(response);
                        String pmode = jsonObject.getString("pay_mode");
                        if (pmode.equals("not set")){
                            checkPayments();
                        }else if (pmode.equals("cash")){
                            alert.hideProgressDialog();
                            btConfirmCashPayment.setVisibility(View.VISIBLE);
                            tvPaymentMsg.setText("Receive in Cash \nsh. "+jsonObject.getString("pay_amount")+" \nfrom the client");
                        }else if (pmode.equals("mpesa")){

                            final JSONObject finalJsonObject = jsonObject;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (counter < 30){
                                        Log.e("DannexDaniels", "run: less counter"+counter);
                                        checkMpesaPayment(finalJsonObject);
                                        counter++;
                                    }else {
                                        Log.e("DannexDaniels", "run: more counter"+counter);
                                        tvPaymentMsg.setText("M-Pesa Payment failed");

                                        updatePayment("failed");
                                    }
                                    handler.postDelayed(this, 2000);
                                }
                            },2000);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {

                    checkPayments();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Log.d("Error.ResponseResult", error.toString());
                Snackbar.make(findViewById(R.id.clWaitClientPayment), Html.fromHtml("<font color=\"#ffffff\">"+new Errors().volleyErrors(error)+"</font>"),Snackbar.LENGTH_LONG).show();

            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("id", request.getString("reqid",""));
                //params.put("id","REQ29728311");
                return params;
            }
        };

        queue.add(postRequest);
    }

    private void checkMpesaPayment(final JSONObject jsonObject) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/Payments/check_Mpesa_Payment";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JSONObject jsonObject1 = null;
                Log.e("ResponseResult", "res = "+response);
                if (!response.equals("null")){
                    try {

                        jsonObject1 = new JSONObject(response);
                        String pmode = jsonObject1.getString("result_code");
                        if (pmode.equals("0")){
                            alert.hideProgressDialog();
                            mPesaStatus = "success";
                            tvPaymentMsg.setText("M-Pesa Payment of\nsh. "+jsonObject1.getString("amount")+" \nis successful");
                            btConfirmPayment.setVisibility(View.VISIBLE);
                            updatePayment("paid");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Log.d("Error.ResponseResult", error.toString());
                Snackbar.make(findViewById(R.id.clWaitClientPayment), Html.fromHtml("<font color=\"#ffffff\">"+new Errors().volleyErrors(error)+"</font>"),Snackbar.LENGTH_LONG).show();

            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                try {
                    params.put("id", jsonObject.getString("pay_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };

        queue.add(postRequest);
    }

    public void updatePayment(final String status){
        handler.removeCallbacksAndMessages(null);

        //post my user details
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/Payments/update_payment_status";
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
                            Snackbar.make(findViewById(R.id.clWaitClientPayment),Html.fromHtml("<font color=\"#ffffff\">An error has occurred. Please try again</font>"),Snackbar.LENGTH_SHORT).show();
                        }else {
                            try {

                                String statuss = jsonObject.getString("status");
                                if (statuss.equals("OK") && status.equals("paid")){
                                    if (mPesaStatus == null){
                                        startActivity(new Intent(WaitClientPayment.this, DriverHome.class));
                                    }
                                }else if (status.equals("failed")){
                                    alert.hideProgressDialog();
                                    mPesaStatus = "fail";
                                    btConfirmPayment.setVisibility(View.VISIBLE);
                                    //alert.showMessage("Error","Mpesa Payment not Successful", new WaitClientPayment());
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Snackbar.make(findViewById(R.id.clWaitClientPayment), Html.fromHtml("<font color=\"#ffffff\">An error has occurred. Please try again</font>"),Snackbar.LENGTH_SHORT).show();
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
                        Snackbar.make(findViewById(R.id.clWaitClientPayment), Html.fromHtml("<font color=\"#ffffff\">"+new Errors().volleyErrors(error)+"</font>"),Snackbar.LENGTH_LONG).show();

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                params.put("id", request.getString("reqid",""));
                //params.put("id","REQ29728311");
                params.put("status", status);
                params.put("user","driver");

                return params;
            }
        };
        queue.add(postRequest);

    }

    public void goBackFinishJourney(View v){
        finish();
    }

}
