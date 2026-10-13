package com.example.campushub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceActivity extends AppCompatActivity {

    private Spinner spinnerCourse;
    private Spinner spinnerYear;
    private Spinner spinnerSemester;
    private Spinner spinnerSubject;

    private Button btnViewStudents;
    private Button btnMarkAllPresent;
    private Button btnSubmitAttendance;

    private LinearLayout studentContainer;
    private TextView txtStudentCount;

    private final List<StudentAttendance> students =
            new ArrayList<>();

    private final List<SubjectItem> subjects =
            new ArrayList<>();

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private String accessToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_attendance);


        // =====================================================
        // FIND VIEWS
        // =====================================================

        spinnerCourse =
                findViewById(R.id.spinnerCourse);

        spinnerYear =
                findViewById(R.id.spinnerYear);

        spinnerSemester =
                findViewById(R.id.spinnerSemester);

        spinnerSubject =
                findViewById(R.id.spinnerSubject);


        btnViewStudents =
                findViewById(R.id.btnViewStudents);

        btnMarkAllPresent =
                findViewById(R.id.btnMarkAllPresent);

        btnSubmitAttendance =
                findViewById(R.id.btnSubmitAttendance);


        studentContainer =
                findViewById(R.id.studentContainer);

        txtStudentCount =
                findViewById(R.id.txtStudentCount);


        // =====================================================
        // GET TEACHER ACCESS TOKEN
        // =====================================================

        accessToken = getSharedPreferences(
                "CampusHubPrefs",
                MODE_PRIVATE
        ).getString(
                "access_token",
                ""
        );


        // =====================================================
        // CHECK LOGIN SESSION
        // =====================================================

        if (accessToken == null || accessToken.isEmpty()) {

            Toast.makeText(
                    this,
                    "Login session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // =====================================================
        // SETUP SPINNERS
        // =====================================================

        setupSpinners();


        // =====================================================
        // SEMESTER SELECTION
        // =====================================================

        spinnerSemester.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        if (position == 0) {

                            subjects.clear();

                            setSubjectSpinner(
                                    new String[]{
                                            "Select Subject"
                                    }
                            );

                            return;
                        }

                        int semester = position;

                        loadSubjects(semester);
                    }


                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );


        // =====================================================
        // VIEW STUDENTS
        // =====================================================

        btnViewStudents.setOnClickListener(
                v -> loadStudents()
        );


        // =====================================================
        // MARK ALL PRESENT
        // =====================================================

        btnMarkAllPresent.setOnClickListener(
                v -> markAllPresent()
        );


        // =====================================================
        // SUBMIT ATTENDANCE
        // =====================================================

        btnSubmitAttendance.setOnClickListener(
                v -> submitAttendance()
        );
    }


    // =========================================================
    // SETUP SPINNERS
    // =========================================================

    private void setupSpinners() {

        setSpinner(
                spinnerCourse,
                new String[]{
                        "Select Course",
                        "BCA",
                        "BBA",
                        "BCom"
                }
        );


        setSpinner(
                spinnerYear,
                new String[]{
                        "Select Year",
                        "1st Year",
                        "2nd Year",
                        "3rd Year"
                }
        );


        setSpinner(
                spinnerSemester,
                new String[]{
                        "Select Semester",
                        "1st Semester",
                        "2nd Semester",
                        "3rd Semester",
                        "4th Semester",
                        "5th Semester",
                        "6th Semester"
                }
        );


        setSubjectSpinner(
                new String[]{
                        "Select Subject"
                }
        );
    }


    // =========================================================
    // NORMAL SPINNER
    // =========================================================

    private void setSpinner(
            Spinner spinner,
            String[] items
    ) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        items
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }


    // =========================================================
    // SUBJECT SPINNER
    // =========================================================

    private void setSubjectSpinner(
            String[] items
    ) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        items
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerSubject.setAdapter(adapter);
    }


    // =========================================================
    // LOAD SUBJECTS FROM DJANGO
    // =========================================================

    private void loadSubjects(int semester) {

        Toast.makeText(
                this,
                "Loading subjects...",
                Toast.LENGTH_SHORT
        ).show();


        executor.execute(() -> {

            try {

                String endpoint =
                        "api/subjects/?semester="
                                + semester;


                String response =
                        ApiClient.get(
                                endpoint,
                                accessToken
                        );


                JSONArray array =
                        new JSONArray(response);


                subjects.clear();


                List<String> subjectNames =
                        new ArrayList<>();


                subjectNames.add(
                        "Select Subject"
                );


                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject object =
                            array.getJSONObject(i);


                    int id =
                            object.getInt("id");


                    String name =
                            object.getString(
                                    "subject_name"
                            );


                    String code =
                            object.getString(
                                    "subject_code"
                            );


                    SubjectItem subject =
                            new SubjectItem(
                                    id,
                                    name,
                                    code
                            );


                    subjects.add(subject);


                    subjectNames.add(
                            name + " (" + code + ")"
                    );
                }


                runOnUiThread(() -> {

                    setSubjectSpinner(
                            subjectNames.toArray(
                                    new String[0]
                            )
                    );


                    if (subjects.isEmpty()) {

                        Toast.makeText(
                                this,
                                "No subjects found for this semester.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Failed to load subjects: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    // =========================================================
    // LOAD STUDENTS FROM DJANGO
    // =========================================================

    private void loadStudents() {

        // -----------------------------------------------------
        // COURSE
        // -----------------------------------------------------

        if (spinnerCourse.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Please select a course.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -----------------------------------------------------
        // YEAR
        // -----------------------------------------------------

        if (spinnerYear.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Please select a year.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -----------------------------------------------------
        // SEMESTER
        // -----------------------------------------------------

        if (spinnerSemester.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Please select a semester.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -----------------------------------------------------
        // SUBJECT
        // -----------------------------------------------------

        if (spinnerSubject.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Please select a subject.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String course =
                spinnerCourse
                        .getSelectedItem()
                        .toString();


        int year =
                spinnerYear
                        .getSelectedItemPosition();


        int semester =
                spinnerSemester
                        .getSelectedItemPosition();


        students.clear();


        Toast.makeText(
                this,
                "Loading students...",
                Toast.LENGTH_SHORT
        ).show();


        executor.execute(() -> {

            try {

                String endpoint =
                        "api/attendance/teacher/students/"
                                + "?course=" + course
                                + "&year=" + year
                                + "&semester=" + semester;


                String response =
                        ApiClient.get(
                                endpoint,
                                accessToken
                        );


                JSONObject result =
                        new JSONObject(response);


                JSONArray studentArray =
                        result.getJSONArray(
                                "students"
                        );


                for (int i = 0;
                     i < studentArray.length();
                     i++) {

                    JSONObject studentObject =
                            studentArray.getJSONObject(i);


                    String studentId =
                            studentObject.getString(
                                    "student_id"
                            );


                    String fullName =
                            studentObject.getString(
                                    "full_name"
                            );


                    students.add(
                            new StudentAttendance(
                                    studentId,
                                    fullName
                            )
                    );
                }


                runOnUiThread(() -> {

                    displayStudents();


                    if (students.isEmpty()) {

                        Toast.makeText(
                                this,
                                "No students found for this class.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Failed to load students: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    // =========================================================
    // DISPLAY STUDENTS
    // =========================================================

    private void displayStudents() {

        studentContainer.removeAllViews();


        for (StudentAttendance student :
                students) {


            LinearLayout row =
                    new LinearLayout(this);


            row.setOrientation(
                    LinearLayout.VERTICAL
            );


            row.setPadding(
                    16,
                    16,
                    16,
                    16
            );


            TextView name =
                    new TextView(this);


            name.setText(
                    student.name
                            + " ("
                            + student.studentId
                            + ")"
            );


            name.setTextSize(16);


            RadioGroup radioGroup =
                    new RadioGroup(this);


            radioGroup.setOrientation(
                    RadioGroup.HORIZONTAL
            );


            RadioButton present =
                    new RadioButton(this);

            RadioButton absent =
                    new RadioButton(this);


            // IMPORTANT:
            // Give IDs BEFORE setting the listener.

            present.setId(
                    View.generateViewId()
            );

            absent.setId(
                    View.generateViewId()
            );


            present.setText("Present");

            absent.setText("Absent");


            radioGroup.addView(present);

            radioGroup.addView(absent);


            radioGroup.setOnCheckedChangeListener(
                    (group, checkedId) -> {

                        if (checkedId ==
                                present.getId()) {

                            student.present = true;

                        } else if (
                                checkedId ==
                                        absent.getId()) {

                            student.present = false;
                        }
                    }
            );


            // Default = Present

            present.setChecked(true);

            student.present = true;


            row.addView(name);

            row.addView(radioGroup);


            studentContainer.addView(row);
        }


        txtStudentCount.setText(
                "Total Students: "
                        + students.size()
        );


        btnMarkAllPresent.setVisibility(
                View.VISIBLE
        );


        btnSubmitAttendance.setVisibility(
                View.VISIBLE
        );
    }


    // =========================================================
    // MARK ALL PRESENT
    // =========================================================

    private void markAllPresent() {

        for (StudentAttendance student :
                students) {

            student.present = true;
        }


        displayStudents();


        Toast.makeText(
                this,
                "All students marked present",
                Toast.LENGTH_SHORT
        ).show();
    }


    // =========================================================
    // SUBMIT ATTENDANCE
    // =========================================================

    private void submitAttendance() {

        if (students.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please load students first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (spinnerSubject.getSelectedItemPosition() == 0) {

            Toast.makeText(
                    this,
                    "Please select a subject.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        int subjectPosition =
                spinnerSubject.getSelectedItemPosition();


        if (subjectPosition - 1 >= subjects.size()) {

            Toast.makeText(
                    this,
                    "Please select a valid subject.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        SubjectItem selectedSubject =
                subjects.get(
                        subjectPosition - 1
                );


        try {

            JSONObject request =
                    new JSONObject();


            // -------------------------------------------------
            // SUBJECT
            // -------------------------------------------------

            request.put(
                    "subject_id",
                    selectedSubject.id
            );


            // -------------------------------------------------
            // DATE
            // -------------------------------------------------

            String date =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                    ).format(
                            new Date()
                    );


            request.put(
                    "date",
                    date
            );


            // -------------------------------------------------
            // STUDENTS
            // -------------------------------------------------

            JSONArray studentArray =
                    new JSONArray();


            for (StudentAttendance student :
                    students) {

                JSONObject studentObject =
                        new JSONObject();


                studentObject.put(
                        "student_id",
                        student.studentId
                );


                studentObject.put(
                        "present",
                        student.present
                );


                studentArray.put(
                        studentObject
                );
            }


            request.put(
                    "students",
                    studentArray
            );


            Toast.makeText(
                    this,
                    "Submitting attendance...",
                    Toast.LENGTH_SHORT
            ).show();


            // -------------------------------------------------
            // SEND TO DJANGO
            // -------------------------------------------------

            executor.execute(() -> {

                try {

                    String response =
                            ApiClient.postAuthenticated(
                                    "api/attendance/teacher/mark/",
                                    request.toString(),
                                    accessToken
                            );


                    JSONObject result =
                            new JSONObject(response);


                    String message =
                            result.optString(
                                    "message",
                                    "Attendance saved successfully."
                            );


                    runOnUiThread(() -> {

                        Toast.makeText(
                                this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();

                    });


                } catch (Exception e) {

                    runOnUiThread(() -> {

                        Toast.makeText(
                                this,
                                "Failed to submit attendance: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }
            });


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Could not create attendance request.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =========================================================
    // SUBJECT CLASS
    // =========================================================

    private static class SubjectItem {

        int id;

        String name;

        String code;


        SubjectItem(
                int id,
                String name,
                String code
        ) {

            this.id = id;

            this.name = name;

            this.code = code;
        }
    }


    // =========================================================
    // STUDENT CLASS
    // =========================================================

    private static class StudentAttendance {

        String studentId;

        String name;

        boolean present = true;


        StudentAttendance(
                String studentId,
                String name
        ) {

            this.studentId = studentId;

            this.name = name;
        }
    }


    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdown();
    }
}