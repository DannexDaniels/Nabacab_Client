package com.dannextech.apps.nab_a_cab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

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

public class PaymentConfirmation extends AppCompatActivity {

    private LinearLayout llpaymentSuccess, llPaymentFailed;

    private Alerts alert;

    private JSONObject jsonObject1;

    private SharedPreferences client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirmation);

        llpaymentSuccess = findViewById(R.id.llPaymentSuccess);
        llPaymentFailed = findViewById(R.id.llPaymentFailed);

        if (!new Errors().isNetworkAvailable(getApplicationContext())){
            Snackbar.make(findViewById(R.id.clFindDriver),Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }

        alert = new Alerts(PaymentConfirmation.this);
        alert.showProgressDialog("","Loading...");

        client = getSharedPreferences("payment", MODE_PRIVATE);

        checkPayments();
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
                        String pStatus = jsonObject.getString("pay_status");
                        if (pStatus.equals("pending")){
                            checkPayments();
                        }else if (pStatus.equals("paid")){
                            alert.hideProgressDialog();
                            llpaymentSuccess.setVisibility(View.VISIBLE);

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    handler.removeCallbacksAndMessages(null);
                                    startActivity(new Intent(PaymentConfirmation.this,ClientHome.class));
                                }
                            },5000);
                        }else if (pStatus.equals("failed")){
                            alert.hideProgressDialog();
                            llPaymentFailed.setVisibility(View.VISIBLE);

                            jsonObject1 = new JSONObject();
                            jsonObject1.put("id",client.getString("request",""));

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    handler.removeCallbacksAndMessages(null);
                                    startActivity(new Intent(PaymentConfirmation.this,MakePayment.class).putExtra("client",jsonObject1.toString()));
                                }
                            },5000);
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
                Snackbar.make(findViewById(R.id.clPaymentConfirmation), Html.fromHtml("<font color=\"#ffffff\">"+new Errors().volleyErrors(error)+"</font>"),Snackbar.LENGTH_LONG).show();

            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("id", client.getString("request",""));
                //params.put("id","REQ29728311");
                return params;
            }
        };

        queue.add(postRequest);
    }


}
