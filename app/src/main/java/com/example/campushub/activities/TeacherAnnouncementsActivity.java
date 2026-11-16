package com.example.campushub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.example.campushub.utils.CampusHubPrefs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TeacherAnnouncementsActivity extends AppCompatActivity {

    // =====================================================
    // VIEWS
    // =====================================================

    private EditText edtTitle;

    private EditText edtMessage;

    private Spinner spinnerSubject;

    private Button btnPublish;

    private LinearLayout announcementContainer;

    private ProgressBar progressBar;

    private TextView txtEmpty;


    // =====================================================
    // THREAD
    // =====================================================

    private ExecutorService executorService;


    // =====================================================
    // SUBJECT IDs
    // =====================================================

    private final List<Integer> subjectIds =
            new ArrayList<>();


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_teacher_announcements
        );


        executorService =
                Executors.newSingleThreadExecutor();


        // =====================================================
        // FIND VIEWS
        // =====================================================

        edtTitle =
                findViewById(R.id.edtTitle);


        edtMessage =
                findViewById(R.id.edtMessage);


        spinnerSubject =
                findViewById(R.id.spinnerSubject);


        btnPublish =
                findViewById(R.id.btnPublish);


        announcementContainer =
                findViewById(
                        R.id.announcementContainer
                );


        progressBar =
                findViewById(R.id.progressBar);


        txtEmpty =
                findViewById(R.id.txtEmpty);


        // =====================================================
        // BACK BUTTON
        // =====================================================

        findViewById(R.id.btnBack)
                .setOnClickListener(v ->
                        finish()
                );


        // =====================================================
        // LOAD SUBJECTS
        // =====================================================

        loadSubjects();


        // =====================================================
        // PUBLISH BUTTON
        // =====================================================

        btnPublish.setOnClickListener(
                v -> publishAnnouncement()
        );


        // =====================================================
        // LOAD EXISTING ANNOUNCEMENTS
        // =====================================================

        loadAnnouncements();
    }


    // =========================================================
    // LOAD SUBJECTS
    // =========================================================

    private void loadSubjects() {

        CampusHubPrefs prefs =
                new CampusHubPrefs(this);


        String accessToken =
                prefs.getAccessToken();


        if (accessToken == null ||
                accessToken.isEmpty()) {

            Toast.makeText(
                    this,
                    "Login session expired",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        executorService.execute(() -> {

            try {

                // =================================================
                // IMPORTANT:
                // /api/subjects/ RETURNS A JSON ARRAY DIRECTLY
                // =================================================

                String response =
                        ApiClient.get(
                                "api/subjects/",
                                accessToken
                        );


                subjectIds.clear();


                List<String> subjectNames =
                        new ArrayList<>();


                // =================================================
                // PARSE SUBJECT ARRAY
                // =================================================

                if (response != null &&
                        response.trim().startsWith("[")) {

                    JSONArray array =
                            new JSONArray(response);


                    for (int i = 0;
                         i < array.length();
                         i++) {

                        JSONObject subject =
                                array.getJSONObject(i);


                        int id =
                                subject.getInt("id");


                        String name =
                                subject.getString(
                                        "subject_name"
                                );


                        String code =
                                subject.getString(
                                        "subject_code"
                                );


                        subjectIds.add(id);


                        if (code != null &&
                                !code.isEmpty()) {

                            subjectNames.add(
                                    name
                                            + " ("
                                            + code
                                            + ")"
                            );

                        } else {

                            subjectNames.add(name);
                        }
                    }
                }


                // =================================================
                // UPDATE SPINNER
                // =================================================

                runOnUiThread(() -> {

                    if (subjectNames.isEmpty()) {

                        Toast.makeText(
                                TeacherAnnouncementsActivity.this,
                                "No subjects available",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }


                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(
                                    TeacherAnnouncementsActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    subjectNames
                            );


                    adapter.setDropDownViewResource(
                            android.R.layout
                                    .simple_spinner_dropdown_item
                    );


                    spinnerSubject.setAdapter(
                            adapter
                    );


                    Toast.makeText(
                            TeacherAnnouncementsActivity.this,
                            subjectNames.size()
                                    + " subjects loaded",
                            Toast.LENGTH_SHORT
                    ).show();

                });


            } catch (Exception e) {

                e.printStackTrace();


                runOnUiThread(() -> {

                    Toast.makeText(
                            TeacherAnnouncementsActivity.this,
                            "Failed to load subjects: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
            }

        });
    }


    // =========================================================
    // PUBLISH ANNOUNCEMENT
    // =========================================================

    private void publishAnnouncement() {

        // =====================================================
        // TITLE
        // =====================================================

        String title =
                edtTitle.getText()
                        .toString()
                        .trim();


        // =====================================================
        // MESSAGE
        // =====================================================

        String message =
                edtMessage.getText()
                        .toString()
                        .trim();


        // =====================================================
        // VALIDATE TITLE
        // =====================================================

        if (title.isEmpty()) {

            edtTitle.setError(
                    "Enter announcement title"
            );

            edtTitle.requestFocus();

            return;
        }


        // =====================================================
        // VALIDATE MESSAGE
        // =====================================================

        if (message.isEmpty()) {

            edtMessage.setError(
                    "Enter announcement message"
            );

            edtMessage.requestFocus();

            return;
        }


        // =====================================================
        // VALIDATE SUBJECT
        // =====================================================

        int selectedPosition =
                spinnerSubject
                        .getSelectedItemPosition();


        if (subjectIds.isEmpty()) {

            Toast.makeText(
                    this,
                    "No subjects available",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (selectedPosition < 0 ||
                selectedPosition >= subjectIds.size()) {

            Toast.makeText(
                    this,
                    "Please select a subject",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // GET SUBJECT ID
        // =====================================================

        int subjectId =
                subjectIds.get(
                        selectedPosition
                );


        // =====================================================
        // GET LOGIN DATA
        // =====================================================

        CampusHubPrefs prefs =
                new CampusHubPrefs(this);


        String accessToken =
                prefs.getAccessToken();


        String teacherId =
                prefs.getTeacherId();


        // =====================================================
        // VALIDATE TOKEN
        // =====================================================

        if (accessToken == null ||
                accessToken.isEmpty()) {

            Toast.makeText(
                    this,
                    "Login session expired",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // VALIDATE TEACHER ID
        // =====================================================

        if (teacherId == null ||
                teacherId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Teacher ID not found",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // =====================================================
        // DISABLE BUTTON
        // =====================================================

        btnPublish.setEnabled(false);

        btnPublish.setText(
                "Publishing..."
        );


        // =====================================================
        // SEND TO DJANGO
        // =====================================================

        executorService.execute(() -> {

            try {

                JSONObject body =
                        new JSONObject();


                body.put(
                        "teacher_id",
                        teacherId
                );


                body.put(
                        "title",
                        title
                );


                body.put(
                        "message",
                        message
                );


                body.put(
                        "subject",
                        subjectId
                );


                String response =
                        ApiClient.postAuthenticated(
                                "api/announcements/teacher/create/",
                                body.toString(),
                                accessToken
                        );


                // =================================================
                // SUCCESS
                // =================================================

                runOnUiThread(() -> {

                    btnPublish.setEnabled(true);

                    btnPublish.setText(
                            "Publish Announcement"
                    );


                    Toast.makeText(
                            TeacherAnnouncementsActivity.this,
                            "Announcement published successfully",
                            Toast.LENGTH_SHORT
                    ).show();


                    // Clear form

                    edtTitle.setText("");

                    edtMessage.setText("");


                    // Reload announcements

                    loadAnnouncements();

                });


            } catch (Exception e) {

                e.printStackTrace();


                // =================================================
                // ERROR
                // =================================================

                runOnUiThread(() -> {

                    btnPublish.setEnabled(true);

                    btnPublish.setText(
                            "Publish Announcement"
                    );


                    Toast.makeText(
                            TeacherAnnouncementsActivity.this,
                            "Failed to publish: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
            }

        });
    }


    // =========================================================
    // LOAD TEACHER ANNOUNCEMENTS
    // =========================================================

    private void loadAnnouncements() {

        CampusHubPrefs prefs =
                new CampusHubPrefs(this);


        String accessToken =
                prefs.getAccessToken();


        String teacherId =
                prefs.getTeacherId();


        if (accessToken == null ||
                accessToken.isEmpty() ||
                teacherId == null ||
                teacherId.isEmpty()) {

            return;
        }


        // =====================================================
        // SHOW LOADING
        // =====================================================

        progressBar.setVisibility(
                View.VISIBLE
        );


        txtEmpty.setVisibility(
                View.GONE
        );


        // =====================================================
        // API CALL
        // =====================================================

        executorService.execute(() -> {

            try {

                String endpoint =
                        "api/announcements/teacher/list/?teacher_id="
                                + teacherId;


                String response =
                        ApiClient.get(
                                endpoint,
                                accessToken
                        );


                JSONObject object =
                        new JSONObject(response);


                JSONArray announcements =
                        object.optJSONArray(
                                "announcements"
                        );


                // =================================================
                // UPDATE UI
                // =================================================

                runOnUiThread(() -> {

                    progressBar.setVisibility(
                            View.GONE
                    );


                    announcementContainer
                            .removeAllViews();


                    if (announcements == null ||
                            announcements.length() == 0) {

                        txtEmpty.setVisibility(
                                View.VISIBLE
                        );


                        txtEmpty.setText(
                                "No announcements yet"
                        );


                        return;
                    }


                    txtEmpty.setVisibility(
                            View.GONE
                    );


                    // =================================================
                    // CREATE CARDS
                    // =================================================

                    for (int i = 0;
                         i < announcements.length();
                         i++) {

                        try {

                            JSONObject announcement =
                                    announcements
                                            .getJSONObject(i);


                            addAnnouncementCard(
                                    announcement
                            );


                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }

                });


            } catch (Exception e) {

                e.printStackTrace();


                runOnUiThread(() -> {

                    progressBar.setVisibility(
                            View.GONE
                    );


                    Toast.makeText(
                            TeacherAnnouncementsActivity.this,
                            "Failed to load announcements: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
            }

        });
    }


    // =========================================================
    // CREATE ANNOUNCEMENT CARD
    // =========================================================

    private void addAnnouncementCard(
            JSONObject announcement
    ) {

        View card =
                getLayoutInflater().inflate(
                        R.layout.item_teacher_announcement,
                        announcementContainer,
                        false
                );


        // =====================================================
        // FIND CARD VIEWS
        // =====================================================

        TextView txtTitle =
                card.findViewById(
                        R.id.txtAnnouncementTitle
                );


        TextView txtSubject =
                card.findViewById(
                        R.id.txtAnnouncementSubject
                );


        TextView txtMessage =
                card.findViewById(
                        R.id.txtAnnouncementMessage
                );


        TextView txtDate =
                card.findViewById(
                        R.id.txtAnnouncementDate
                );


        // =====================================================
        // GET DATA
        // =====================================================

        String title =
                announcement.optString(
                        "title",
                        "Untitled Announcement"
                );


        String message =
                announcement.optString(
                        "message",
                        ""
                );


        String subjectName =
                announcement.optString(
                        "subject_name",
                        "General"
                );


        String subjectCode =
                announcement.optString(
                        "subject_code",
                        ""
                );


        String createdAt =
                announcement.optString(
                        "created_at",
                        ""
                );


        // =====================================================
        // SET TITLE
        // =====================================================

        txtTitle.setText(
                title
        );


        // =====================================================
        // SET SUBJECT
        // =====================================================

        if (subjectCode != null &&
                !subjectCode.isEmpty()) {

            txtSubject.setText(
                    subjectName
                            + " ("
                            + subjectCode
                            + ")"
            );

        } else {

            txtSubject.setText(
                    subjectName
            );
        }


        // =====================================================
        // SET MESSAGE
        // =====================================================

        txtMessage.setText(
                message
        );


        // =====================================================
        // SET DATE
        // =====================================================

        if (createdAt != null &&
                !createdAt.isEmpty()) {

            txtDate.setText(
                    createdAt
            );

        } else {

            txtDate.setText(
                    "Recently published"
            );
        }


        // =====================================================
        // ADD CARD
        // =====================================================

        announcementContainer.addView(
                card
        );
    }


    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();


        if (executorService != null) {

            executorService.shutdown();
        }
    }
}