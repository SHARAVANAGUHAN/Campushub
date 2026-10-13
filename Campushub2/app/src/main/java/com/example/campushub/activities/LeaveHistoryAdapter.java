package com.example.campushub.activities;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campushub.R;
import com.example.campushub.models.LeaveRecord;

import java.util.List;

public class LeaveHistoryAdapter extends RecyclerView.Adapter<LeaveHistoryAdapter.ViewHolder> {

    private final List<LeaveRecord> leaveList;

    public LeaveHistoryAdapter(List<LeaveRecord> leaveList) {
        this.leaveList = leaveList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leave_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaveRecord leave = leaveList.get(position);

        holder.txtLeaveType.setText(leave.getLeaveType());
        holder.txtSentDate.setText("Sent to HOD on: " + leave.getSentDate());
        holder.txtLeaveDuration.setText("Duration: " + leave.getFromDate() + " to " + leave.getToDate());

        holder.txtStatus.setText(leave.getStatus());
        if ("Approved".equalsIgnoreCase(leave.getStatus())) {
            holder.txtStatus.setTextColor(Color.parseColor("#15803D")); // Green
        } else if ("Pending".equalsIgnoreCase(leave.getStatus())) {
            holder.txtStatus.setTextColor(Color.parseColor("#D97706")); // Orange
        } else {
            holder.txtStatus.setTextColor(Color.parseColor("#DC2626")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return leaveList != null ? leaveList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtLeaveType, txtSentDate, txtLeaveDuration, txtStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLeaveType = itemView.findViewById(R.id.txtLeaveType);
            txtSentDate = itemView.findViewById(R.id.txtSentDate);
            txtLeaveDuration = itemView.findViewById(R.id.txtLeaveDuration);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}