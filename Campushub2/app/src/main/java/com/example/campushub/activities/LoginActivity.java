package com.example.campushub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialCheckBox cbRememberMe;
    private TextView tvForgotPassword;
    private TextView tvSignUp;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        tvForgotPassword = findViewById(R.id.txtForgotPassword);
        tvSignUp = findViewById(R.id.txtSignup);


        // =========================
        // FORGOT PASSWORD
        // =========================

        if (tvForgotPassword != null) {

            tvForgotPassword.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                LoginActivity.this,
                                ForgotPasswordActivity.class
                        );

                startActivity(intent);
            });
        }


        // =========================
        // SIGN UP
        // =========================

        if (tvSignUp != null) {

            tvSignUp.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                LoginActivity.this,
                                SignupRoleActivity.class
                        );

                startActivity(intent);
            });
        }


        // =========================
        // LOGIN
        // =========================

        btnLogin.setOnClickListener(v -> {

            String studentId =
                    etEmail.getText()
                            .toString()
                            .trim();

            String password =
                    etPassword.getText()
                            .toString();


            // =========================
            // VALIDATION
            // =========================

            if (studentId.isEmpty()) {

                etEmail.setError(
                        "Enter your Student ID"
                );

                etEmail.requestFocus();

                return;
            }


            if (password.isEmpty()) {

                etPassword.setError(
                        "Enter your password"
                );

                etPassword.requestFocus();

                return;
            }


            // =========================
            // DISABLE LOGIN BUTTON
            // =========================

            btnLogin.setEnabled(false);


            Toast.makeText(
                    LoginActivity.this,
                    "Signing in...",
                    Toast.LENGTH_SHORT
            ).show();


            // =========================
            // SEND LOGIN REQUEST
            // =========================

            executorService.execute(() -> {

                try {

                    JSONObject json =
                            new JSONObject();

                    json.put(
                            "student_id",
                            studentId
                    );

                    json.put(
                            "password",
                            password
                    );


                    String response =
                            ApiClient.post(
                                    "api/auth/student/login/",
                                    json.toString()
                            );


                    runOnUiThread(() -> {

                        btnLogin.setEnabled(true);

                        handleLoginResponse(response);
                    });


                } catch (Exception e) {

                    runOnUiThread(() -> {

                        btnLogin.setEnabled(true);

                        Toast.makeText(
                                LoginActivity.this,
                                "Connection error: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }
            });
        });
    }


    // =====================================================
    // HANDLE LOGIN RESPONSE
    // =====================================================

    private void handleLoginResponse(
            String response
    ) {

        try {

            JSONObject json =
                    new JSONObject(response);


            // =========================
            // SUCCESS
            // =========================

            if (json.has("access")
                    && json.has("refresh")) {


                String accessToken =
                        json.getString("access");

                String refreshToken =
                        json.getString("refresh");

                String role =
                        json.optString(
                                "role",
                                ""
                        );


                // =========================
                // SAVE JWT
                // =========================

                getSharedPreferences(
                        "CampusHubPrefs",
                        MODE_PRIVATE
                )
                        .edit()
                        .putString(
                                "access_token",
                                accessToken
                        )
                        .putString(
                                "refresh_token",
                                refreshToken
                        )
                        .putString(
                                "student_id",
                                etEmail.getText()
                                        .toString()
                                        .trim()
                        )
                        .apply();


                Toast.makeText(
                        LoginActivity.this,
                        "Login successful",
                        Toast.LENGTH_SHORT
                ).show();


                // =========================
                // OPEN STUDENT DASHBOARD
                // =========================

                Intent intent =
                        new Intent(
                                LoginActivity.this,
                                StudentDashboardActivity.class
                        );

                startActivity(intent);

                finish();

                return;
            }


            // =========================
            // LOGIN ERROR
            // =========================

            String errorMessage =
                    "Invalid Student ID or Password.";


            if (json.has("non_field_errors")) {

                errorMessage =
                        json.getJSONArray(
                                "non_field_errors"
                        ).getString(0);
            }


            Toast.makeText(
                    LoginActivity.this,
                    errorMessage,
                    Toast.LENGTH_LONG
            ).show();


        } catch (Exception e) {

            Toast.makeText(
                    LoginActivity.this,
                    "Server response: "
                            + response,
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        executorService.shutdown();
    }
}