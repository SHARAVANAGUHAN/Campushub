package com.example.campushub.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.ProfileActivity;
import com.example.campushub.R;
import com.example.campushub.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.TreeSet;

public class StudentDashboardActivity extends AppCompatActivity {

    // =====================================================
    // DOUBLE BACK TO EXIT
    // =====================================================

    private long backPressedTime = 0;


    // =====================================================
    // PROGRESS BARS
    // =====================================================

    private ProgressBar attendanceBar;
    private ProgressBar cgpaBar;


    // =====================================================
    // STATISTICS TEXT
    // =====================================================

    private TextView attendanceText;
    private TextView cgpaText;
    private TextView pendingAssignmentsText;


    // =====================================================
    // STUDENT INFORMATION
    // =====================================================

    private TextView studentNameText;
    private TextView headerCourseText;
    private TextView headerYearText;

    private TextView studentIdText;
    private TextView courseText;
    private TextView yearTextView;


    // =====================================================
    // SUBJECTS
    // =====================================================

    private LinearLayout subjectsContainer;

    private Spinner semesterSpinner;

    private JSONArray dashboardSubjects;

    private final ArrayList<Integer> subjectSemesters = new ArrayList<>();

    private int selectedSubjectSemester = -1;


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
                R.layout.activity_student_dashboard
        );


        // =====================================================
        // DOUBLE BACK TO EXIT
        // =====================================================

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        long currentTime =
                                System.currentTimeMillis();

                        if (backPressedTime != 0
                                && currentTime - backPressedTime <= 2000) {

                            finishAffinity();

                        } else {

                            backPressedTime = currentTime;

                            Toast.makeText(
                                    StudentDashboardActivity.this,
                                    "Press back again to exit",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );


        // =====================================================
        // PROFILE BUTTON
        // =====================================================

        ImageView profileButton =
                findViewById(R.id.imgProfile);

        profileButton.setAlpha(0f);
        profileButton.setScaleX(0.7f);
        profileButton.setScaleY(0.7f);

        profileButton.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(
                        new DecelerateInterpolator()
                )
                .start();


        profileButton.setOnClickListener(v -> {

            v.animate()
                    .scaleX(0.92f)
                    .scaleY(0.92f)
                    .setDuration(60)
                    .withEndAction(() -> {

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(60)
                                .start();

                    })
                    .start();


            Intent intent =
                    new Intent(
                            StudentDashboardActivity.this,
                            ProfileActivity.class
                    );

            startActivity(intent);

        });


        // =====================================================
        // PROGRESS BARS
        // =====================================================

        attendanceBar =
                findViewById(R.id.progressAttendance);

        cgpaBar =
                findViewById(R.id.progressCgpa);


        // =====================================================
        // STATISTICS TEXT
        // =====================================================

        attendanceText =
                findViewById(R.id.txtAttendance);

        cgpaText =
                findViewById(R.id.txtCgpa);

        pendingAssignmentsText =
                findViewById(R.id.txtPendingAssignments);


        // =====================================================
        // STUDENT INFORMATION
        // =====================================================

        studentNameText =
                findViewById(R.id.txtStudentName);

        headerCourseText =
                findViewById(R.id.txtHeaderCourse);

        headerYearText =
                findViewById(R.id.txtHeaderYear);

        studentIdText =
                findViewById(R.id.txtStudentId);

        courseText =
                findViewById(R.id.txtCourse);

        yearTextView =
                findViewById(R.id.txtYear);


        // =====================================================
        // SUBJECT CONTAINER
        // =====================================================

        subjectsContainer =
                findViewById(R.id.subjectsContainer);

        semesterSpinner =
                findViewById(R.id.spinnerSemester);


        // =====================================================
        // CARDS
        // =====================================================

        View studentCard =
                findViewById(R.id.cardStudent);

        View statistics =
                findViewById(R.id.statisticsContainer);

        View assignments =
                findViewById(R.id.cardAssignments);

        View quickAssignments =
                findViewById(R.id.cardAssignmentsQuick);

        View announcements =
                findViewById(R.id.cardAnnouncements);

        View attendanceButton =
                findViewById(R.id.btnAttendance);

        View timetableButton =
                findViewById(R.id.btnTimetable);

        View leaveButton =
                findViewById(R.id.cardLeave);

        View libraryButton =
                findViewById(R.id.cardLibrary);

        View semesterDropdown =
                findViewById(R.id.semesterDropdownCard);

        View dashboardHeader =
                findViewById(R.id.dashboardHeader);

        View[] quickAccessCards = {
                attendanceButton,
                timetableButton,
                quickAssignments,
                announcements,
                leaveButton,
                libraryButton
        };


        // =====================================================
        // ANNOUNCEMENTS
        // =====================================================

        if (announcements != null) {

            announcements.setOnClickListener(v -> {

                v.animate()
                        .scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(100)
                        .withEndAction(() -> {

                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .withEndAction(() -> {

                                        Intent intent =
                                                new Intent(
                                                        StudentDashboardActivity.this,
                                                        StudentAnnouncementsActivity.class
                                                );

                                        startActivity(intent);

                                    })
                                    .start();

                        })
                        .start();

            });

        }


        // =====================================================
        // ASSIGNMENTS
        // =====================================================

        if (assignments != null) {

            assignments.setOnClickListener(v -> {

                v.animate()
                        .scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(100)
                        .withEndAction(() -> {

                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .withEndAction(() -> {

                                        Intent intent =
                                                new Intent(
                                                        StudentDashboardActivity.this,
                                                        AssignmentsActivity.class
                                                );

                                        startActivity(intent);

                                    })
                                    .start();

                        })
                        .start();

            });

        }


        // =====================================================
        // QUICK ACCESS - ASSIGNMENTS
        // =====================================================

        if (quickAssignments != null) {

            quickAssignments.setOnClickListener(v -> {

                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(80)
                        .withEndAction(() -> {

                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(80)
                                    .withEndAction(() -> {

                                        Intent intent =
                                                new Intent(
                                                        StudentDashboardActivity.this,
                                                        AssignmentsActivity.class
                                                );

                                        startActivity(intent);

                                    })
                                    .start();

                        })
                        .start();

            });

        }


        // =====================================================
        // ATTENDANCE
        // =====================================================

        if (attendanceButton != null) {

            attendanceButton.setOnClickListener(v -> {

                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(80)
                        .withEndAction(() -> {

                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(80)
                                    .withEndAction(() -> {

                                        Intent intent =
                                                new Intent(
                                                        StudentDashboardActivity.this,
                                                        StudentAttendanceActivity.class
                                                );

                                        startActivity(intent);

                                    })
                                    .start();

                        })
                        .start();

            });

        }


        // =====================================================
        // TIMETABLE
        // =====================================================

        if (timetableButton != null) {

            timetableButton.setOnClickListener(v -> {

                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(80)
                        .withEndAction(() -> {

                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(80)
                                    .withEndAction(() -> {

                                        Intent intent =
                                                new Intent(
                                                        StudentDashboardActivity.this,
                                                        TimetableActivity.class
                                                );

                                        startActivity(intent);

                                    })
                                    .start();

                        })
                        .start();

            });

        }


        // =====================================================
        // LEAVE APPLICATION
        // =====================================================

        if (leaveButton != null) {

            leaveButton.setOnClickListener(v -> {

                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(80)
                        .withEndAction(() -> {

                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(80)
                                    .withEndAction(() -> {

                                        Intent intent =
                                                new Intent(
                                                        StudentDashboardActivity.this,
                                                        LeaveApplicationActivity.class
                                                );

                                        startActivity(intent);

                                    })
                                    .start();

                        })
                        .start();

            });

        }

        if (libraryButton != null) {
            libraryButton.setOnClickListener(v ->
                    startActivity(new Intent(
                            StudentDashboardActivity.this,
                            SharedLibraryActivity.class
                    ))
            );
        }


        // =====================================================
        // LOAD DASHBOARD DATA
        // =====================================================

        loadDashboardData();


        // =====================================================
        // ENTRANCE ANIMATION
        // =====================================================

        prepareAnimation(dashboardHeader);
        prepareAnimation(studentCard);
        prepareAnimation(statistics);
        prepareAnimation(assignments);
        prepareAnimation(semesterDropdown);

        for (View quickAccessCard : quickAccessCards) {
            prepareAnimation(quickAccessCard);
        }

        animateView(dashboardHeader, 0);
        animateView(studentCard, 100);
        animateView(statistics, 220);
        animateView(assignments, 340);
        animateView(semesterDropdown, 420);

        for (int i = 0; i < quickAccessCards.length; i++) {
            animateView(quickAccessCards[i], 500 + (i * 85L));
        }

    }


    // =====================================================
    // LOAD DASHBOARD FROM DJANGO
    // =====================================================

    private void loadDashboardData() {

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
                        new JSONObject(response);


                // =================================================
                // STUDENT INFORMATION
                // =================================================

                JSONObject student =
                        json.getJSONObject("student");


                String studentId =
                        student.getString(
                                "student_id"
                        );


                String fullName =
                        student.getString(
                                "full_name"
                        );


                String course =
                        student.optString(
                                "course",
                                ""
                        );


                String courseCode =
                        student.optString(
                                "course_code",
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
                                ""
                        );


                // =================================================
                // ATTENDANCE
                // =================================================

                double attendance =
                        json.optDouble(
                                "attendance",
                                0
                        );


                // =================================================
                // PENDING ASSIGNMENTS
                // =================================================

                int pendingAssignments =
                        json.optInt(
                                "pending_assignments",
                                0
                        );


                // =================================================
                // GET REAL PENDING ASSIGNMENT COUNT
                // =================================================

                try {

                    String assignmentResponse =
                            ApiClient.get(
                                    "api/assignments/student/list/",
                                    accessToken
                            );


                    JSONObject assignmentJson =
                            new JSONObject(
                                    assignmentResponse
                            );


                    pendingAssignments =
                            assignmentJson.optInt(
                                    "pending_count",
                                    pendingAssignments
                            );


                } catch (Exception ignored) {

                    // Keep dashboard value

                }


                final int finalPendingAssignments =
                        pendingAssignments;


                // =================================================
                // CURRENT SUBJECTS
                // =================================================

                JSONArray subjectsArray =
                        json.optJSONArray(
                                "subjects"
                        );


                // =================================================
                // UPDATE UI
                // =================================================

                runOnUiThread(() -> {

                    // ---------------------------------------------
                    // STUDENT NAME
                    // ---------------------------------------------

                    studentNameText.setText(
                            fullName
                    );


                    // ---------------------------------------------
                    // HEADER COURSE
                    // ---------------------------------------------

                    headerCourseText.setText(
                            course
                    );


                    // ---------------------------------------------
                    // HEADER YEAR
                    // ---------------------------------------------

                    headerYearText.setText(
                            yearText
                    );


                    // ---------------------------------------------
                    // STUDENT ID
                    // ---------------------------------------------

                    studentIdText.setText(
                            "Student ID : "
                                    + studentId
                    );


                    // ---------------------------------------------
                    // COURSE
                    // ---------------------------------------------

                    if (courseCode != null
                            && !courseCode.isEmpty()) {

                        courseText.setText(
                                "Course : "
                                        + courseCode
                                        + " - "
                                        + course
                        );

                    } else {

                        courseText.setText(
                                "Course : "
                                        + course
                        );

                    }


                    // ---------------------------------------------
                    // YEAR
                    // ---------------------------------------------

                    if (admissionYear > 0) {

                        yearTextView.setText(
                                "Year : "
                                        + yearText
                                        + " (Joined "
                                        + admissionYear
                                        + ")"
                        );

                    } else {

                        yearTextView.setText(
                                "Year : "
                                        + yearText
                        );

                    }


                    // ---------------------------------------------
                    // ATTENDANCE
                    // ---------------------------------------------

                    animateAttendance(
                            attendanceBar,
                            attendanceText,
                            (int) Math.round(
                                    attendance
                            )
                    );


                    // ---------------------------------------------
                    // PENDING ASSIGNMENTS
                    // ---------------------------------------------

                    animatePendingAssignments(
                            pendingAssignmentsText,
                            finalPendingAssignments
                    );


                    // ---------------------------------------------
                    // CGPA
                    // ---------------------------------------------

                    cgpaText.setText("--");

                    cgpaBar.setProgress(0);


                    // ---------------------------------------------
                    // SUBJECTS
                    // ---------------------------------------------

                    displaySubjects(
                            subjectsArray
                    );

                });


            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() -> {

                    Toast.makeText(
                            StudentDashboardActivity.this,
                            "Unable to load dashboard",
                            Toast.LENGTH_LONG
                    ).show();

                });

            }

        });

    }


    // =====================================================
    // DISPLAY CURRENT SUBJECTS
    // =====================================================

    private void displaySubjects(
            JSONArray subjectsArray
    ) {

        if (subjectsContainer == null) {
            return;
        }


        subjectsContainer.removeAllViews();


        if (subjectsArray == null
                || subjectsArray.length() == 0) {

            findViewById(R.id.semesterDropdownCard).setVisibility(View.GONE);

            TextView noSubjects =
                    new TextView(this);


            noSubjects.setText(
                    "No subjects available for your current semester."
            );


            noSubjects.setTextSize(16);


            noSubjects.setTextColor(
                    Color.GRAY
            );


            noSubjects.setPadding(
                    20,
                    20,
                    20,
                    20
            );


            subjectsContainer.addView(
                    noSubjects
            );


            return;
        }

        dashboardSubjects = subjectsArray;

        if (semesterSpinner.getAdapter() == null) {
            TreeSet<Integer> semesters = new TreeSet<>();
            for (int i = 0; i < subjectsArray.length(); i++) {
                JSONObject subject = subjectsArray.optJSONObject(i);
                if (subject != null) {
                    int semester = subject.optInt("semester", 0);
                    if (semester > 0) {
                        semesters.add(semester);
                    }
                }
            }

            subjectSemesters.clear();
            subjectSemesters.addAll(semesters);

            if (subjectSemesters.isEmpty()) {
                findViewById(R.id.semesterDropdownCard).setVisibility(View.GONE);
                return;
            }

            ArrayList<String> semesterLabels = new ArrayList<>();
            for (int semester : subjectSemesters) {
                semesterLabels.add("Semester " + semester);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    semesterLabels
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            semesterSpinner.setAdapter(adapter);
            selectedSubjectSemester = subjectSemesters.get(0);
            semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedSubjectSemester = subjectSemesters.get(position);
                    displaySubjects(dashboardSubjects);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }


        // =====================================================
        // LOOP THROUGH SUBJECTS
        // =====================================================

        for (int i = 0;
             i < subjectsArray.length();
             i++) {

            try {

                JSONObject subject =
                        subjectsArray.getJSONObject(i);


                String subjectCode =
                        subject.optString(
                                "subject_code",
                                ""
                        );


                String subjectName =
                        subject.optString(
                                "subject_name",
                                ""
                        );


                int semester =
                        subject.optInt(
                                "semester",
                                0
                        );

                if (selectedSubjectSemester > 0
                        && semester != selectedSubjectSemester) {
                    continue;
                }


                LinearLayout card =
                        createSubjectCard(
                                subjectCode,
                                subjectName,
                                semester
                        );


                prepareAnimation(card);
                subjectsContainer.addView(card);
                animateView(card, subjectsContainer.getChildCount() * 55L);


            } catch (Exception e) {

                e.printStackTrace();

            }

        }

    }


    // =====================================================
    // CREATE SUBJECT CARD
    // =====================================================

    private LinearLayout createSubjectCard(
            String subjectCode,
            String subjectName,
            int semester
    ) {

        LinearLayout card =
                new LinearLayout(this);


        card.setOrientation(
                LinearLayout.VERTICAL
        );


        // =====================================================
        // BACKGROUND
        // =====================================================

        GradientDrawable background =
                new GradientDrawable();


        background.setColor(
                Color.WHITE
        );


        background.setCornerRadius(
                22f
        );


        background.setStroke(
                2,
                Color.parseColor(
                        "#E0E0E0"
                )
        );


        card.setBackground(
                background
        );


        // =====================================================
        // ELEVATION
        // =====================================================

        card.setElevation(
                5f
        );


        // =====================================================
        // PADDING
        // =====================================================

        card.setPadding(
                20,
                18,
                20,
                18
        );


        // =====================================================
        // LAYOUT PARAMS
        // =====================================================

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        cardParams.setMargins(
                0,
                0,
                0,
                14
        );


        card.setLayoutParams(
                cardParams
        );


        // =====================================================
        // SUBJECT NAME
        // =====================================================

        TextView nameText =
                new TextView(this);


        nameText.setText(
                formatSubjectName(
                        subjectName
                )
        );


        nameText.setTextSize(
                19
        );


        nameText.setTextColor(
                Color.parseColor(
                        "#172B4D"
                )
        );


        nameText.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );


        // =====================================================
        // SUBJECT CODE
        // =====================================================

        TextView codeText =
                new TextView(this);


        codeText.setText(
                subjectCode.toUpperCase()
        );


        codeText.setTextSize(
                15
        );


        codeText.setTextColor(
                Color.parseColor(
                        "#1565C0"
                )
        );


        codeText.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );


        LinearLayout.LayoutParams codeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        codeParams.setMargins(
                0,
                6,
                0,
                0
        );


        codeText.setLayoutParams(
                codeParams
        );


        // =====================================================
        // SEMESTER
        // =====================================================

        TextView semesterText =
                new TextView(this);


        semesterText.setText(
                "Semester " + semester
        );


        semesterText.setTextSize(
                14
        );


        semesterText.setTextColor(
                Color.parseColor(
                        "#757575"
                )
        );


        LinearLayout.LayoutParams semesterParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        semesterParams.setMargins(
                0,
                5,
                0,
                0
        );


        semesterText.setLayoutParams(
                semesterParams
        );


        // =====================================================
        // ADD TO CARD
        // =====================================================

        card.addView(
                nameText
        );


        card.addView(
                codeText
        );


        card.addView(
                semesterText
        );


        return card;

    }


    // =====================================================
    // FORMAT SUBJECT NAME
    // =====================================================

    private String formatSubjectName(
            String subjectName
    ) {

        if (subjectName == null
                || subjectName.trim().isEmpty()) {

            return "Unknown Subject";
        }


        String[] words =
                subjectName.trim().split("\\s+");


        StringBuilder result =
                new StringBuilder();


        for (String word : words) {

            if (word.length() == 0) {
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
    // PREPARE VIEW
    // =====================================================

    private void prepareAnimation(
            View view
    ) {

        if (view != null) {

            view.setAlpha(0f);

            view.setTranslationY(50f);

            view.setScaleX(0.96f);

            view.setScaleY(0.96f);

        }

    }


    // =====================================================
    // ENTRANCE ANIMATION
    // =====================================================

    private void animateView(
            View view,
            long delay
    ) {

        if (view != null) {

            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(delay)
                    .setDuration(500)
                    .setInterpolator(
                            new DecelerateInterpolator()
                    )
                    .start();

        }

    }


    // =====================================================
    // ATTENDANCE ANIMATION
    // =====================================================

    private void animateAttendance(
            ProgressBar bar,
            TextView text,
            int target
    ) {

        ObjectAnimator progressAnimation =
                ObjectAnimator.ofInt(
                        bar,
                        "progress",
                        0,
                        target
                );


        progressAnimation.setDuration(
                1200
        );


        progressAnimation.setInterpolator(
                new DecelerateInterpolator()
        );


        progressAnimation.start();


        ValueAnimator numberAnimation =
                ValueAnimator.ofInt(
                        0,
                        target
                );


        numberAnimation.setDuration(
                1200
        );


        numberAnimation.setInterpolator(
                new DecelerateInterpolator()
        );


        numberAnimation.addUpdateListener(
                animation -> {

                    int value =
                            (int)
                                    animation.getAnimatedValue();


                    text.setText(
                            value + "%"
                    );

                }
        );


        numberAnimation.start();

    }


    // =====================================================
    // PENDING ASSIGNMENTS ANIMATION
    // =====================================================

    private void animatePendingAssignments(
            TextView text,
            int target
    ) {

        ValueAnimator animator =
                ValueAnimator.ofInt(
                        0,
                        target
                );


        animator.setDuration(
                600
        );


        animator.setInterpolator(
                new DecelerateInterpolator()
        );


        animator.addUpdateListener(
                animation -> {

                    int value =
                            (int)
                                    animation.getAnimatedValue();


                    text.setText(
                            String.format(
                                    "%02d",
                                    value
                            )
                    );

                }
        );


        animator.start();

    }


    // =====================================================
    // REFRESH DASHBOARD WHEN RETURNING
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();

        loadDashboardData();

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
