package com.example.campushub.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.google.android.material.button.MaterialButton;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private MaterialButton btnReset;
    private TextView txtBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnReset = findViewById(R.id.btnReset);
        txtBack = findViewById(R.id.txtBack);

        // RESET BUTTON CLICK
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {

                String email = etEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    etEmail.setError("Please enter your college email");
                    etEmail.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Invalid email address");
                    etEmail.requestFocus();
                    return;
                }

                Toast.makeText(
                        ForgotPasswordActivity.this,
                        "Password reset link sent successfully!",
                        Toast.LENGTH_LONG
                ).show();
            });
        }

        // BACK TO LOGIN (Pops this activity off stack instead of opening duplicate LoginActivity)
        if (txtBack != null) {
            txtBack.setOnClickListener(v -> finish());
        }
    }
}