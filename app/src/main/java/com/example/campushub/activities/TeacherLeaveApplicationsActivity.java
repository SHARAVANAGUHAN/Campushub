package com.example.campushub.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeacherLeaveApplicationsActivity extends AppCompatActivity {

    private LinearLayout applicationsContainer;
    private ProgressBar progressBar;
    private TextView txtEmpty;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private String accessToken;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_teacher_leave_applications
        );


        applicationsContainer =
                findViewById(
                        R.id.applicationsContainer
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );

        txtEmpty =
                findViewById(
                        R.id.txtEmpty
                );


        // =================================================
        // GET ACCESS TOKEN
        // =================================================

        accessToken =
                getSharedPreferences(
                        "CampusHubPrefs",
                        MODE_PRIVATE
                ).getString(
                        "access_token",
                        null
                );


        if (accessToken == null
                || accessToken.isEmpty()) {

            Toast.makeText(
                    this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        loadApplications();
    }


    // =====================================================
    // LOAD APPLICATIONS
    // =====================================================

    private void loadApplications() {

        runOnUiThread(() -> {

            progressBar.setVisibility(
                    View.VISIBLE
            );

            txtEmpty.setVisibility(
                    View.GONE
            );

            applicationsContainer.removeAllViews();

        });


        executor.execute(() -> {

            try {

                String response =
                        ApiClient.get(
                                "api/leave/teacher/applications/",
                                accessToken
                        );


                JSONObject json =
                        new JSONObject(response);


                JSONArray applications =
                        json.optJSONArray(
                                "applications"
                        );


                if (applications == null) {

                    applications =
                            new JSONArray();
                }


                JSONArray finalApplications =
                        applications;


                runOnUiThread(() -> {

                    progressBar.setVisibility(
                            View.GONE
                    );


                    // =========================================
                    // NO APPLICATIONS
                    // =========================================

                    if (
                            finalApplications.length()
                                    == 0
                    ) {

                        txtEmpty.setText(
                                "No leave applications yet."
                        );

                        txtEmpty.setVisibility(
                                View.VISIBLE
                        );

                        return;
                    }


                    // =========================================
                    // ADD APPLICATION CARDS
                    // =========================================

                    for (
                            int i = 0;
                            i < finalApplications.length();
                            i++
                    ) {

                        try {

                            JSONObject application =
                                    finalApplications
                                            .getJSONObject(i);

                            addApplicationCard(
                                    application
                            );

                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }

                });


            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() -> {

                    progressBar.setVisibility(
                            View.GONE
                    );

                    txtEmpty.setText(
                            "Unable to load leave applications."
                    );

                    txtEmpty.setVisibility(
                            View.VISIBLE
                    );

                    Toast.makeText(
                            this,
                            "Failed to load leave requests.",
                            Toast.LENGTH_LONG
                    ).show();

                });

            }

        });
    }


    // =====================================================
    // CREATE APPLICATION CARD
    // =====================================================

    private void addApplicationCard(
            JSONObject application
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                30,
                25,
                30,
                25
        );


        // =================================================
        // CARD BACKGROUND
        // =================================================

        android.graphics.drawable.GradientDrawable
                background =
                new android.graphics.drawable.GradientDrawable();

        background.setColor(
                android.graphics.Color.WHITE
        );

        background.setCornerRadius(
                30f
        );

        background.setStroke(
                2,
                android.graphics.Color.parseColor(
                        "#E0E0E0"
                )
        );

        card.setBackground(
                background
        );

        card.setElevation(
                5f
        );


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                20
        );

        card.setLayoutParams(
                cardParams
        );


        // =================================================
        // GET DATA
        // =================================================

        int applicationId =
                application.optInt(
                        "id",
                        -1
                );


        String studentName =
                application.optString(
                        "student_name",
                        "Unknown Student"
                );


        String studentId =
                application.optString(
                        "student_id",
                        "-"
                );


        String department =
                application.optString(
                        "department_name",
                        application.optString(
                                "student_department",
                                "-"
                        )
                );


        String teacherName =
                application.optString(
                        "teacher_name",
                        "-"
                );


        String fromDate =
                application.optString(
                        "from_date",
                        "-"
                );


        String toDate =
                application.optString(
                        "to_date",
                        "-"
                );


        int totalDays =
                application.optInt(
                        "total_days",
                        0
                );


        String reason =
                application.optString(
                        "reason",
                        "-"
                );


        String status =
                application.optString(
                        "status",
                        "pending"
                );


        String remarks =
                application.optString(
                        "teacher_remarks",
                        ""
                );


        String attachmentUrl =
                application.optString(
                        "attachment_url",
                        ""
                );


        // =================================================
        // STUDENT NAME
        // =================================================

        TextView title =
                createText(
                        studentName,
                        21,
                        "#1565C0",
                        true
                );

        card.addView(
                title
        );


        // =================================================
        // STUDENT ID
        // =================================================

        card.addView(
                createText(
                        "Student ID: "
                                + studentId,
                        15,
                        "#424242",
                        false
                )
        );


        // =================================================
        // DEPARTMENT
        // =================================================

        card.addView(
                createText(
                        "Department: "
                                + department,
                        15,
                        "#424242",
                        false
                )
        );


        // =================================================
        // CLASS TEACHER
        // =================================================

        card.addView(
                createText(
                        "Class Teacher: "
                                + teacherName,
                        15,
                        "#424242",
                        false
                )
        );


        // =================================================
        // DATES
        // =================================================

        card.addView(
                createText(
                        "From: "
                                + fromDate
                                + "\nTo: "
                                + toDate
                                + "\nTotal Days: "
                                + totalDays,
                        15,
                        "#424242",
                        false
                )
        );


        // =================================================
        // REASON
        // =================================================

        TextView reasonText =
                createText(
                        "Reason:\n"
                                + reason,
                        15,
                        "#212121",
                        false
                );

        reasonText.setPadding(
                0,
                15,
                0,
                10
        );

        card.addView(
                reasonText
        );


        // =================================================
        // STATUS
        // =================================================

        TextView statusText =
                createText(
                        "Status: "
                                + status.toUpperCase(),
                        16,
                        getStatusColor(status),
                        true
                );

        statusText.setPadding(
                0,
                8,
                0,
                12
        );

        card.addView(
                statusText
        );


        // =================================================
        // ATTACHMENT
        // =================================================

        if (
                attachmentUrl != null
                        && !attachmentUrl.isEmpty()
                        && !attachmentUrl.equals("null")
        ) {

            TextView attachment =
                    createText(
                            "📎 View Attachment",
                            15,
                            "#1565C0",
                            true
                    );

            attachment.setPadding(
                    0,
                    10,
                    0,
                    15
            );

            attachment.setOnClickListener(
                    v -> openAttachment(
                            attachmentUrl
                    )
            );

            card.addView(
                    attachment
            );
        }


        // =================================================
        // PENDING ACTIONS
        // =================================================

        if (
                status.equalsIgnoreCase(
                        "pending"
                )
        ) {

            // =============================================
            // TEACHER REMARKS LABEL
            // =============================================

            TextView remarksLabel =
                    createText(
                            "Teacher Remarks",
                            15,
                            "#424242",
                            true
                    );

            card.addView(
                    remarksLabel
            );


            // =============================================
            // REMARKS INPUT
            // =============================================

            EditText remarksInput =
                    new EditText(this);

            remarksInput.setHint(
                    "Enter remarks (optional)"
            );

            remarksInput.setText(
                    remarks
            );

            remarksInput.setGravity(
                    android.view.Gravity.TOP
            );

            remarksInput.setMinLines(
                    3
            );

            remarksInput.setPadding(
                    18,
                    15,
                    18,
                    15
            );

            card.addView(
                    remarksInput
            );


            // =============================================
            // BUTTON CONTAINER
            // =============================================

            LinearLayout buttons =
                    new LinearLayout(this);

            buttons.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            buttons.setPadding(
                    0,
                    20,
                    0,
                    0
            );


            // =============================================
            // APPROVE BUTTON
            // =============================================

            com.google.android.material.button.MaterialButton
                    approve =
                    new com.google.android.material.button.MaterialButton(
                            this
                    );

            approve.setText(
                    "APPROVE"
            );

            approve.setTextColor(
                    android.graphics.Color.WHITE
            );

            approve.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor(
                                    "#2E7D32"
                            )
                    )
            );


            LinearLayout.LayoutParams approveParams =
                    new LinearLayout.LayoutParams(
                            0,
                            55,
                            1
                    );

            approveParams.setMargins(
                    0,
                    0,
                    8,
                    0
            );

            buttons.addView(
                    approve,
                    approveParams
            );


            // =============================================
            // REJECT BUTTON
            // =============================================

            com.google.android.material.button.MaterialButton
                    reject =
                    new com.google.android.material.button.MaterialButton(
                            this
                    );

            reject.setText(
                    "REJECT"
            );

            reject.setTextColor(
                    android.graphics.Color.WHITE
            );

            reject.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor(
                                    "#C62828"
                            )
                    )
            );


            LinearLayout.LayoutParams rejectParams =
                    new LinearLayout.LayoutParams(
                            0,
                            55,
                            1
                    );

            rejectParams.setMargins(
                    8,
                    0,
                    0,
                    0
            );

            buttons.addView(
                    reject,
                    rejectParams
            );


            card.addView(
                    buttons
            );


            // =============================================
            // APPROVE CLICK
            // =============================================

            approve.setOnClickListener(
                    v -> {

                        String teacherRemarks =
                                remarksInput
                                        .getText()
                                        .toString()
                                        .trim();

                        processLeave(
                                applicationId,
                                true,
                                teacherRemarks
                        );

                    }
            );


            // =============================================
            // REJECT CLICK
            // =============================================

            reject.setOnClickListener(
                    v -> {

                        String teacherRemarks =
                                remarksInput
                                        .getText()
                                        .toString()
                                        .trim();

                        processLeave(
                                applicationId,
                                false,
                                teacherRemarks
                        );

                    }
            );
        }


        applicationsContainer.addView(
                card
        );
    }


    // =====================================================
    // APPROVE / REJECT LEAVE
    // =====================================================

    private void processLeave(
            int applicationId,
            boolean approve,
            String remarks
    ) {

        if (applicationId == -1) {

            Toast.makeText(
                    this,
                    "Invalid leave application.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String endpoint;


        if (approve) {

            endpoint =
                    "api/leave/teacher/applications/"
                            + applicationId
                            + "/approve/";

        } else {

            endpoint =
                    "api/leave/teacher/applications/"
                            + applicationId
                            + "/reject/";
        }


        try {

            JSONObject body =
                    new JSONObject();

            body.put(
                    "teacher_remarks",
                    remarks
            );


            Toast.makeText(
                    this,
                    approve
                            ? "Approving leave..."
                            : "Rejecting leave...",
                    Toast.LENGTH_SHORT
            ).show();


            executor.execute(() -> {

                try {

                    String response =
                            ApiClient.postAuthenticated(
                                    endpoint,
                                    body.toString(),
                                    accessToken
                            );


                    JSONObject result =
                            new JSONObject(
                                    response
                            );


                    String message =
                            result.optString(
                                    "message",
                                    approve
                                            ? "Leave approved."
                                            : "Leave rejected."
                            );


                    runOnUiThread(() -> {

                        Toast.makeText(
                                this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();


                        // Reload applications
                        loadApplications();

                    });


                } catch (Exception e) {

                    e.printStackTrace();

                    runOnUiThread(() -> {

                        Toast.makeText(
                                this,
                                "Failed to process leave request.",
                                Toast.LENGTH_LONG
                        ).show();

                    });

                }

            });


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to create request.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =====================================================
    // OPEN ATTACHMENT
    // =====================================================

    private void openAttachment(
            String url
    ) {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(
                    intent
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to open attachment.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =====================================================
    // CREATE TEXTVIEW
    // =====================================================

    private TextView createText(
            String text,
            int size,
            String color,
            boolean bold
    ) {

        TextView view =
                new TextView(this);


        view.setText(
                text
        );


        view.setTextSize(
                size
        );


        view.setTextColor(
                android.graphics.Color.parseColor(
                        color
                )
        );


        if (bold) {

            view.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }


        view.setPadding(
                0,
                5,
                0,
                5
        );


        return view;
    }


    // =====================================================
    // STATUS COLOR
    // =====================================================

    private String getStatusColor(
            String status
    ) {

        if (
                status.equalsIgnoreCase(
                        "approved"
                )
        ) {

            return "#2E7D32";
        }


        if (
                status.equalsIgnoreCase(
                        "rejected"
                )
        ) {

            return "#C62828";
        }


        return "#EF6C00";
    }


    // =====================================================
    // REFRESH WHEN RETURNING
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (accessToken != null) {

            loadApplications();
        }
    }


    // =====================================================
    // CLEANUP
    // =====================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();
    }
}