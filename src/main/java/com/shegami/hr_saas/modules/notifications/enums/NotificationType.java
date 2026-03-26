package com.shegami.hr_saas.modules.notifications.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    // ==================== MISSION NOTIFICATIONS ====================
    MISSION_ASSIGNED("Mission Assigned", "You've been assigned to a mission"),
    MISSION_UPDATED("Mission Updated", "A mission you're working on was updated"),
    MISSION_COMPLETED("Mission Completed", "A mission has been completed"),
    MISSION_COMMENTED("New Comment", "Someone commented on a mission"),

    // ==================== TIMESHEET NOTIFICATIONS ====================
    TIMESHEET_SUBMITTED("Timesheet Submitted", "Your timesheet has been submitted"),
    TIMESHEET_APPROVED("Timesheet Approved", "Your timesheet has been approved"),
    TIMESHEET_REJECTED("Timesheet Rejected", "Your timesheet has been rejected"),
    TIMESHEET_REMINDER("Timesheet Reminder", "Don't forget to submit your timesheet"),

    // ==================== MENTION NOTIFICATIONS ====================
    MENTION("Mentioned", "You were mentioned in a comment"),

    // ==================== COMMENT NOTIFICATIONS ====================
    COMMENT_ADDED("New Comment", "Someone commented"),
    COMMENT_REPLY("Comment Reply", "Someone replied to your comment"),

    // ==================== PROJECT NOTIFICATIONS ====================
    PROJECT_ASSIGNED("Project Assigned", "You've been assigned to a project"),
    PROJECT_UPDATED("Project Updated", "A project you're on was updated"),
    PROJECT_DEADLINE_APPROACHING("Project Deadline", "Project deadline is approaching"),

    // ==================== INVITATION NOTIFICATIONS ====================
    INVITATION_RECEIVED("Invitation", "You've been invited to join"),
    INVITATION_ACCEPTED("Invitation Accepted", "Your invitation was accepted"),

    // ==================== SYSTEM NOTIFICATIONS ====================
    SYSTEM_ANNOUNCEMENT("System Announcement", "Important system announcement"),
    SYSTEM_MAINTENANCE("Maintenance", "Scheduled system maintenance"),
    SYSTEM_UPDATE("System Update", "New features and updates available"),

    // ==================== ACCOUNT NOTIFICATIONS ====================
    ACCOUNT_UPDATED("Account Updated", "Your account has been updated"),
    PASSWORD_CHANGED("Password Changed", "Your password was changed"),
    EMAIL_CHANGED("Email Changed", "Your email address was changed"),

    // ==================== INVOICE NOTIFICATIONS ====================
    INVOICE_GENERATED("Invoice Generated", "An invoice has been generated"),
    INVOICE_REMINDER("Invoice Reminder", "Don't forget to pay your invoice"),
    INVOICE_PAID("Invoice Paid", "Your invoice has been paid"),
    INVOICE_OVERDUE("Invoice Overdue", "Your invoice is overdue");

    private final String displayName;
    private final String defaultTitle;

    NotificationType(String displayName, String defaultTitle) {
        this.displayName = displayName;
        this.defaultTitle = defaultTitle;
    }

    public String getColor() {
        return switch (this) {
            case MISSION_ASSIGNED, PROJECT_ASSIGNED, INVOICE_GENERATED -> "#3B82F6"; // Blue
            case MISSION_COMPLETED, TIMESHEET_APPROVED, INVOICE_PAID -> "#10B981"; // Green
            case TIMESHEET_REMINDER, INVOICE_REMINDER -> "#F59E0B"; // Orange
            case MENTION, COMMENT_REPLY -> "#8B5CF6"; // Purple
            case INVOICE_OVERDUE -> "#EF4444"; // Red
            default -> "#6B7280"; // Gray
        };
    }
}
