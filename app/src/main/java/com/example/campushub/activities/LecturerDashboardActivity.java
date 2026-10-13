package com.example.campushub.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;

public class LecturerDashboardActivity extends AppCompatActivity {

    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_lecturer_dashboard
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
                                    LecturerDashboardActivity.this,
                                    "Press back again to exit",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );


        // =====================================================
        // PROFILE
        // =====================================================

        ImageView profileButton =
                findViewById(R.id.imgProfile);

        if (profileButton != null) {

            profileButton.setOnClickListener(v -> {

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

                            Intent intent =
                                    new Intent(
                                            LecturerDashboardActivity.this,
                                            LecturerProfileActivity.class
                                    );

                            startActivity(intent);

                        })
                        .start();

            });
        }


        // =====================================================
        // STATISTICS
        // =====================================================

        TextView todayClasses =
                findViewById(R.id.txtTodayClasses);

        TextView pendingWork =
                findViewById(R.id.txtPendingWork);


        int classesToday = 3;

        int pending = 5;


        animateNumber(
                todayClasses,
                classesToday
        );

        animateNumber(
                pendingWork,
                pending
        );


        // =====================================================
        // MAIN SECTIONS
        // =====================================================

        View classesCard =
                findViewById(R.id.cardClasses);

        View statistics =
                findViewById(R.id.statisticsContainer);

        View announcements =
                findViewById(R.id.cardAnnouncements);

        View quickActions =
                findViewById(R.id.quickActionsContainer);


        // =====================================================
        // ENTRANCE ANIMATIONS
        // =====================================================

        prepareAnimation(classesCard);
        prepareAnimation(statistics);
        prepareAnimation(announcements);
        prepareAnimation(quickActions);

        animateView(
                classesCard,
                100
        );

        animateView(
                statistics,
                220
        );

        animateView(
                announcements,
                340
        );

        animateView(
                quickActions,
                460
        );


        // =====================================================
        // QUICK ACTION BUTTONS
        // =====================================================

        View takeAttendance =
                findViewById(R.id.btnTakeAttendance);

        View addAssignment =
                findViewById(R.id.btnAddAssignment);

        View announcement =
                findViewById(R.id.btnAnnouncement);

        View viewSubmissions =
                findViewById(R.id.btnViewSubmissions);

        View leaveRequests =
                findViewById(R.id.btnLeaveRequests);


        // =====================================================
        // EXISTING BUTTONS
        // =====================================================

        setupButtonAnimation(
                takeAttendance
        );

        setupButtonAnimation(
                addAssignment
        );

        setupButtonAnimation(
                announcement
        );

        setupButtonAnimation(
                viewSubmissions
        );


        // =====================================================
        // LEAVE REQUESTS
        // =====================================================

        setupButtonAnimation(
                leaveRequests
        );

    }


    // =====================================================
    // PREPARE ENTRANCE ANIMATION
    // =====================================================

    private void prepareAnimation(
            View view
    ) {

        if (view != null) {

            view.setAlpha(0f);

            view.setTranslationY(45f);
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
                    .setStartDelay(delay)
                    .setDuration(500)
                    .setInterpolator(
                            new DecelerateInterpolator()
                    )
                    .start();
        }
    }


    // =====================================================
    // NUMBER ANIMATION
    // =====================================================

    private void animateNumber(
            TextView text,
            int target
    ) {

        if (text == null) {
            return;
        }

        ValueAnimator animator =
                ValueAnimator.ofInt(
                        0,
                        target
                );

        animator.setDuration(
                900
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
                            String.valueOf(
                                    value
                            )
                    );
                }
        );

        animator.start();
    }


    // =====================================================
    // BUTTON TAP ANIMATION + NAVIGATION
    // =====================================================

    private void setupButtonAnimation(
            View button
    ) {

        if (button == null) {
            return;
        }

        button.setOnClickListener(v -> {

            v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(60)
                    .withEndAction(() -> {

                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(60)
                                .start();


                        // =================================================
                        // TAKE ATTENDANCE
                        // =================================================

                        if (
                                v.getId()
                                        == R.id.btnTakeAttendance
                        ) {

                            Intent intent =
                                    new Intent(
                                            LecturerDashboardActivity.this,
                                            AttendanceActivity.class
                                    );

                            startActivity(intent);
                        }


                        // =================================================
                        // ADD ASSIGNMENT
                        // =================================================

                        else if (
                                v.getId()
                                        == R.id.btnAddAssignment
                        ) {

                            Intent intent =
                                    new Intent(
                                            LecturerDashboardActivity.this,
                                            AddAssignmentActivity.class
                                    );

                            startActivity(intent);
                        }


                        // =================================================
                        // POST ANNOUNCEMENT
                        // =================================================

                        else if (
                                v.getId()
                                        == R.id.btnAnnouncement
                        ) {

                            Intent intent =
                                    new Intent(
                                            LecturerDashboardActivity.this,
                                            TeacherAnnouncementsActivity.class
                                    );

                            startActivity(intent);
                        }


                        // =================================================
                        // VIEW ASSIGNMENT SUBMISSIONS
                        // =================================================

                        else if (
                                v.getId()
                                        == R.id.btnViewSubmissions
                        ) {

                            Intent intent =
                                    new Intent(
                                            LecturerDashboardActivity.this,
                                            TeacherAssignmentsActivity.class
                                    );

                            startActivity(intent);
                        }


                        // =================================================
                        // LEAVE REQUESTS
                        // =================================================

                        else if (
                                v.getId()
                                        == R.id.btnLeaveRequests
                        ) {

                            Intent intent =
                                    new Intent(
                                            LecturerDashboardActivity.this,
                                            TeacherLeaveApplicationsActivity.class
                                    );

                            startActivity(intent);
                        }

                    })
                    .start();
        });
    }
}