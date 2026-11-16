package com.example.campushub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.google.android.material.card.MaterialCardView;

public class SignupRoleActivity extends AppCompatActivity {

    private MaterialCardView cardStudent;
    private MaterialCardView cardLecturer;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_role);

        cardStudent = findViewById(R.id.cardStudent);
        cardLecturer = findViewById(R.id.cardLecturer);
        btnBack = findViewById(R.id.btnBack);

        // OPTION 1: Student Sign Up
        if (cardStudent != null) {
            cardStudent.setOnClickListener(v -> {
                Intent intent = new Intent(SignupRoleActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }

        // OPTION 2: Lecturer Sign Up
        if (cardLecturer != null) {
            cardLecturer.setOnClickListener(v -> {
                Intent intent = new Intent(SignupRoleActivity.this, LecturerSignupActivity.class);
                startActivity(intent);
            });
        }

        // BACK BUTTON
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}