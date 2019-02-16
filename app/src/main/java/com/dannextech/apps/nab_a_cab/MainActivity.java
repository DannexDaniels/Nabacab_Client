package com.dannextech.apps.nab_a_cab;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dannextech.apps.nab_a_cab.utils.Alerts;

public class MainActivity extends AppCompatActivity {

    private ImageView ivLogo;
    private ProgressBar pbSplash;

    private static final int LOCATION_REQUEST = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivLogo = findViewById(R.id.logo);
        pbSplash = findViewById(R.id.pbSplashScreen);

        checkPermissions();

        final Animation myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ivLogo.startAnimation(myFadeInAnimation);

        myFadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                pbSplash.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences client = getSharedPreferences("client", MODE_PRIVATE);
                if (client.contains("phone")){
                    handler.removeCallbacksAndMessages(null);
                    startActivity(new Intent(MainActivity.this,ClientHome.class));
                }else {
                    handler.removeCallbacksAndMessages(null);
                    startActivity(new Intent(MainActivity.this, SignInUser.class));
                }
            }
        },5000);
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                5000);
    }
}
