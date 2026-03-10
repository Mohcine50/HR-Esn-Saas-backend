package com.shegami.hr_saas.modules.mission.enums;

public enum ActivityType {
    // Mission lifecycle
    MISSION_CREATED,
    MISSION_UPDATED,
    MISSION_DELETED,

    // Field changes
    STATUS_CHANGED,
    PRIORITY_CHANGED,
    TITLE_CHANGED,
    DESCRIPTION_CHANGED,
    DATE_CHANGED,

    // Relations
    CONSULTANT_ASSIGNED,
    CONSULTANT_REMOVED,
    LABEL_ADDED,
    LABEL_REMOVED,
    PROJECT_ASSIGNED,
    CLIENT_ASSIGNED,
    ATTACHMENT_ADDED,
    ATTACHMENT_REMOVED,

    // Comments
    COMMENT_ADDED,
    COMMENT_EDITED,
    COMMENT_DELETED,
}