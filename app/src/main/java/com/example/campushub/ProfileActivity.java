package com.example.campushub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.activities.SignupRoleActivity;
import com.example.campushub.api.ApiClient;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ProfileActivity extends AppCompatActivity {

    // =====================================================
    // PROFILE HEADER
    // =====================================================

    private ImageView profileImage;

    private TextView profileName;

    private TextView profileId;


    // =====================================================
    // PERSONAL INFORMATION
    // =====================================================

    private TextView txtFullName;

    private TextView txtEmail;

    private TextView txtDob;


    // =====================================================
    // ACADEMIC INFORMATION
    // =====================================================

    private TextView txtAcademicStudentId;

    private TextView txtCourse;

    private TextView txtDepartment;

    private TextView txtYear;

    private TextView txtSemester;


    // =====================================================
    // CONTACT INFORMATION
    // =====================================================

    private TextView txtContactEmail;

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
                R.layout.activity_profile
        );


        // =====================================================
        // FIND VIEWS
        // =====================================================

        findViews();


        // =====================================================
        // BACK BUTTON
        // =====================================================

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());


        // =====================================================
        // LOGOUT
        // =====================================================

        TextView btnLogout =
                findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(
                v -> showLogoutDialog()
        );


        // =====================================================
        // ANIMATIONS
        // =====================================================

        prepareAnimations();

        startAnimations();


        // =====================================================
        // LOAD PROFILE
        // =====================================================

        loadProfile();

    }


    // =====================================================
    // FIND ALL VIEWS
    // =====================================================

    private void findViews() {

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        profileImage =
                findViewById(
                        R.id.imgProfileLarge
                );

        profileName =
                findViewById(
                        R.id.txtProfileName
                );

        profileId =
                findViewById(
                        R.id.txtProfileId
                );


        // -----------------------------------------------------
        // PERSONAL
        // -----------------------------------------------------

        txtFullName =
                findViewById(
                        R.id.txtFullName
                );

        txtEmail =
                findViewById(
                        R.id.txtEmail
                );

        txtDob =
                findViewById(
                        R.id.txtDob
                );


        // -----------------------------------------------------
        // ACADEMIC
        // -----------------------------------------------------

        txtAcademicStudentId =
                findViewById(
                        R.id.txtAcademicStudentId
                );

        txtCourse =
                findViewById(
                        R.id.txtCourse
                );

        txtDepartment =
                findViewById(
                        R.id.txtDepartment
                );

        txtYear =
                findViewById(
                        R.id.txtYear
                );

        txtSemester =
                findViewById(
                        R.id.txtSemester
                );


        // -----------------------------------------------------
        // CONTACT
        // -----------------------------------------------------

        txtContactEmail =
                findViewById(
                        R.id.txtContactEmail
                );

        txtPhone =
                findViewById(
                        R.id.txtPhone
                );

    }


    // =====================================================
    // LOAD PROFILE FROM DATABASE
    // =====================================================

    private void loadProfile() {

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
        // CHECK TOKEN
        // =====================================================

        if (accessToken == null
                || accessToken.isEmpty()) {

            Toast.makeText(
                    this,
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
                                "api/auth/student/dashboard/",
                                accessToken
                        );


                JSONObject json =
                        new JSONObject(
                                response
                        );


                JSONObject student =
                        json.getJSONObject(
                                "student"
                        );


                // =================================================
                // GET DATA
                // =================================================

                String studentId =
                        student.optString(
                                "student_id",
                                ""
                        );


                String fullName =
                        student.optString(
                                "full_name",
                                ""
                        );


                String email =
                        student.optString(
                                "email",
                                ""
                        );


                String dob =
                        student.optString(
                                "dob",
                                ""
                        );


                String courseCode =
                        student.optString(
                                "course_code",
                                ""
                        );


                String course =
                        student.optString(
                                "course",
                                ""
                        );


                String department =
                        student.optString(
                                "department",
                                ""
                        );


                int admissionYear =
                        student.optInt(
                                "admission_year",
                                0
                        );


                String yearText =
                        student.optString(
                                "year_text",
                                "Unknown"
                        );


                String semesters =
                        formatSemesters(
                                student.optJSONArray(
                                        "current_semesters"
                                )
                        );


                // =================================================
                // UPDATE UI
                // =================================================

                runOnUiThread(() -> {

                    // ---------------------------------------------
                    // HEADER
                    // ---------------------------------------------

                    profileName.setText(
                            fullName
                    );


                    profileId.setText(
                            studentId
                    );


                    // ---------------------------------------------
                    // PERSONAL INFORMATION
                    // ---------------------------------------------

                    txtFullName.setText(
                            fullName
                    );


                    txtEmail.setText(
                            email
                    );


                    txtDob.setText(
                            dob.isEmpty()
                                    ? "Not available"
                                    : dob
                    );


                    // ---------------------------------------------
                    // ACADEMIC INFORMATION
                    // ---------------------------------------------

                    txtAcademicStudentId.setText(
                            studentId
                    );


                    if (!courseCode.isEmpty()
                            && !course.isEmpty()) {

                        txtCourse.setText(
                                courseCode
                                        + " - "
                                        + course
                        );

                    } else if (!course.isEmpty()) {

                        txtCourse.setText(
                                course
                        );

                    } else {

                        txtCourse.setText(
                                "Not available"
                        );

                    }


                    txtDepartment.setText(
                            formatText(
                                    department
                            )
                    );


                    if (admissionYear > 0) {

                        txtYear.setText(
                                yearText
                                        + " (Joined "
                                        + admissionYear
                                        + ")"
                        );

                    } else {

                        txtYear.setText(
                                yearText
                        );

                    }


                    txtSemester.setText(
                            semesters
                    );


                    // ---------------------------------------------
                    // CONTACT INFORMATION
                    // ---------------------------------------------

                    txtContactEmail.setText(
                            email
                    );


                    /*
                     * The current Student model does not have
                     * a phone field.
                     *
                     * Therefore we deliberately do not use
                     * the old hard-coded phone number.
                     */

                    txtPhone.setText(
                            "Not available"
                    );

                });


            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Unable to load profile",
                            Toast.LENGTH_LONG
                    ).show();

                });

            }

        });

    }


    // =====================================================
    // FORMAT SEMESTERS
    // =====================================================

    private String formatSemesters(
            org.json.JSONArray semesters
    ) {

        if (semesters == null
                || semesters.length() == 0) {

            return "Not available";
        }


        StringBuilder result =
                new StringBuilder();


        for (
                int i = 0;
                i < semesters.length();
                i++
        ) {

            if (i > 0) {

                if (i == semesters.length() - 1) {

                    result.append(" & ");

                } else {

                    result.append(", ");

                }

            }


            result.append(
                    "Semester "
            );


            result.append(
                    semesters.optInt(
                            i
                    )
            );

        }


        return result.toString();

    }


    // =====================================================
    // FORMAT TEXT
    // =====================================================

    private String formatText(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return "Not available";
        }


        String[] words =
                value.trim().split("\\s+");


        StringBuilder result =
                new StringBuilder();


        for (String word : words) {

            if (word.isEmpty()) {
                continue;
            }


            if (result.length() > 0) {

                result.append(" ");

            }


            if (word.length() == 1) {

                result.append(
                        word.toUpperCase()
                );

            } else {

                result.append(
                        word.substring(
                                0,
                                1
                        ).toUpperCase()
                );

                result.append(
                        word.substring(
                                1
                        ).toLowerCase()
                );

            }

        }


        return result.toString();

    }


    // =====================================================
    // LOGOUT DIALOG
    // =====================================================

    private void showLogoutDialog() {

        new AlertDialog.Builder(this)

                .setTitle(
                        "Log out of CampusHub?"
                )

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

                            /*
                             * Clear the stored access token.
                             */

                            getSharedPreferences(
                                    "CampusHubPrefs",
                                    MODE_PRIVATE
                            )
                                    .edit()
                                    .clear()
                                    .apply();


                            Intent intent =
                                    new Intent(
                                            ProfileActivity.this,
                                            SignupRoleActivity.class
                                    );


                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );


                            startActivity(
                                    intent
                            );


                            finish();

                        })

                .show();

    }


    // =====================================================
    // PREPARE ANIMATIONS
    // =====================================================

    private void prepareAnimations() {

        prepareAnimation(
                profileImage
        );

        prepareAnimation(
                profileName
        );

        prepareAnimation(
                profileId
        );


        MaterialCardView personalCard =
                findViewById(
                        R.id.cardPersonal
                );


        MaterialCardView academicCard =
                findViewById(
                        R.id.cardAcademic
                );


        MaterialCardView contactCard =
                findViewById(
                        R.id.cardContact
                );


        MaterialCardView accountCard =
                findViewById(
                        R.id.cardAccount
                );


        prepareAnimation(
                personalCard
        );

        prepareAnimation(
                academicCard
        );

        prepareAnimation(
                contactCard
        );

        prepareAnimation(
                accountCard
        );

    }


    // =====================================================
    // START ANIMATIONS
    // =====================================================

    private void startAnimations() {

        MaterialCardView personalCard =
                findViewById(
                        R.id.cardPersonal
                );


        MaterialCardView academicCard =
                findViewById(
                        R.id.cardAcademic
                );


        MaterialCardView contactCard =
                findViewById(
                        R.id.cardContact
                );


        MaterialCardView accountCard =
                findViewById(
                        R.id.cardAccount
                );


        animateView(
                profileImage,
                100
        );


        animateView(
                profileName,
                180
        );


        animateView(
                profileId,
                240
        );


        animateView(
                personalCard,
                350
        );


        animateView(
                academicCard,
                450
        );


        animateView(
                contactCard,
                550
        );


        animateView(
                accountCard,
                650
        );

    }


    // =====================================================
    // PREPARE SINGLE VIEW
    // =====================================================

    private void prepareAnimation(
            View view
    ) {

        if (view == null) {
            return;
        }


        view.setAlpha(
                0f
        );


        view.setTranslationY(
                35f
        );

    }


    // =====================================================
    // ANIMATE SINGLE VIEW
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

                .setStartDelay(
                        delay
                )

                .setDuration(
                        500
                )

                .setInterpolator(
                        new DecelerateInterpolator()
                )

                .start();

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