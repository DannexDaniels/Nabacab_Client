package com.dannextech.apps.nab_a_cab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

public class MakePayment extends AppCompatActivity {

    private LinearLayout llMpesa, llCash, llAirtel, llCard;
    private ImageView ivMpesa, ivAirtel, ivCard, ivCash;

    private int paymentSelected = 0;
    private String charges = null, payTo, payFrom, payId, requestId;

    private Alerts alert;

    private SharedPreferences pickup, request;

    private JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);

        if (!new Errors().isNetworkAvailable(getApplicationContext())){
            Snackbar.make(findViewById(R.id.clSignUpUser),Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }

        alert = new Alerts(MakePayment.this);

        pickup = getSharedPreferences("pickup", MODE_PRIVATE);
        request = getSharedPreferences("request", MODE_PRIVATE);

        llAirtel = findViewById(R.id.llAirtelPayment);
        llCard = findViewById(R.id.llCardPayment);
        llCash = findViewById(R.id.llCashPayment);
        llMpesa = findViewById(R.id.llMpesaPayment);

        ivAirtel = findViewById(R.id.ivAirtelMoneySelected);
        ivMpesa = findViewById(R.id.ivMpesaSelected);
        ivCard = findViewById(R.id.ivCreditCardSelected);
        ivCash = findViewById(R.id.ivCashSelected);

        try {
            jsonObject = new JSONObject(getIntent().getStringExtra("client"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        alert.showProgressDialog("","Loading...");
        checkPayments();

    }

    private void checkPayments() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/Payments/check_payment";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                JSONObject jsonObject1 = null;
                Log.e("ResponseResult", "res = "+response);
                if (!response.equals("null")){
                    try {
                        alert.hideProgressDialog();
                        jsonObject1 = new JSONObject(response);

                        charges = jsonObject1.getString("pay_amount").trim();
                        payFrom = "254"+Integer.parseInt(jsonObject1.getString("pay_by"));
                        payTo = jsonObject1.getString("pay_to");
                        payId = jsonObject1.getString("pay_id");
                        requestId = jsonObject1.getString("pay_request");
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
                Snackbar.make(findViewById(R.id.clMakePayment), Html.fromHtml("<font color=\"#ffffff\">"+new Errors().volleyErrors(error)+"</font>"),Snackbar.LENGTH_LONG).show();
                checkPayments();
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                try {
                    params.put("id", jsonObject.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //params.put("id","REQ29728311");


                return params;
            }
        };

        queue.add(postRequest);
    }

    public void selectedPayment(View v){
        if (v.getId() == llMpesa.getId()){
            ivMpesa.setVisibility(View.VISIBLE);
            ivCash.setVisibility(View.INVISIBLE);
            ivCard.setVisibility(View.INVISIBLE);
            ivAirtel.setVisibility(View.INVISIBLE);

            paymentSelected = 1;

            llMpesa.setBackgroundResource(R.drawable.selected_list_back);
            llCash.setBackgroundColor(Color.WHITE);
            llCard.setBackgroundColor(Color.WHITE);
            llAirtel.setBackgroundColor(Color.WHITE);
        }else if (v.getId() == llAirtel.getId()){
            ivMpesa.setVisibility(View.INVISIBLE);
            ivCash.setVisibility(View.INVISIBLE);
            ivCard.setVisibility(View.INVISIBLE);
            ivAirtel.setVisibility(View.VISIBLE);

            paymentSelected = 2;

            llMpesa.setBackgroundColor(Color.WHITE);
            llCash.setBackgroundColor(Color.WHITE);
            llCard.setBackgroundColor(Color.WHITE);
            llAirtel.setBackgroundResource(R.drawable.selected_list_back);
        }else if (v.getId() == llCard.getId()){
            ivMpesa.setVisibility(View.INVISIBLE);
            ivCash.setVisibility(View.INVISIBLE);
            ivCard.setVisibility(View.VISIBLE);
            ivAirtel.setVisibility(View.INVISIBLE);

            paymentSelected = 3;

            llMpesa.setBackgroundColor(Color.WHITE);
            llCash.setBackgroundColor(Color.WHITE);
            llCard.setBackgroundResource(R.drawable.selected_list_back);
            llAirtel.setBackgroundColor(Color.WHITE);
        }else if (v.getId() == llCash.getId()){
            ivMpesa.setVisibility(View.INVISIBLE);
            ivCash.setVisibility(View.VISIBLE);
            ivCard.setVisibility(View.INVISIBLE);
            ivAirtel.setVisibility(View.INVISIBLE);

            paymentSelected = 4;

            llMpesa.setBackgroundColor(Color.WHITE);
            llCash.setBackgroundResource(R.drawable.selected_list_back);
            llCard.setBackgroundColor(Color.WHITE);
            llAirtel.setBackgroundColor(Color.WHITE);
        }
    }

    public void makePayments(View view){
        switch (paymentSelected){
            case 1:
                updatePayment("mpesa");
                //alert.showMessage("Payment","M-Pesa payment is yet to be implemented",new MakePayment());

                break;
            case 2:
                alert.showMessage("Payment","Airtel Money payment is yet to be implemented",new MakePayment());
                break;
            case 3:
                alert.showMessage("Payment","Credit Card payment is yet to be implemented",new MakePayment());
                break;
            case 4:
                alert.showMessage("Payment","You will pay "+charges+" to the driver",new ClientHome());
                updatePayment("cash");
                break;
            default:
                Snackbar.make(view,"Please select a payment option",Snackbar.LENGTH_LONG).show();

        }
    }

    public void goBackConfirmPickup(View v){
        finish();
    }

    public void updatePayment(final String pmode){
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
                            Snackbar.make(findViewById(R.id.clMakePayment),Html.fromHtml("<font color=\"#ffffff\">An error has occurred. Please try again</font>"),Snackbar.LENGTH_SHORT).show();
                        }else {
                            try {

                                String status = jsonObject.getString("status");
                                if (status.equals("OK")){
                                    SharedPreferences.Editor editor = getSharedPreferences("payment", MODE_PRIVATE).edit();
                                    editor.putString("pay_id", payId);
                                    editor.putString("request",requestId);
                                    editor.apply();
                                    if (pmode.equals("cash"))
                                        startActivity(new Intent(MakePayment.this, PaymentConfirmation.class).putExtra("client",jsonObject.toString()));
                                    else if (pmode.equals("mpesa"))
                                        payMpesa();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Snackbar.make(findViewById(R.id.clMakePayment), Html.fromHtml("<font color=\"#ffffff\">An error has occurred. Please try again</font>"),Snackbar.LENGTH_SHORT).show();
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
                        updatePayment(pmode);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                try {
                    //params.put("amount", jsonObject.getString("charges"));
                    params.put("id", jsonObject.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //params.put("id","REQ29728311");
                params.put("mode", pmode);
                params.put("user","client");
                return params;
            }
        };
        queue.add(postRequest);

    }

    private void payMpesa() {
        alert.showProgressDialog("Processing","please wait");
        //post my payment details
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/Payments/c2bPayment";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // response
                JSONObject jsonObject = null;
                String responseCode = null, responseMessage = null;

                alert.hideProgressDialog();
                Log.e("ResponseResult", response);
                try {
                    jsonObject = new JSONObject(response);
                    responseCode = jsonObject.getString("ResponseCode");
                    responseMessage = jsonObject.getString("ResponseDescription");
                    if (responseCode == null){
                        responseCode = jsonObject.getString("errorCode");
                        responseMessage = jsonObject.getString("errorMessage");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (jsonObject == null){
                    Snackbar.make(findViewById(R.id.clMakePayment),Html.fromHtml("<font color=\"#ffffff\">An error has occurred. Please try again</font>"),Snackbar.LENGTH_SHORT).show();
                }else {
                    alert.showMessage("","Wait to enter your Mpesa Pin", new PaymentConfirmation());
                    /*if (responseCode != null && responseCode.equals("0")){

                    }else {
                        Snackbar.make(findViewById(R.id.clMakePayment),Html.fromHtml("<font color=\"#ffffff\">An error has occurred: "+responseMessage+". Please try again</font>"),Snackbar.LENGTH_SHORT).show();
                    }*/
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                alert.hideProgressDialog();
                Log.e("Error.ResponseResult", error.toString());
                Snackbar.make(findViewById(R.id.clMakePayment), new Errors().volleyErrors(error), Snackbar.LENGTH_LONG).show();
                payMpesa();
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();

                params.put("phone", "254704040556");
                params.put("cost", charges);
                params.put("receipt", payTo);
                params.put("pay_no", payId);
                params.put("description","Taxi services");
                return params;
            }
        };
        queue.add(postRequest);

    }
}
