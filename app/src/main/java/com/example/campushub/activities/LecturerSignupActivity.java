package com.example.campushub.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class LecturerSignupActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etLecturerId;
    private EditText etInvitationCode;
    private EditText etEmail;
    private EditText etSecretPin;
    private EditText etDob;
    private EditText etDepartment;
    private EditText etDesignation;
    private EditText etSubjects;
    private EditText etSections;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private CheckBox checkTerms;

    private MaterialButton btnCreateAccount;

    private TextView txtLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lecturer_signup);


        // =========================
        // INITIALIZE VIEWS
        // =========================

        etFullName = findViewById(R.id.etFullName);
        etLecturerId = findViewById(R.id.etLecturerId);
        etInvitationCode = findViewById(R.id.etInvitationCode);
        etEmail = findViewById(R.id.etEmail);
        etSecretPin = findViewById(R.id.etSecretPin);
        etDob = findViewById(R.id.etDob);
        etDepartment = findViewById(R.id.etDepartment);
        etDesignation = findViewById(R.id.etDesignation);
        etSubjects = findViewById(R.id.etSubjects);
        etSections = findViewById(R.id.etSections);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

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
                            LecturerSignupActivity.this,
                            (view, selectedYear, selectedMonth, selectedDay) -> {

                                String dob =
                                        selectedDay + "/"
                                                + (selectedMonth + 1) + "/"
                                                + selectedYear;

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
        // CLEAR PASSWORD ERROR
        // =========================

        etPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        // =========================
        // CLEAR CONFIRM PASSWORD ERROR
        // =========================

        etConfirmPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                tilConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        // =========================
        // PASSWORD MATCH CHECK
        // =========================

        etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {

            if (!hasFocus) {

                String password =
                        etPassword.getText()
                                .toString();

                String confirmPassword =
                        etConfirmPassword.getText()
                                .toString();

                if (!confirmPassword.isEmpty()
                        && !password.equals(confirmPassword)) {

                    tilConfirmPassword.setError(
                            "Passwords do not match"
                    );
                }
            }
        });


        // =========================
        // CREATE ACCOUNT
        // =========================

        btnCreateAccount.setOnClickListener(v ->
                createLecturerAccount()
        );


        // =========================
        // BACK TO LOGIN
        // =========================

        txtLogin.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LecturerSignupActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);
            finish();
        });
    }


    // =====================================================
    // CREATE LECTURER ACCOUNT
    // =====================================================

    private void createLecturerAccount() {

        String fullName =
                etFullName.getText()
                        .toString()
                        .trim();

        String lecturerId =
                etLecturerId.getText()
                        .toString()
                        .trim();

        String invitationCode =
                etInvitationCode.getText()
                        .toString()
                        .trim();

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String secretPin =
                etSecretPin.getText()
                        .toString()
                        .trim();

        String dob =
                etDob.getText()
                        .toString()
                        .trim();

        String department =
                etDepartment.getText()
                        .toString()
                        .trim();

        String designation =
                etDesignation.getText()
                        .toString()
                        .trim();

        String subjects =
                etSubjects.getText()
                        .toString()
                        .trim();

        String sections =
                etSections.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString();

        String confirmPassword =
                etConfirmPassword.getText()
                        .toString();


        // =========================
        // FULL NAME
        // =========================

        if (fullName.isEmpty()) {

            etFullName.setError(
                    "Please enter your full name"
            );

            etFullName.requestFocus();
            return;
        }


        // =========================
        // LECTURER ID
        // =========================

        if (lecturerId.isEmpty()) {

            etLecturerId.setError(
                    "Please enter your Lecturer ID"
            );

            etLecturerId.requestFocus();
            return;
        }

        // =========================
// INVITATION CODE
// =========================

        if (invitationCode.isEmpty()) {

            etInvitationCode.setError(
                    "Please enter the invitation code"
            );

            etInvitationCode.requestFocus();
            return;
        }
        // =========================
        // EMAIL
        // =========================

        if (email.isEmpty()) {

            etEmail.setError(
                    "Please enter your college email"
            );

            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etEmail.setError(
                    "Please enter a valid email address"
            );

            etEmail.requestFocus();
            return;
        }

        // =========================
// SECRET PIN
// =========================

        if (secretPin.isEmpty()) {

            etSecretPin.setError(
                    "Please enter your Secret PIN"
            );

            etSecretPin.requestFocus();
            return;
        }
        // =========================
        // DATE OF BIRTH
        // =========================

        if (dob.isEmpty()) {

            etDob.setError(
                    "Please select your date of birth"
            );

            etDob.requestFocus();
            return;
        }


        // =========================
        // DEPARTMENT
        // =========================

        if (department.isEmpty()) {

            etDepartment.setError(
                    "Please enter your department"
            );

            etDepartment.requestFocus();
            return;
        }


        // =========================
        // DESIGNATION
        // =========================

        if (designation.isEmpty()) {

            etDesignation.setError(
                    "Please enter your designation"
            );

            etDesignation.requestFocus();
            return;
        }


        // =========================
        // SUBJECTS
        // =========================

        if (subjects.isEmpty()) {

            etSubjects.setError(
                    "Please enter at least one subject"
            );

            etSubjects.requestFocus();
            return;
        }


        // =========================
        // SECTIONS
        // =========================

        if (sections.isEmpty()) {

            etSections.setError(
                    "Please enter at least one section"
            );

            etSections.requestFocus();
            return;
        }


        // =========================
        // PASSWORD
        // =========================

        if (password.isEmpty()) {

            tilPassword.setError(
                    "Please enter a password"
            );

            etPassword.requestFocus();
            return;
        }


        if (password.length() < 8) {

            tilPassword.setError(
                    "Password must be at least 8 characters"
            );

            etPassword.requestFocus();
            return;
        }


        // =========================
        // CONFIRM PASSWORD
        // =========================

        if (confirmPassword.isEmpty()) {

            tilConfirmPassword.setError(
                    "Please confirm your password"
            );

            etConfirmPassword.requestFocus();
            return;
        }


        if (!password.equals(confirmPassword)) {

            tilConfirmPassword.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();
            return;
        }


        // =========================
        // TERMS
        // =========================

        if (!checkTerms.isChecked()) {

            Toast.makeText(
                    this,
                    "Please accept the Terms and Conditions",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =========================
// SEND TO DJANGO BACKEND
// =========================

        new Thread(() -> {

            try {

                org.json.JSONObject json =
                        new org.json.JSONObject();

                json.put("invitation_code", invitationCode);
                json.put("teacher_id", lecturerId);
                json.put("full_name", fullName);
                json.put("email", email);
                json.put("secret_pin", secretPin);
                json.put("password", password);

                String response =
                        com.example.campushub.api.ApiClient.post(
                                "api/auth/teacher/signup/",
                                json.toString()
                        );

                runOnUiThread(() -> {

                    try {

                        org.json.JSONObject responseJson =
                                new org.json.JSONObject(response);

                        if (responseJson.has("message")) {

                            Toast.makeText(
                                    LecturerSignupActivity.this,
                                    responseJson.getString("message"),
                                    Toast.LENGTH_LONG
                            ).show();

                            Intent intent =
                                    new Intent(
                                            LecturerSignupActivity.this,
                                            LoginActivity.class
                                    );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                            finish();

                        } else {

                            Toast.makeText(
                                    LecturerSignupActivity.this,
                                    response,
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    } catch (Exception e) {

                        Toast.makeText(
                                LecturerSignupActivity.this,
                                response,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                LecturerSignupActivity.this,
                                "Connection error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            }

        }).start();
    }
}