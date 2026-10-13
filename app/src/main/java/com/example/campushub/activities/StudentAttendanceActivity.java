package com.example.campushub.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
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


public class StudentAttendanceActivity extends AppCompatActivity {

    // =====================================================
    // VIEWS
    // =====================================================

    private ImageButton btnBack;

    private TextView txtTitle;
    private TextView txtSubtitle;

    private TextView txtOverallPercentage;
    private TextView txtAttendanceStatus;
    private TextView txtOverallClasses;

    private TextView txtAttended;
    private TextView txtMissed;

    private ProgressBar progressOverall;

    private LinearLayout subjectsContainer;


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

        setContentView(
                R.layout.activity_student_attendance
        );


        // =====================================================
        // FIND VIEWS
        // =====================================================

        btnBack =
                findViewById(R.id.btnBack);

        txtTitle =
                findViewById(R.id.txtTitle);

        txtSubtitle =
                findViewById(R.id.txtSubtitle);

        txtOverallPercentage =
                findViewById(R.id.txtOverallPercentage);

        txtAttendanceStatus =
                findViewById(R.id.txtAttendanceStatus);

        txtOverallClasses =
                findViewById(R.id.txtOverallClasses);

        txtAttended =
                findViewById(R.id.txtAttended);

        txtMissed =
                findViewById(R.id.txtMissed);

        progressOverall =
                findViewById(R.id.progressOverall);

        subjectsContainer =
                findViewById(R.id.subjectsContainer);


        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener(v -> {

            v.animate()
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(80)
                    .withEndAction(() -> {

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start();

                        finish();

                    })
                    .start();

        });


        // =====================================================
        // INITIAL VALUES
        // =====================================================

        txtOverallPercentage.setText(
                "0%"
        );

        txtAttendanceStatus.setText(
                "Loading attendance..."
        );

        txtOverallClasses.setText(
                "Loading classes..."
        );

        txtAttended.setText(
                "0"
        );

        txtMissed.setText(
                "0"
        );

        progressOverall.setProgress(
                0
        );


        // =====================================================
        // HEADER ANIMATION
        // =====================================================

        prepareView(
                txtTitle
        );

        prepareView(
                txtSubtitle
        );

        animateView(
                txtTitle,
                100
        );

        animateView(
                txtSubtitle,
                180
        );


        // =====================================================
        // LOAD DATA
        // =====================================================

        loadAttendanceData();

    }


    // =====================================================
    // LOAD ATTENDANCE DATA
    // =====================================================

    private void loadAttendanceData() {

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
        // TOKEN CHECK
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
        // API CALL
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


                // =================================================
                // STUDENT INFORMATION
                // =================================================

                JSONObject student =
                        json.optJSONObject(
                                "student"
                        );


                String courseCode = "";

                String yearText = "";


                if (student != null) {

                    courseCode =
                            student.optString(
                                    "course_code",
                                    ""
                            );

                    yearText =
                            student.optString(
                                    "year_text",
                                    ""
                            );

                }


                // =================================================
                // OVERALL ATTENDANCE
                // =================================================

                double overallAttendance =
                        json.optDouble(
                                "attendance",
                                0
                        );


                int totalAttended =
                        json.optInt(
                                "attended_classes",
                                0
                        );


                int totalClasses =
                        json.optInt(
                                "total_classes",
                                0
                        );


                int missedClasses =
                        json.optInt(
                                "missed_classes",
                                Math.max(
                                        0,
                                        totalClasses
                                                - totalAttended
                                )
                        );


                // =================================================
                // SUBJECTS
                // =================================================

                JSONArray subjects =
                        json.optJSONArray(
                                "subjects"
                        );


                // =================================================
                // FINAL VALUES
                // =================================================

                final double finalAttendance =
                        overallAttendance;


                final int finalAttended =
                        totalAttended;


                final int finalClasses =
                        totalClasses;


                final int finalMissed =
                        missedClasses;


                final String finalCourseCode =
                        courseCode;


                final String finalYearText =
                        yearText;


                // =================================================
                // UPDATE UI
                // =================================================

                runOnUiThread(() -> {


                    // ---------------------------------------------
                    // HEADER
                    // ---------------------------------------------

                    if (!finalCourseCode.isEmpty()
                            && !finalYearText.isEmpty()) {

                        txtSubtitle.setText(
                                finalCourseCode
                                        + " • "
                                        + finalYearText
                        );

                    } else {

                        txtSubtitle.setText(
                                "Track your class attendance"
                        );

                    }


                    // ---------------------------------------------
                    // OVERALL ATTENDANCE
                    // ---------------------------------------------

                    updateOverallAttendance(
                            finalAttendance,
                            finalAttended,
                            finalClasses,
                            finalMissed
                    );


                    // ---------------------------------------------
                    // SUBJECTS
                    // ---------------------------------------------

                    displaySubjects(
                            subjects
                    );

                });


            } catch (Exception e) {

                e.printStackTrace();


                runOnUiThread(() -> {

                    Toast.makeText(
                            StudentAttendanceActivity.this,
                            "Unable to load attendance",
                            Toast.LENGTH_LONG
                    ).show();


                    txtAttendanceStatus.setText(
                            "Unable to load attendance"
                    );

                });

            }

        });

    }


    // =====================================================
    // UPDATE OVERALL ATTENDANCE
    // =====================================================

    private void updateOverallAttendance(
            double attendance,
            int attended,
            int total,
            int missed
    ) {

        int percentage =
                (int) Math.round(
                        attendance
                );


        // =====================================================
        // STATUS
        // =====================================================

        if (total == 0) {

            txtAttendanceStatus.setText(
                    "No classes yet"
            );

            txtAttendanceStatus.setTextColor(
                    Color.parseColor(
                            "#64748B"
                    )
            );

        } else if (percentage >= 85) {

            txtAttendanceStatus.setText(
                    "Excellent Attendance"
            );

            txtAttendanceStatus.setTextColor(
                    Color.parseColor(
                            "#2E7D32"
                    )
            );

        } else if (percentage >= 75) {

            txtAttendanceStatus.setText(
                    "Good Attendance"
            );

            txtAttendanceStatus.setTextColor(
                    Color.parseColor(
                            "#558B2F"
                    )
            );

        } else {

            txtAttendanceStatus.setText(
                    "Attendance Below Required Level"
            );

            txtAttendanceStatus.setTextColor(
                    Color.parseColor(
                            "#D32F2F"
                    )
            );

        }


        // =====================================================
        // CLASSES
        // =====================================================

        if (total == 0) {

            txtOverallClasses.setText(
                    "0 classes conducted"
            );

        } else {

            txtOverallClasses.setText(
                    attended
                            + " attended out of "
                            + total
                            + " classes"
            );

        }


        // =====================================================
        // SUMMARY
        // =====================================================

        animateNumber(
                txtAttended,
                attended,
                800
        );


        animateNumber(
                txtMissed,
                missed,
                800
        );


        // =====================================================
        // OVERALL PROGRESS
        // =====================================================

        int safePercentage =
                Math.max(
                        0,
                        Math.min(
                                100,
                                percentage
                        )
                );


        animateProgress(
                progressOverall,
                safePercentage,
                1200
        );


        // =====================================================
        // OVERALL PERCENTAGE
        // =====================================================

        if (total == 0) {

            txtOverallPercentage.setText(
                    "No classes"
            );

        } else {

            animatePercentage(
                    txtOverallPercentage,
                    safePercentage,
                    1200
            );

        }

    }


    // =====================================================
    // DISPLAY SUBJECTS
    // =====================================================

    private void displaySubjects(
            JSONArray subjects
    ) {

        subjectsContainer.removeAllViews();


        // =====================================================
        // NO SUBJECTS
        // =====================================================

        if (subjects == null
                || subjects.length() == 0) {

            TextView emptyText =
                    new TextView(
                            this
                    );


            emptyText.setText(
                    "No subjects available for your current semester."
            );


            emptyText.setTextSize(
                    15
            );


            emptyText.setTextColor(
                    Color.parseColor(
                            "#64748B"
                    )
            );


            emptyText.setGravity(
                    Gravity.CENTER
            );


            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );


            params.setMargins(
                    20,
                    16,
                    20,
                    0
            );


            emptyText.setLayoutParams(
                    params
            );


            subjectsContainer.addView(
                    emptyText
            );


            return;

        }


        // =====================================================
        // CREATE SUBJECT CARDS
        // =====================================================

        for (
                int i = 0;
                i < subjects.length();
                i++
        ) {

            try {

                JSONObject subject =
                        subjects.getJSONObject(
                                i
                        );


                String subjectCode =
                        subject.optString(
                                "subject_code",
                                ""
                        );


                String subjectName =
                        subject.optString(
                                "subject_name",
                                "Unknown Subject"
                        );


                int semester =
                        subject.optInt(
                                "semester",
                                0
                        );


                int attended =
                        subject.optInt(
                                "attended_classes",
                                0
                        );


                int total =
                        subject.optInt(
                                "total_classes",
                                0
                        );


                double percentage =
                        subject.optDouble(
                                "percentage",
                                0
                        );

                String attendanceStatus =
                        subject.optString(
                                "status",
                                total == 0
                                        ? "No Classes"
                                        : (attended > 0 ? "Present" : "Absent")
                        );


                addSubjectCard(
                        subjectName,
                        subjectCode,
                        semester,
                        attended,
                        total,
                        percentage,
                        attendanceStatus,
                        i
                );


            } catch (Exception e) {

                e.printStackTrace();

            }

        }

    }


    // =====================================================
    // ADD SUBJECT CARD
    // =====================================================

    private void addSubjectCard(
            String subjectName,
            String subjectCode,
            int semester,
            int attended,
            int total,
            double percentage,
            String attendanceStatus,
            int position
    ) {

        // =====================================================
        // CARD
        // =====================================================

        com.google.android.material.card.MaterialCardView card =
                new com.google.android.material.card.MaterialCardView(
                        this
                );


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        cardParams.setMargins(
                20,
                14,
                20,
                0
        );


        card.setLayoutParams(
                cardParams
        );


        card.setRadius(
                20
        );


        card.setCardElevation(
                2
        );


        card.setCardBackgroundColor(
                Color.WHITE
        );


        card.setStrokeWidth(
                1
        );


        card.setStrokeColor(
                Color.parseColor(
                        "#E2E8F0"
                )
        );


        // =====================================================
        // MAIN LAYOUT
        // =====================================================

        LinearLayout mainLayout =
                new LinearLayout(
                        this
                );


        mainLayout.setOrientation(
                LinearLayout.VERTICAL
        );


        mainLayout.setPadding(
                18,
                18,
                18,
                18
        );


        // =====================================================
        // TOP ROW
        // =====================================================

        LinearLayout topRow =
                new LinearLayout(
                        this
                );


        topRow.setOrientation(
                LinearLayout.HORIZONTAL
        );


        topRow.setGravity(
                Gravity.CENTER_VERTICAL
        );


        // =====================================================
        // SUBJECT INFO
        // =====================================================

        LinearLayout infoLayout =
                new LinearLayout(
                        this
                );


        infoLayout.setOrientation(
                LinearLayout.VERTICAL
        );


        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );


        infoLayout.setLayoutParams(
                infoParams
        );


        // =====================================================
        // SUBJECT NAME
        // =====================================================

        TextView nameText =
                new TextView(
                        this
                );


        nameText.setText(
                formatSubjectName(
                        subjectName
                )
        );


        nameText.setTextSize(
                17
        );


        nameText.setTextColor(
                Color.parseColor(
                        "#1E293B"
                )
        );


        nameText.setTypeface(
                null,
                Typeface.BOLD
        );


        // =====================================================
        // SUBJECT CODE
        // =====================================================

        TextView codeText =
                new TextView(
                        this
                );


        codeText.setText(
                subjectCode.toUpperCase()
                        + " • Semester "
                        + semester
        );


        codeText.setTextSize(
                13
        );


        codeText.setTextColor(
                Color.parseColor(
                        "#64748B"
                )
        );


        LinearLayout.LayoutParams codeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        codeParams.setMargins(
                0,
                5,
                0,
                0
        );


        codeText.setLayoutParams(
                codeParams
        );


        infoLayout.addView(
                nameText
        );


        infoLayout.addView(
                codeText
        );


        // =====================================================
        // PERCENTAGE
        // =====================================================

        TextView percentageText =
                new TextView(
                        this
                );


        if (total == 0) {

            percentageText.setText(
                    "No classes yet"
            );

        } else {

            percentageText.setText(
                    "0%"
            );

        }


        percentageText.setTextSize(
                total == 0
                        ? 14
                        : 20
        );


        percentageText.setTypeface(
                null,
                Typeface.BOLD
        );


        percentageText.setGravity(
                Gravity.CENTER
        );


        if (total == 0) {

            percentageText.setTextColor(
                    Color.parseColor(
                            "#64748B"
                    )
            );

        } else {

            percentageText.setTextColor(
                    getAttendanceColor(
                            percentage
                    )
            );

        }


        topRow.addView(
                infoLayout
        );


        topRow.addView(
                percentageText
        );


        // =====================================================
        // CLASSES TEXT
        // =====================================================

        TextView classesText =
                new TextView(
                        this
                );


        if (total == 0) {

            classesText.setText(
                    "0 / 0 classes conducted"
            );

        } else {

            String statusText =
                    attendanceStatus.equalsIgnoreCase("Absent")
                            ? "🔴 Absent"
                            : attendanceStatus.equalsIgnoreCase("Present")
                              ? "🟢 Present"
                              : attendanceStatus;

            classesText.setText(
                    attended
                            + " / "
                            + total
                            + " classes attended  •  "
                            + statusText
            );

        }


        classesText.setTextSize(
                13
        );


        classesText.setTextColor(
                Color.parseColor(
                        "#64748B"
                )
        );


        LinearLayout.LayoutParams classesParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


        classesParams.setMargins(
                0,
                12,
                0,
                0
        );


        classesText.setLayoutParams(
                classesParams
        );


        // =====================================================
        // PROGRESS BAR
        // =====================================================

        ProgressBar progressBar =
                new ProgressBar(
                        this,
                        null,
                        android.R.attr.progressBarStyleHorizontal
                );


        progressBar.setMax(
                100
        );


        progressBar.setProgress(
                0
        );


        if (total == 0) {

            progressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            Color.parseColor(
                                    "#E8EEF5"
                            )
                    )
            );

        } else {

            progressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(
                            getAttendanceColor(
                                    percentage
                            )
                    )
            );

        }


        progressBar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.parseColor(
                                "#E8EEF5"
                        )
                )
        );


        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        8
                );


        progressParams.setMargins(
                0,
                12,
                0,
                0
        );


        progressBar.setLayoutParams(
                progressParams
        );


        // =====================================================
        // WARNING
        // =====================================================

        TextView warningText =
                new TextView(
                        this
                );


        if (total > 0
                && percentage < 75) {

            warningText.setText(
                    "Attendance below required level"
            );


            warningText.setTextColor(
                    Color.parseColor(
                            "#D32F2F"
                    )
            );


            warningText.setTextSize(
                    12
            );


            warningText.setTypeface(
                    null,
                    Typeface.BOLD
            );


            LinearLayout.LayoutParams warningParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );


            warningParams.setMargins(
                    0,
                    10,
                    0,
                    0
            );


            warningText.setLayoutParams(
                    warningParams
            );


            mainLayout.addView(
                    warningText
            );

        }


        // =====================================================
        // ADD CONTENT
        // =====================================================

        mainLayout.addView(
                topRow
        );


        mainLayout.addView(
                classesText
        );


        mainLayout.addView(
                progressBar
        );


        card.addView(
                mainLayout
        );


        subjectsContainer.addView(
                card
        );


        // =====================================================
        // ANIMATION
        // =====================================================

        prepareView(
                card
        );


        animateView(
                card,
                250L
                        + (position * 120L)
        );


        // =====================================================
        // PROGRESS ANIMATION
        // =====================================================

        if (total > 0) {

            animateProgress(
                    progressBar,
                    (int) Math.round(
                            Math.max(
                                    0,
                                    Math.min(
                                            100,
                                            percentage
                                    )
                            )
                    ),
                    1000
            );

        }


        // =====================================================
        // PERCENTAGE ANIMATION
        // =====================================================

        if (total > 0) {

            animatePercentage(
                    percentageText,
                    (int) Math.round(
                            Math.max(
                                    0,
                                    Math.min(
                                            100,
                                            percentage
                                    )
                            )
                    ),
                    1000
            );

        }


        // =====================================================
        // CLICK ANIMATION
        // =====================================================

        addCardAnimation(
                card
        );

    }


    // =====================================================
    // SUBJECT NAME FORMAT
    // =====================================================

    private String formatSubjectName(
            String subjectName
    ) {

        if (subjectName == null
                || subjectName.trim().isEmpty()) {

            return "Unknown Subject";
        }


        // =====================================================
        // SPECIAL ABBREVIATIONS
        // =====================================================

        if (subjectName.equalsIgnoreCase(
                "ai"
        )) {

            return "AI";

        }


        if (subjectName.equalsIgnoreCase(
                "da"
        )) {

            return "Data Analytics";

        }


        // =====================================================
        // NORMAL TITLE CASE
        // =====================================================

        String[] words =
                subjectName
                        .trim()
                        .split("\\s+");


        StringBuilder result =
                new StringBuilder();


        for (String word : words) {

            if (word.isEmpty()) {
                continue;
            }


            if (result.length() > 0) {

                result.append(
                        " "
                );

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
    // ATTENDANCE COLOR
    // =====================================================

    private int getAttendanceColor(
            double percentage
    ) {

        if (percentage >= 85) {

            return Color.parseColor(
                    "#2E7D32"
            );

        } else if (percentage >= 75) {

            return Color.parseColor(
                    "#F9A825"
            );

        } else {

            return Color.parseColor(
                    "#D32F2F"
            );

        }

    }


    // =====================================================
    // PREPARE VIEW
    // =====================================================

    private void prepareView(
            View view
    ) {

        if (view == null) {
            return;
        }


        view.setAlpha(
                0f
        );


        view.setTranslationY(
                50f
        );


        view.setScaleX(
                0.96f
        );


        view.setScaleY(
                0.96f
        );

    }


    // =====================================================
    // VIEW ANIMATION
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
                .scaleX(1f)
                .scaleY(1f)
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
    // PROGRESS ANIMATION
    // =====================================================

    private void animateProgress(
            ProgressBar progressBar,
            int target,
            long duration
    ) {

        if (progressBar == null) {
            return;
        }


        ObjectAnimator animator =
                ObjectAnimator.ofInt(
                        progressBar,
                        "progress",
                        0,
                        target
                );


        animator.setDuration(
                duration
        );


        animator.setInterpolator(
                new DecelerateInterpolator()
        );


        animator.start();

    }


    // =====================================================
    // PERCENTAGE ANIMATION
    // =====================================================

    private void animatePercentage(
            TextView textView,
            int target,
            long duration
    ) {

        if (textView == null) {
            return;
        }


        ValueAnimator animator =
                ValueAnimator.ofInt(
                        0,
                        target
                );


        animator.setDuration(
                duration
        );


        animator.setInterpolator(
                new DecelerateInterpolator()
        );


        animator.addUpdateListener(
                animation -> {

                    int value =
                            (int)
                                    animation.getAnimatedValue();


                    textView.setText(
                            value
                                    + "%"
                    );

                }
        );


        animator.start();

    }


    // =====================================================
    // NUMBER ANIMATION
    // =====================================================

    private void animateNumber(
            TextView textView,
            int target,
            long duration
    ) {

        if (textView == null) {
            return;
        }


        ValueAnimator animator =
                ValueAnimator.ofInt(
                        0,
                        target
                );


        animator.setDuration(
                duration
        );


        animator.setInterpolator(
                new DecelerateInterpolator()
        );


        animator.addUpdateListener(
                animation -> {

                    int value =
                            (int)
                                    animation.getAnimatedValue();


                    textView.setText(
                            String.valueOf(
                                    value
                            )
                    );

                }
        );


        animator.start();

    }


    // =====================================================
    // CARD PRESS ANIMATION
    // =====================================================

    private void addCardAnimation(
            View card
    ) {

        if (card == null) {
            return;
        }


        card.setOnClickListener(
                v -> {

                    v.animate()
                            .scaleX(0.97f)
                            .scaleY(0.97f)
                            .setDuration(100)
                            .withEndAction(() -> {

                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .start();

                            })
                            .start();

                }
        );

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