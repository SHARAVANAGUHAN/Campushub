package com.example.campushub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        View brandGroup = findViewById(R.id.brandGroup);

        Animation animation =
                AnimationUtils.loadAnimation(
                        this,
                        R.anim.logo_animation
                );

        brandGroup.startAnimation(animation);

        new android.os.Handler(getMainLooper()).postDelayed(() -> {

            android.content.SharedPreferences prefs =
                    getSharedPreferences(
                            "CampusHubPrefs",
                            MODE_PRIVATE
                    );

            // Check student Remember Me
            boolean rememberStudent =
                    prefs.getBoolean(
                            "remember_me",
                            false
                    );

            // Check lecturer Remember Me
            boolean rememberTeacher =
                    prefs.getBoolean(
                            "remember_me_teacher",
                            false
                    );

            String accessToken =
                    prefs.getString(
                            "access_token",
                            ""
                    );

            String studentId =
                    prefs.getString(
                            "student_id",
                            ""
                    );

            String teacherId =
                    prefs.getString(
                            "teacher_id",
                            ""
                    );

            Intent intent;

            /*
             * STUDENT REMEMBER ME
             */
            if (rememberStudent
                    && !accessToken.isEmpty()
                    && !studentId.isEmpty()) {

                intent = new Intent(
                        SplashActivity.this,
                        StudentDashboardActivity.class
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

            }

            /*
             * LECTURER REMEMBER ME
             */
            else if (rememberTeacher
                    && !accessToken.isEmpty()
                    && !teacherId.isEmpty()) {

                intent = new Intent(
                        SplashActivity.this,
                        LecturerDashboardActivity.class
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

            }

            /*
             * NO REMEMBERED LOGIN
             */
            else {

                intent = new Intent(
                        SplashActivity.this,
                        SignupRoleActivity.class
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );
            }

            startActivity(intent);
            finish();

        }, SPLASH_TIME);
    }
}
