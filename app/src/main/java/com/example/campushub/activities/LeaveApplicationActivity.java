package com.example.campushub.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.example.campushub.utils.CampusHubPrefs;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LeaveApplicationActivity extends AppCompatActivity {

    // =====================================================
    // VIEWS
    // =====================================================

    private AutoCompleteTextView spinnerDepartment;
    private AutoCompleteTextView spinnerTeacher;

    private EditText edtFromDate;
    private EditText edtToDate;
    private EditText edtReason;

    private MaterialButton btnAttachment;
    private MaterialButton btnSubmit;

    private TextView txtSelectedFile;
    private TextView txtStatus;
    private TextView txtApplications;

    // =====================================================
    // DEPARTMENT DATA
    // =====================================================

    private final ArrayList<String> departmentNames =
            new ArrayList<>();

    private final ArrayList<Integer> departmentIds =
            new ArrayList<>();

    private int selectedDepartmentId = -1;

    // =====================================================
    // TEACHER DATA
    // =====================================================

    private final ArrayList<String> teacherNames =
            new ArrayList<>();

    private final ArrayList<String> teacherIds =
            new ArrayList<>();

    private String selectedTeacherId = "";

    // =====================================================
    // DATE DATA
    // =====================================================

    private String selectedFromDate = "";
    private String selectedToDate = "";

    // =====================================================
    // FILE
    // =====================================================

    private Uri selectedFileUri = null;

    // =====================================================
    // THREAD
    // =====================================================

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    // =====================================================
    // DATE FORMAT
    // =====================================================

    private final SimpleDateFormat apiDateFormat =
            new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
            );

    private final SimpleDateFormat displayDateFormat =
            new SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.US
            );

    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_leave_application
        );

        // =================================================
        // FIND VIEWS
        // =================================================

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        spinnerDepartment =
                findViewById(R.id.spinnerDepartment);

        spinnerTeacher =
                findViewById(R.id.spinnerTeacher);

        edtFromDate =
                findViewById(R.id.edtFromDate);

        edtToDate =
                findViewById(R.id.edtToDate);

        edtReason =
                findViewById(R.id.edtReason);

        btnAttachment =
                findViewById(R.id.btnAttachment);

        btnSubmit =
                findViewById(R.id.btnSubmit);

        txtSelectedFile =
                findViewById(R.id.txtSelectedFile);

        txtStatus =
                findViewById(R.id.txtStatus);

        txtApplications =
                findViewById(R.id.txtApplications);

        // =================================================
        // BACK BUTTON
        // =================================================

        btnBack.setOnClickListener(v -> finish());

        // =================================================
        // DEPARTMENT
        // =================================================

        spinnerDepartment.setFocusable(false);
        spinnerDepartment.setClickable(true);

        spinnerDepartment.setOnClickListener(v -> {

            if (!departmentNames.isEmpty()) {

                spinnerDepartment.showDropDown();

            } else {

                Toast.makeText(
                        LeaveApplicationActivity.this,
                        "Loading departments...",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // =================================================
        // DEPARTMENT SELECTED
        // =================================================

        spinnerDepartment.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedDepartmentId =
                            departmentIds.get(position);

                    // Reset teacher
                    selectedTeacherId = "";

                    teacherNames.clear();
                    teacherIds.clear();

                    spinnerTeacher.setText(
                            "",
                            false
                    );

                    loadTeachers(
                            selectedDepartmentId
                    );
                }
        );

        // =================================================
        // TEACHER
        // =================================================

        spinnerTeacher.setFocusable(false);
        spinnerTeacher.setClickable(true);

        spinnerTeacher.setOnClickListener(v -> {

            if (selectedDepartmentId == -1) {

                Toast.makeText(
                        LeaveApplicationActivity.this,
                        "Please select department first.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (!teacherNames.isEmpty()) {

                spinnerTeacher.showDropDown();

            } else {

                Toast.makeText(
                        LeaveApplicationActivity.this,
                        "Loading teachers...",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // =================================================
        // TEACHER SELECTED
        // =================================================

        spinnerTeacher.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedTeacherId =
                            teacherIds.get(position);
                }
        );

        // =================================================
        // FROM DATE
        // =================================================

        edtFromDate.setFocusable(false);
        edtFromDate.setClickable(true);

        edtFromDate.setOnClickListener(
                v -> showDatePicker(true)
        );

        // =================================================
        // TO DATE
        // =================================================

        edtToDate.setFocusable(false);
        edtToDate.setClickable(true);

        edtToDate.setOnClickListener(
                v -> showDatePicker(false)
        );

        // =================================================
        // ATTACHMENT
        // =================================================

        btnAttachment.setOnClickListener(
                v -> openFilePicker()
        );

        // =================================================
        // SUBMIT
        // =================================================

        btnSubmit.setOnClickListener(
                v -> submitLeave()
        );

        // =================================================
        // INITIAL LOAD
        // =================================================

        loadDepartments();

        loadApplications();
    }

    // =====================================================
    // GET ACCESS TOKEN
    // =====================================================

    private String getAccessToken() {

        CampusHubPrefs prefs =
                new CampusHubPrefs(this);

        return prefs.getAccessToken();
    }

    // =====================================================
    // LOAD DEPARTMENTS
    // =====================================================

    private void loadDepartments() {

        String accessToken =
                getAccessToken();

        if (accessToken == null ||
                accessToken.isEmpty()) {

            Toast.makeText(
                    this,
                    "Login session expired.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        executorService.execute(() -> {

            try {

                String response =
                        ApiClient.get(
                                "api/leave/student/departments/",
                                accessToken
                        );

                JSONArray array =
                        new JSONArray(response);

                departmentNames.clear();
                departmentIds.clear();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject object =
                            array.getJSONObject(i);

                    int id =
                            object.getInt("id");

                    String name =
                            object.optString(
                                    "department_name",
                                    "Department"
                            );

                    String code =
                            object.optString(
                                    "department_code",
                                    ""
                            );

                    String displayName;

                    if (!code.isEmpty()) {

                        displayName =
                                code + " - " + name;

                    } else {

                        displayName = name;
                    }

                    departmentIds.add(id);
                    departmentNames.add(displayName);
                }

                runOnUiThread(() -> {

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(
                                    LeaveApplicationActivity.this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    departmentNames
                            );

                    spinnerDepartment.setAdapter(
                            adapter
                    );
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            LeaveApplicationActivity.this,
                            "Failed to load departments.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    // =====================================================
    // LOAD TEACHERS
    // =====================================================

    private void loadTeachers(
            int departmentId
    ) {

        String accessToken =
                getAccessToken();

        executorService.execute(() -> {

            try {

                String endpoint =
                        "api/leave/student/teachers/"
                                + "?department_id="
                                + departmentId;

                String response =
                        ApiClient.get(
                                endpoint,
                                accessToken
                        );

                JSONArray array =
                        new JSONArray(response);

                teacherNames.clear();
                teacherIds.clear();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject object =
                            array.getJSONObject(i);

                    String teacherId =
                            object.optString(
                                    "teacher_id",
                                    ""
                            );

                    String fullName =
                            object.optString(
                                    "full_name",
                                    "Teacher"
                            );

                    teacherIds.add(
                            teacherId
                    );

                    teacherNames.add(
                            fullName
                    );
                }

                runOnUiThread(() -> {

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(
                                    LeaveApplicationActivity.this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    teacherNames
                            );

                    spinnerTeacher.setAdapter(
                            adapter
                    );

                    if (teacherNames.isEmpty()) {

                        Toast.makeText(
                                LeaveApplicationActivity.this,
                                "No teachers found.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            LeaveApplicationActivity.this,
                            "Failed to load teachers.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    // =====================================================
    // DATE PICKER
    // =====================================================

    private void showDatePicker(
            boolean isFromDate
    ) {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            Calendar selected =
                                    Calendar.getInstance();

                            selected.set(
                                    year,
                                    month,
                                    dayOfMonth
                            );

                            String apiDate =
                                    apiDateFormat.format(
                                            selected.getTime()
                                    );

                            String displayDate =
                                    displayDateFormat.format(
                                            selected.getTime()
                                    );

                            if (isFromDate) {

                                selectedFromDate =
                                        apiDate;

                                edtFromDate.setText(
                                        displayDate
                                );

                            } else {

                                selectedToDate =
                                        apiDate;

                                edtToDate.setText(
                                        displayDate
                                );
                            }
                        },
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }

    // =====================================================
    // FILE PICKER
    // =====================================================

    private void openFilePicker() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.setType("*/*");

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.putExtra(
                Intent.EXTRA_ALLOW_MULTIPLE,
                false
        );

        startActivityForResult(
                intent,
                1001
        );
    }

    // =====================================================
    // FILE RESULT
    // =====================================================

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

        if (requestCode != 1001) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        if (data == null) {
            return;
        }

        selectedFileUri =
                data.getData();

        if (selectedFileUri != null) {

            String fileName =
                    getFileName(
                            selectedFileUri
                    );

            txtSelectedFile.setText(
                    fileName
            );
        }
    }

    // =====================================================
    // GET FILE NAME
    // =====================================================

    private String getFileName(
            Uri uri
    ) {

        String result = null;

        if ("content".equals(
                uri.getScheme()
        )) {

            try (
                    Cursor cursor =
                            getContentResolver().query(
                                    uri,
                                    null,
                                    null,
                                    null,
                                    null
                            )
            ) {

                if (cursor != null &&
                        cursor.moveToFirst()) {

                    int index =
                            cursor.getColumnIndex(
                                    OpenableColumns.DISPLAY_NAME
                            );

                    if (index >= 0) {

                        result =
                                cursor.getString(index);
                    }
                }

            } catch (Exception ignored) {
            }
        }

        if (result == null) {

            result =
                    uri.getLastPathSegment();
        }

        if (result == null ||
                result.isEmpty()) {

            result = "Selected file";
        }

        return result;
    }

    // =====================================================
    // SUBMIT LEAVE
    // =====================================================

    private void submitLeave() {

        // -------------------------------------------------
        // DEPARTMENT
        // -------------------------------------------------

        if (selectedDepartmentId == -1) {

            Toast.makeText(
                    this,
                    "Please select department.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // TEACHER
        // -------------------------------------------------

        if (selectedTeacherId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select class teacher.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // FROM DATE
        // -------------------------------------------------

        if (selectedFromDate.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select from date.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // TO DATE
        // -------------------------------------------------

        if (selectedToDate.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select to date.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // DATE VALIDATION
        // -------------------------------------------------

        if (selectedToDate.compareTo(
                selectedFromDate
        ) < 0) {

            Toast.makeText(
                    this,
                    "To date cannot be before from date.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // REASON
        // -------------------------------------------------

        String reason =
                edtReason
                        .getText()
                        .toString()
                        .trim();

        if (reason.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter the reason.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // -------------------------------------------------
        // DISABLE SUBMIT BUTTON
        // -------------------------------------------------

        btnSubmit.setEnabled(false);

        btnSubmit.setText(
                "SUBMITTING..."
        );

        // -------------------------------------------------
        // SEND REQUEST
        // -------------------------------------------------

        executorService.execute(() -> {

            InputStream fileInputStream = null;

            try {

                String accessToken =
                        getAccessToken();

                if (accessToken == null ||
                        accessToken.isEmpty()) {

                    throw new Exception(
                            "Login session expired."
                    );
                }

                // =========================================
                // FORM DATA
                // =========================================

                Map<String, String> fields =
                        new HashMap<>();

                fields.put(
                        "department",
                        String.valueOf(
                                selectedDepartmentId
                        )
                );

                fields.put(
                        "class_teacher",
                        selectedTeacherId
                );

                fields.put(
                        "from_date",
                        selectedFromDate
                );

                fields.put(
                        "to_date",
                        selectedToDate
                );

                fields.put(
                        "reason",
                        reason
                );

                // =========================================
                // FILE INFORMATION
                // =========================================

                String fileName = null;
                String mimeType = null;

                if (selectedFileUri != null) {

                    fileName =
                            getFileName(
                                    selectedFileUri
                            );

                    mimeType =
                            getContentResolver()
                                    .getType(
                                            selectedFileUri
                                    );

                    if (mimeType == null ||
                            mimeType.isEmpty()) {

                        mimeType =
                                "application/octet-stream";
                    }

                    fileInputStream =
                            getContentResolver()
                                    .openInputStream(
                                            selectedFileUri
                                    );

                    if (fileInputStream == null) {

                        throw new Exception(
                                "Unable to read selected file."
                        );
                    }
                }

                // =========================================
                // MULTIPART REQUEST
                // =========================================

                String response =
                        ApiClient.postMultipart(
                                "api/leave/student/apply/",
                                accessToken,
                                fields,

                                selectedFileUri != null
                                        ? "attachment"
                                        : null,

                                fileName,
                                mimeType,
                                fileInputStream
                        );

                // =========================================
                // RESPONSE
                // =========================================

                JSONObject result =
                        new JSONObject(response);

                // Backend error response
                if (result.has("error")) {

                    String error =
                            result.optString(
                                    "error",
                                    "Unable to submit leave."
                            );

                    throw new Exception(error);
                }

                String message =
                        result.optString(
                                "message",
                                "Leave application submitted successfully."
                        );

                runOnUiThread(() -> {

                    btnSubmit.setEnabled(true);

                    btnSubmit.setText(
                            "SUBMIT LEAVE APPLICATION"
                    );

                    Toast.makeText(
                            LeaveApplicationActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();

                    clearForm();

                    loadApplications();
                });

            } catch (Exception e) {

                String errorMessage =
                        e.getMessage();

                if (errorMessage == null ||
                        errorMessage.isEmpty()) {

                    errorMessage =
                            "Unable to submit leave.";
                }

                final String finalErrorMessage =
                        errorMessage;

                runOnUiThread(() -> {

                    btnSubmit.setEnabled(true);

                    btnSubmit.setText(
                            "SUBMIT LEAVE APPLICATION"
                    );

                    Toast.makeText(
                            LeaveApplicationActivity.this,
                            "Failed to submit leave: "
                                    + finalErrorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    // =====================================================
    // CLEAR FORM
    // =====================================================

    private void clearForm() {

        spinnerDepartment.setText(
                "",
                false
        );

        spinnerTeacher.setText(
                "",
                false
        );

        edtFromDate.setText("");

        edtToDate.setText("");

        edtReason.setText("");

        txtSelectedFile.setText(
                "No file selected"
        );

        selectedDepartmentId = -1;

        selectedTeacherId = "";

        selectedFromDate = "";

        selectedToDate = "";

        selectedFileUri = null;

        teacherNames.clear();

        teacherIds.clear();
    }

    // =====================================================
    // LOAD APPLICATIONS
    // =====================================================

    private void loadApplications() {

        String accessToken =
                getAccessToken();

        if (accessToken == null ||
                accessToken.isEmpty()) {

            return;
        }

        executorService.execute(() -> {

            try {

                String response =
                        ApiClient.get(
                                "api/leave/student/applications/",
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

                runOnUiThread(() ->
                        displayApplications(
                                finalApplications
                        )
                );

            } catch (Exception e) {

                runOnUiThread(() -> {

                    txtApplications.setText(
                            "Unable to load applications."
                    );

                    txtStatus.setText(
                            "Status unavailable"
                    );
                });
            }
        });
    }

    // =====================================================
    // DISPLAY APPLICATIONS
    // =====================================================

    private void displayApplications(
            JSONArray applications
    ) {

        if (applications.length() == 0) {

            txtStatus.setText(
                    "No applications"
            );

            txtApplications.setText(
                    "You haven't submitted any leave applications yet."
            );

            return;
        }

        StringBuilder builder =
                new StringBuilder();

        String latestStatus =
                "pending";

        for (int i = 0;
             i < applications.length();
             i++) {

            try {

                JSONObject application =
                        applications.getJSONObject(i);

                String teacherName =
                        application.optString(
                                "teacher_name",
                                "Not available"
                        );

                String departmentName =
                        application.optString(
                                "department_name",
                                "Not available"
                        );

                String fromDate =
                        application.optString(
                                "from_date",
                                ""
                        );

                String toDate =
                        application.optString(
                                "to_date",
                                ""
                        );

                int totalDays =
                        application.optInt(
                                "total_days",
                                0
                        );

                String reason =
                        application.optString(
                                "reason",
                                ""
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

                // =========================================
                // ATTACHMENT
                // =========================================

                String attachmentUrl =
                        application.optString(
                                "attachment_url",
                                ""
                        );

                if (i == 0) {

                    latestStatus =
                            status;
                }

                builder.append(
                        "Teacher: "
                );

                builder.append(
                        teacherName
                );

                builder.append(
                        "\n"
                );

                builder.append(
                        "Department: "
                );

                builder.append(
                        departmentName
                );

                builder.append(
                        "\n"
                );

                builder.append(
                        "From: "
                );

                builder.append(
                        fromDate
                );

                builder.append(
                        "\n"
                );

                builder.append(
                        "To: "
                );

                builder.append(
                        toDate
                );

                builder.append(
                        "\n"
                );

                builder.append(
                        "Total Days: "
                );

                builder.append(
                        totalDays
                );

                builder.append(
                        "\n"
                );

                builder.append(
                        "Reason: "
                );

                builder.append(
                        reason
                );

                builder.append(
                        "\n"
                );

                builder.append(
                        "Status: "
                );

                builder.append(
                        getStatusText(status)
                );

                builder.append(
                        "\n"
                );

                if (!remarks.isEmpty()) {

                    builder.append(
                            "Teacher Remarks: "
                    );

                    builder.append(
                            remarks
                    );

                    builder.append(
                            "\n"
                    );
                }

                if (!attachmentUrl.isEmpty()) {

                    builder.append(
                            "Supporting Document: Attached"
                    );

                    builder.append(
                            "\n"
                    );
                }

                if (i <
                        applications.length() - 1) {

                    builder.append(
                            "\n"
                    );

                    builder.append(
                            "────────────────────"
                    );

                    builder.append(
                            "\n\n"
                    );
                }

            } catch (Exception ignored) {
            }
        }

        txtStatus.setText(
                "Latest Status: "
                        + getStatusText(
                        latestStatus
                )
        );

        txtApplications.setText(
                builder.toString()
        );
    }

    // =====================================================
    // STATUS
    // =====================================================

    private String getStatusText(
            String status
    ) {

        if ("approved".equalsIgnoreCase(
                status
        )) {

            return "APPROVED";
        }

        if ("rejected".equalsIgnoreCase(
                status
        )) {

            return "REJECTED";
        }

        return "PENDING";
    }

    // =====================================================
    // DESTROY
    // =====================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executorService.shutdownNow();
    }
}