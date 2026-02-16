package com.shegami.hr_saas.modules.notifications.enums;

import lombok.Getter;

@Getter
public enum NotificationStatus {

    UNREAD("Unread", "New notification"),

    READ("Read", "Notification has been read"),

    ARCHIVED("Archived", "Notification has been archived"),

    DELETED("Deleted", "Notification has been deleted");

    private final String displayName;
    private final String description;

    NotificationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}
