package com.example.campushub.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campushub.R;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class LibraryActivity extends AppCompatActivity {
    private static final int REQUEST_PDF = 41;
    private static final String PREFS = "campus_library";
    private static final String BOOKS_KEY = "books_json";
    private final List<Book> books = new ArrayList<>();
    private final List<Book> visibleBooks = new ArrayList<>();
    private KindleBookAdapter adapter;
    private View emptyView;
    private TextView emptyTitle, emptyMessage, countText;
    private TextInputEditText searchInput;
    private int selectedFilter = R.id.filterAll;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_library);

        RecyclerView recycler = findViewById(R.id.recyclerBooks);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new KindleBookAdapter(this, visibleBooks, this::openBook, this::confirmRemove);
        recycler.setAdapter(adapter);
        emptyView = findViewById(R.id.emptyLibrary);
        emptyTitle = findViewById(R.id.tvEmptyTitle);
        emptyMessage = findViewById(R.id.tvEmptyMessage);
        countText = findViewById(R.id.tvLibraryCount);
        searchInput = findViewById(R.id.etBookSearch);

        findViewById(R.id.btnAddBook).setOnClickListener(v -> choosePdf());
        findViewById(R.id.btnEmptyAdd).setOnClickListener(v -> choosePdf());
        MaterialButtonToggleGroup filters = findViewById(R.id.libraryFilters);
        filters.check(R.id.filterAll);
        filters.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) { selectedFilter = checkedId; renderBooks(); }
        });
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderBooks(); }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });
        loadBooks();
    }

    @Override protected void onResume() {
        super.onResume();
        if (adapter != null) { loadBooks(); renderBooks(); }
    }

    private void choosePdf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PDF);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_PDF || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String title = getDisplayName(uri);
            if (title.toLowerCase(Locale.ROOT).endsWith(".pdf")) title = title.substring(0, title.length() - 4);
            for (Book existing : books) {
                if (uri.toString().equals(existing.getDocumentUri())) {
                    Toast.makeText(this, "This PDF is already in your library", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            books.add(new Book(UUID.randomUUID().toString(), title, null, uri.toString(), 0, false));
            saveBooks();
            renderBooks();
            Toast.makeText(this, R.string.library_added, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.library_permission_error, Toast.LENGTH_LONG).show();
        }
    }

    private String getDisplayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        } catch (Exception ignored) { }
        String last = uri.getLastPathSegment();
        return last == null || last.trim().isEmpty() ? "PDF book" : last;
    }

    private void openBook(Book book) {
        if (book.isLocked()) {
            Toast.makeText(this, R.string.book_soft_copy_locked, Toast.LENGTH_SHORT).show();
            return;
        }
        if (book.getDocumentUriValue() == null) {
            Toast.makeText(this, R.string.library_open_error, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, PdfReaderActivity.class);
        intent.putExtra(PdfReaderActivity.EXTRA_BOOK_ID, book.getId());
        intent.putExtra(PdfReaderActivity.EXTRA_BOOK_TITLE, book.getTitle());
        intent.setData(book.getDocumentUriValue());
        try { startActivity(intent); }
        catch (Exception e) { Toast.makeText(this, R.string.library_open_error, Toast.LENGTH_LONG).show(); }
    }

    private void confirmRemove(Book book) {
        new AlertDialog.Builder(this)
                .setTitle("Remove book?")
                .setMessage("“" + book.getTitle() + "” will be removed from your shelf. The original PDF stays on your device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> {
                    books.remove(book);
                    Uri documentUri = book.getDocumentUriValue();
                    if (documentUri != null) {
                        try {
                            getContentResolver().releasePersistableUriPermission(
                                    documentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException ignored) { }
                    }
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                            .remove("progress_" + book.getId()).apply();
                    saveBooks();
                    renderBooks();
                    Toast.makeText(this, R.string.library_removed, Toast.LENGTH_SHORT).show();
                }).show();
    }

    private void loadBooks() {
        books.clear();
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(BOOKS_KEY, "[]");
        try {
            JSONArray rows = new JSONArray(raw);
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.optJSONObject(i);
                if (row == null) continue;
                String id = row.optString("id");
                int progress = getSharedPreferences(PREFS, MODE_PRIVATE)
                        .getInt("progress_" + id, row.optInt("progress", 0));
                books.add(new Book(id, row.optString("title", "PDF book"),
                        row.optString("cover", ""), row.optString("uri", ""),
                        progress, false));
            }
        } catch (Exception ignored) { }
    }

    private void saveBooks() {
        JSONArray rows = new JSONArray();
        for (Book book : books) {
            JSONObject row = new JSONObject();
            try {
                row.put("id", book.getId()); row.put("title", book.getTitle());
                row.put("cover", book.getCoverUrl()); row.put("uri", book.getDocumentUri());
                row.put("progress", book.getProgressPercentage()); rows.put(row);
            } catch (Exception ignored) { }
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(BOOKS_KEY, rows.toString()).apply();
    }

    private void renderBooks() {
        if (adapter == null) return;
        String query = searchInput == null || searchInput.getText() == null ? "" : searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        visibleBooks.clear();
        for (Book book : books) {
            if (!book.getTitle().toLowerCase(Locale.ROOT).contains(query)) continue;
            if (selectedFilter == R.id.filterReading && (book.getProgressPercentage() == 0 || book.getProgressPercentage() == 100)) continue;
            if (selectedFilter == R.id.filterFinished && book.getProgressPercentage() < 100) continue;
            visibleBooks.add(book);
        }
        adapter.setBooks(visibleBooks);
        countText.setText(getResources().getQuantityString(R.plurals.library_book_count, books.size(), books.size()));
        emptyView.setVisibility(visibleBooks.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.recyclerBooks).setVisibility(visibleBooks.isEmpty() ? View.GONE : View.VISIBLE);
        if (books.isEmpty()) {
            emptyTitle.setText("Your shelf is waiting");
            emptyMessage.setText("Add a PDF to start reading and track your progress here.");
            findViewById(R.id.btnEmptyAdd).setVisibility(View.VISIBLE);
        } else if (visibleBooks.isEmpty()) {
            emptyTitle.setText("No books found");
            emptyMessage.setText("Try another search or filter.");
            findViewById(R.id.btnEmptyAdd).setVisibility(View.GONE);
        }
    }
}
