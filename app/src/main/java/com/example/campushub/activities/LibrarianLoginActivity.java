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

public class LibrarianLoginActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextInputEditText librarianId, password;
    private android.view.View loginButton;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_librarian_login);
        librarianId = findViewById(R.id.etLibrarianId);
        password = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.btnLogin);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.tvRegister).setOnClickListener(v -> startActivity(new Intent(this, LibrarianSignupActivity.class)));
        loginButton.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        String id = librarianId.getText() == null ? "" : librarianId.getText().toString().trim();
        String pass = password.getText() == null ? "" : password.getText().toString();
        if (id.isEmpty()) { librarianId.setError("Enter your librarian ID"); return; }
        if (pass.isEmpty()) { password.setError("Enter your password"); return; }
        loginButton.setEnabled(false);
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("librarian_id", id);
                body.put("password", pass);
                String response = ApiClient.post("api/auth/librarian/login/", body.toString());
                JSONObject json = new JSONObject(response);
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    if (json.has("access")) {
                        getSharedPreferences("CampusHubPrefs", MODE_PRIVATE).edit()
                                .putString("access_token", json.optString("access"))
                                .putString("refresh_token", json.optString("refresh"))
                                .putString("librarian_id", id)
                                .putString("role", "librarian").apply();
                        startActivity(new Intent(this, LibrarianDashboardActivity.class));
                        finish();
                    } else Toast.makeText(this, json.optString("error", response), Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> { loginButton.setEnabled(true); Toast.makeText(this, "Sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show(); });
            }
        });
    }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
