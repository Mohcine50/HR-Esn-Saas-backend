package com.shegami.hr_saas.modules.notifications.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    // ==================== MISSION NOTIFICATIONS ====================
    CONSULTANT_ASSIGNED("Mission Assigned", "You've been assigned to a mission"),
    MISSION_STATUS_CHANGED("Mission Status Updated", "A mission status has changed"),
    CONSULTANT_REMOVED_FROM_MISSION("Removed from Mission", "You've been removed from a mission"),
    MISSION_DEADLINE_APPROACHING("Deadline Approaching", "A mission deadline is approaching"),

    // ==================== TIMESHEET NOTIFICATIONS ====================
    TIMESHEET_SUBMITTED("Timesheet Submitted", "New timesheet submitted for approval"),
    TIMESHEET_APPROVED("Timesheet Approved", "Your timesheet has been approved"),
    TIMESHEET_REJECTED("Timesheet Rejected", "Your timesheet has been rejected"),

    // ==================== COMMENT NOTIFICATIONS ====================
    MISSION_COMMENT_ADDED("New Comment on Mission", "A new comment was added to a mission"),
    MISSION_COMMENT_MENTION("Mentioned on Mission", "You were mentioned in a mission comment"),

    // ==================== INVOICE & BILLING NOTIFICATIONS ====================
    INVOICE_GENERATED("Invoice Generated", "A new invoice has been generated"),
    INVOICE_OVERDUE("Invoice Overdue", "An invoice is overdue for payment"),
    PAYMENT_RECORDED("Payment Recorded", "A payment has been recorded for an invoice"),

    // ==================== PROJECT NOTIFICATIONS ====================
    PROJECT_CONSULTANT_ASSIGNED("Project Assigned", "You've been assigned to a project"),
    PROJECT_STATUS_CHANGED("Project Status Updated", "A project status has changed"),

    // ==================== INVITATION & HR NOTIFICATIONS ====================
    INVITATION_SENT("Invitation Sent", "You've been invited to join the platform"),
    EMPLOYEE_ONBOARDED("Employee Onboarded", "A new employee has completed onboarding"),

    // ==================== SYSTEM NOTIFICATIONS ====================
    SYSTEM_ANNOUNCEMENT("System Announcement", "Important system announcement"),
    SYSTEM_UPDATE("System Update", "New features and updates available");

    private final String displayName;
    private final String defaultTitle;

    NotificationType(String displayName, String defaultTitle) {
        this.displayName = displayName;
        this.defaultTitle = defaultTitle;
    }

    public String getColor() {
        return switch (this) {
            case CONSULTANT_ASSIGNED, PROJECT_CONSULTANT_ASSIGNED -> "#3B82F6"; // Blue
            case TIMESHEET_APPROVED, PAYMENT_RECORDED -> "#10B981"; // Green
            case MISSION_COMMENT_MENTION -> "#8B5CF6"; // Purple
            case TIMESHEET_REJECTED, INVOICE_OVERDUE, MISSION_DEADLINE_APPROACHING -> "#EF4444"; // Red
            case INVOICE_GENERATED -> "#F59E0B"; // Amber
            default -> "#6B7280"; // Gray
        };
    }

    public String getIcon() {
        return switch (this) {
            case CONSULTANT_ASSIGNED, PROJECT_CONSULTANT_ASSIGNED -> "user-plus";
            case MISSION_STATUS_CHANGED, PROJECT_STATUS_CHANGED -> "refresh-cw";
            case TIMESHEET_SUBMITTED -> "clock";
            case TIMESHEET_APPROVED -> "check-circle";
            case TIMESHEET_REJECTED -> "x-circle";
            case MISSION_COMMENT_ADDED -> "message-square";
            case MISSION_COMMENT_MENTION -> "at-sign";
            case INVOICE_GENERATED -> "file-text";
            case INVOICE_OVERDUE -> "alert-triangle";
            case PAYMENT_RECORDED -> "credit-card";
            default -> "bell";
        };
    }
}
