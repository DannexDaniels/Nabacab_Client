package com.dannextech.apps.nab_a_cab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ConfirmRequestDetails extends AppCompatActivity {

    private TextView tvOrigin, tvDestination, tvDistTime, tvCost, tvVehicle, tvCapacity;

    private SharedPreferences pickup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_request_details);

        tvOrigin = findViewById(R.id.tvOriginConfirmPickup);
        tvDestination = findViewById(R.id.tvDestinationConfirmPickup);
        tvDistTime = findViewById(R.id.tvDistanceTimeConfirmPickup);
        tvCost = findViewById(R.id.tvPaymentConfirmPickup);
        tvVehicle = findViewById(R.id.tvVehicleConfirmPickup);
        tvCapacity = findViewById(R.id.tvCapacityConfirmPickup);

        pickup = getSharedPreferences("pickup", MODE_PRIVATE);

        tvOrigin.setText(pickup.getString("origin",""));
        tvDestination.setText(pickup.getString("destination",""));
        tvDistTime.setText(pickup.getString("disttime",""));
        tvCost.setText(pickup.getString("cost",""));
        tvVehicle.setText(pickup.getString("vehicle",""));
        tvCapacity.setText(pickup.getString("pass",""));
    }

    public void confirmPickup(View view){
        startActivity(new Intent(ConfirmRequestDetails.this, FindDriver.class));
    }

    public void goBackConfirmPickup(View v){
        finish();
    }


}
