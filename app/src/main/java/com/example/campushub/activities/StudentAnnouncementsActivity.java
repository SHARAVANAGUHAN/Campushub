package com.example.campushub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campushub.R;
import com.example.campushub.api.ApiClient;
import com.example.campushub.utils.CampusHubPrefs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class StudentAnnouncementsActivity extends AppCompatActivity {

    // =====================================================
    // VIEWS
    // =====================================================

    private LinearLayout announcementContainer;

    private ProgressBar progressBar;

    private TextView txtEmpty;

    private TextView txtCount;


    // =====================================================
    // EXECUTOR
    // =====================================================

    private ExecutorService executorService;


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_student_announcements
        );


        executorService =
                Executors.newSingleThreadExecutor();


        // =====================================================
        // FIND VIEWS
        // =====================================================

        announcementContainer =
                findViewById(
                        R.id.announcementContainer
                );


        progressBar =
                findViewById(
                        R.id.progressBar
                );


        txtEmpty =
                findViewById(
                        R.id.txtEmpty
                );


        txtCount =
                findViewById(
                        R.id.txtCount
                );


        // =====================================================
        // BACK
        // =====================================================

        findViewById(R.id.btnBack)
                .setOnClickListener(v ->
                        finish()
                );


        // =====================================================
        // LOAD ANNOUNCEMENTS
        // =====================================================

        loadAnnouncements();
    }


    // =========================================================
    // LOAD STUDENT ANNOUNCEMENTS
    // =========================================================

    private void loadAnnouncements() {

        CampusHubPrefs prefs =
                new CampusHubPrefs(this);


        String accessToken =
                prefs.getAccessToken();


        // =====================================================
        // CHECK LOGIN
        // =====================================================

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
        // API REQUEST
        // =====================================================

        executorService.execute(() -> {

            try {

                String response =
                        ApiClient.get(
                                "api/announcements/student/list/",
                                accessToken
                        );


                JSONObject object =
                        new JSONObject(response);


                JSONArray announcements =
                        object.optJSONArray(
                                "announcements"
                        );


                int count =
                        object.optInt(
                                "count",
                                announcements == null
                                        ? 0
                                        : announcements.length()
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


                    txtCount.setText(
                            count
                                    + " announcement"
                                    + (count == 1
                                    ? ""
                                    : "s")
                    );


                    // =============================================
                    // NO ANNOUNCEMENTS
                    // =============================================

                    if (announcements == null ||
                            announcements.length() == 0) {

                        txtEmpty.setVisibility(
                                View.VISIBLE
                        );


                        txtEmpty.setText(
                                "No announcements available"
                        );


                        return;
                    }


                    txtEmpty.setVisibility(
                            View.GONE
                    );


                    // =============================================
                    // CREATE CARDS
                    // =============================================

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
                            StudentAnnouncementsActivity.this,
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
                        R.layout.item_student_announcement,
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


        TextView txtTeacher =
                card.findViewById(
                        R.id.txtAnnouncementTeacher
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
                        "Announcement"
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


        String teacherName =
                announcement.optString(
                        "teacher_name",
                        "Teacher"
                );


        String teacherId =
                announcement.optString(
                        "teacher_id",
                        ""
                );


        String message =
                announcement.optString(
                        "message",
                        ""
                );


        String createdAt =
                announcement.optString(
                        "created_at",
                        ""
                );


        // =====================================================
        // TITLE
        // =====================================================

        txtTitle.setText(
                title
        );


        // =====================================================
        // SUBJECT
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
        // TEACHER
        // =====================================================

        if (teacherId != null &&
                !teacherId.isEmpty()) {

            txtTeacher.setText(
                    "Posted by "
                            + teacherName
                            + " • "
                            + teacherId
            );

        } else {

            txtTeacher.setText(
                    "Posted by "
                            + teacherName
            );
        }


        // =====================================================
        // MESSAGE
        // =====================================================

        txtMessage.setText(
                message
        );


        // =====================================================
        // DATE
        // =====================================================

        if (createdAt != null &&
                !createdAt.isEmpty()) {

            txtDate.setText(
                    createdAt
            );

        } else {

            txtDate.setText(
                    "Recently posted"
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