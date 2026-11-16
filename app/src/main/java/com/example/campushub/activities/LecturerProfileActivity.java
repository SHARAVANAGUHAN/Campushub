package com.example.campushub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LecturerProfileActivity extends AppCompatActivity {

    // =====================================================
    // PROFILE HEADER
    // =====================================================

    private TextView profileName;
    private TextView profileId;


    // =====================================================
    // PERSONAL DETAILS
    // =====================================================

    private TextView txtFullName;
    private TextView txtDob;


    // =====================================================
    // ACADEMIC DETAILS
    // =====================================================

    private TextView txtDepartment;
    private TextView txtDesignation;


    // =====================================================
    // TEACHING DETAILS
    // =====================================================

    private TextView txtSubjects;
    private TextView txtSections;


    // =====================================================
    // CONTACT
    // =====================================================

    private TextView txtEmail;
    private TextView txtPhone;


    // =====================================================
    // EXECUTOR
    // =====================================================

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_lecturer_profile
        );


        // =====================================================
        // FIND VIEWS
        // =====================================================

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        View profileImage =
                findViewById(R.id.imgProfileLarge);

        profileName =
                findViewById(R.id.txtProfileName);

        profileId =
                findViewById(R.id.txtProfileId);


        // =====================================================
        // PERSONAL
        // =====================================================

        txtFullName =
                findViewById(R.id.txtFullName);

        txtDob =
                findViewById(R.id.txtDob);


        // =====================================================
        // ACADEMIC
        // =====================================================

        txtDepartment =
                findViewById(R.id.txtDepartment);

        txtDesignation =
                findViewById(R.id.txtDesignation);


        // =====================================================
        // TEACHING
        // =====================================================

        txtSubjects =
                findViewById(R.id.txtSubjects);

        txtSections =
                findViewById(R.id.txtSections);


        // =====================================================
        // CONTACT
        // =====================================================

        txtEmail =
                findViewById(R.id.txtEmail);

        txtPhone =
                findViewById(R.id.txtPhone);


        // =====================================================
        // LOGOUT BUTTON
        // =====================================================

        MaterialButton btnLogout =
                findViewById(R.id.btnLogout);


        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // LOGOUT BUTTON
        // =====================================================

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


        // =====================================================
        // DEFAULT VALUES
        // =====================================================

        setLoadingValues();


        // =====================================================
        // ANIMATIONS
        // =====================================================

        prepareAnimation(profileImage);
        prepareAnimation(profileName);
        prepareAnimation(profileId);


        MaterialCardView personalCard =
                findViewById(R.id.cardPersonal);

        MaterialCardView academicCard =
                findViewById(R.id.cardAcademic);

        MaterialCardView teachingCard =
                findViewById(R.id.cardTeaching);

        MaterialCardView contactCard =
                findViewById(R.id.cardContact);


        prepareAnimation(personalCard);
        prepareAnimation(academicCard);
        prepareAnimation(teachingCard);
        prepareAnimation(contactCard);


        animateView(profileImage, 80);
        animateView(profileName, 150);
        animateView(profileId, 210);

        animateView(personalCard, 300);
        animateView(academicCard, 400);
        animateView(teachingCard, 500);
        animateView(contactCard, 600);


        // =====================================================
        // LOAD PROFILE
        // =====================================================

        loadTeacherProfile();
    }


    // =====================================================
    // DEFAULT / LOADING VALUES
    // =====================================================

    private void setLoadingValues() {

        profileName.setText("Loading...");

        profileId.setText("Loading...");

        txtFullName.setText("Loading...");

        txtDob.setText("Not available");

        txtDepartment.setText("Loading...");

        txtDesignation.setText("Not available");

        txtSubjects.setText("Not available");

        txtSections.setText("Not available");

        txtEmail.setText("Loading...");

        txtPhone.setText("Not available");
    }


    // =====================================================
    // LOAD TEACHER PROFILE
    // =====================================================

    private void loadTeacherProfile() {

        String accessToken =
                getSharedPreferences(
                        "CampusHubPrefs",
                        MODE_PRIVATE
                )
                        .getString(
                                "access_token",
                                null
                        );


        // =====================================================
        // TOKEN CHECK
        // =====================================================

        if (accessToken == null
                || accessToken.trim().isEmpty()) {

            Toast.makeText(
                    LecturerProfileActivity.this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // API REQUEST
        // =====================================================

        executorService.execute(() -> {

            try {

                String response =
                        ApiClient.get(
                                "api/auth/teacher/profile/",
                                accessToken
                        );


                JSONObject root =
                        new JSONObject(response);


                JSONObject teacher =
                        root.getJSONObject("teacher");


                // =================================================
                // READ DATABASE VALUES
                // =================================================

                String teacherId =
                        teacher.optString(
                                "teacher_id",
                                "Not available"
                        );

                String fullName =
                        teacher.optString(
                                "full_name",
                                "Not available"
                        );

                String email =
                        teacher.optString(
                                "email",
                                "Not available"
                        );

                String department =
                        teacher.optString(
                                "department",
                                "Not available"
                        );


                // =================================================
                // UPDATE UI
                // =================================================

                runOnUiThread(() -> {

                    profileName.setText(
                            fullName
                    );

                    profileId.setText(
                            "Teacher ID: "
                                    + teacherId
                    );

                    txtFullName.setText(
                            fullName
                    );

                    txtDepartment.setText(
                            formatText(department)
                    );

                    txtEmail.setText(
                            email
                    );


                    // Fields currently unavailable
                    // in the Teacher model/API.

                    txtDob.setText(
                            "Not available"
                    );

                    txtDesignation.setText(
                            "Not available"
                    );

                    txtSubjects.setText(
                            "Not available"
                    );

                    txtSections.setText(
                            "Not available"
                    );

                    txtPhone.setText(
                            "Not available"
                    );

                });

            }

            catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() -> {

                    Toast.makeText(
                            LecturerProfileActivity.this,
                            "Unable to load teacher profile",
                            Toast.LENGTH_LONG
                    ).show();

                });

            }

        });
    }


    // =====================================================
    // FORMAT DEPARTMENT
    // =====================================================

    private String formatText(String text) {

        if (text == null
                || text.trim().isEmpty()) {

            return "Not available";
        }


        String[] words =
                text.trim().split("\\s+");


        StringBuilder result =
                new StringBuilder();


        for (String word : words) {

            if (word.length() == 0) {
                continue;
            }


            result.append(
                    Character.toUpperCase(
                            word.charAt(0)
                    )
            );


            if (word.length() > 1) {

                result.append(
                        word.substring(1)
                                .toLowerCase()
                );

            }

            result.append(" ");
        }


        return result.toString().trim();
    }


    // =====================================================
    // ANIMATION PREPARE
    // =====================================================

    private void prepareAnimation(View view) {

        if (view == null) {
            return;
        }

        view.setAlpha(0f);

        view.setTranslationY(35f);
    }


    // =====================================================
    // ANIMATION
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
                .setDuration(450)
                .setInterpolator(
                        new DecelerateInterpolator()
                )
                .start();
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    private void showLogoutMessage() {

        new AlertDialog.Builder(this)

                .setTitle("Log Out")

                .setMessage(
                        "Are you sure you want to log out?"
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

                            Intent intent =
                                    new Intent(
                                            LecturerProfileActivity.this,
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
    // CLEANUP
    // =====================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executorService.shutdown();
    }
}