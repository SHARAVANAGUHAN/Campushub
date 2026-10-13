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

public class LecturerLoginActivity extends AppCompatActivity {

    private TextInputEditText etTeacherId;
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

        setContentView(R.layout.activity_lecturer_login);

        etTeacherId = findViewById(R.id.etTeacherId);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        tvForgotPassword = findViewById(R.id.txtForgotPassword);
        tvSignUp = findViewById(R.id.txtSignup);

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(
                        LecturerLoginActivity.this,
                        ForgotPasswordActivity.class
                );
                startActivity(intent);
            });
        }

        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                Intent intent = new Intent(
                        LecturerLoginActivity.this,
                        LecturerSignupActivity.class
                );
                startActivity(intent);
            });
        }

        btnLogin.setOnClickListener(v -> {

            String teacherId = etTeacherId.getText()
                    .toString()
                    .trim();

            String password = etPassword.getText()
                    .toString();

            if (teacherId.isEmpty()) {
                etTeacherId.setError(
                        "Enter your Lecturer / Staff ID"
                );
                etTeacherId.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Enter your password");
                etPassword.requestFocus();
                return;
            }

            btnLogin.setEnabled(false);

            Toast.makeText(
                    LecturerLoginActivity.this,
                    "Signing in...",
                    Toast.LENGTH_SHORT
            ).show();

            executorService.execute(() -> {
                try {
                    JSONObject json = new JSONObject();

                    json.put("teacher_id", teacherId);
                    json.put("password", password);

                    String response = ApiClient.post(
                            "api/auth/teacher/login/",
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
                                LecturerLoginActivity.this,
                                "Connection error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }
            });
        });
    }

    private void handleLoginResponse(String response) {

        try {

            JSONObject json = new JSONObject(response);

            if (json.has("access") && json.has("refresh")) {

                String accessToken =
                        json.getString("access");

                String refreshToken =
                        json.getString("refresh");

                String role =
                        json.optString("role", "teacher");

                // Get Remember Me state
                boolean rememberMe =
                        cbRememberMe != null &&
                                cbRememberMe.isChecked();

                /*
                 * Save lecturer login information
                 */
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
                                "teacher_id",
                                etTeacherId.getText()
                                        .toString()
                                        .trim()
                        )
                        .putString(
                                "role",
                                role
                        )
                        .putBoolean(
                                "remember_me_teacher",
                                rememberMe
                        )
                        .apply();

                Toast.makeText(
                        LecturerLoginActivity.this,
                        "Login successful",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        LecturerLoginActivity.this,
                        LecturerDashboardActivity.class
                );

                startActivity(intent);
                finish();

                return;
            }

            String errorMessage =
                    "Invalid Lecturer ID or Password.";

            if (json.has("error")) {
                errorMessage =
                        json.getString("error");
            }

            Toast.makeText(
                    LecturerLoginActivity.this,
                    errorMessage,
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    LecturerLoginActivity.this,
                    "Server response: " + response,
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