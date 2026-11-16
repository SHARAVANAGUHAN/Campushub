package com.example.campushub.activities;

import android.content.Intent;
import android.net.Uri;
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

public class TeacherAssignmentSubmissionsActivity
        extends AppCompatActivity {

    private LinearLayout submissionContainer;

    private ProgressBar progressBar;

    private TextView txtAssignmentTitle;
    private TextView txtAssignmentSubject;
    private TextView txtSubmissionCount;
    private TextView txtEmpty;

    private ExecutorService executorService;

    private int assignmentId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_teacher_assignment_submissions
        );

        executorService =
                Executors.newSingleThreadExecutor();


        // =====================================================
        // GET ASSIGNMENT ID
        // =====================================================

        assignmentId =
                getIntent().getIntExtra(
                        "assignment_id",
                        -1
                );


        if (assignmentId == -1) {

            Toast.makeText(
                    this,
                    "Invalid assignment",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // =====================================================
        // FIND VIEWS
        // =====================================================

        submissionContainer =
                findViewById(
                        R.id.submissionContainer
                );

        progressBar =
                findViewById(
                        R.id.progressBar
                );

        txtAssignmentTitle =
                findViewById(
                        R.id.txtAssignmentTitle
                );

        txtAssignmentSubject =
                findViewById(
                        R.id.txtAssignmentSubject
                );

        txtSubmissionCount =
                findViewById(
                        R.id.txtSubmissionCount
                );

        txtEmpty =
                findViewById(
                        R.id.txtEmpty
                );


        // =====================================================
        // BACK
        // =====================================================

        findViewById(
                R.id.btnBack
        ).setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // LOAD
        // =====================================================

        loadSubmissions();
    }


    // =========================================================
    // LOAD SUBMISSIONS
    // =========================================================

    private void loadSubmissions() {

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


        progressBar.setVisibility(
                View.VISIBLE
        );

        txtEmpty.setVisibility(
                View.GONE
        );


        executorService.execute(() -> {

            try {

                String response =
                        ApiClient.get(
                                "api/assignments/teacher/"
                                        + assignmentId
                                        + "/submissions/",
                                accessToken
                        );


                JSONObject object =
                        new JSONObject(response);


                JSONObject assignment =
                        object.optJSONObject(
                                "assignment"
                        );


                JSONArray submissions =
                        object.optJSONArray(
                                "submissions"
                        );


                runOnUiThread(() -> {

                    progressBar.setVisibility(
                            View.GONE
                    );


                    // =================================================
                    // ASSIGNMENT INFORMATION
                    // =================================================

                    if (assignment != null) {

                        txtAssignmentTitle.setText(
                                assignment.optString(
                                        "title",
                                        "Assignment"
                                )
                        );


                        txtAssignmentSubject.setText(
                                assignment.optString(
                                        "subject_name",
                                        "Subject"
                                )
                                        +
                                        " • "
                                        +
                                        assignment.optString(
                                                "subject_code",
                                                ""
                                        )
                        );
                    }


                    int count =
                            object.optInt(
                                    "count",
                                    submissions == null
                                            ? 0
                                            : submissions.length()
                            );


                    txtSubmissionCount.setText(
                            count
                                    + " submission"
                                    + (count == 1
                                    ? ""
                                    : "s")
                    );


                    submissionContainer.removeAllViews();


                    if (submissions == null ||
                            submissions.length() == 0) {

                        txtEmpty.setVisibility(
                                View.VISIBLE
                        );

                        txtEmpty.setText(
                                "No students have submitted this assignment yet."
                        );

                        return;
                    }


                    // =================================================
                    // ADD STUDENT SUBMISSIONS
                    // =================================================

                    for (int i = 0;
                         i < submissions.length();
                         i++) {

                        try {

                            JSONObject submission =
                                    submissions.getJSONObject(i);

                            addSubmissionCard(
                                    submission
                            );

                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    progressBar.setVisibility(
                            View.GONE
                    );

                    Toast.makeText(
                            TeacherAssignmentSubmissionsActivity.this,
                            "Failed to load submissions: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    // =========================================================
    // CREATE SUBMISSION CARD
    // =========================================================

    private void addSubmissionCard(
            JSONObject submission
    ) {

        View card =
                getLayoutInflater().inflate(
                        R.layout.item_student_submission,
                        submissionContainer,
                        false
                );


        TextView txtStudentName =
                card.findViewById(
                        R.id.txtStudentName
                );

        TextView txtStudentId =
                card.findViewById(
                        R.id.txtStudentId
                );

        TextView txtStatus =
                card.findViewById(
                        R.id.txtStatus
                );

        TextView txtSubmittedAt =
                card.findViewById(
                        R.id.txtSubmittedAt
                );

        TextView txtMarks =
                card.findViewById(
                        R.id.txtMarks
                );

        TextView txtFeedback =
                card.findViewById(
                        R.id.txtFeedback
                );

        TextView btnOpenFile =
                card.findViewById(
                        R.id.btnOpenFile
                );


        String studentName =
                submission.optString(
                        "student_name",
                        "Unknown Student"
                );

        String studentId =
                submission.optString(
                        "student_id",
                        "Unknown ID"
                );

        String status =
                submission.optString(
                        "status",
                        "submitted"
                );

        String submittedAt =
                submission.optString(
                        "submitted_at",
                        ""
                );

        String attachmentUrl =
                submission.optString(
                        "attachment_url",
                        ""
                );


        txtStudentName.setText(
                studentName
        );

        txtStudentId.setText(
                "Student ID: " + studentId
        );


        txtStatus.setText(
                formatStatus(status)
        );


        if (submittedAt.isEmpty() ||
                submittedAt.equals("null")) {

            txtSubmittedAt.setText(
                    "Submitted time: Not available"
            );

        } else {

            txtSubmittedAt.setText(
                    "Submitted: " + submittedAt
            );
        }


        // =====================================================
        // MARKS
        // =====================================================

        if (submission.isNull("marks_obtained")) {

            txtMarks.setText(
                    "Marks: Not graded"
            );

        } else {

            txtMarks.setText(
                    "Marks: "
                            + submission.optInt(
                            "marks_obtained"
                    )
            );
        }


        // =====================================================
        // FEEDBACK
        // =====================================================

        String feedback =
                submission.optString(
                        "feedback",
                        ""
                );


        if (feedback.isEmpty()) {

            txtFeedback.setText(
                    "Feedback: No feedback"
            );

        } else {

            txtFeedback.setText(
                    "Feedback: " + feedback
            );
        }


        // =====================================================
        // OPEN SUBMITTED FILE
        // =====================================================

        if (attachmentUrl.isEmpty() ||
                attachmentUrl.equals("null")) {

            btnOpenFile.setVisibility(
                    View.GONE
            );

        } else {

            btnOpenFile.setVisibility(
                    View.VISIBLE
            );

            btnOpenFile.setOnClickListener(
                    v -> openFile(
                            attachmentUrl
                    )
            );
        }


        submissionContainer.addView(
                card
        );
    }


    // =========================================================
    // FORMAT STATUS
    // =========================================================

    private String formatStatus(
            String status
    ) {

        if (status == null) {

            return "Status: Submitted";
        }

        switch (status.toLowerCase()) {

            case "late":
                return "Status: Late";

            case "graded":
                return "Status: Graded";

            case "returned":
                return "Status: Returned";

            default:
                return "Status: Submitted";
        }
    }


    // =========================================================
    // OPEN FILE
    // =========================================================

    private void openFile(
            String url
    ) {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to open submitted file",
                    Toast.LENGTH_SHORT
            ).show();
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