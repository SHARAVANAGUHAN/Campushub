package com.example.campushub.activities;

public class LeaveRecord {
    private String id;
    private String leaveType;
    private String fromDate;
    private String toDate;
    private String sentDate; // Date sent to HOD
    private String status;   // "Approved", "Pending", "Rejected"
    private String reason;

    public LeaveRecord(String id, String leaveType, String fromDate, String toDate, String sentDate, String status, String reason) {
        this.id = id;
        this.leaveType = leaveType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.sentDate = sentDate;
        this.status = status;
        this.reason = reason;
    }

    public String getId() { return id; }
    public String getLeaveType() { return leaveType; }
    public String getFromDate() { return fromDate; }
    public String getToDate() { return toDate; }
    public String getSentDate() { return sentDate; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
}