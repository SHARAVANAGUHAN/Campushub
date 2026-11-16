package com.example.campushub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

public class TeacherAssignmentsActivity extends AppCompatActivity {

    private LinearLayout assignmentContainer;
    private ProgressBar progressBar;
    private TextView txtEmpty;

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_teacher_assignments);

        executorService = Executors.newSingleThreadExecutor();

        // =====================================================
        // FIND VIEWS
        // =====================================================

        assignmentContainer =
                findViewById(R.id.assignmentContainer);

        progressBar =
                findViewById(R.id.progressBar);

        txtEmpty =
                findViewById(R.id.txtEmpty);

        // =====================================================
        // BACK
        // =====================================================

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // =====================================================
        // LOAD ASSIGNMENTS
        // =====================================================

        loadAssignments();
    }


    // =========================================================
    // LOAD TEACHER ASSIGNMENTS
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

            finish();

            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        txtEmpty.setVisibility(View.GONE);

        executorService.execute(() -> {

            try {

                String response =
                        ApiClient.get(
                                "api/assignments/teacher/submissions/",
                                accessToken
                        );

                JSONObject object =
                        new JSONObject(response);

                JSONArray assignments =
                        object.optJSONArray("assignments");

                runOnUiThread(() -> {

                    progressBar.setVisibility(View.GONE);

                    if (assignments == null ||
                            assignments.length() == 0) {

                        txtEmpty.setVisibility(View.VISIBLE);

                        txtEmpty.setText(
                                "No assignments found"
                        );

                        return;
                    }

                    assignmentContainer.removeAllViews();

                    for (int i = 0;
                         i < assignments.length();
                         i++) {

                        try {

                            JSONObject assignment =
                                    assignments.getJSONObject(i);

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

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(
                            TeacherAssignmentsActivity.this,
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
    ) {

        View card =
                getLayoutInflater().inflate(
                        R.layout.item_teacher_assignment,
                        assignmentContainer,
                        false
                );

        TextView txtTitle =
                card.findViewById(R.id.txtAssignmentTitle);

        TextView txtSubject =
                card.findViewById(R.id.txtAssignmentSubject);

        TextView txtDue =
                card.findViewById(R.id.txtDueDate);

        TextView txtSubmissions =
                card.findViewById(R.id.txtSubmissions);

        TextView txtStatus =
                card.findViewById(R.id.txtStatus);


        int assignmentId =
                assignment.optInt("id", -1);

        String title =
                assignment.optString(
                        "title",
                        "Untitled Assignment"
                );

        String subjectName =
                assignment.optString(
                        "subject_name",
                        "Unknown Subject"
                );

        String subjectCode =
                assignment.optString(
                        "subject_code",
                        ""
                );

        String dueDate =
                assignment.optString(
                        "due_date",
                        ""
                );

        String dueTime =
                assignment.optString(
                        "due_time",
                        ""
                );

        int submissionCount =
                assignment.optInt(
                        "submission_count",
                        0
                );

        int submittedCount =
                assignment.optInt(
                        "submitted_count",
                        0
                );

        int lateCount =
                assignment.optInt(
                        "late_count",
                        0
                );

        int gradedCount =
                assignment.optInt(
                        "graded_count",
                        0
                );


        txtTitle.setText(title);

        txtSubject.setText(
                subjectName +
                        (subjectCode.isEmpty()
                                ? ""
                                : " (" + subjectCode + ")")
        );

        String dueText =
                "Due: " + dueDate;

        if (!dueTime.isEmpty() &&
                !dueTime.equals("null")) {

            dueText +=
                    " • " + dueTime;
        }

        txtDue.setText(dueText);


        txtSubmissions.setText(
                submissionCount +
                        " submission" +
                        (submissionCount == 1
                                ? ""
                                : "s")
        );


        txtStatus.setText(
                "Submitted: " + submittedCount +
                        "   •   Late: " + lateCount +
                        "   •   Graded: " + gradedCount
        );


        // =====================================================
        // OPEN SUBMISSIONS
        // =====================================================

        card.setOnClickListener(v -> {

            if (assignmentId == -1) {

                Toast.makeText(
                        this,
                        "Invalid assignment",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent =
                    new Intent(
                            TeacherAssignmentsActivity.this,
                            TeacherAssignmentSubmissionsActivity.class
                    );

            intent.putExtra(
                    "assignment_id",
                    assignmentId
            );

            startActivity(intent);
        });


        assignmentContainer.addView(card);
    }


    @Override
    protected void onResume() {

        super.onResume();

        if (assignmentContainer != null) {

            loadAssignments();
        }
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (executorService != null) {

            executorService.shutdown();
        }
    }
}