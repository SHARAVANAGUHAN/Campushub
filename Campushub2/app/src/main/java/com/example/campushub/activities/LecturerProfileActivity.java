package com.example.campushub.activities;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class LecturerProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_lecturer_profile);


        // =========================
        // BACK BUTTON
        // =========================

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());


        // =========================
        // CARDS
        // =========================

        View profileImage =
                findViewById(R.id.imgProfileLarge);

        View profileName =
                findViewById(R.id.txtProfileName);

        View profileId =
                findViewById(R.id.txtProfileId);

        MaterialCardView personalCard =
                findViewById(R.id.cardPersonal);

        MaterialCardView academicCard =
                findViewById(R.id.cardAcademic);

        MaterialCardView teachingCard =
                findViewById(R.id.cardTeaching);

        MaterialCardView contactCard =
                findViewById(R.id.cardContact);


        // =========================
        // PREPARE ANIMATIONS
        // =========================

        prepareAnimation(profileImage);
        prepareAnimation(profileName);
        prepareAnimation(profileId);

        prepareAnimation(personalCard);
        prepareAnimation(academicCard);
        prepareAnimation(teachingCard);
        prepareAnimation(contactCard);


        // =========================
        // START ANIMATIONS
        // =========================

        animateView(profileImage, 80);
        animateView(profileName, 150);
        animateView(profileId, 210);

        animateView(personalCard, 300);
        animateView(academicCard, 400);
        animateView(teachingCard, 500);
        animateView(contactCard, 600);


        // =========================
        // LOGOUT
        // =========================

        MaterialButton btnLogout =
                findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(60)
                    .withEndAction(() -> {

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(60)
                                .start();

                        showLogoutMessage();
                    })
                    .start();
        });
    }


    // =====================================================
    // PREPARE
    // =====================================================

    private void prepareAnimation(View view) {

        view.setAlpha(0f);

        view.setTranslationY(35f);
    }


    // =====================================================
    // ANIMATE
    // =====================================================

    private void animateView(
            View view,
            long delay) {

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(450)
                .setInterpolator(
                        new DecelerateInterpolator()
                )
                .start();
    }


    // =====================================================
    // TEMPORARY LOGOUT
    // =====================================================

    private void showLogoutMessage() {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log Out", (dialog, which) -> {

                    Intent intent =
                            new Intent(
                                    LecturerProfileActivity.this,
                                    LoginActivity.class
                            );

                    // Clear dashboard/profile from the back stack
                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);

                    finish();
                })
                .show();
    }
}