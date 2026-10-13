package com.example.campushub.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.ProfileActivity;
import com.example.campushub.R;

public class StudentDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_student_dashboard);

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

        ProgressBar attendanceBar =
                findViewById(R.id.progressAttendance);

        ProgressBar cgpaBar =
                findViewById(R.id.progressCgpa);

        // =====================================================
        // NUMBERS
        // =====================================================

        TextView attendanceText =
                findViewById(R.id.txtAttendance);

        TextView cgpaText =
                findViewById(R.id.txtCgpa);

        TextView pendingAssignmentsText =
                findViewById(R.id.txtPendingAssignments);

        // =====================================================
        // CARDS
        // =====================================================

        View studentCard =
                findViewById(R.id.cardStudent);

        View statistics =
                findViewById(R.id.statisticsContainer);

        View assignments =
                findViewById(R.id.cardAssignments);

        // =====================================================
        // ASSIGNMENTS CARD
        // =====================================================

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

        // =====================================================
        // ATTENDANCE QUICK ACCESS
        // =====================================================

        View attendanceButton =
                findViewById(R.id.btnAttendance);

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
        View timetableButton = findViewById(R.id.btnTimetable);

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
        // =====================================================
// LEAVE APPLICATION
// =====================================================

        View leaveButton =
                findViewById(R.id.cardLeave);

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

        // =====================================================
        // TEMPORARY VALUES
        // =====================================================

        int attendance = 87;

        float cgpa = 8.75f;

        int pendingAssignments = 4;

        // =====================================================
        // ANIMATE NUMBERS
        // =====================================================

        animateAttendance(
                attendanceBar,
                attendanceText,
                attendance
        );

        animateCgpa(
                cgpaBar,
                cgpaText,
                cgpa
        );

        animatePendingAssignments(
                pendingAssignmentsText,
                pendingAssignments
        );

        // =====================================================
        // ENTRANCE ANIMATION
        // =====================================================

        prepareAnimation(studentCard);
        prepareAnimation(statistics);
        prepareAnimation(assignments);

        animateView(studentCard, 0);
        animateView(statistics, 150);
        animateView(assignments, 300);
    }

    // =====================================================
    // PREPARE VIEW
    // =====================================================

    private void prepareAnimation(View view) {

        if (view != null) {
            view.setAlpha(0f);
            view.setTranslationY(50f);
        }
    }

    // =====================================================
    // ENTRANCE ANIMATION
    // =====================================================

    private void animateView(
            View view,
            long delay) {

        if (view != null) {

            view.animate()
                    .alpha(1f)
                    .translationY(0f)
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
            int target) {

        ObjectAnimator progressAnimation =
                ObjectAnimator.ofInt(
                        bar,
                        "progress",
                        0,
                        target
                );

        progressAnimation.setDuration(1200);

        progressAnimation.setInterpolator(
                new DecelerateInterpolator()
        );

        progressAnimation.start();

        ValueAnimator numberAnimation =
                ValueAnimator.ofInt(
                        0,
                        target
                );

        numberAnimation.setDuration(1200);

        numberAnimation.setInterpolator(
                new DecelerateInterpolator()
        );

        numberAnimation.addUpdateListener(animation -> {

            int value =
                    (int) animation.getAnimatedValue();

            text.setText(
                    value + "%"
            );
        });

        numberAnimation.start();
    }

    // =====================================================
    // CGPA ANIMATION
    // =====================================================

    private void animateCgpa(
            ProgressBar bar,
            TextView text,
            float target) {

        ObjectAnimator progressAnimation =
                ObjectAnimator.ofInt(
                        bar,
                        "progress",
                        0,
                        Math.round(target * 10)
                );

        progressAnimation.setDuration(1200);

        progressAnimation.setInterpolator(
                new DecelerateInterpolator()
        );

        progressAnimation.start();

        ValueAnimator numberAnimation =
                ValueAnimator.ofFloat(
                        0f,
                        target
                );

        numberAnimation.setDuration(1200);

        numberAnimation.setInterpolator(
                new DecelerateInterpolator()
        );

        numberAnimation.addUpdateListener(animation -> {

            float value =
                    (float) animation.getAnimatedValue();

            text.setText(
                    String.format(
                            "%.2f",
                            value
                    )
            );
        });

        numberAnimation.start();
    }

    // =====================================================
    // PENDING ASSIGNMENTS
    // =====================================================

    private void animatePendingAssignments(
            TextView text,
            int target) {

        ValueAnimator animator =
                ValueAnimator.ofInt(
                        0,
                        target
                );

        animator.setDuration(600);

        animator.setInterpolator(
                new DecelerateInterpolator()
        );

        animator.addUpdateListener(animation -> {

            int value =
                    (int) animation.getAnimatedValue();

            text.setText(
                    String.format(
                            "%02d",
                            value
                    )
            );
        });

        animator.start();
    }

}
