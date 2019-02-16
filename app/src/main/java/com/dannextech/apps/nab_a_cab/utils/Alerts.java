package com.dannextech.apps.nab_a_cab.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.dannextech.apps.nab_a_cab.ClientHome;

public class Alerts {
    private ProgressDialog progressDialog;
    private Context context;

    public Alerts(Context context){
        this.context = context;
    }

    public void showProgressDialog(String title,String message){
        progressDialog = ProgressDialog.show(context,title,message,true);
    }
    public void hideProgressDialog(){
        progressDialog.dismiss();
    }

    public void exitApp(){
        Log.e("Dannex Daniels", "promptUser: I'm in");

        new AlertDialog.Builder(context)
                .setTitle("Confirm")
                .setMessage("Do you wish to exit from the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        System.exit(1);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    public void showMessage(String title, String message, final Activity activity){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(new Intent(context,activity.getClass()));
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
