package com.example.campushub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.google.android.material.button.MaterialButton;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibrarianProfileActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView name, id, fullName, email;
    private String token;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_librarian_profile);
        name = findViewById(R.id.tvProfileName);
        id = findViewById(R.id.tvProfileId);
        fullName = findViewById(R.id.tvProfileFullName);
        email = findViewById(R.id.tvProfileEmail);
        token = getSharedPreferences("CampusHubPrefs", MODE_PRIVATE).getString("access_token", "");
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        ((MaterialButton) findViewById(R.id.btnLogout)).setOnClickListener(v -> confirmLogout());
        loadProfile();
    }

    private void loadProfile() {
        executor.execute(() -> {
            try {
                JSONObject profile = new JSONObject(ApiClient.get("api/auth/librarian/profile/", token));
                runOnUiThread(() -> {
                    String librarianName = profile.optString("full_name", "Librarian");
                    String librarianId = profile.optString("librarian_id", "");
                    name.setText(librarianName);
                    id.setText(librarianId);
                    fullName.setText(librarianName);
                    email.setText(profile.optString("email", "Not available"));
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Couldn’t load profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log out of CampusHub?")
                .setMessage("You’ll need to sign in again to manage the library.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out", (dialog, which) -> {
                    getSharedPreferences("CampusHubPrefs", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(this, SignupRoleActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }).show();
    }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
