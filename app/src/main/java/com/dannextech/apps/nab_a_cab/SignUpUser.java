package com.dannextech.apps.nab_a_cab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dannextech.apps.nab_a_cab.utils.Errors;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpUser extends AppCompatActivity {

    private EditText etUser, etPhone, etEmail;
    private ProgressBar pbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_user);

        etUser = findViewById(R.id.etNamesUser);
        etPhone = findViewById(R.id.etPhoneUser);
        etEmail = findViewById(R.id.etEmailUser);
        pbLoading = findViewById(R.id.pbSignUpUser);

        if (!new Errors().isNetworkAvailable(getApplicationContext())){
            Snackbar.make(findViewById(R.id.clSignUpUser),Html.fromHtml("<font color=\"#ffffff\">There is no internet connection. The app might not work properly</font>"), Snackbar.LENGTH_LONG).show();
        }

    }

    public void createAccount(View v){

        pbLoading.setVisibility(View.VISIBLE);

        String name = etUser.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.equals("")){
            etUser.setError("Full Names is Required");
            pbLoading.setVisibility(View.GONE);
        }else if (phone.equals("")){
            etPhone.setError("Phone Number is Required");
            pbLoading.setVisibility(View.GONE);
        }else if (email.equals("")){
            etEmail.setError("Email Address is Required");
            pbLoading.setVisibility(View.GONE);
        }else{
            inactivateFields();
            registerClient(name, phone, email);

        }

    }

    private void inactivateFields(){
        etPhone.setEnabled(false);
        etEmail.setEnabled(false);
        etUser.setEnabled(false);
        findViewById(R.id.btnSignUp).setEnabled(false);
    }

    private void activateFields(){
        etUser.setEnabled(true);
        etEmail.setEnabled(true);
        etPhone.setEnabled(true);
        findViewById(R.id.btnSignUp).setEnabled(true);
    }

    private void registerClient(final String name, final String phone, final String email) {

        //post my user details
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://dannextech.com/nabacab/API/create_client";
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
                            Snackbar.make(findViewById(R.id.clSignUpUser),Html.fromHtml("<font color=\"#ffffff\">An error has occurred. Please try again</font>"),Snackbar.LENGTH_SHORT).show();
                        }else {
                            try {

                                String status = jsonObject.getString("status");
                                if (status.equals("OK")){
                                    SharedPreferences.Editor editor = getSharedPreferences("client", MODE_PRIVATE).edit();
                                    editor.putString("name", name);
                                    editor.putString("phone", phone);
                                    editor.putString("email", email);
                                    editor.apply();

                                    Snackbar.make(findViewById(R.id.clSignUpUser),Html.fromHtml("<font color=\"#ffffff\">Account Created Successfully</font>"),Snackbar.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignUpUser.this, ClientHome.class));
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
                        Snackbar.make(findViewById(R.id.clSignUpUser), Html.fromHtml("<font color=\"#ffffff\">"+new Errors().volleyErrors(error)+"</font>"),Snackbar.LENGTH_LONG).show();
                        activateFields();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("name", name);
                params.put("phone", phone);
                params.put("email", email);
                return params;
            }
        };
        queue.add(postRequest);

    }
}
