package com.example.campushub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.campushub.R;

import java.util.HashMap;
import java.util.Map;

public class TimetableActivity extends AppCompatActivity {

    private LinearLayout timetableContainer;
    private TextView[] dayButtons;
    private TextView txtSelectedDay;

    private final String[] days = {
            "MON", "TUE", "WED", "THU", "FRI", "SAT"
    };

    // Temporary timetable data
    // Later this will come from the database.
    private final Map<String, String[][]> timetable = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timetable);

        timetableContainer = findViewById(R.id.timetableContainer);
        txtSelectedDay = findViewById(R.id.txtSelectedDay);

        TextView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        setupTimetableData();
        setupDayButtons();

        // Show Monday when page opens
        selectDay(0);
    }

    // =====================================================
    // TEMPORARY TIMETABLE DATA
    // =====================================================

    private void setupTimetableData() {

        timetable.put("MON", new String[][]{
                {"09:00 AM", "DA", "Data Analytics", "NORMAL"},
                {"10:00 AM", "SE", "Software Engineering", "NORMAL"},
                {"11:00 AM", "WP", "Web Programming", "NORMAL"},
                {"12:00 PM", "DA LAB", "Data Analytics Lab", "LAB"}
        });

        timetable.put("TUE", new String[][]{
                {"09:00 AM", "WP", "Web Programming", "NORMAL"},
                {"10:00 AM", "DA", "Data Analytics", "NORMAL"},
                {"11:00 AM", "CRYPTO", "Cryptography", "NORMAL"},
                {"12:00 PM", "SE", "Software Engineering", "NORMAL"},
                {"01:00 PM", "QT", "Quantitative Techniques", "NORMAL"}
        });

        timetable.put("WED", new String[][]{
                {"09:00 AM", "DA LAB", "Data Analytics Lab", "LAB"},
                {"10:00 AM", "DA LAB", "Data Analytics Lab", "LAB"},
                {"12:00 PM", "WP", "Web Programming", "NORMAL"},
                {"01:00 PM", "SE", "Software Engineering", "NORMAL"}
        });

        timetable.put("THU", new String[][]{
                {"09:00 AM", "QT", "Quantitative Techniques", "NORMAL"},
                {"10:00 AM", "DA", "Data Analytics", "NORMAL"},
                {"11:00 AM", "WP LAB", "Web Programming Lab", "LAB"},
                {"12:00 PM", "WP LAB", "Web Programming Lab", "LAB"}
        });

        timetable.put("FRI", new String[][]{
                {"09:00 AM", "CRYPTO", "Cryptography", "NORMAL"},
                {"10:00 AM", "DA", "Data Analytics", "NORMAL"},
                {"11:00 AM", "SE", "Software Engineering", "NORMAL"},
                {"12:00 PM", "QT", "Quantitative Techniques", "NORMAL"},
                {"01:00 PM", "WP", "Web Programming", "NORMAL"}
        });

        // Saturday = ONLY 3 hours
        timetable.put("SAT", new String[][]{
                {"09:00 AM", "DA", "Data Analytics", "NORMAL"},
                {"10:00 AM", "CRYPTO", "Cryptography", "NORMAL"},
                {"11:00 AM", "SE", "Software Engineering", "NORMAL"}
        });
    }

    // =====================================================
    // DAY BUTTONS
    // =====================================================

    private void setupDayButtons() {

        dayButtons = new TextView[]{
                findViewById(R.id.dayMon),
                findViewById(R.id.dayTue),
                findViewById(R.id.dayWed),
                findViewById(R.id.dayThu),
                findViewById(R.id.dayFri),
                findViewById(R.id.daySat)
        };

        for (int i = 0; i < dayButtons.length; i++) {

            final int position = i;

            dayButtons[i].setOnClickListener(v -> {

                // Small click animation
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

                selectDay(position);
            });
        }
    }

    // =====================================================
    // SELECT DAY
    // =====================================================

    private void selectDay(int selectedDay) {

        // Update selected/unselected appearance
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

        String selectedDayName = days[selectedDay];
        String[] fullDays = {
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday"
        };

        txtSelectedDay.setText(fullDays[selectedDay]);

        showTimetable(selectedDayName);
    }

    // =====================================================
    // SHOW SELECTED DAY
    // =====================================================

    private void showTimetable(String day) {

        // Remove the previous day's classes
        timetableContainer.removeAllViews();

        String[][] classes = timetable.get(day);

        if (classes == null) {
            return;
        }

        for (int i = 0; i < classes.length; i++) {

            String time = classes[i][0];
            String shortName = classes[i][1];
            String fullName = classes[i][2];
            String type = classes[i][3];

            addClassCard(
                    time,
                    shortName,
                    fullName,
                    type,
                    i
            );
        }
    }

    // =====================================================
    // CREATE CLASS CARD
    // =====================================================

    private void addClassCard(
            String time,
            String shortName,
            String fullName,
            String type,
            int position) {

        View card = getLayoutInflater().inflate(
                R.layout.item_timetable,
                timetableContainer,
                false
        );

        TextView txtTime = card.findViewById(R.id.txtTime);
        TextView txtSubject = card.findViewById(R.id.txtSubject);
        TextView txtFullName = card.findViewById(R.id.txtFullName);

        txtTime.setText(time);
        txtSubject.setText(shortName);
        txtFullName.setText(fullName);

        // Subject color
        int color;

        switch (shortName) {

            case "DA":
                color = R.color.subject_da;
                break;

            case "DA LAB":
                color = R.color.subject_da_lab;
                break;

            case "SE":
                color = R.color.subject_se;
                break;

            case "WP":
                color = R.color.subject_wp;
                break;

            case "WP LAB":
                color = R.color.subject_wp_lab;
                break;

            case "QT":
                color = R.color.subject_qt;
                break;

            case "CRYPTO":
                color = R.color.subject_crypto;
                break;

            default:
                color = R.color.blue_primary;
                break;
        }

        View colorStrip = card.findViewById(R.id.colorStrip);

        colorStrip.setBackgroundColor(
                ContextCompat.getColor(this, color)
        );

        // Entrance animation
        card.setAlpha(0f);
        card.setTranslationY(40f);

        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(position * 80L)
                .setDuration(400)
                .start();

        timetableContainer.addView(card);
    }
}