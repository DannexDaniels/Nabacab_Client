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

    private Alerts alert;

    private SharedPreferences pickup, request;

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
                alert.showMessage("Payment","M-Pesa payment is yet to be implemented",new MakePayment());
                /*alert.showProgressDialog("Processing","please wait");
                //post my payment details
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://dannextech.com/nabacab/API/authenticate_user";
                StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
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

                        alert.hideProgressDialog();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        alert.hideProgressDialog();
                        Log.e("Error.ResponseResult", error.toString());
                        Snackbar.make(findViewById(R.id.clConfirmPickup), new Errors().volleyErrors(error), Snackbar.LENGTH_LONG).show();
                    }
                });*/
                break;
            case 2:
                alert.showMessage("Payment","Airtel Money payment is yet to be implemented",new MakePayment());
                break;
            case 3:
                alert.showMessage("Payment","Credit Card payment is yet to be implemented",new MakePayment());
                break;
            case 4:
                alert.showMessage("Payment","You will pay "+pickup.getString("cost","")+" to the driver"+request.getString("client",""),new ClientHome());
                //makePayment("cash");
                break;
            default:
                Snackbar.make(view,"Please select a payment option",Snackbar.LENGTH_LONG).show();

        }
    }

    public void goBackConfirmPickup(View v){
        finish();
    }

    public void makePayment(final String pmode){
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
                                    startActivity(new Intent(MakePayment.this, PaymentConfirmation.class));
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
                params.put("amount", pickup.getString("cost",""));
                params.put("mode", pmode);
                params.put("client", request.getString("client",""));
                params.put("driver", request.getString("driver",""));
                params.put("id", request.getString("id",""));
                return params;
            }
        };
        queue.add(postRequest);

    }
}
