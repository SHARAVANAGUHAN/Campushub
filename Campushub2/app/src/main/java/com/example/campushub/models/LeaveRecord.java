package com.example.campushub.models;

public class LeaveRecord {
    private String id;
    private String leaveType;
    private String fromDate;
    private String toDate;
    private String sentDate;
    private String status; // "Approved", "Pending", "Rejected"

    public LeaveRecord() {
        // Required for Firebase/Database deserialization
    }

    public LeaveRecord(String leaveType, String fromDate, String toDate, String sentDate, String status) {
        this.leaveType = leaveType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.sentDate = sentDate;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLeaveType() { return leaveType; }
    public String getFromDate() { return fromDate; }
    public String getToDate() { return toDate; }
    public String getSentDate() { return sentDate; }
    public String getStatus() { return status; }
}