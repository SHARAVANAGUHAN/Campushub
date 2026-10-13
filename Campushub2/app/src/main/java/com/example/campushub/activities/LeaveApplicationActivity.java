package com.example.campushub.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.campushub.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class LeaveApplicationActivity extends AppCompatActivity {

    private TextInputEditText edtFromDate;
    private TextInputEditText edtToDate;
    private TextInputEditText edtReason;

    private TextView txtCharacterCount;
    private ActivityResultLauncher<Intent> filePicker;

    // Keeps track of the selected dates
    private String selectedFromDate = "";
    private String selectedToDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_application);

        // ==============================
        // FILE PICKER REGISTRATION
        // ==============================

        TextView txtFileName = findViewById(R.id.txtFileName);

        filePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK &&
                            result.getData() != null &&
                            result.getData().getData() != null) {

                        Uri uri = result.getData().getData();
                        String name = uri.getLastPathSegment();
                        txtFileName.setText(name);
                    }
                });

        // ==============================
        // THREE-DOT MENU BUTTON (POPUP)
        // ==============================

        ImageButton btnMenu = findViewById(R.id.btnMenu);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, v);
                popup.getMenu().add(0, 1, 0, "View History");
                popup.getMenu().add(0, 2, 0, "Check Status");

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        Intent intent = new Intent(LeaveApplicationActivity.this, LeaveHistoryActivity.class);
                        startActivity(intent);
                        return true;
                    } else if (item.getItemId() == 2) {
                        Toast.makeText(this, "Checking Application Status...", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                });

                popup.show();
            });
        }

        // ==============================
        // FILE UPLOAD BUTTON
        // ==============================

        MaterialButton btnUpload = findViewById(R.id.btnUpload);

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES,
                    new String[]{
                            "application/pdf",
                            "image/jpeg",
                            "image/png"
                    });

            filePicker.launch(intent);
        });

        // ==============================
        // BACK BUTTON
        // ==============================

        ImageButton btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // ==============================
        // LEAVE TYPE
        // ==============================

        AutoCompleteTextView leaveType = findViewById(R.id.spinnerLeaveType);

        String[] leaveTypes = {
                "Medical Leave",
                "Personal Leave",
                "Emergency Leave",
                "Other"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        leaveTypes
                ) {

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            ViewGroup parent) {

                        TextView view = (TextView) super.getView(position, convertView, parent);

                        view.setTextColor(Color.rgb(30, 41, 59));
                        view.setTextSize(15);
                        view.setPadding(20, 18, 20, 18);

                        return view;
                    }
                };

        leaveType.setAdapter(adapter);

        // ==============================
        // DATE FIELDS
        // ==============================

        edtFromDate = findViewById(R.id.edtFromDate);
        edtToDate = findViewById(R.id.edtToDate);

        edtFromDate.setOnClickListener(v -> showDatePicker(edtFromDate, true));
        edtToDate.setOnClickListener(v -> showDatePicker(edtToDate, false));

        // ==============================
        // REASON
        // ==============================

        edtReason = findViewById(R.id.edtReason);
        txtCharacterCount = findViewById(R.id.txtCharacterCount);

        edtReason.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtCharacterCount.setText(s.length() + " / 1000");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // ==============================
        // QUICK TEMPLATES
        // ==============================

        MaterialButton btnMedical = findViewById(R.id.btnMedical);
        MaterialButton btnPersonal = findViewById(R.id.btnPersonal);
        MaterialButton btnEmergency = findViewById(R.id.btnEmergency);

        btnMedical.setOnClickListener(v -> {
            leaveType.setText("Medical Leave", false);
            generateLetter(
                    "I am writing to request leave due to medical reasons. " +
                            "I kindly request you to grant me leave for the mentioned period.\n\n" +
                            "Thank you for your consideration."
            );
        });

        btnPersonal.setOnClickListener(v -> {
            leaveType.setText("Personal Leave", false);
            generateLetter(
                    "I kindly request leave due to personal reasons. " +
                            "I request you to grant me leave for the mentioned period.\n\n" +
                            "Thank you."
            );
        });

        btnEmergency.setOnClickListener(v -> {
            leaveType.setText("Emergency Leave", false);
            generateLetter(
                    "Due to an unexpected emergency, I am unable to attend college. " +
                            "I kindly request you to grant me leave for the mentioned period.\n\n" +
                            "Thank you for your understanding."
            );
        });

        // ==============================
        // SUBMIT
        // ==============================

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitLeave);

        btnSubmit.setOnClickListener(v -> {
            if (edtFromDate.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please select the From Date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (edtToDate.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please select the To Date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (edtReason.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please write your application", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Leave application submitted!", Toast.LENGTH_SHORT).show();
        });
    }

    // =====================================================
    // OPTIONS MENU (TOOLBAR THREE DOTS)
    // =====================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "View History");
        menu.add(Menu.NONE, 2, Menu.NONE, "Check Status");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Intent intent = new Intent(LeaveApplicationActivity.this, LeaveHistoryActivity.class);
                startActivity(intent);
                return true;

            case 2:
                Toast.makeText(this, "Checking Application Status...", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // =====================================================
    // DATE PICKER
    // =====================================================

    private void showDatePicker(TextInputEditText target, boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    target.setText(date);

                    if (isFromDate) {
                        selectedFromDate = date;
                    } else {
                        selectedToDate = date;
                    }

                    updateLeaveLetter();
                },
                year,
                month,
                day
        );

        dialog.show();
    }

    // =====================================================
    // UPDATE LETTER WITH DATES
    // =====================================================

    private void updateLeaveLetter() {
        if (selectedFromDate.isEmpty() || selectedToDate.isEmpty()) {
            return;
        }

        String currentText = edtReason.getText().toString();

        if (currentText.trim().isEmpty()) {
            edtReason.setText(
                    "Respected Sir/Madam,\n\n" +
                            "I kindly request leave from " + selectedFromDate + " to " + selectedToDate + ".\n\n" +
                            "Thank you for your consideration."
            );
            edtReason.setSelection(edtReason.getText().length());
            return;
        }

        String updatedText = currentText.replaceAll(
                "from \\d{2}/\\d{2}/\\d{4} to \\d{2}/\\d{2}/\\d{4}",
                "from " + selectedFromDate + " to " + selectedToDate
        );

        if (updatedText.equals(currentText)) {
            String marker = "Respected Sir/Madam,\n\n";

            if (currentText.startsWith(marker)) {
                updatedText = marker +
                        "I kindly request leave from " + selectedFromDate + " to " + selectedToDate + ".\n\n" +
                        currentText.substring(marker.length());
            }
        }

        edtReason.setText(updatedText);
        edtReason.setSelection(edtReason.getText().length());
    }

    // =====================================================
    // GENERATE TEMPLATE LETTER
    // =====================================================

    private void generateLetter(String body) {
        String letter = "Respected Sir/Madam,\n\n";

        if (!selectedFromDate.isEmpty() && !selectedToDate.isEmpty()) {
            letter += "I kindly request leave from " + selectedFromDate + " to " + selectedToDate + ".\n\n";
        }

        letter += body;

        edtReason.setText(letter);
        edtReason.setSelection(edtReason.getText().length());
    }
}