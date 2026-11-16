package com.example.campushub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.imgLogo);

        Animation animation =
                AnimationUtils.loadAnimation(
                        this,
                        R.anim.logo_animation
                );

        logo.startAnimation(animation);

        new Handler(getMainLooper()).postDelayed(() -> {

            Intent intent =
                    new Intent(
                            SplashActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);
            finish();

        }, SPLASH_TIME);
    }
}