package com.example.campushub.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.example.campushub.utils.CampusHubPrefs;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssignmentDetailActivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST = 1001;

    private ImageButton btnBack;

    private TextView txtTitle;
    private TextView txtSubject;
    private TextView txtTeacher;
    private TextView txtDescription;
    private TextView txtDueDate;
    private TextView txtMarks;

    private TextView txtSubmissionStatus;
    private TextView txtSubmittedTime;
    private TextView txtMarksObtained;
    private TextView txtFeedback;
    private TextView txtSelectedFile;

    private Button btnAddAttachment;
    private Button btnSubmitAssignment;

    private View submissionCard;
    private View feedbackSection;

    private ExecutorService executorService;

    private int assignmentId;

    private Uri selectedFileUri = null;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_assignment_detail);

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

        btnBack =
                findViewById(R.id.btnBack);

        txtTitle =
                findViewById(R.id.txtTitle);

        txtSubject =
                findViewById(R.id.txtSubject);

        txtTeacher =
                findViewById(R.id.txtTeacher);

        txtDescription =
                findViewById(R.id.txtDescription);

        txtDueDate =
                findViewById(R.id.txtDueDate);

        txtMarks =
                findViewById(R.id.txtMarks);

        txtSubmissionStatus =
                findViewById(R.id.txtSubmissionStatus);

        txtSubmittedTime =
                findViewById(R.id.txtSubmittedTime);

        txtMarksObtained =
                findViewById(R.id.txtMarksObtained);

        txtFeedback =
                findViewById(R.id.txtFeedback);

        txtSelectedFile =
                findViewById(R.id.txtSelectedFile);

        btnAddAttachment =
                findViewById(R.id.btnAddAttachment);

        btnSubmitAssignment =
                findViewById(R.id.btnSubmitAssignment);

        submissionCard =
                findViewById(R.id.submissionCard);

        feedbackSection =
                findViewById(R.id.feedbackSection);


        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        // =====================================================
        // ADD ATTACHMENT
        // =====================================================

        btnAddAttachment.setOnClickListener(
                v -> openFilePicker()
        );


        // =====================================================
        // SUBMIT ASSIGNMENT
        // =====================================================

        btnSubmitAssignment.setOnClickListener(v -> {

            if (selectedFileUri == null) {

                Toast.makeText(
                        AssignmentDetailActivity.this,
                        "Please select a file first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            uploadAssignment();

        });


        // =====================================================
        // LOAD ASSIGNMENT
        // =====================================================

        loadAssignment();


        // =====================================================
        // LOAD SUBMISSION STATUS
        // =====================================================

        loadSubmissionStatus();
    }


    // =========================================================
    // LOAD ASSIGNMENT DETAILS
    // =========================================================

    private void loadAssignment() {

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


        executorService.execute(() -> {

            try {

                String response =
                        ApiClient.get(
                                "api/assignments/student/"
                                        + assignmentId
                                        + "/",
                                accessToken
                        );


                JSONObject assignment =
                        new JSONObject(response);


                runOnUiThread(() -> {

                    try {

                        displayAssignment(
                                assignment
                        );

                    } catch (Exception e) {

                        Toast.makeText(
                                AssignmentDetailActivity.this,
                                "Unable to display assignment",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            AssignmentDetailActivity.this,
                            "Failed to load assignment: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    // =========================================================
    // DISPLAY ASSIGNMENT
    // =========================================================

    private void displayAssignment(
            JSONObject assignment
    ) throws Exception {

        String title =
                assignment.optString(
                        "title",
                        "Assignment"
                );

        String subjectName =
                assignment.optString(
                        "subject_name",
                        "Subject"
                );

        String subjectCode =
                assignment.optString(
                        "subject_code",
                        ""
                );

        String teacherName =
                assignment.optString(
                        "teacher_name",
                        "Teacher"
                );

        String description =
                assignment.optString(
                        "description",
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


        txtTitle.setText(title);


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


        txtTeacher.setText(
                "Teacher: "
                        + teacherName
        );


        if (description.isEmpty()) {

            txtDescription.setText(
                    "No instructions provided."
            );

        } else {

            txtDescription.setText(
                    description
            );
        }


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


        txtMarks.setText(
                "Maximum Marks: "
                        + maxMarks
        );
    }


    // =========================================================
    // LOAD SUBMISSION STATUS
    // =========================================================

    private void loadSubmissionStatus() {

        CampusHubPrefs prefs =
                new CampusHubPrefs(this);

        String accessToken =
                prefs.getAccessToken();


        if (accessToken == null ||
                accessToken.isEmpty()) {

            return;
        }


        executorService.execute(() -> {

            try {

                String response =
                        ApiClient.get(
                                "api/assignments/student/"
                                        + assignmentId
                                        + "/submission/",
                                accessToken
                        );


                JSONObject object =
                        new JSONObject(response);


                boolean submitted =
                        object.optBoolean(
                                "submitted",
                                false
                        );


                JSONObject submission =
                        object.optJSONObject(
                                "submission"
                        );


                runOnUiThread(() -> {

                    displaySubmissionStatus(
                            submitted,
                            submission
                    );
                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    txtSubmissionStatus.setText(
                            "Unable to check submission status"
                    );
                });
            }
        });
    }


    // =========================================================
    // DISPLAY SUBMISSION
    // =========================================================

    private void displaySubmissionStatus(
            boolean submitted,
            JSONObject submission
    ) {

        if (!submitted || submission == null) {

            txtSubmissionStatus.setText(
                    "Not submitted"
            );

            txtSubmissionStatus.setTextColor(
                    getColor(
                            android.R.color.holo_orange_dark
                    )
            );

            txtSubmittedTime.setVisibility(
                    View.GONE
            );

            txtMarksObtained.setVisibility(
                    View.GONE
            );

            feedbackSection.setVisibility(
                    View.GONE
            );

            btnSubmitAssignment.setEnabled(
                    selectedFileUri != null
            );

            btnSubmitAssignment.setAlpha(
                    selectedFileUri != null
                            ? 1.0f
                            : 0.5f
            );

            return;
        }


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

        Object marks =
                submission.opt(
                        "marks_obtained"
                );

        String feedback =
                submission.optString(
                        "feedback",
                        ""
                );


        txtSubmissionStatus.setText(
                formatStatus(status)
        );


        txtSubmissionStatus.setTextColor(
                getColor(
                        android.R.color.holo_green_dark
                )
        );


        if (!submittedAt.isEmpty()
                && !submittedAt.equals("null")) {

            txtSubmittedTime.setVisibility(
                    View.VISIBLE
            );

            txtSubmittedTime.setText(
                    "Submitted: "
                            + submittedAt
            );

        } else {

            txtSubmittedTime.setVisibility(
                    View.GONE
            );
        }


        if (marks != null
                && !marks.toString().equals("null")) {

            txtMarksObtained.setVisibility(
                    View.VISIBLE
            );

            txtMarksObtained.setText(
                    "Marks: "
                            + marks
            );

        } else {

            txtMarksObtained.setVisibility(
                    View.GONE
            );
        }


        if (!feedback.isEmpty()) {

            feedbackSection.setVisibility(
                    View.VISIBLE
            );

            txtFeedback.setText(
                    feedback
            );

        } else {

            feedbackSection.setVisibility(
                    View.GONE
            );
        }


        btnSubmitAssignment.setEnabled(
                true
        );

        btnSubmitAssignment.setAlpha(
                1.0f
        );
    }


    // =========================================================
    // FORMAT STATUS
    // =========================================================

    private String formatStatus(
            String status
    ) {

        if (status == null ||
                status.isEmpty()) {

            return "Submitted";
        }


        switch (status.toLowerCase()) {

            case "late":
                return "Submitted Late";

            case "graded":
                return "Graded";

            case "returned":
                return "Returned";

            case "submitted":

            default:
                return "Submitted";
        }
    }


    // =========================================================
    // OPEN FILE PICKER
    // =========================================================

    private void openFilePicker() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType("*/*");

        startActivityForResult(
                intent,
                FILE_PICKER_REQUEST
        );
    }


    // =========================================================
    // FILE SELECTED
    // =========================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );


        if (requestCode == FILE_PICKER_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            selectedFileUri =
                    data.getData();


            if (selectedFileUri != null) {

                try {

                    final int takeFlags =
                            data.getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    getContentResolver()
                            .takePersistableUriPermission(
                                    selectedFileUri,
                                    takeFlags
                            );

                } catch (Exception ignored) {
                }


                String fileName =
                        getFileName(
                                selectedFileUri
                        );


                txtSelectedFile.setVisibility(
                        View.VISIBLE
                );


                txtSelectedFile.setText(
                        "Selected: "
                                + fileName
                );


                btnSubmitAssignment.setEnabled(
                        true
                );

                btnSubmitAssignment.setAlpha(
                        1.0f
                );
            }
        }
    }


    // =========================================================
    // UPLOAD ASSIGNMENT
    // =========================================================

    private void uploadAssignment() {

        if (selectedFileUri == null) {

            Toast.makeText(
                    this,
                    "Please select a file first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


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


        // Prevent double-clicking.
        btnSubmitAssignment.setEnabled(false);

        btnAddAttachment.setEnabled(false);

        btnSubmitAssignment.setText(
                "Submitting..."
        );


        executorService.execute(() -> {

            try {

                // -------------------------------------------------
                // OPEN SELECTED FILE
                // -------------------------------------------------

                InputStream inputStream =
                        getContentResolver()
                                .openInputStream(
                                        selectedFileUri
                                );


                if (inputStream == null) {

                    throw new Exception(
                            "Unable to open selected file"
                    );
                }


                // -------------------------------------------------
                // FILE INFORMATION
                // -------------------------------------------------

                String fileName =
                        getFileName(
                                selectedFileUri
                        );


                String mimeType =
                        getContentResolver()
                                .getType(
                                        selectedFileUri
                                );


                if (mimeType == null ||
                        mimeType.isEmpty()) {

                    mimeType =
                            "application/octet-stream";
                }


                // -------------------------------------------------
                // UPLOAD
                // -------------------------------------------------

                String response =
                        ApiClient.uploadFile(
                                "api/assignments/student/"
                                        + assignmentId
                                        + "/submit/",
                                accessToken,
                                "attachment",
                                fileName,
                                mimeType,
                                inputStream
                        );


                // -------------------------------------------------
                // PARSE SERVER RESPONSE
                // -------------------------------------------------

                JSONObject result =
                        new JSONObject(response);

                /*
                 * Django returns HTTP 201 when the submission
                 * is successfully created.
                 *
                 * The response may contain a "submission"
                 * object rather than "submitted": true.
                 *
                 * Therefore we accept either format.
                 */

                boolean success =
                        result.has("submission")
                                || result.optBoolean(
                                "submitted",
                                false
                        );


                if (!success) {

                    String errorMessage =
                            result.optString(
                                    "detail",
                                    result.optString(
                                            "message",
                                            "Submission failed"
                                    )
                            );

                    throw new Exception(
                            errorMessage
                    );
                }


                // -------------------------------------------------
                // SUCCESS
                // -------------------------------------------------

                runOnUiThread(() -> {

                    Toast.makeText(
                            AssignmentDetailActivity.this,
                            "Assignment submitted successfully!",
                            Toast.LENGTH_LONG
                    ).show();


                    btnSubmitAssignment.setText(
                            "Submitted"
                    );

                    btnSubmitAssignment.setEnabled(
                            false
                    );

                    btnSubmitAssignment.setAlpha(
                            0.6f
                    );

                    btnAddAttachment.setEnabled(
                            true
                    );


                    // Refresh submission information.
                    loadSubmissionStatus();
                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    btnSubmitAssignment.setEnabled(
                            true
                    );

                    btnSubmitAssignment.setAlpha(
                            1.0f
                    );

                    btnSubmitAssignment.setText(
                            "Submit Assignment"
                    );

                    btnAddAttachment.setEnabled(
                            true
                    );


                    Toast.makeText(
                            AssignmentDetailActivity.this,
                            "Upload failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    // =========================================================
    // GET FILE NAME
    // =========================================================

    private String getFileName(
            Uri uri
    ) {

        String result = null;


        if ("content".equals(
                uri.getScheme()
        )) {

            try {

                Cursor cursor =
                        getContentResolver().query(
                                uri,
                                null,
                                null,
                                null,
                                null
                        );


                if (cursor != null) {

                    int nameIndex =
                            cursor.getColumnIndex(
                                    OpenableColumns.DISPLAY_NAME
                            );


                    if (cursor.moveToFirst()
                            && nameIndex >= 0) {

                        result =
                                cursor.getString(
                                        nameIndex
                                );
                    }


                    cursor.close();
                }

            } catch (Exception ignored) {
            }
        }


        if (result == null) {

            result =
                    uri.getPath();


            if (result != null) {

                int cut =
                        result.lastIndexOf('/');


                if (cut != -1) {

                    result =
                            result.substring(
                                    cut + 1
                            );
                }
            }
        }


        if (result == null ||
                result.isEmpty()) {

            result = "Selected file";
        }


        return result;
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