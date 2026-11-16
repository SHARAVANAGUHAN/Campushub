package com.example.campushub.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.google.android.material.card.MaterialCardView;

public class StudentAttendanceActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private TextView txtTitle;
    private TextView txtSubtitle;

    private TextView txtOverallPercentage;
    private TextView txtAttendanceStatus;
    private TextView txtOverallClasses;

    private TextView txtAttended;
    private TextView txtMissed;

    private TextView txtSubject1Percentage;
    private TextView txtSubject2Percentage;
    private TextView txtSubject3Percentage;
    private TextView txtSubject4Percentage;

    private ProgressBar progressOverall;
    private ProgressBar progressSubject1;
    private ProgressBar progressSubject2;
    private ProgressBar progressSubject3;
    private ProgressBar progressSubject4;

    private MaterialCardView cardOverall;
    private MaterialCardView cardSubject1;
    private MaterialCardView cardSubject2;
    private MaterialCardView cardSubject3;
    private MaterialCardView cardSubject4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_student_attendance);

        // =====================================================
        // FIND VIEWS
        // =====================================================

        btnBack = findViewById(R.id.btnBack);

        txtTitle = findViewById(R.id.txtTitle);
        txtSubtitle = findViewById(R.id.txtSubtitle);

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

        txtSubject1Percentage =
                findViewById(R.id.txtSubject1Percentage);

        txtSubject2Percentage =
                findViewById(R.id.txtSubject2Percentage);

        txtSubject3Percentage =
                findViewById(R.id.txtSubject3Percentage);

        txtSubject4Percentage =
                findViewById(R.id.txtSubject4Percentage);


        progressOverall =
                findViewById(R.id.progressOverall);

        progressSubject1 =
                findViewById(R.id.progressSubject1);

        progressSubject2 =
                findViewById(R.id.progressSubject2);

        progressSubject3 =
                findViewById(R.id.progressSubject3);

        progressSubject4 =
                findViewById(R.id.progressSubject4);


        cardOverall =
                findViewById(R.id.cardOverall);

        cardSubject1 =
                findViewById(R.id.cardSubject1);

        cardSubject2 =
                findViewById(R.id.cardSubject2);

        cardSubject3 =
                findViewById(R.id.cardSubject3);

        cardSubject4 =
                findViewById(R.id.cardSubject4);


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
        // INITIAL STATES
        // =====================================================

        prepareView(txtTitle);
        prepareView(txtSubtitle);

        prepareView(cardOverall);

        prepareView(cardSubject1);
        prepareView(cardSubject2);
        prepareView(cardSubject3);
        prepareView(cardSubject4);


        // =====================================================
        // HEADER ANIMATION
        // =====================================================

        animateView(txtTitle, 100);
        animateView(txtSubtitle, 180);


        // =====================================================
        // OVERALL CARD
        // =====================================================

        animateView(cardOverall, 250);


        // =====================================================
        // OVERALL ATTENDANCE
        // =====================================================

        txtOverallPercentage.setText("0%");
        txtAttended.setText("0");
        txtMissed.setText("0");

        progressOverall.setProgress(0);

        animatePercentage(
                txtOverallPercentage,
                87,
                1200
        );

        animateProgress(
                progressOverall,
                87,
                1200
        );

        animateNumber(
                txtAttended,
                87,
                1000
        );

        animateNumber(
                txtMissed,
                13,
                1000
        );


        // =====================================================
        // SUBJECT CARDS
        // =====================================================

        progressSubject1.setProgress(0);
        progressSubject2.setProgress(0);
        progressSubject3.setProgress(0);
        progressSubject4.setProgress(0);

        txtSubject1Percentage.setText("0%");
        txtSubject2Percentage.setText("0%");
        txtSubject3Percentage.setText("0%");
        txtSubject4Percentage.setText("0%");


        // =====================================================
        // SUBJECT 1
        // =====================================================

        animateView(cardSubject1, 400);

        animateProgress(
                progressSubject1,
                90,
                1200
        );

        animatePercentage(
                txtSubject1Percentage,
                90,
                1200
        );


        // =====================================================
        // SUBJECT 2
        // =====================================================

        animateView(cardSubject2, 550);

        animateProgress(
                progressSubject2,
                80,
                1200
        );

        animatePercentage(
                txtSubject2Percentage,
                80,
                1200
        );


        // =====================================================
        // SUBJECT 3
        // =====================================================

        animateView(cardSubject3, 700);

        animateProgress(
                progressSubject3,
                72,
                1200
        );

        animatePercentage(
                txtSubject3Percentage,
                72,
                1200
        );


        // =====================================================
        // SUBJECT 4
        // =====================================================

        animateView(cardSubject4, 850);

        animateProgress(
                progressSubject4,
                85,
                1200
        );

        animatePercentage(
                txtSubject4Percentage,
                85,
                1200
        );


        // =====================================================
        // CARD CLICK ANIMATIONS
        // =====================================================

        addCardAnimation(cardSubject1);
        addCardAnimation(cardSubject2);
        addCardAnimation(cardSubject3);
        addCardAnimation(cardSubject4);
    }


    // =========================================================
    // PREPARE VIEW
    // =========================================================

    private void prepareView(View view) {

        if (view == null) {
            return;
        }

        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.setScaleX(0.96f);
        view.setScaleY(0.96f);
    }


    // =========================================================
    // VIEW ENTRANCE ANIMATION
    // =========================================================

    private void animateView(
            View view,
            long delay) {

        if (view == null) {
            return;
        }

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


    // =========================================================
    // PROGRESS BAR ANIMATION
    // =========================================================

    private void animateProgress(
            ProgressBar progressBar,
            int target,
            long duration) {

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

        animator.setDuration(duration);

        animator.setInterpolator(
                new DecelerateInterpolator()
        );

        animator.start();
    }


    // =========================================================
    // PERCENTAGE ANIMATION
    // =========================================================

    private void animatePercentage(
            TextView textView,
            int target,
            long duration) {

        if (textView == null) {
            return;
        }

        ValueAnimator animator =
                ValueAnimator.ofInt(
                        0,
                        target
                );

        animator.setDuration(duration);

        animator.setInterpolator(
                new DecelerateInterpolator()
        );

        animator.addUpdateListener(animation -> {

            int value =
                    (int) animation.getAnimatedValue();

            textView.setText(
                    value + "%"
            );
        });

        animator.start();
    }


    // =========================================================
    // NUMBER ANIMATION
    // =========================================================

    private void animateNumber(
            TextView textView,
            int target,
            long duration) {

        if (textView == null) {
            return;
        }

        ValueAnimator animator =
                ValueAnimator.ofInt(
                        0,
                        target
                );

        animator.setDuration(duration);

        animator.setInterpolator(
                new DecelerateInterpolator()
        );

        animator.addUpdateListener(animation -> {

            int value =
                    (int) animation.getAnimatedValue();

            textView.setText(
                    String.valueOf(value)
            );
        });

        animator.start();
    }


    // =========================================================
    // CARD PRESS ANIMATION
    // =========================================================

    private void addCardAnimation(
            MaterialCardView card) {

        if (card == null) {
            return;
        }

        card.setOnClickListener(v -> {

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
        });
    }
}