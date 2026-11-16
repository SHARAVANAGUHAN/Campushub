package com.example.campushub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibrarianSignupActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextInputEditText librarianId, fullName, email, password, confirm;
    private android.view.View createButton;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_librarian_signup);
        librarianId = findViewById(R.id.etLibrarianId);
        fullName = findViewById(R.id.etFullName);
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        confirm = findViewById(R.id.etConfirmPassword);
        createButton = findViewById(R.id.btnCreateAccount);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        createButton.setOnClickListener(v -> register());
    }

    private String value(TextInputEditText field) { return field.getText() == null ? "" : field.getText().toString().trim(); }

    private void register() {
        if (value(librarianId).isEmpty()) { librarianId.setError("Enter your librarian ID"); return; }
        if (value(fullName).isEmpty()) { fullName.setError("Enter your full name"); return; }
        if (value(email).isEmpty()) { email.setError("Enter your campus email"); return; }
        String pass = value(password);
        if (pass.length() < 8) { password.setError("Use at least 8 characters"); return; }
        if (!pass.equals(value(confirm))) { confirm.setError("Passwords do not match"); return; }

        createButton.setEnabled(false);
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("librarian_id", value(librarianId));
                body.put("full_name", value(fullName));
                body.put("email", value(email));
                body.put("password", pass);
                String response = ApiClient.post("api/auth/librarian/signup/", body.toString());
                JSONObject json = new JSONObject(response);
                runOnUiThread(() -> {
                    createButton.setEnabled(true);
                    if (json.has("access") && json.has("refresh")) {
                        getSharedPreferences("CampusHubPrefs", MODE_PRIVATE).edit()
                                .putString("access_token", json.optString("access"))
                                .putString("refresh_token", json.optString("refresh"))
                                .putString("librarian_id", value(librarianId))
                                .putString("role", "librarian").apply();
                        Toast.makeText(this, json.optString("message", "Account created"), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LibrarianDashboardActivity.class));
                        finish();
                    } else Toast.makeText(this, firstError(json), Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> { createButton.setEnabled(true); Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show(); });
            }
        });
    }

    private String firstError(JSONObject json) {
        java.util.Iterator<String> keys = json.keys();
        if (!keys.hasNext()) return json.toString();
        String key = keys.next();
        Object value = json.opt(key);
        if (value instanceof org.json.JSONArray && ((org.json.JSONArray) value).length() > 0) return ((org.json.JSONArray) value).optString(0);
        return String.valueOf(value);
    }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
