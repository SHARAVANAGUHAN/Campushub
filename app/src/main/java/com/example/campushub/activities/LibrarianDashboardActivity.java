package com.example.campushub.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibrarianDashboardActivity extends AppCompatActivity {
    private static final int REQUEST_BOOK_PDF = 62;
    private static final int REQUEST_BOOK_COVER = 63;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private DrawerLayout drawer;
    private LinearLayout booksList, requestsList;
    private TextView drawerSubtitle;
    private TextView catalogCount, availableCopiesCount, pendingCountView;
    private String token;
    private String requestStatus = "pending";
    private Uri selectedPdf;
    private Uri selectedCoverUri;
    private PendingBook pendingBook;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override public void run() {
            reloadBooks();
            refreshHandler.postDelayed(this, 30000);
        }
    };

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_librarian_dashboard);
        token = getSharedPreferences("CampusHubPrefs", MODE_PRIVATE).getString("access_token", "");
        drawer = findViewById(R.id.librarianDrawerLayout);
        booksList = findViewById(R.id.librarianBookList);
        requestsList = findViewById(R.id.librarianRequestsList);
        drawerSubtitle = findViewById(R.id.tvRequestDrawerSubtitle);
        catalogCount = findViewById(R.id.tvCatalogCount);
        availableCopiesCount = findViewById(R.id.tvAvailableCopies);
        pendingCountView = findViewById(R.id.tvPendingCount);
        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, LibrarianProfileActivity.class)));
        findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            ((MaterialButtonToggleGroup) findViewById(R.id.requestDrawerTabs)).check(
                    "pending".equals(requestStatus) ? R.id.tabPendingRequests : R.id.tabAllottedBooks);
            drawer.openDrawer(Gravity.END);
            loadRequests();
        });
        findViewById(R.id.btnCloseNotificationDrawer).setOnClickListener(v -> drawer.closeDrawer(Gravity.END));
        findViewById(R.id.btnAddCatalogBook).setOnClickListener(v -> showAddBookDialog());
        MaterialButtonToggleGroup tabs = findViewById(R.id.requestDrawerTabs);
        tabs.check(R.id.tabPendingRequests);
        tabs.addOnButtonCheckedListener((group, id, checked) -> {
            if (checked) {
                requestStatus = id == R.id.tabAllottedBooks ? "allotted" : "pending";
                drawerSubtitle.setText("allotted".equals(requestStatus) ? "Books currently out on loan" : "Review incoming book requests");
                loadRequests();
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (booksList != null) {
            reloadBooks();
            refreshHandler.removeCallbacks(refreshRunnable);
            refreshHandler.postDelayed(refreshRunnable, 30000);
        }
    }

    @Override protected void onPause() {
        refreshHandler.removeCallbacks(refreshRunnable);
        super.onPause();
    }

    private void reloadBooks() {
        executor.execute(() -> {
            try {
                JSONArray rows = new JSONArray(ApiClient.get("api/library/books/", token));
                runOnUiThread(() -> renderBooks(rows));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Couldn’t load book collection: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void loadRequests() {
        executor.execute(() -> {
            try {
                JSONArray rows = new JSONArray(ApiClient.get("api/library/requests/?status=" + requestStatus, token));
                runOnUiThread(() -> renderRequests(rows));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Couldn’t load requests: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void renderBooks(JSONArray rows) {
        booksList.removeAllViews();
        int pendingCount = 0;
        int availableCopies = 0;
        for (int i = 0; i < rows.length(); i++) {
            JSONObject book = rows.optJSONObject(i);
            if (book == null) continue;
            pendingCount += book.optInt("pending_requests", 0);
            availableCopies += book.optInt("available_copies", 0);
            MaterialCardView card = card();
            int[] strokes = {0xFFBFDBFE, 0xFFB7E6DA, 0xFFF7D8A6, 0xFFDCCFF8};
            card.setStrokeColor(strokes[i % strokes.length]);
            LinearLayout body = body();
            LinearLayout heading = new LinearLayout(this);
            heading.setGravity(Gravity.CENTER_VERTICAL);
            heading.setOrientation(LinearLayout.HORIZONTAL);
            TextView icon = new TextView(this);
            icon.setText("▤");
            icon.setTextSize(22);
            icon.setTextColor(new int[]{0xFF1D4ED8, 0xFF0F766E, 0xFFB45309, 0xFF7C3AED}[i % 4]);
            MaterialCardView iconTile = new MaterialCardView(this);
            iconTile.setRadius(dp(12));
            iconTile.setCardElevation(0);
            iconTile.setCardBackgroundColor(new int[]{0xFFE8F1FF, 0xFFE2F5F1, 0xFFFFF3DE, 0xFFF2EDFF}[i % 4]);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(42), dp(42));
            iconTile.addView(icon, new android.widget.FrameLayout.LayoutParams(-1, -1));
            icon.setGravity(Gravity.CENTER);
            heading.addView(iconTile, iconParams);
            TextView bookTitle = text(book.optString("title"), 17, "#172B4D", true);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -2, 1);
            titleParams.leftMargin = dp(12);
            heading.addView(bookTitle, titleParams);
            body.addView(heading);
            String author = book.optString("author", "");
            if (!author.isEmpty()) {
                TextView authorView = text("by " + author, 13, "#64748B", false);
                LinearLayout.LayoutParams authorParams = (LinearLayout.LayoutParams) authorView.getLayoutParams();
                authorParams.leftMargin = dp(54);
                body.addView(authorView);
            }
            String availability = book.optInt("available_copies") + " of " + book.optInt("total_copies") + " copies free";
            TextView metadata = text(availability + (book.optBoolean("pdf_available") ? "  ·  Digital copy" : "  ·  Desk loan"), 12, "#0F766E", true);
            LinearLayout.LayoutParams metaParams = (LinearLayout.LayoutParams) metadata.getLayoutParams();
            metaParams.leftMargin = dp(54);
            body.addView(metadata);
            card.addView(body);
            booksList.addView(card);
        }
        if (rows.length() == 0) {
            TextView empty = text("No books in the shared catalog. Add the first book to get started.", 15, "#64748B", false);
            empty.setPadding(dp(20), dp(26), dp(20), dp(20));
            booksList.addView(empty);
        }
        MaterialButton notifications = findViewById(R.id.btnNotifications);
        notifications.setText(pendingCount > 0 ? "Requests (" + pendingCount + ")" : "Requests");
        catalogCount.setText(String.valueOf(rows.length()));
        availableCopiesCount.setText(String.valueOf(availableCopies));
        pendingCountView.setText(String.valueOf(pendingCount));
        loadRequests();
    }

    private void renderRequests(JSONArray rows) {
        requestsList.removeAllViews();
        if (rows.length() == 0) {
            TextView empty = text("pending".equals(requestStatus) ? "No new requests." : "No books currently allotted.", 14, "#64748B", false);
            empty.setPadding(dp(12), dp(20), dp(12), dp(20));
            requestsList.addView(empty);
            return;
        }
        for (int i = 0; i < rows.length(); i++) {
            JSONObject request = rows.optJSONObject(i);
            if (request == null) continue;
            int requestId = request.optInt("id");
            MaterialCardView card = card();
            LinearLayout body = body();
            body.addView(text(request.optString("book_title", "Book"), 16, "#172B4D", true));
            body.addView(text(request.optString("student_name") + " · " + request.optString("student_id"), 13, "#334155", true));
            String info = request.optInt("requested_days") + " day loan requested";
            if (request.has("due_date") && !request.isNull("due_date")) info += " · due " + request.optString("due_date");
            body.addView(text(info, 13, "#64748B", false));
            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            if ("pending".equals(requestStatus)) {
                MaterialButton allot = button("Allot");
                MaterialButton reject = button("Decline");
                allot.setOnClickListener(v -> reviewRequest(requestId, "allot"));
                reject.setOnClickListener(v -> reviewRequest(requestId, "reject"));
                actions.addView(allot, new LinearLayout.LayoutParams(0, dp(44), 1));
                LinearLayout.LayoutParams rejectParams = new LinearLayout.LayoutParams(0, dp(44), 1);
                rejectParams.leftMargin = dp(8);
                actions.addView(reject, rejectParams);
            } else {
                MaterialButton returned = button("Mark returned");
                returned.setOnClickListener(v -> returnBook(requestId));
                actions.addView(returned, new LinearLayout.LayoutParams(-1, dp(44)));
            }
            LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(-1, -2);
            actionParams.topMargin = dp(10);
            body.addView(actions, actionParams);
            card.addView(body);
            requestsList.addView(card);
        }
    }

    private void reviewRequest(int id, String action) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject(); body.put("action", action);
                String raw = ApiClient.patchAuthenticated("api/library/requests/" + id + "/review/", body.toString(), token);
                JSONObject response = new JSONObject(raw);
                if (response.has("error")) throw new Exception(response.optString("error"));
                runOnUiThread(() -> {
                    Toast.makeText(this, "allot".equals(action) ? "Book allotted for the requested period" : "Request declined", Toast.LENGTH_LONG).show();
                    loadRequests(); reloadBooks();
                });
            } catch (Exception e) { runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()); }
        });
    }

    private void returnBook(int id) {
        executor.execute(() -> {
            try {
                String raw = ApiClient.postAuthenticated("api/library/requests/" + id + "/return/", "{}", token);
                JSONObject response = new JSONObject(raw);
                if (response.has("error")) throw new Exception(response.optString("error"));
                runOnUiThread(() -> { Toast.makeText(this, "Book marked as returned", Toast.LENGTH_SHORT).show(); loadRequests(); reloadBooks(); });
            } catch (Exception e) { runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()); }
        });
    }

    private void showAddBookDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setPadding(dp(22), dp(8), dp(22), 0);
        form.setOrientation(LinearLayout.VERTICAL);
        EditText title = input("Book title");
        EditText author = input("Author (optional)");
        EditText copies = input("Number of physical copies");
        copies.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        EditText description = input("Description (optional)");
        description.setMinLines(2);
        form.addView(title); form.addView(author); form.addView(copies); form.addView(description);
        MaterialButton chooseCover = button("Choose cover image (optional)");
        chooseCover.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("image/*");
            pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(pick, REQUEST_BOOK_COVER);
        });
        form.addView(chooseCover);
        new AlertDialog.Builder(this).setTitle("Add to catalog").setView(form)
                .setNegativeButton("Cancel", (dialog, which) -> selectedCoverUri = null)
                .setNeutralButton("Choose PDF", (dialog, which) -> {
                    if (title.getText().toString().trim().isEmpty()) { Toast.makeText(this, "Enter a book title first", Toast.LENGTH_SHORT).show(); return; }
                    int copyCount = parseCopies(copies.getText().toString());
                    if (copyCount < 1) { Toast.makeText(this, "Enter at least one copy", Toast.LENGTH_SHORT).show(); return; }
                    pendingBook = new PendingBook(title.getText().toString().trim(), author.getText().toString().trim(), description.getText().toString().trim(), copyCount);
                    Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    pick.addCategory(Intent.CATEGORY_OPENABLE); pick.setType("application/pdf");
                    pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    startActivityForResult(pick, REQUEST_BOOK_PDF);
                })
                .setPositiveButton("Add without PDF", (dialog, which) -> {
                    int copyCount = parseCopies(copies.getText().toString());
                    if (title.getText().toString().trim().isEmpty() || copyCount < 1) { Toast.makeText(this, "Enter a title and at least one copy", Toast.LENGTH_SHORT).show(); return; }
                    createBook(new PendingBook(title.getText().toString().trim(), author.getText().toString().trim(), description.getText().toString().trim(), copyCount), null, selectedCoverUri);
                    selectedCoverUri = null;
                }).show();
    }

    private int parseCopies(String value) { try { return Integer.parseInt(value.trim()); } catch (Exception e) { return 0; } }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BOOK_COVER && resultCode != RESULT_OK) {
            selectedCoverUri = null;
            return;
        }
        if (requestCode == REQUEST_BOOK_COVER && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedCoverUri = data.getData();
            try { getContentResolver().takePersistableUriPermission(selectedCoverUri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
            catch (SecurityException ignored) { }
            Toast.makeText(this, "Cover selected: " + displayName(selectedCoverUri), Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == REQUEST_BOOK_PDF && resultCode == RESULT_OK && data != null && data.getData() != null && pendingBook != null) {
            selectedPdf = data.getData();
            try { getContentResolver().takePersistableUriPermission(selectedPdf, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
            catch (SecurityException ignored) { }
            createBook(pendingBook, selectedPdf, selectedCoverUri);
            pendingBook = null;
            selectedCoverUri = null;
        }
    }

    private void createBook(PendingBook book, Uri pdfUri, Uri coverUri) {
        executor.execute(() -> {
            try {
                Map<String, String> fields = new HashMap<>();
                fields.put("title", book.title); fields.put("author", book.author);
                fields.put("description", book.description); fields.put("total_copies", String.valueOf(book.copies));
                InputStream pdfStream = pdfUri == null ? null : getContentResolver().openInputStream(pdfUri);
                InputStream coverStream = coverUri == null ? null : getContentResolver().openInputStream(coverUri);
                String raw = ApiClient.postMultipartFiles("api/library/books/", token, fields,
                        new String[]{"pdf_file", "cover_image"},
                        new String[]{pdfUri == null ? null : displayName(pdfUri), coverUri == null ? null : displayName(coverUri)},
                        new String[]{"application/pdf", coverUri == null ? null : coverMimeType(coverUri)},
                        new InputStream[]{pdfStream, coverStream});
                JSONObject response = new JSONObject(raw);
                if (response.has("error")) throw new Exception(response.optString("error"));
                runOnUiThread(() -> { Toast.makeText(this, "Book added to shared catalog", Toast.LENGTH_SHORT).show(); reloadBooks(); });
            } catch (Exception e) { runOnUiThread(() -> Toast.makeText(this, "Couldn’t add book: " + e.getMessage(), Toast.LENGTH_LONG).show()); }
        });
    }

    private String displayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        } catch (Exception ignored) { }
        return "book.pdf";
    }

    private String coverMimeType(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType == null || !mimeType.startsWith("image/") ? "image/jpeg" : mimeType;
    }

    private MaterialCardView card() {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(8); params.bottomMargin = dp(6);
        card.setLayoutParams(params); card.setRadius(dp(16)); card.setCardElevation(dp(2));
        card.setCardBackgroundColor(0xFFFFFFFF); card.setStrokeColor(0xFFE2E8F0); card.setStrokeWidth(dp(1));
        return card;
    }

    private LinearLayout body() { LinearLayout body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(16), dp(14), dp(16), dp(14)); return body; }
    private TextView text(String value, int size, String color, boolean bold) {
        TextView text = new TextView(this); text.setText(value); text.setTextSize(size); text.setTextColor(android.graphics.Color.parseColor(color));
        if (bold) text.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2); params.topMargin = dp(4); text.setLayoutParams(params); return text;
    }
    private MaterialButton button(String label) { MaterialButton b = new MaterialButton(this); b.setText(label); b.setAllCaps(false); b.setCornerRadius(dp(12)); return b; }
    private EditText input(String hint) { EditText input = new EditText(this); input.setSingleLine(true); input.setHint(hint); return input; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }

    private static class PendingBook {
        final String title, author, description; final int copies;
        PendingBook(String title, String author, String description, int copies) { this.title = title; this.author = author; this.description = description; this.copies = copies; }
    }
}
