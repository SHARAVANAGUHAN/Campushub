package com.example.campushub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campushub.R;
import com.example.campushub.models.LeaveRecord;

import java.util.ArrayList;
import java.util.List;

public class LeaveHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerLeaveHistory;
    private LinearLayout layoutEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_history);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        recyclerLeaveHistory = findViewById(R.id.recyclerLeaveHistory);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        recyclerLeaveHistory.setLayoutManager(new LinearLayoutManager(this));

        // Fetch only APPROVED history records
        List<LeaveRecord> approvedHistoryList = fetchApprovedLeaveHistory();

        if (approvedHistoryList.isEmpty()) {
            recyclerLeaveHistory.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerLeaveHistory.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);

            LeaveHistoryAdapter adapter = new LeaveHistoryAdapter(approvedHistoryList);
            recyclerLeaveHistory.setAdapter(adapter);
        }
    }

    private List<LeaveRecord> fetchApprovedLeaveHistory() {
        List<LeaveRecord> allApplications = getAllApplications(); // Fetch all records from database/source
        List<LeaveRecord> approvedList = new ArrayList<>();

        // Filter: ONLY add applications that have been APPROVED
        for (LeaveRecord record : allApplications) {
            if ("Approved".equalsIgnoreCase(record.getStatus())) {
                approvedList.add(record);
            }
        }

        return approvedList;
    }

    private List<LeaveRecord> getAllApplications() {
        List<LeaveRecord> list = new ArrayList<>();

        // Example records for testing:
        // list.add(new LeaveRecord("Medical Leave", "10/08/2026", "12/08/2026", "09/08/2026", "Approved")); // Shown in History
        // list.add(new LeaveRecord("Personal Leave", "20/08/2026", "22/08/2026", "18/08/2026", "Pending"));  // Filtered out (shown in Check Status)

        return list;
    }
}