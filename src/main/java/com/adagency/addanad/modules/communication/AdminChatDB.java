package com.adagency.addanad.modules.communication;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// @Entity: Marks this class as a JPA entity mapped to the database table.
@Entity
// @Table: Specifies the name of the database table ("AdminChatDB").
@Table(name = "AdminChatDB")
public class AdminChatDB {

    // Primary Key for AdminChatDB
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adminMessageID")
    private Long adminMessageID;

    // Unique ID of the admin sending or replying to the message (foreign key referencing admin table)
    @Column(name = "adminID", nullable = false)
    private Long adminID;

    // Unique ID of the client (foreign key referencing client table)
    @Column(name = "clientID", nullable = false)
    private Long clientID;

    // References the specific client message in ClientChatDB (foreign key referencing ClientChatDB.clientMessageID)
    @Column(name = "clientMessageID")
    private Long clientMessageID;

    // Stores message or reply sent by the admin
    @Column(name = "adminMessage", columnDefinition = "TEXT")
    private String adminMessage;

    // Timestamp when the admin message was sent
    @Column(name = "adminMessageTime")
    private LocalDateTime adminMessageTime;

    // Default no-args constructor required by JPA
    public AdminChatDB() {
    }

    public AdminChatDB(Long adminID, Long clientID, Long clientMessageID, String adminMessage) {
        this.adminID = adminID;
        this.clientID = clientID;
        this.clientMessageID = clientMessageID;
        this.adminMessage = adminMessage;
        this.adminMessageTime = LocalDateTime.now();
    }

    // Automatically set message time before inserting into database if not provided
    @PrePersist
    protected void onCreate() {
        if (this.adminMessageTime == null) {
            this.adminMessageTime = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getAdminMessageID() {
        return adminMessageID;
    }

    public void setAdminMessageID(Long adminMessageID) {
        this.adminMessageID = adminMessageID;
    }

    public Long getAdminID() {
        return adminID;
    }

    public void setAdminID(Long adminID) {
        this.adminID = adminID;
    }

    public Long getClientID() {
        return clientID;
    }

    public void setClientID(Long clientID) {
        this.clientID = clientID;
    }

    public Long getClientMessageID() {
        return clientMessageID;
    }

    public void setClientMessageID(Long clientMessageID) {
        this.clientMessageID = clientMessageID;
    }

    public String getAdminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }

    public LocalDateTime getAdminMessageTime() {
        return adminMessageTime;
    }

    public void setAdminMessageTime(LocalDateTime adminMessageTime) {
        this.adminMessageTime = adminMessageTime;
    }
}
