package com.dannextech.apps.nab_a_cab.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

public class Errors {

    public String volleyErrors(VolleyError error){
        if (error instanceof TimeoutError) {
            //This indicates that the reuest has time out
            return "Your network is slow, check it and try again";
        } else if (error instanceof NoConnectionError){
            //This indicates that there is no connection
            return "You don't have internet connection!";
        }else if (error instanceof AuthFailureError) {
            //Error indicating that there was an Authentication Failure while performing the request
            return "Authentication Failed. Please check your internet connection";
        } else if (error instanceof ServerError) {
            //Indicates that the server responded with a error response
            return "Server error occured: "+error.getMessage();
        } else if (error instanceof NetworkError) {
            //Indicates that there was network error while performing the request
            return "Network error occurred while performing the request";
        } else if (error instanceof ParseError) {
            // Indicates that the server response could not be parsed
            return "Could not get response from the server";
        }

        return null;
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
