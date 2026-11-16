package com.example.campushub.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.example.campushub.utils.CampusHubPrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddAssignmentActivity extends AppCompatActivity {

    private EditText edtTitle;
    private EditText edtDescription;
    private EditText edtDueDate;
    private EditText edtDueTime;
    private EditText edtMaxMarks;

    private AutoCompleteTextView spinnerSubject;
    private TextInputLayout subjectInputLayout;

    private MaterialButton btnPublish;

    private final List<Integer> subjectIds = new ArrayList<>();
    private final List<String> subjectNames = new ArrayList<>();

    private ExecutorService executorService;

    private int selectedSubjectId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_add_assignment);

        executorService = Executors.newSingleThreadExecutor();


        // =====================================================
        // FIND VIEWS
        // =====================================================

        ImageButton btnBack = findViewById(R.id.btnBack);

        edtTitle = findViewById(R.id.edtTitle);

        edtDescription = findViewById(R.id.edtDescription);

        spinnerSubject = findViewById(R.id.spinnerSubject);

        subjectInputLayout = findViewById(R.id.subjectInputLayout);

        edtDueDate = findViewById(R.id.edtDueDate);

        edtDueTime = findViewById(R.id.edtDueTime);

        edtMaxMarks = findViewById(R.id.edtMaxMarks);

        btnPublish = findViewById(R.id.btnPublish);


        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener(v -> finish());


        // =====================================================
        // SUBJECT FIELD
        // =====================================================

        spinnerSubject.setFocusable(false);
        spinnerSubject.setFocusableInTouchMode(false);
        spinnerSubject.setCursorVisible(false);
        spinnerSubject.setClickable(false);

        subjectInputLayout.setEndIconOnClickListener(v -> {

            if (subjectNames.isEmpty()) {

                Toast.makeText(
                        AddAssignmentActivity.this,
                        "Subjects are still loading...",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            showSubjectDialog();
        });


        // Also allow tapping anywhere inside the Subject field
        subjectInputLayout.setOnClickListener(v -> {

            if (subjectNames.isEmpty()) {

                Toast.makeText(
                        AddAssignmentActivity.this,
                        "Subjects are still loading...",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            showSubjectDialog();
        });


        // Extra touch protection
        spinnerSubject.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (subjectNames.isEmpty()) {

                    Toast.makeText(
                            AddAssignmentActivity.this,
                            "Subjects are still loading...",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    showSubjectDialog();
                }
            }

            return true;
        });


        // =====================================================
        // DATE
        // =====================================================

        edtDueDate.setFocusable(false);

        edtDueDate.setOnClickListener(v -> showDatePicker());


        // =====================================================
        // TIME
        // =====================================================

        edtDueTime.setFocusable(false);

        edtDueTime.setOnClickListener(v -> showTimePicker());


        // =====================================================
        // PUBLISH
        // =====================================================

        btnPublish.setOnClickListener(v -> validateAndPublish());


        // =====================================================
        // LOAD SUBJECTS
        // =====================================================

        loadSubjects();
    }


    // =========================================================
    // LOAD SUBJECTS
    // =========================================================

    private void loadSubjects() {

        CampusHubPrefs prefs = new CampusHubPrefs(this);

        String accessToken = prefs.getAccessToken();


        if (accessToken == null || accessToken.isEmpty()) {

            Toast.makeText(
                    this,
                    "Login session not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        executorService.execute(() -> {

            try {

                String response = ApiClient.get(
                        "api/subjects/",
                        accessToken
                );


                subjectIds.clear();
                subjectNames.clear();


                // =================================================
                // NORMAL ARRAY RESPONSE
                // =================================================

                if (response.trim().startsWith("[")) {

                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i++) {

                        JSONObject subject = array.getJSONObject(i);

                        int id = subject.getInt("id");

                        String name = subject.getString("subject_name");

                        String code = subject.getString("subject_code");

                        subjectIds.add(id);

                        subjectNames.add(
                                name + " (" + code + ")"
                        );
                    }
                }


                // =================================================
                // PAGINATED RESPONSE
                // =================================================

                else {

                    JSONObject object = new JSONObject(response);

                    if (object.has("results")) {

                        JSONArray array =
                                object.getJSONArray("results");

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject subject =
                                    array.getJSONObject(i);

                            int id = subject.getInt("id");

                            String name =
                                    subject.getString("subject_name");

                            String code =
                                    subject.getString("subject_code");

                            subjectIds.add(id);

                            subjectNames.add(
                                    name + " (" + code + ")"
                            );
                        }
                    }
                }


                runOnUiThread(() -> {

                    if (subjectNames.isEmpty()) {

                        Toast.makeText(
                                AddAssignmentActivity.this,
                                "No subjects received from server",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                AddAssignmentActivity.this,
                                subjectNames.size()
                                        + " subjects loaded",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            AddAssignmentActivity.this,
                            "Subject error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    // =========================================================
    // SUBJECT SELECTION DIALOG
    // =========================================================

    private void showSubjectDialog() {

        if (subjectNames.isEmpty()) {

            Toast.makeText(
                    this,
                    "No subjects available",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String[] subjects =
                subjectNames.toArray(new String[0]);


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Select Subject")
                        .setItems(
                                subjects,
                                (dialogInterface, which) -> {

                                    selectedSubjectId =
                                            subjectIds.get(which);

                                    spinnerSubject.setText(
                                            subjectNames.get(which),
                                            false
                                    );

                                    spinnerSubject.setError(null);
                                }
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .create();


        dialog.show();
    }


    // =========================================================
    // DATE PICKER
    // =========================================================

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year =
                calendar.get(Calendar.YEAR);

        int month =
                calendar.get(Calendar.MONTH);

        int day =
                calendar.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view,
                         selectedYear,
                         selectedMonth,
                         selectedDay) -> {

                            String date =
                                    String.format(
                                            "%04d-%02d-%02d",
                                            selectedYear,
                                            selectedMonth + 1,
                                            selectedDay
                                    );

                            edtDueDate.setText(date);
                        },
                        year,
                        month,
                        day
                );


        dialog.show();
    }


    // =========================================================
    // TIME PICKER
    // =========================================================

    private void showTimePicker() {

        Calendar calendar = Calendar.getInstance();

        int hour =
                calendar.get(Calendar.HOUR_OF_DAY);

        int minute =
                calendar.get(Calendar.MINUTE);


        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view,
                         selectedHour,
                         selectedMinute) -> {

                            String time =
                                    String.format(
                                            "%02d:%02d",
                                            selectedHour,
                                            selectedMinute
                                    );

                            edtDueTime.setText(time);
                        },
                        hour,
                        minute,
                        true
                );


        dialog.show();
    }


    // =========================================================
    // VALIDATE
    // =========================================================

    private void validateAndPublish() {

        String title =
                edtTitle.getText()
                        .toString()
                        .trim();


        String description =
                edtDescription.getText()
                        .toString()
                        .trim();


        String dueDate =
                edtDueDate.getText()
                        .toString()
                        .trim();


        String dueTime =
                edtDueTime.getText()
                        .toString()
                        .trim();


        String maxMarksText =
                edtMaxMarks.getText()
                        .toString()
                        .trim();


        if (title.isEmpty()) {

            edtTitle.setError(
                    "Enter assignment title"
            );

            edtTitle.requestFocus();

            return;
        }


        if (selectedSubjectId == -1) {

            spinnerSubject.setError(
                    "Select a subject"
            );

            if (!subjectNames.isEmpty()) {
                showSubjectDialog();
            }

            return;
        }


        if (dueDate.isEmpty()) {

            edtDueDate.setError(
                    "Select due date"
            );

            edtDueDate.requestFocus();

            return;
        }


        if (dueTime.isEmpty()) {

            edtDueTime.setError(
                    "Select due time"
            );

            edtDueTime.requestFocus();

            return;
        }


        if (maxMarksText.isEmpty()) {

            edtMaxMarks.setError(
                    "Enter maximum marks"
            );

            edtMaxMarks.requestFocus();

            return;
        }


        int maxMarks;

        try {

            maxMarks =
                    Integer.parseInt(maxMarksText);

        } catch (NumberFormatException e) {

            edtMaxMarks.setError(
                    "Enter a valid number"
            );

            edtMaxMarks.requestFocus();

            return;
        }


        if (maxMarks <= 0) {

            edtMaxMarks.setError(
                    "Marks must be greater than 0"
            );

            edtMaxMarks.requestFocus();

            return;
        }


        publishAssignment(
                selectedSubjectId,
                title,
                description,
                dueDate,
                dueTime,
                maxMarks
        );
    }


    // =========================================================
    // PUBLISH ASSIGNMENT
    // =========================================================

    private void publishAssignment(
            int subjectId,
            String title,
            String description,
            String dueDate,
            String dueTime,
            int maxMarks
    ) {

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


        JSONObject json = new JSONObject();

        try {

            json.put(
                    "subject",
                    subjectId
            );

            json.put(
                    "title",
                    title
            );

            json.put(
                    "description",
                    description
            );

            json.put(
                    "due_date",
                    dueDate
            );

            json.put(
                    "due_time",
                    dueTime
            );

            json.put(
                    "max_marks",
                    maxMarks
            );

            json.put(
                    "is_published",
                    true
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to prepare assignment",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        btnPublish.setEnabled(false);

        btnPublish.setText(
                "Publishing..."
        );


        executorService.execute(() -> {

            try {

                String response =
                        ApiClient.postAuthenticated(
                                "api/assignments/teacher/create/",
                                json.toString(),
                                accessToken
                        );


                runOnUiThread(() -> {

                    Toast.makeText(
                            AddAssignmentActivity.this,
                            "Assignment published successfully",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    btnPublish.setEnabled(true);

                    btnPublish.setText(
                            "Publish Assignment"
                    );


                    Toast.makeText(
                            AddAssignmentActivity.this,
                            "Failed to publish: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
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