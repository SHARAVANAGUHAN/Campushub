package com.example.campushub.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.example.campushub.utils.CampusHubPrefs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AssignmentsActivity extends AppCompatActivity {


    private LinearLayout assignmentsContainer;

    private TextView txtEmpty;

    private ExecutorService executorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_assignments
        );


        executorService =
                Executors.newSingleThreadExecutor();


        // =====================================================
        // FIND VIEWS
        // =====================================================

        ImageButton btnBack =
                findViewById(R.id.btnBack);


        assignmentsContainer =
                findViewById(
                        R.id.assignmentsContainer
                );


        txtEmpty =
                findViewById(
                        R.id.txtEmpty
                );


        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // LOAD ASSIGNMENTS
        // =====================================================

        loadAssignments();

    }


    // =========================================================
    // LOAD STUDENT ASSIGNMENTS
    // =========================================================

    private void loadAssignments() {


        CampusHubPrefs prefs =
                new CampusHubPrefs(this);


        String accessToken =
                prefs.getAccessToken();


        if (accessToken == null ||
                accessToken.isEmpty()) {

            Toast.makeText(
                    this,
                    "Login session expired",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        executorService.execute(() -> {

            try {


                String response =
                        ApiClient.get(
                                "api/assignments/student/list/",
                                accessToken
                        );


                JSONObject object =
                        new JSONObject(
                                response
                        );


                JSONArray assignments =
                        object.getJSONArray(
                                "assignments"
                        );


                runOnUiThread(() -> {

                    assignmentsContainer
                            .removeAllViews();


                    // =========================================
                    // NO ASSIGNMENTS
                    // =========================================

                    if (assignments.length() == 0) {

                        txtEmpty.setVisibility(
                                View.VISIBLE
                        );

                        return;
                    }


                    txtEmpty.setVisibility(
                            View.GONE
                    );


                    // =========================================
                    // CREATE CARDS
                    // =========================================

                    for (int i = 0;
                         i < assignments.length();
                         i++) {

                        try {

                            JSONObject assignment =
                                    assignments
                                            .getJSONObject(i);


                            addAssignmentCard(
                                    assignment
                            );


                        } catch (Exception e) {

                            e.printStackTrace();

                        }

                    }

                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            AssignmentsActivity.this,
                            "Failed to load assignments: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });

            }

        });

    }


    // =========================================================
    // CREATE ASSIGNMENT CARD
    // =========================================================

    private void addAssignmentCard(
            JSONObject assignment
    ) throws Exception {


        View card =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.item_assignment,
                                assignmentsContainer,
                                false
                        );


        // =====================================================
        // FIND VIEWS
        // =====================================================

        TextView txtTitle =
                card.findViewById(
                        R.id.txtAssignmentTitle
                );


        TextView txtSubject =
                card.findViewById(
                        R.id.txtAssignmentSubject
                );


        TextView txtDueDate =
                card.findViewById(
                        R.id.txtAssignmentDue
                );


        TextView txtMarks =
                card.findViewById(
                        R.id.txtAssignmentMarks
                );


        TextView txtStatus =
                card.findViewById(
                        R.id.txtAssignmentStatus
                );


        // =====================================================
        // ASSIGNMENT DATA
        // =====================================================

        String title =
                assignment.optString(
                        "title",
                        "Assignment"
                );


        String subjectName =
                assignment.optString(
                        "subject_name",
                        ""
                );


        String subjectCode =
                assignment.optString(
                        "subject_code",
                        ""
                );


        String dueDate =
                assignment.optString(
                        "due_date",
                        "Not specified"
                );


        String dueTime =
                assignment.optString(
                        "due_time",
                        ""
                );


        int maxMarks =
                assignment.optInt(
                        "max_marks",
                        0
                );


        // =====================================================
        // REAL SUBMISSION STATUS
        // =====================================================

        boolean submitted =
                assignment.optBoolean(
                        "submitted",
                        false
                );


        String submissionStatus =
                assignment.optString(
                        "submission_status",
                        ""
                );


        // =====================================================
        // TITLE
        // =====================================================

        txtTitle.setText(
                title
        );


        // =====================================================
        // SUBJECT
        // =====================================================

        if (!subjectName.isEmpty()) {

            if (!subjectCode.isEmpty()) {

                txtSubject.setText(
                        subjectName
                                + " ("
                                + subjectCode
                                + ")"
                );

            } else {

                txtSubject.setText(
                        subjectName
                );

            }

        } else {

            txtSubject.setText(
                    "Subject"
            );

        }


        // =====================================================
        // DUE DATE
        // =====================================================

        if (!dueTime.isEmpty()
                && !dueTime.equals("null")) {

            txtDueDate.setText(
                    "Due: "
                            + dueDate
                            + " • "
                            + dueTime
            );

        } else {

            txtDueDate.setText(
                    "Due: "
                            + dueDate
            );

        }


        // =====================================================
        // MARKS
        // =====================================================

        txtMarks.setText(
                maxMarks
                        + " Marks"
        );


        // =====================================================
        // STATUS
        // =====================================================

        if (submitted) {


            // -----------------------------------------------
            // SUBMITTED
            // -----------------------------------------------

            String displayStatus =
                    "✓ SUBMITTED";


            // If the assignment was submitted late,
            // show that clearly.

            if ("late".equalsIgnoreCase(
                    submissionStatus
            )) {

                displayStatus =
                        "✓ SUBMITTED • LATE";

            }


            txtStatus.setText(
                    displayStatus
            );


            // GREEN

            txtStatus.setTextColor(
                    Color.parseColor(
                            "#2E7D32"
                    )
            );


        } else {


            // -----------------------------------------------
            // NOT SUBMITTED
            // -----------------------------------------------

            txtStatus.setText(
                    "NOT SUBMITTED"
            );


            // RED

            txtStatus.setTextColor(
                    Color.parseColor(
                            "#D32F2F"
                    )
            );

        }


        // =====================================================
        // OPEN ASSIGNMENT
        // =====================================================

        card.setOnClickListener(v -> {

            try {

                int assignmentId =
                        assignment.getInt(
                                "id"
                        );


                android.content.Intent intent =
                        new android.content.Intent(
                                AssignmentsActivity.this,
                                AssignmentDetailActivity.class
                        );


                intent.putExtra(
                        "assignment_id",
                        assignmentId
                );


                startActivity(
                        intent
                );


            } catch (Exception e) {

                Toast.makeText(
                        AssignmentsActivity.this,
                        "Unable to open assignment",
                        Toast.LENGTH_SHORT
                ).show();

            }

        });


        // =====================================================
        // ADD CARD
        // =====================================================

        assignmentsContainer.addView(
                card
        );

    }


    // =========================================================
    // REFRESH WHEN RETURNING
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();


        if (executorService != null) {

            loadAssignments();

        }

    }


    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();


        if (executorService != null) {

            executorService.shutdown();

        }

    }

}