package com.example.campushub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.activities.SignupRoleActivity;
import com.google.android.material.card.MaterialCardView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_profile);

        // =====================================================
        // BACK BUTTON
        // =====================================================

        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());


        // =====================================================
        // PROFILE HEADER
        // =====================================================

        ImageView profileImage =
                findViewById(R.id.imgProfileLarge);

        TextView profileName =
                findViewById(R.id.txtProfileName);

        TextView profileId =
                findViewById(R.id.txtProfileId);


        // =====================================================
        // CARDS
        // =====================================================

        MaterialCardView personalCard =
                findViewById(R.id.cardPersonal);

        MaterialCardView academicCard =
                findViewById(R.id.cardAcademic);

        MaterialCardView contactCard =
                findViewById(R.id.cardContact);

        MaterialCardView accountCard =
                findViewById(R.id.cardAccount);


        // =====================================================
        // LOGOUT BUTTON
        // =====================================================

        TextView btnLogout =
                findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> showLogoutDialog());


        // =====================================================
        // ANIMATION PREPARATION
        // =====================================================

        prepareAnimation(profileImage);
        prepareAnimation(profileName);
        prepareAnimation(profileId);

        prepareAnimation(personalCard);
        prepareAnimation(academicCard);
        prepareAnimation(contactCard);
        prepareAnimation(accountCard);


        // =====================================================
        // START ANIMATIONS
        // =====================================================

        animateView(profileImage, 100);
        animateView(profileName, 180);
        animateView(profileId, 240);

        animateView(personalCard, 350);
        animateView(academicCard, 450);
        animateView(contactCard, 550);
        animateView(accountCard, 650);
    }


    // =====================================================
    // LOGOUT DIALOG
    // =====================================================

    private void showLogoutDialog() {

        new AlertDialog.Builder(this)

                .setTitle("Log out of CampusHub?")

                .setMessage(
                        "You'll need to sign in again to access your dashboard."
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Log Out",
                        (dialog, which) -> {

                            // =================================================
                            // CLEAR LOGIN DATA
                            // =================================================

                            getSharedPreferences(
                                    "CampusHubPrefs",
                                    MODE_PRIVATE
                            )
                                    .edit()
                                    .clear()
                                    .apply();


                            // =================================================
                            // GO TO FIRST SCREEN
                            // STUDENT / LECTURER SELECTION
                            // =================================================

                            Intent intent = new Intent(
                                    ProfileActivity.this,
                                    SignupRoleActivity.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);

                            finish();
                        })

                .show();
    }


    // =====================================================
    // PREPARE ANIMATION
    // =====================================================

    private void prepareAnimation(View view) {

        if (view == null) {
            return;
        }

        view.setAlpha(0f);
        view.setTranslationY(35f);
    }


    // =====================================================
    // ANIMATE VIEW
    // =====================================================

    private void animateView(
            View view,
            long delay
    ) {

        if (view == null) {
            return;
        }

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(500)
                .setInterpolator(
                        new DecelerateInterpolator()
                )
                .start();
    }
}