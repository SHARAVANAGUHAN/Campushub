package com.example.campushub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.provider.Settings;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.google.android.material.card.MaterialCardView;

public class SignupRoleActivity extends AppCompatActivity {

    private MaterialCardView cardStudent;
    private MaterialCardView cardLecturer;
    private MaterialCardView cardLibrarian;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup_role);

        playEntranceAnimation();

        cardStudent = findViewById(R.id.cardStudent);
        cardLecturer = findViewById(R.id.cardLecturer);
        cardLibrarian = findViewById(R.id.cardLibrarian);
        btnBack = findViewById(R.id.btnBack);


        // =====================================================
        // STUDENT
        // =====================================================

        if (cardStudent != null) {

            cardStudent.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                SignupRoleActivity.this,
                                LoginActivity.class
                        );

                startActivity(intent);
            });
        }


        // =====================================================
        // LECTURER
        // =====================================================

        if (cardLecturer != null) {

            cardLecturer.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                SignupRoleActivity.this,
                                LecturerLoginActivity.class
                        );

                startActivity(intent);
            });
        }

        if (cardLibrarian != null) {
            cardLibrarian.setOnClickListener(v ->
                    startActivity(new Intent(SignupRoleActivity.this, LibrarianLoginActivity.class)));
        }


        // =====================================================
        // BACK BUTTON
        // =====================================================

        if (btnBack != null) {

            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void playEntranceAnimation() {
        float animatorScale;
        animatorScale = Settings.Global.getFloat(
                getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        if (animatorScale == 0f) return;

        View brand = findViewById(R.id.roleBrand);
        View content = findViewById(R.id.roleContent);
        View student = findViewById(R.id.cardStudent);
        View lecturer = findViewById(R.id.cardLecturer);
        View librarian = findViewById(R.id.cardLibrarian);

        View[] views = {brand, content, student, lecturer, librarian};
        for (View view : views) {
            view.setAlpha(0f);
            view.setTranslationY(18f);
        }

        AnimatorSet entrance = new AnimatorSet();
        entrance.playTogether(
                fadeSlide(brand, 0, 420),
                fadeSlide(content, 100, 420),
                fadeSlide(student, 180, 400),
                fadeSlide(lecturer, 250, 400),
                fadeSlide(librarian, 320, 400));
        entrance.setDuration((long) (740 * animatorScale));
        entrance.setInterpolator(new DecelerateInterpolator());
        entrance.start();

        ObjectAnimator logoPop = ObjectAnimator.ofFloat(
                findViewById(R.id.imgLogo), View.SCALE_X, 0.92f, 1f);
        ObjectAnimator logoPopY = ObjectAnimator.ofFloat(
                findViewById(R.id.imgLogo), View.SCALE_Y, 0.92f, 1f);
        AnimatorSet logoEntrance = new AnimatorSet();
        logoEntrance.playTogether(logoPop, logoPopY);
        logoEntrance.setStartDelay((long) (80 * animatorScale));
        logoEntrance.setDuration((long) (440 * animatorScale));
        logoEntrance.setInterpolator(new OvershootInterpolator(1.15f));
        logoEntrance.start();
    }

    private AnimatorSet fadeSlide(View view, long delay, long duration) {
        ObjectAnimator fade = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);
        ObjectAnimator slide = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 18f, 0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(fade, slide);
        set.setStartDelay(delay);
        set.setDuration(duration);
        return set;
    }
}
