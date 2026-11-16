package com.example.campushub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
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

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(R.layout.activity_login);


        // =====================================================
        // FIND VIEWS
        // =====================================================

        etEmail =
                findViewById(R.id.etEmail);

        etPassword =
                findViewById(R.id.etPassword);

        btnLogin =
                findViewById(R.id.btnLogin);

        cbRememberMe =
                findViewById(R.id.cbRememberMe);

        tvForgotPassword =
                findViewById(R.id.txtForgotPassword);

        tvSignUp =
                findViewById(R.id.txtSignup);


        // =====================================================
        // FORGOT PASSWORD
        // =====================================================

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


        // =====================================================
        // SIGN UP
        // =====================================================

        if (tvSignUp != null) {

            tvSignUp.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                LoginActivity.this,
                                SignupActivity.class
                        );

                startActivity(intent);
            });
        }


        // =====================================================
        // LOGIN BUTTON
        // =====================================================

        btnLogin.setOnClickListener(v -> {

            String studentId =
                    etEmail.getText()
                            .toString()
                            .trim();

            String password =
                    etPassword.getText()
                            .toString();


            // =================================================
            // VALIDATE STUDENT ID
            // =================================================

            if (studentId.isEmpty()) {

                etEmail.setError(
                        "Enter your Student ID"
                );

                etEmail.requestFocus();

                return;
            }


            // =================================================
            // VALIDATE PASSWORD
            // =================================================

            if (password.isEmpty()) {

                etPassword.setError(
                        "Enter your password"
                );

                etPassword.requestFocus();

                return;
            }


            // =================================================
            // DISABLE LOGIN BUTTON
            // =================================================

            btnLogin.setEnabled(false);


            Toast.makeText(
                    LoginActivity.this,
                    "Signing in...",
                    Toast.LENGTH_SHORT
            ).show();


            // =================================================
            // LOGIN API
            // =================================================

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

                        handleLoginResponse(
                                response,
                                studentId
                        );

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
            String response,
            String studentId
    ) {

        try {

            JSONObject json =
                    new JSONObject(response);


            // =================================================
            // LOGIN SUCCESS
            // =================================================

            if (json.has("access")
                    && json.has("refresh")) {


                String accessToken =
                        json.getString("access");

                String refreshToken =
                        json.getString("refresh");


                // =================================================
                // REMEMBER ME CHECK
                // =================================================

                boolean rememberMe =
                        cbRememberMe.isChecked();


                // =================================================
                // CAMPUSHUB PREFERENCES
                // =================================================

                android.content.SharedPreferences prefs =
                        getSharedPreferences(
                                "CampusHubPrefs",
                                MODE_PRIVATE
                        );


                android.content.SharedPreferences.Editor editor =
                        prefs.edit();


                // =================================================
                // ALWAYS SAVE CURRENT SESSION
                // =================================================

                editor.putString(
                        "access_token",
                        accessToken
                );

                editor.putString(
                        "refresh_token",
                        refreshToken
                );

                editor.putString(
                        "student_id",
                        studentId
                );


                // =================================================
                // SAVE REMEMBER ME STATUS
                // =================================================

                editor.putBoolean(
                        "remember_me",
                        rememberMe
                );


                editor.apply();


                // =================================================
                // LOGIN SUCCESS
                // =================================================

                Toast.makeText(
                        LoginActivity.this,
                        "Login successful",
                        Toast.LENGTH_SHORT
                ).show();


                // =================================================
                // OPEN STUDENT DASHBOARD
                // =================================================

                Intent intent =
                        new Intent(
                                LoginActivity.this,
                                StudentDashboardActivity.class
                        );

                startActivity(intent);

                finish();

                return;
            }


            // =================================================
            // LOGIN ERROR
            // =================================================

            String errorMessage =
                    "Invalid Student ID or Password.";


            if (json.has("non_field_errors")
                    && json.getJSONArray(
                    "non_field_errors"
            ).length() > 0) {

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


    // =====================================================
    // CLEANUP
    // =====================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executorService.shutdown();
    }
}