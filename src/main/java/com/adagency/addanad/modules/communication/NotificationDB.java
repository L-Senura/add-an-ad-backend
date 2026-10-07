package com.adagency.addanad.modules.communication;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * NotificationDB: JPA entity representing communication notifications.
 *
 * Used by the Observer design pattern to record events (e.g. Chat messages,
 * reviews, system announcements) dispatched to Clients or Administrators.
 */
@Entity
@Table(name = "NotificationDB")
public class NotificationDB {

    public static final String TYPE_CHAT_MESSAGE = "CHAT_MESSAGE";
    public static final String TYPE_CHAT_REPLY = "CHAT_REPLY";
    public static final String TYPE_CLIENT_REVIEW = "CLIENT_REVIEW";
    public static final String TYPE_PUBLIC_REVIEW = "PUBLIC_REVIEW";
    public static final String TYPE_SYSTEM_ALERT = "SYSTEM_ALERT";

    public static final String ROLE_CLIENT = "CLIENT";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_ALL = "ALL";
    public static final String ROLE_VISITOR = "VISITOR";
    public static final String ROLE_SYSTEM = "SYSTEM";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificationID")
    private Long notificationID;

    @Column(name = "recipientRole", nullable = false)
    private String recipientRole;

    @Column(name = "recipientID")
    private Long recipientID;

    @Column(name = "senderRole")
    private String senderRole;

    @Column(name = "senderID")
    private Long senderID;

    @Column(name = "senderName")
    private String senderName;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "notificationType")
    private String notificationType;

    @Column(name = "relatedEntityId")
    private Long relatedEntityId;

    @Column(name = "isRead", nullable = false)
    private Boolean isRead = false;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    public NotificationDB() {
    }

    public NotificationDB(String recipientRole, Long recipientID, String senderRole,
                          Long senderID, String senderName, String title,
                          String message, String notificationType, Long relatedEntityId) {
        this.recipientRole = recipientRole;
        this.recipientID = recipientID;
        this.senderRole = senderRole;
        this.senderID = senderID;
        this.senderName = senderName;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.relatedEntityId = relatedEntityId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isRead == null) {
            this.isRead = false;
        }
    }

    public Long getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(Long notificationID) {
        this.notificationID = notificationID;
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public void setRecipientRole(String recipientRole) {
        this.recipientRole = recipientRole;
    }

    public Long getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(Long recipientID) {
        this.recipientID = recipientID;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public Long getSenderID() {
        return senderID;
    }

    public void setSenderID(Long senderID) {
        this.senderID = senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
