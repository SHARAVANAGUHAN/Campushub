package com.example.campushub.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.HorizontalScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SharedLibraryActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private JSONArray catalog = new JSONArray();
    private JSONArray requests = new JSONArray();
    private LinearLayout items;
    private TextView empty;
    private ProgressBar loading;
    private TextInputEditText search;
    private String token;
    private boolean showingRequests;
    private int sortMode;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_shared_library);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sharedLibraryRoot), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });
        items = findViewById(R.id.sharedLibraryItems);
        empty = findViewById(R.id.sharedLibraryEmpty);
        loading = findViewById(R.id.sharedLibraryLoading);
        search = findViewById(R.id.etSharedSearch);
        token = getSharedPreferences("CampusHubPrefs", MODE_PRIVATE).getString("access_token", "");
        findViewById(R.id.btnSharedLibraryBack).setOnClickListener(v -> finish());
        MaterialButtonToggleGroup tabs = findViewById(R.id.sharedLibraryTabs);
        Spinner sort = findViewById(R.id.librarySort);
        ArrayAdapter<String> sortOptions = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Title A–Z", "Availability"});
        sortOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort.setAdapter(sortOptions);
        sort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                sortMode = position;
                if (!showingRequests) render();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        tabs.check(R.id.tabCatalog);
        tabs.addOnButtonCheckedListener((group, id, checked) -> {
            if (checked) {
                showingRequests = id == R.id.tabMyRequests;
                search.setText("");
                findViewById(R.id.sharedSearchLayout).setVisibility(showingRequests ? View.GONE : View.VISIBLE);
                findViewById(R.id.librarySortRow).setVisibility(showingRequests ? View.GONE : View.VISIBLE);
                render();
            }
        });
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { render(); }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });
        reload();
    }

    @Override protected void onResume() { super.onResume(); if (items != null) reload(); }

    private void reload() {
        loading.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                JSONArray newCatalog = new JSONArray(ApiClient.get("api/library/books/", token));
                JSONArray newRequests = new JSONArray(ApiClient.get("api/library/requests/", token));
                runOnUiThread(() -> {
                    catalog = newCatalog;
                    requests = newRequests;
                    loading.setVisibility(View.GONE);
                    render();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    empty.setText("Couldn’t load the library. Check your connection and try again.");
                    empty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Library unavailable: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void render() {
        if (items == null) return;
        items.removeAllViews();
        if (showingRequests) renderRequests(); else renderCatalog();
        if (showingRequests) {
            empty.setVisibility(items.getChildCount() == 0 && loading.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
            if (items.getChildCount() == 0) empty.setText("You haven’t requested any books yet.");
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    private void renderCatalog() {
        String query = search.getText() == null ? "" : search.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<JSONObject> availableBooks = new ArrayList<>();
        List<JSONObject> unavailableBooks = new ArrayList<>();
        for (int i = 0; i < catalog.length(); i++) {
            JSONObject book = catalog.optJSONObject(i);
            if (book == null) continue;
            String title = book.optString("title", "Untitled book");
            String author = book.optString("author", "");
            if (!title.toLowerCase(Locale.ROOT).contains(query) && !author.toLowerCase(Locale.ROOT).contains(query)) continue;
            int available = book.optInt("available_copies", 0);
            String requestStatus = book.optString("current_request_status", "");
            boolean canRead = book.optBoolean("can_read", false);
            if (canRead || (available > 0 && !"pending".equals(requestStatus) && !"allotted".equals(requestStatus))) {
                availableBooks.add(book);
            } else {
                unavailableBooks.add(book);
            }
        }
        if (sortMode == 0) {
            Collections.sort(availableBooks, Comparator.comparing(book -> book.optString("title", "").toLowerCase(Locale.ROOT)));
            Collections.sort(unavailableBooks, Comparator.comparing(book -> book.optString("title", "").toLowerCase(Locale.ROOT)));
        } else {
            Comparator<JSONObject> byAvailability = Comparator
                    .comparingInt((JSONObject book) -> book.optBoolean("can_read", false) ? 0 : 1)
                    .thenComparing(Comparator.comparingInt((JSONObject book) -> book.optInt("available_copies", 0)).reversed())
                    .thenComparing(book -> book.optString("title", "").toLowerCase(Locale.ROOT));
            Collections.sort(availableBooks, byAvailability);
            Collections.sort(unavailableBooks, byAvailability);
        }
        addBookshelfRow("Available now", availableBooks, true);
        addBookshelfRow("Not available", unavailableBooks, false);
    }

    private void addBookshelfRow(String heading, List<JSONObject> books, boolean availableShelf) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(-1, -2);
        sectionParams.bottomMargin = dp(20);
        section.setLayoutParams(sectionParams);

        LinearLayout headingRow = new LinearLayout(this);
        headingRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        headingRow.setPadding(dp(18), dp(4), dp(18), dp(10));
        TextView title = text(heading, 18, "#172B4D", true);
        headingRow.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView count = text(String.valueOf(books.size()), 12,
                availableShelf ? "#087E68" : "#6B7280", true);
        count.setGravity(android.view.Gravity.CENTER);
        GradientDrawable badge = new GradientDrawable();
        badge.setColor(Color.parseColor(availableShelf ? "#DDF7EE" : "#E9EDF3"));
        badge.setCornerRadius(dp(20));
        count.setBackground(badge);
        count.setPadding(dp(10), dp(5), dp(10), dp(5));
        headingRow.addView(count);
        section.addView(headingRow);

        if (books.isEmpty()) {
            TextView shelfEmpty = text(availableShelf
                    ? "No books are available right now. Check back soon."
                    : "Nothing on this shelf at the moment.", 13, "#64748B", false);
            shelfEmpty.setPadding(dp(16), dp(17), dp(16), dp(17));
            GradientDrawable emptyBackground = new GradientDrawable();
            emptyBackground.setColor(Color.WHITE);
            emptyBackground.setCornerRadius(dp(15));
            shelfEmpty.setBackground(emptyBackground);
            LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(-1, -2);
            emptyParams.setMargins(dp(16), 0, dp(16), dp(2));
            section.addView(shelfEmpty, emptyParams);
            items.addView(section);
            return;
        }

        HorizontalScrollView shelfScroll = new HorizontalScrollView(this);
        shelfScroll.setHorizontalScrollBarEnabled(false);
        shelfScroll.setClipToPadding(false);
        shelfScroll.setPadding(dp(16), dp(2), dp(16), 0);
        LinearLayout bookRow = new LinearLayout(this);
        bookRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < books.size(); i++) {
            JSONObject book = books.get(i);
            bookRow.addView(createBookTile(book, i, availableShelf));
        }
        shelfScroll.addView(bookRow);
        section.addView(shelfScroll);

        View shelfBoard = new View(this);
        GradientDrawable wood = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#D6A36C"), Color.parseColor("#A96D3D"), Color.parseColor("#8A542F")});
        wood.setCornerRadius(dp(5));
        shelfBoard.setBackground(wood);
        shelfBoard.setElevation(dp(3));
        LinearLayout.LayoutParams boardParams = new LinearLayout.LayoutParams(-1, dp(13));
        boardParams.setMargins(dp(13), 0, dp(13), dp(5));
        section.addView(shelfBoard, boardParams);
        items.addView(section);
    }

    private View createBookTile(JSONObject book, int index, boolean availableShelf) {
        String title = book.optString("title", "Untitled book");
        String author = book.optString("author", "");
        String requestStatus = book.optString("current_request_status", "");
        int copies = book.optInt("available_copies", 0);
        int bookId = book.optInt("id");
        boolean canRead = book.optBoolean("can_read", false);

        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setPadding(dp(7), dp(7), dp(7), dp(10));
        tile.setBackgroundColor(Color.WHITE);
        tile.setElevation(dp(2));
        LinearLayout.LayoutParams tileParams = new LinearLayout.LayoutParams(dp(150), -2);
        tileParams.setMarginEnd(dp(12));
        tile.setLayoutParams(tileParams);

        FrameLayout cover = new FrameLayout(this);
        int[][] palette = {
                {0xFF256D68, 0xFF174E50}, {0xFFB96845, 0xFF834331},
                {0xFF536F9A, 0xFF354969}, {0xFF8A648D, 0xFF62466A},
                {0xFFAD8748, 0xFF80612F}, {0xFF4A7C83, 0xFF315A60}
        };
        int[] colors = palette[Math.floorMod(bookId + index, palette.length)];
        GradientDrawable coverBackground = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{colors[1], colors[0], colors[0]});
        coverBackground.setCornerRadius(dp(5));
        cover.setBackground(coverBackground);
        LinearLayout.LayoutParams coverParams = new LinearLayout.LayoutParams(-1, dp(146));
        coverParams.bottomMargin = dp(9);
        String coverUrl = book.optString("cover_url", "");
        if (!coverUrl.isEmpty() && !"null".equals(coverUrl)) {
            ImageView coverImage = new ImageView(this);
            coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            cover.addView(coverImage, new FrameLayout.LayoutParams(-1, -1));
            Glide.with(this).load(coverUrl).placeholder(new ColorDrawable(colors[0]))
                    .error(new ColorDrawable(colors[0])).centerCrop().into(coverImage);
        }
        View spine = new View(this);
        spine.setBackgroundColor(0x33000000);
        cover.addView(spine, new FrameLayout.LayoutParams(dp(8), -1, android.view.Gravity.START));
        TextView coverTitle = text(title, 15, "#FFFFFF", true);
        coverTitle.setMaxLines(5);
        coverTitle.setGravity(android.view.Gravity.CENTER);
        coverTitle.setPadding(dp(14), dp(12), dp(10), dp(12));
        cover.addView(coverTitle, new FrameLayout.LayoutParams(-1, -1));
        TextView coverMark = text("CAMPUS LIBRARY", 8, "#DCEBE8", true);
        coverMark.setGravity(android.view.Gravity.CENTER);
        FrameLayout.LayoutParams markParams = new FrameLayout.LayoutParams(-1, dp(20), android.view.Gravity.BOTTOM);
        cover.addView(coverMark, markParams);
        tile.addView(cover, coverParams);

        TextView bookTitle = text(title, 14, "#172B4D", true);
        bookTitle.setMaxLines(2);
        tile.addView(bookTitle);
        TextView authorText = text(author.isEmpty() ? "Campus collection" : author, 11, "#64748B", false);
        authorText.setMaxLines(1);
        LinearLayout.LayoutParams authorParams = new LinearLayout.LayoutParams(-1, -2);
        authorParams.topMargin = dp(3);
        tile.addView(authorText, authorParams);
        TextView format = text(book.optBoolean("pdf_available", false) ? "DIGITAL PDF" : "LIBRARY COPY",
                9, book.optBoolean("pdf_available", false) ? "#315F9A" : "#75532F", true);
        format.setLetterSpacing(0.06f);
        LinearLayout.LayoutParams formatParams = new LinearLayout.LayoutParams(-2, -2);
        formatParams.topMargin = dp(6);
        tile.addView(format, formatParams);

        TextView status = text(availabilityLabel(copies, requestStatus, book), 11,
                colorHex(statusColor(copies, requestStatus)), true);
        status.setMaxLines(2);
        GradientDrawable statusBadge = new GradientDrawable();
        statusBadge.setColor(tintColor(statusColor(copies, requestStatus), 0.11f));
        statusBadge.setCornerRadius(dp(10));
        status.setBackground(statusBadge);
        status.setPadding(dp(7), dp(5), dp(7), dp(5));
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = dp(7);
        tile.addView(status, statusParams);

        MaterialButton action;
        if (canRead) {
            action = button("Read PDF");
            action.setOnClickListener(v -> readBook(bookId, title));
        } else if ("pending".equals(requestStatus)) {
            action = button("Awaiting approval");
            action.setEnabled(false);
        } else if ("allotted".equals(requestStatus)) {
            action = button("Access ended");
            action.setEnabled(false);
        } else if (copies > 0) {
            action = button("Request book");
            action.setOnClickListener(v -> choosePeriod(bookId, title));
        } else {
            action = button("Unavailable");
            action.setEnabled(false);
        }
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(-1, dp(42));
        actionParams.topMargin = dp(8);
        tile.addView(action, actionParams);
        return tile;
    }

    private void renderRequests() {
        for (int i = 0; i < requests.length(); i++) {
            JSONObject request = requests.optJSONObject(i);
            if (request == null) continue;
            String title = request.optString("book_title", "Book");
            MaterialCardView card = newCard();
            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(dp(16), dp(14), dp(16), dp(14));
            content.addView(text(title, 17, "#172B4D", true));
            String state = request.optString("status", "pending");
            String details = request.optInt("requested_days") + " day request · " + stateLabel(state);
            if (request.has("due_date") && !request.isNull("due_date")) details += " · due " + formatDueDate(request.optString("due_date"));
            TextView detail = text(details, 13, colorHex(statusColor(0, state)), false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.topMargin = dp(7);
            content.addView(detail, params);
            int bookId = request.optInt("book");
            JSONObject book = findBook(bookId);
            if (book != null) {
                content.addView(text(book.optBoolean("pdf_available", false) ? "Digital PDF" : "Physical library copy",
                        12, "#64748B", false));
            }
            if ("allotted".equals(state) && book != null && book.optBoolean("can_read", false)) {
                MaterialButton read = button("Read PDF");
                read.setOnClickListener(v -> readBook(bookId, title));
                addButton(content, read);
            }
            card.addView(content);
            items.addView(card);
        }
    }

    private JSONObject findBook(int id) {
        for (int i = 0; i < catalog.length(); i++) {
            JSONObject item = catalog.optJSONObject(i);
            if (item != null && item.optInt("id") == id) return item;
        }
        return null;
    }

    private String availabilityLabel(int available, String requestStatus, JSONObject book) {
        if ("pending".equals(requestStatus)) return "Request pending librarian approval";
        if ("allotted".equals(requestStatus)) {
            if (book.optBoolean("can_read", false)) {
                String dueDate = formatDueDate(book.optString("current_due_date", ""));
                return dueDate.isEmpty() ? "Allotted · ready to read" : "Allotted · due " + dueDate;
            }
            return book.optBoolean("pdf_available", false)
                    ? "Loan period ended · contact the librarian"
                    : "Allotted · collect this library copy";
        }
        if (available == 0) return "Currently unavailable";
        return available + (available == 1 ? " copy available" : " copies available");
    }

    private String formatDueDate(String date) {
        if (date == null || date.isEmpty() || "null".equals(date)) return "";
        try {
            return java.time.LocalDate.parse(date)
                    .format(java.time.format.DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()));
        } catch (Exception ignored) {
            return date;
        }
    }

    private int tintColor(int color, float alpha) {
        return Color.argb(Math.round(alpha * 255), Color.red(color), Color.green(color), Color.blue(color));
    }

    private String stateLabel(String state) {
        switch (state) {
            case "allotted": return "Allotted";
            case "rejected": return "Declined";
            case "returned": return "Returned";
            default: return "Awaiting librarian";
        }
    }

    private int statusColor(int available, String state) {
        if ("pending".equals(state)) return Color.parseColor("#B45309");
        if ("rejected".equals(state)) return Color.parseColor("#B91C1C");
        if ("allotted".equals(state)) return Color.parseColor("#0F766E");
        return available > 0 ? Color.parseColor("#0F766E") : Color.parseColor("#64748B");
    }

    private String colorHex(int color) { return String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF); }

    private void choosePeriod(int bookId, String title) {
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(30);
        picker.setValue(7);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(22), dp(4), dp(22), 0);
        wrap.addView(text("Choose the number of days you need this book. It will only be unlocked if the librarian approves your request.", 14, "#64748B", false));
        wrap.addView(picker);
        new AlertDialog.Builder(this).setTitle(title).setView(wrap).setNegativeButton("Cancel", null)
                .setPositiveButton("Send request", (d, w) -> sendRequest(bookId, picker.getValue())).show();
    }

    private void sendRequest(int bookId, int days) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("book_id", bookId);
                body.put("requested_days", days);
                String result = ApiClient.postAuthenticated("api/library/requests/", body.toString(), token);
                JSONObject response = new JSONObject(result);
                if (response.has("error")) throw new Exception(response.optString("error"));
                runOnUiThread(() -> { Toast.makeText(this, "Request sent to the librarian", Toast.LENGTH_LONG).show(); reload(); });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void readBook(int bookId, String title) {
        loading.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                File pdf = ApiClient.downloadAuthenticated("api/library/books/" + bookId + "/pdf/", token,
                        new File(getCacheDir(), "library-book-" + bookId + ".pdf"));
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    Intent intent = new Intent(this, PdfReaderActivity.class);
                    intent.putExtra(PdfReaderActivity.EXTRA_BOOK_ID, "shared-" + bookId);
                    intent.putExtra(PdfReaderActivity.EXTRA_BOOK_TITLE, title);
                    intent.putExtra(PdfReaderActivity.EXTRA_PDF_PATH, pdf.getAbsolutePath());
                    startActivity(intent);
                });
            } catch (Exception e) {
                runOnUiThread(() -> { loading.setVisibility(View.GONE); Toast.makeText(this, "Couldn’t open book: " + e.getMessage(), Toast.LENGTH_LONG).show(); });
            }
        });
    }

    private MaterialCardView newCard() {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dp(16), 0, dp(16), dp(12));
        card.setLayoutParams(params);
        card.setRadius(dp(18));
        card.setCardElevation(dp(2));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(Color.parseColor("#E2E8F0"));
        card.setCardBackgroundColor(Color.WHITE);
        return card;
    }

    private TextView text(String value, int size, String color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(Color.parseColor(color));
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private MaterialButton button(String label) {
        MaterialButton button = new MaterialButton(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setCornerRadius(dp(12));
        return button;
    }

    private void addButton(LinearLayout content, MaterialButton button) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(46));
        params.topMargin = dp(10);
        content.addView(button, params);
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
