package com.example.campushub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TimetableActivity extends AppCompatActivity {


    // =====================================================
    // VIEWS
    // =====================================================

    private LinearLayout timetableContainer;

    private TextView[] dayButtons;

    private TextView txtSelectedDay;


    // =====================================================
    // DAYS
    // =====================================================

    private final String[] days = {
            "MON",
            "TUE",
            "WED",
            "THU",
            "FRI",
            "SAT"
    };


    private final String[] fullDays = {
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday"
    };


    // =====================================================
    // TIMETABLE DATA
    // =====================================================

    private final Map<String, List<JSONObject>> timetable =
            new HashMap<>();


    // =====================================================
    // BACKGROUND THREAD
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
                R.layout.activity_timetable
        );


        // =================================================
        // FIND VIEWS
        // =================================================

        timetableContainer =
                findViewById(
                        R.id.timetableContainer
                );

        txtSelectedDay =
                findViewById(
                        R.id.txtSelectedDay
                );

        TextView btnBack =
                findViewById(
                        R.id.btnBack
                );


        // =================================================
        // BACK BUTTON
        // =================================================

        btnBack.setOnClickListener(v -> {

            v.animate()
                    .scaleX(0.90f)
                    .scaleY(0.90f)
                    .setDuration(70)
                    .withEndAction(() -> {

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(70)
                                .start();

                        finish();

                    })
                    .start();

        });


        // =================================================
        // DAY BUTTONS
        // =================================================

        setupDayButtons();


        // =================================================
        // INITIAL DAY
        // =================================================

        selectDay(0);


        // =================================================
        // LOAD REAL TIMETABLE
        // =================================================

        loadTimetable();

    }


    // =====================================================
    // SETUP DAY BUTTONS
    // =====================================================

    private void setupDayButtons() {

        dayButtons = new TextView[]{

                findViewById(
                        R.id.dayMon
                ),

                findViewById(
                        R.id.dayTue
                ),

                findViewById(
                        R.id.dayWed
                ),

                findViewById(
                        R.id.dayThu
                ),

                findViewById(
                        R.id.dayFri
                ),

                findViewById(
                        R.id.daySat
                )

        };


        for (int i = 0; i < dayButtons.length; i++) {

            final int position = i;


            dayButtons[i].setOnClickListener(v -> {

                // -----------------------------------------
                // CLICK ANIMATION
                // -----------------------------------------

                v.animate()
                        .scaleX(0.92f)
                        .scaleY(0.92f)
                        .setDuration(70)
                        .withEndAction(() -> {

                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(70)
                                    .start();

                        })
                        .start();


                // -----------------------------------------
                // SELECT DAY
                // -----------------------------------------

                selectDay(position);

            });

        }

    }


    // =====================================================
    // SELECT DAY
    // =====================================================

    private void selectDay(int selectedDay) {

        // =================================================
        // UPDATE BUTTON COLORS
        // =================================================

        for (int i = 0; i < dayButtons.length; i++) {

            if (i == selectedDay) {

                dayButtons[i].setTextColor(
                        ContextCompat.getColor(
                                this,
                                android.R.color.white
                        )
                );

                dayButtons[i].setBackgroundResource(
                        R.drawable.bg_day_selected
                );

            } else {

                dayButtons[i].setTextColor(
                        ContextCompat.getColor(
                                this,
                                R.color.blue_primary
                        )
                );

                dayButtons[i].setBackgroundResource(
                        R.drawable.bg_day_unselected
                );

            }

        }


        // =================================================
        // UPDATE SELECTED DAY TITLE
        // =================================================

        txtSelectedDay.setText(
                fullDays[selectedDay]
        );


        // =================================================
        // SHOW CLASSES
        // =================================================

        showTimetable(
                fullDays[selectedDay]
        );

    }


    // =====================================================
    // LOAD TIMETABLE FROM DJANGO
    // =====================================================

    private void loadTimetable() {

        // =================================================
        // GET ACCESS TOKEN
        // =================================================

        String accessToken =
                getSharedPreferences(
                        "CampusHubPrefs",
                        MODE_PRIVATE
                )
                        .getString(
                                "access_token",
                                null
                        );


        // =================================================
        // TOKEN CHECK
        // =================================================

        if (accessToken == null
                || accessToken.isEmpty()) {

            Toast.makeText(
                    this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_LONG
            ).show();

            return;

        }


        // =================================================
        // API REQUEST
        // =================================================

        executorService.execute(() -> {

            try {

                String response =
                        ApiClient.get(
                                "api/timetable/student/",
                                accessToken
                        );


                JSONObject json =
                        new JSONObject(
                                response
                        );


                // =================================================
                // CLEAR OLD DATA
                // =================================================

                timetable.clear();


                // =================================================
                // GET TIMETABLE ARRAY
                // =================================================

                JSONArray timetableArray =
                        json.optJSONArray(
                                "timetable"
                        );


                if (timetableArray == null) {

                    timetableArray =
                            new JSONArray();

                }


                // =================================================
                // PROCESS EACH CLASS
                // =================================================

                for (
                        int i = 0;
                        i < timetableArray.length();
                        i++
                ) {

                    JSONObject classObject =
                            timetableArray.getJSONObject(i);


                    String day =
                            classObject.optString(
                                    "day",
                                    ""
                            );


                    if (day.isEmpty()) {

                        continue;

                    }


                    // ---------------------------------------------
                    // CREATE LIST FOR DAY
                    // ---------------------------------------------

                    if (!timetable.containsKey(day)) {

                        timetable.put(
                                day,
                                new ArrayList<>()
                        );

                    }


                    timetable.get(day).add(
                            classObject
                    );

                }


                // =================================================
                // SORT EACH DAY BY PERIOD
                // =================================================

                for (
                        List<JSONObject> classes
                        : timetable.values()
                ) {

                    Collections.sort(
                            classes,
                            new Comparator<JSONObject>() {

                                @Override
                                public int compare(
                                        JSONObject first,
                                        JSONObject second
                                ) {

                                    int firstPeriod =
                                            first.optInt(
                                                    "period_number",
                                                    0
                                            );

                                    int secondPeriod =
                                            second.optInt(
                                                    "period_number",
                                                    0
                                            );

                                    return Integer.compare(
                                            firstPeriod,
                                            secondPeriod
                                    );

                                }

                            }
                    );

                }


                // =================================================
                // UPDATE UI
                // =================================================

                runOnUiThread(() -> {

                    // Refresh currently selected day
                    showTimetable(
                            txtSelectedDay.getText()
                                    .toString()
                    );

                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            TimetableActivity.this,
                            "Failed to load timetable: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });

            }

        });

    }


    // =====================================================
    // SHOW SELECTED DAY
    // =====================================================

    private void showTimetable(String day) {

        // =================================================
        // REMOVE OLD CARDS
        // =================================================

        timetableContainer.removeAllViews();


        // =================================================
        // GET CLASSES
        // =================================================

        List<JSONObject> classes =
                timetable.get(day);


        // =================================================
        // NO CLASSES
        // =================================================

        if (classes == null
                || classes.isEmpty()) {

            addNoClassesMessage();

            return;

        }


        // =================================================
        // ADD CLASS CARDS
        // =================================================

        for (
                int i = 0;
                i < classes.size();
                i++
        ) {

            JSONObject classObject =
                    classes.get(i);


            addClassCard(
                    classObject,
                    i
            );

        }

    }


    // =====================================================
    // NO CLASSES MESSAGE
    // =====================================================

    private void addNoClassesMessage() {

        TextView emptyText =
                new TextView(this);


        emptyText.setText(
                "No classes scheduled"
        );


        emptyText.setTextSize(
                15
        );


        emptyText.setTextColor(
                ContextCompat.getColor(
                        this,
                        android.R.color.darker_gray
                )
        );


        emptyText.setGravity(
                android.view.Gravity.CENTER
        );


        emptyText.setPadding(
                20,
                50,
                20,
                50
        );


        timetableContainer.addView(
                emptyText
        );

    }


    // =====================================================
    // CREATE CLASS CARD
    // =====================================================

    private void addClassCard(
            JSONObject classObject,
            int position
    ) {

        // =================================================
        // GET DATA
        // =================================================

        String startTime =
                classObject.optString(
                        "start_time",
                        ""
                );


        String endTime =
                classObject.optString(
                        "end_time",
                        ""
                );


        String subjectCode =
                classObject.optString(
                        "subject_code",
                        ""
                );


        String subjectName =
                classObject.optString(
                        "subject_name",
                        "Subject"
                );


        String teacherName =
                classObject.optString(
                        "teacher_name",
                        "Teacher"
                );


        String room =
                classObject.optString(
                        "room",
                        ""
                );


        String section =
                classObject.optString(
                        "section",
                        ""
                );


        int period =
                classObject.optInt(
                        "period_number",
                        0
                );


        // =================================================
        // FORMAT TIME
        // =================================================

        String time =
                formatTime(
                        startTime,
                        endTime
                );


        // =================================================
        // INFLATE CARD
        // =================================================

        View card =
                getLayoutInflater().inflate(
                        R.layout.item_timetable,
                        timetableContainer,
                        false
                );


        // =================================================
        // FIND CARD VIEWS
        // =================================================

        TextView txtTime =
                card.findViewById(
                        R.id.txtTime
                );


        TextView txtSubject =
                card.findViewById(
                        R.id.txtSubject
                );


        TextView txtFullName =
                card.findViewById(
                        R.id.txtFullName
                );


        View colorStrip =
                card.findViewById(
                        R.id.colorStrip
                );


        // =================================================
        // TIME
        // =================================================

        txtTime.setText(
                time
        );


        // =================================================
        // SUBJECT CODE
        // =================================================

        if (subjectCode.isEmpty()) {

            txtSubject.setText(
                    subjectName
            );

        } else {

            txtSubject.setText(
                    subjectCode
            );

        }


        // =================================================
        // SUBJECT NAME + DETAILS
        // =================================================

        StringBuilder details =
                new StringBuilder();


        details.append(
                subjectName
        );


        if (!teacherName.isEmpty()) {

            details.append(
                    "\nTeacher: "
            );

            details.append(
                    teacherName
            );

        }


        if (!room.isEmpty()) {

            details.append(
                    "\nRoom: "
            );

            details.append(
                    room
            );

        }


        if (!section.isEmpty()) {

            details.append(
                    " • Section "
            );

            details.append(
                    section.toUpperCase()
            );

        }


        if (period > 0) {

            details.append(
                    "\nPeriod "
            );

            details.append(
                    period
            );

        }


        txtFullName.setText(
                details.toString()
        );


        // =================================================
        // SUBJECT COLOR
        // =================================================

        int color =
                getSubjectColor(
                        subjectCode,
                        subjectName
                );


        colorStrip.setBackgroundColor(
                ContextCompat.getColor(
                        this,
                        color
                )
        );


        // =================================================
        // ENTRANCE ANIMATION
        // =================================================

        card.setAlpha(
                0f
        );

        card.setTranslationY(
                40f
        );


        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(
                        position * 80L
                )
                .setDuration(
                        400
                )
                .start();


        // =================================================
        // ADD CARD
        // =================================================

        timetableContainer.addView(
                card
        );

    }


    // =====================================================
    // SUBJECT COLOR
    // =====================================================

    private int getSubjectColor(
            String subjectCode,
            String subjectName
    ) {

        String value =
                (
                        subjectCode
                                + " "
                                + subjectName
                )
                        .toUpperCase();


        // =================================================
        // DATA ANALYTICS
        // =================================================

        if (value.contains("DA")
                || value.contains("DATA ANALYTICS")) {

            if (value.contains("LAB")) {

                return R.color.subject_da_lab;

            }

            return R.color.subject_da;

        }


        // =================================================
        // SOFTWARE ENGINEERING
        // =================================================

        if (value.contains("SE")
                || value.contains("SOFTWARE ENGINEERING")) {

            return R.color.subject_se;

        }


        // =================================================
        // WEB PROGRAMMING
        // =================================================

        if (value.contains("WP")
                || value.contains("WEB PROGRAMMING")) {

            if (value.contains("LAB")) {

                return R.color.subject_wp_lab;

            }

            return R.color.subject_wp;

        }


        // =================================================
        // QUANTITATIVE TECHNIQUES
        // =================================================

        if (value.contains("QT")
                || value.contains("QUANTITATIVE TECHNIQUES")) {

            return R.color.subject_qt;

        }


        // =================================================
        // CRYPTOGRAPHY
        // =================================================

        if (value.contains("CRYPTO")
                || value.contains("CRYPTOGRAPHY")) {

            return R.color.subject_crypto;

        }


        // =================================================
        // DEFAULT
        // =================================================

        return R.color.blue_primary;

    }


    // =====================================================
    // FORMAT TIME
    // =====================================================

    private String formatTime(
            String startTime,
            String endTime
    ) {

        if (startTime.isEmpty()
                && endTime.isEmpty()) {

            return "";

        }


        String start =
                convertTo12Hour(
                        startTime
                );


        String end =
                convertTo12Hour(
                        endTime
                );


        if (!start.isEmpty()
                && !end.isEmpty()) {

            return start
                    + " - "
                    + end;

        }


        if (!start.isEmpty()) {

            return start;

        }


        return end;

    }


    // =====================================================
    // CONVERT TIME
    // =====================================================

    private String convertTo12Hour(
            String time
    ) {

        try {

            if (time == null
                    || time.isEmpty()) {

                return "";

            }


            String[] parts =
                    time.split(":");


            int hour =
                    Integer.parseInt(
                            parts[0]
                    );


            int minute =
                    Integer.parseInt(
                            parts[1]
                    );


            String amPm =
                    hour >= 12
                            ? "PM"
                            : "AM";


            int displayHour =
                    hour % 12;


            if (displayHour == 0) {

                displayHour = 12;

            }


            return String.format(
                    "%02d:%02d %s",
                    displayHour,
                    minute,
                    amPm
            );


        } catch (Exception e) {

            return time;

        }

    }


    // =====================================================
    // CLEANUP
    // =====================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executorService.shutdownNow();

    }

}