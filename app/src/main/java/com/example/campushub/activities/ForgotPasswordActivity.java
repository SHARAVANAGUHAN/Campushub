package com.example.campushub.activities;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Step 1
    private EditText etStudentId;
    private EditText etEmail;
    private MaterialButton btnSendOtp;

    // Step 2
    private EditText etOtp;
    private MaterialButton btnVerifyOtp;

    // Step 3
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private MaterialButton btnResetPassword;

    private TextView txtBack;
    private TextView txtStepTitle;
    private TextView txtStepDescription;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(R.layout.activity_forgot_password);

        // -----------------------------
        // FIND VIEWS
        // -----------------------------

        etStudentId = findViewById(R.id.etStudentId);
        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        txtBack = findViewById(R.id.txtBack);
        txtStepTitle = findViewById(R.id.txtStepTitle);
        txtStepDescription = findViewById(R.id.txtStepDescription);

        // -----------------------------
        // INITIAL SCREEN
        // -----------------------------

        showStep1();

        // -----------------------------
        // SEND OTP
        // -----------------------------

        btnSendOtp.setOnClickListener(v -> sendOtp());

        // -----------------------------
        // VERIFY OTP
        // -----------------------------

        btnVerifyOtp.setOnClickListener(v -> verifyOtp());

        // -----------------------------
        // RESET PASSWORD
        // -----------------------------

        btnResetPassword.setOnClickListener(v -> resetPassword());

        // -----------------------------
        // BACK
        // -----------------------------

        txtBack.setOnClickListener(v -> finish());
    }


    // =====================================================
    // STEP 1 - SEND OTP
    // =====================================================

    private void sendOtp() {

        String enteredStudentId =
                etStudentId.getText().toString().trim();

        String email =
                etEmail.getText().toString().trim();

        if (enteredStudentId.isEmpty()) {
            etStudentId.setError("Enter your Student ID");
            etStudentId.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Enter your college email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        studentId = enteredStudentId;

        btnSendOtp.setEnabled(false);

        Toast.makeText(
                this,
                "Sending OTP...",
                Toast.LENGTH_SHORT
        ).show();

        executorService.execute(() -> {

            try {

                JSONObject json = new JSONObject();

                json.put(
                        "student_id",
                        enteredStudentId
                );

                json.put(
                        "email",
                        email
                );

                String response = ApiClient.post(
                        "api/auth/password/forgot/",
                        json.toString()
                );

                runOnUiThread(() -> {

                    btnSendOtp.setEnabled(true);

                    try {

                        JSONObject result =
                                new JSONObject(response);

                        if (result.has("message")) {

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    result.getString("message"),
                                    Toast.LENGTH_LONG
                            ).show();

                            showStep2();

                        } else {

                            showError(result);
                        }

                    } catch (Exception e) {

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                "Server response: " + response,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    btnSendOtp.setEnabled(true);

                    Toast.makeText(
                            ForgotPasswordActivity.this,
                            "Connection error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    // =====================================================
    // STEP 2 - VERIFY OTP
    // =====================================================

    private void verifyOtp() {

        String otp =
                etOtp.getText().toString().trim();

        if (otp.isEmpty()) {

            etOtp.setError("Enter the OTP");
            etOtp.requestFocus();
            return;
        }

        if (otp.length() != 6) {

            etOtp.setError("OTP must contain 6 digits");
            etOtp.requestFocus();
            return;
        }

        btnVerifyOtp.setEnabled(false);

        Toast.makeText(
                this,
                "Verifying OTP...",
                Toast.LENGTH_SHORT
        ).show();

        executorService.execute(() -> {

            try {

                JSONObject json = new JSONObject();

                json.put(
                        "student_id",
                        studentId
                );

                json.put(
                        "otp",
                        otp
                );

                String response = ApiClient.post(
                        "api/auth/password/verify-otp/",
                        json.toString()
                );

                runOnUiThread(() -> {

                    btnVerifyOtp.setEnabled(true);

                    try {

                        JSONObject result =
                                new JSONObject(response);

                        if (result.has("message")) {

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    result.getString("message"),
                                    Toast.LENGTH_SHORT
                            ).show();

                            showStep3();

                        } else {

                            showError(result);
                        }

                    } catch (Exception e) {

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                "Server response: " + response,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    btnVerifyOtp.setEnabled(true);

                    Toast.makeText(
                            ForgotPasswordActivity.this,
                            "Connection error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    // =====================================================
    // STEP 3 - RESET PASSWORD
    // =====================================================

    private void resetPassword() {

        String newPassword =
                etNewPassword.getText().toString();

        String confirmPassword =
                etConfirmPassword.getText().toString();

        if (newPassword.isEmpty()) {

            etNewPassword.setError(
                    "Enter a new password"
            );

            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {

            etNewPassword.setError(
                    "Password must be at least 6 characters"
            );

            etNewPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {

            etConfirmPassword.setError(
                    "Confirm your password"
            );

            etConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {

            etConfirmPassword.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();
            return;
        }

        btnResetPassword.setEnabled(false);

        Toast.makeText(
                this,
                "Resetting password...",
                Toast.LENGTH_SHORT
        ).show();

        executorService.execute(() -> {

            try {

                JSONObject json = new JSONObject();

                json.put(
                        "student_id",
                        studentId
                );

                json.put(
                        "new_password",
                        newPassword
                );

                String response = ApiClient.post(
                        "api/auth/password/reset/",
                        json.toString()
                );

                runOnUiThread(() -> {

                    btnResetPassword.setEnabled(true);

                    try {

                        JSONObject result =
                                new JSONObject(response);

                        if (result.has("message")) {

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    "Password reset successfully!",
                                    Toast.LENGTH_LONG
                            ).show();

                            // Return to login
                            finish();

                        } else {

                            showError(result);
                        }

                    } catch (Exception e) {

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                "Server response: " + response,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    btnResetPassword.setEnabled(true);

                    Toast.makeText(
                            ForgotPasswordActivity.this,
                            "Connection error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    // =====================================================
    // STEP 1 UI
    // =====================================================

    private void showStep1() {

        findViewById(R.id.layoutStep1)
                .setVisibility(android.view.View.VISIBLE);

        findViewById(R.id.layoutStep2)
                .setVisibility(android.view.View.GONE);

        findViewById(R.id.layoutStep3)
                .setVisibility(android.view.View.GONE);

        txtStepTitle.setText(
                "Forgot Password?"
        );

        txtStepDescription.setText(
                "Enter your Student ID and registered college email to receive an OTP."
        );
    }


    // =====================================================
    // STEP 2 UI
    // =====================================================

    private void showStep2() {

        findViewById(R.id.layoutStep1)
                .setVisibility(android.view.View.GONE);

        findViewById(R.id.layoutStep2)
                .setVisibility(android.view.View.VISIBLE);

        findViewById(R.id.layoutStep3)
                .setVisibility(android.view.View.GONE);

        txtStepTitle.setText(
                "Verify OTP"
        );

        txtStepDescription.setText(
                "Enter the 6-digit OTP sent to your registered email."
        );

        etOtp.requestFocus();
    }


    // =====================================================
    // STEP 3 UI
    // =====================================================

    private void showStep3() {

        findViewById(R.id.layoutStep1)
                .setVisibility(android.view.View.GONE);

        findViewById(R.id.layoutStep2)
                .setVisibility(android.view.View.GONE);

        findViewById(R.id.layoutStep3)
                .setVisibility(android.view.View.VISIBLE);

        txtStepTitle.setText(
                "Create New Password"
        );

        txtStepDescription.setText(
                "Enter a new password for your CampusHub account."
        );

        etNewPassword.requestFocus();
    }


    // =====================================================
    // ERROR HANDLING
    // =====================================================

    private void showError(JSONObject result) {

        String message = "Something went wrong.";

        try {

            if (result.has("error")) {

                message = result.getString("error");

            } else if (result.has("detail")) {

                message = result.getString("detail");
            }

        } catch (Exception ignored) {
        }

        Toast.makeText(
                ForgotPasswordActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        executorService.shutdown();
    }
}