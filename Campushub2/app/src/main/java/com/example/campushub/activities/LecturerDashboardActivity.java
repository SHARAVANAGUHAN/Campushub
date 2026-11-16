package com.example.campushub.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.activities.LecturerProfileActivity;
import com.example.campushub.R;

public class LecturerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lecturer_dashboard);


        // =====================================================
        // PROFILE BUTTON
        // =====================================================

        ImageView profileButton =
                findViewById(R.id.imgProfile);

        profileButton.setOnClickListener(v -> {

            // Instant tap feedback
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


        // =====================================================
        // TEXT / STATISTICS
        // =====================================================

        TextView todayClasses =
                findViewById(R.id.txtTodayClasses);

        TextView pendingWork =
                findViewById(R.id.txtPendingWork);


        // Temporary values

        int classesToday = 3;
        int pending = 5;


        // Animate statistics

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
        // PREPARE ENTRANCE ANIMATIONS
        // =====================================================

        prepareAnimation(classesCard);
        prepareAnimation(statistics);
        prepareAnimation(announcements);
        prepareAnimation(quickActions);


        // =====================================================
        // START ENTRANCE ANIMATIONS
        // =====================================================

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


        // Temporary click animations

        setupButtonAnimation(
                takeAttendance
        );

        setupButtonAnimation(
                addAssignment
        );

        setupButtonAnimation(
                announcement
        );
    }


    // =====================================================
    // PREPARE ENTRANCE ANIMATION
    // =====================================================

    private void prepareAnimation(View view) {

        view.setAlpha(0f);

        view.setTranslationY(45f);
    }


    // =====================================================
    // ENTRANCE ANIMATION
    // =====================================================

    private void animateView(
            View view,
            long delay) {

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


    // =====================================================
    // NUMBER ANIMATION
    // =====================================================

    private void animateNumber(
            TextView text,
            int target) {

        ValueAnimator animator =
                ValueAnimator.ofInt(
                        0,
                        target
                );

        animator.setDuration(900);

        animator.setInterpolator(
                new DecelerateInterpolator()
        );

        animator.addUpdateListener(animation -> {

            int value =
                    (int) animation.getAnimatedValue();

            text.setText(
                    String.valueOf(value)
            );
        });

        animator.start();
    }


    // =====================================================
    // BUTTON TAP ANIMATION
    // =====================================================

    private void setupButtonAnimation(
            View button) {

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
                    })
                    .start();
        });
    }
}