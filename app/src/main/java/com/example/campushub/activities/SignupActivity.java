package com.example.campushub.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignupActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etStudentId;
    private EditText etEmail;
    private EditText etDob;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private CheckBox checkTerms;

    private MaterialButton btnCreateAccount;

    private TextView txtLogin;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);


        // =========================
        // INITIALIZE VIEWS
        // =========================

        etFullName = findViewById(R.id.etFullName);
        etStudentId = findViewById(R.id.etStudentId);
        etEmail = findViewById(R.id.etEmail);
        etDob = findViewById(R.id.etDob);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        checkTerms = findViewById(R.id.checkTerms);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        txtLogin = findViewById(R.id.txtLogin);


        // =========================
        // DATE PICKER
        // =========================

        Calendar calendar = Calendar.getInstance();

        etDob.setOnClickListener(v -> {

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(
                            SignupActivity.this,
                            (view, selectedYear, selectedMonth, selectedDay) -> {

                                String dob =
                                        String.format(
                                                "%02d/%02d/%04d",
                                                selectedDay,
                                                selectedMonth + 1,
                                                selectedYear
                                        );

                                etDob.setText(dob);

                                etDob.setError(null);
                            },
                            year,
                            month,
                            day
                    );

            datePickerDialog
                    .getDatePicker()
                    .setMaxDate(System.currentTimeMillis());

            datePickerDialog.show();
        });


        // =========================
        // CREATE ACCOUNT
        // =========================

        btnCreateAccount.setOnClickListener(v ->
                createAccount()
        );


        // =========================
        // BACK TO LOGIN
        // =========================

        txtLogin.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SignupActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);

            finish();
        });
    }


    // =====================================================
    // CREATE ACCOUNT
    // =====================================================

    private void createAccount() {

        String fullName =
                etFullName.getText()
                        .toString()
                        .trim();

        String studentId =
                etStudentId.getText()
                        .toString()
                        .trim();

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String dob =
                etDob.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString();

        String confirmPassword =
                etConfirmPassword.getText()
                        .toString();


        // =========================
        // VALIDATION
        // =========================

        if (fullName.isEmpty()) {

            etFullName.setError(
                    "Please enter your full name"
            );

            etFullName.requestFocus();
            return;
        }


        if (studentId.isEmpty()) {

            etStudentId.setError(
                    "Please enter your Student ID"
            );

            etStudentId.requestFocus();
            return;
        }


        if (email.isEmpty()) {

            etEmail.setError(
                    "Please enter your college email"
            );

            etEmail.requestFocus();
            return;
        }


        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    "Please enter a valid email address"
            );

            etEmail.requestFocus();
            return;
        }


        if (dob.isEmpty()) {

            etDob.setError(
                    "Please select your date of birth"
            );

            etDob.requestFocus();
            return;
        }


        if (password.isEmpty()) {

            etPassword.setError(
                    "Please enter a password"
            );

            etPassword.requestFocus();
            return;
        }


        if (password.length() < 8) {

            etPassword.setError(
                    "Password must be at least 8 characters"
            );

            etPassword.requestFocus();
            return;
        }


        if (confirmPassword.isEmpty()) {

            etConfirmPassword.setError(
                    "Please confirm your password"
            );

            etConfirmPassword.requestFocus();
            return;
        }


        if (!password.equals(confirmPassword)) {

            etConfirmPassword.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();
            return;
        }


        if (!checkTerms.isChecked()) {

            Toast.makeText(
                    this,
                    "Please accept the Terms and Conditions",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =========================
        // CONVERT DATE
        // =========================

        String backendDob;

        try {

            String[] parts = dob.split("/");

            if (parts.length != 3) {

                etDob.setError(
                        "Invalid date"
                );

                return;
            }

            String day = parts[0];
            String month = parts[1];
            String year = parts[2];

            backendDob =
                    year + "-"
                            + month + "-"
                            + day;

        } catch (Exception e) {

            etDob.setError(
                    "Invalid date"
            );

            return;
        }


        // =========================
        // DISABLE BUTTON
        // =========================

        btnCreateAccount.setEnabled(false);

        Toast.makeText(
                this,
                "Creating account...",
                Toast.LENGTH_SHORT
        ).show();


        // =========================
        // SEND TO DJANGO
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
                        "full_name",
                        fullName
                );

                json.put(
                        "college_email",
                        email
                );

                json.put(
                        "dob",
                        backendDob
                );

                json.put(
                        "password",
                        password
                );


                String response =
                        ApiClient.post(
                                "api/auth/student/signup/",
                                json.toString()
                        );


                runOnUiThread(() -> {

                    btnCreateAccount.setEnabled(true);

                    handleSignupResponse(response);
                });


            } catch (Exception e) {

                runOnUiThread(() -> {

                    btnCreateAccount.setEnabled(true);

                    Toast.makeText(
                            SignupActivity.this,
                            "Connection error: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }


    // =====================================================
    // HANDLE DJANGO RESPONSE
    // =====================================================

    private void handleSignupResponse(
            String response
    ) {

        try {

            JSONObject json =
                    new JSONObject(response);


            // =========================
            // SUCCESS
            // =========================

            if (json.has("message")) {

                Toast.makeText(
                        this,
                        json.getString("message"),
                        Toast.LENGTH_LONG
                ).show();


                Intent intent =
                        new Intent(
                                SignupActivity.this,
                                LoginActivity.class
                        );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);

                finish();

                return;
            }


            // =========================
            // ERROR
            // =========================

            StringBuilder errorMessage =
                    new StringBuilder();


            if (json.has("student_id")) {

                errorMessage.append(
                        json.getString("student_id")
                );
            }

            if (json.has("full_name")) {

                if (errorMessage.length() > 0) {
                    errorMessage.append("\n");
                }

                errorMessage.append(
                        json.getString("full_name")
                );
            }

            if (json.has("college_email")) {

                if (errorMessage.length() > 0) {
                    errorMessage.append("\n");
                }

                errorMessage.append(
                        json.getString("college_email")
                );
            }

            if (json.has("dob")) {

                if (errorMessage.length() > 0) {
                    errorMessage.append("\n");
                }

                errorMessage.append(
                        json.getString("dob")
                );
            }


            if (errorMessage.length() == 0) {

                errorMessage.append(
                        "Signup failed."
                );
            }


            Toast.makeText(
                    this,
                    errorMessage.toString(),
                    Toast.LENGTH_LONG
            ).show();


        } catch (Exception e) {

            Toast.makeText(
                    this,
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