package com.adagency.addanad.modules.communication;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// @Entity: Marks this class as a JPA entity mapped to the database table.
@Entity
// @Table: Specifies the name of the database table ("ClientChatDB").
@Table(name = "ClientChatDB")
public class ClientChatDB {

    // Primary Key for ClientChatDB
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clientMessageID")
    private Long clientMessageID;

    // Unique ID of the client sending the message (foreign key referencing precreated client table)
    @Column(name = "clientID", nullable = false)
    private Long clientID;

    // Stores message sent by the client
    @Column(name = "clientMessage", columnDefinition = "TEXT")
    private String clientMessage;

    // Timestamp when the message was sent
    @Column(name = "clientMessageTime")
    private LocalDateTime clientMessageTime;

    // Default no-args constructor required by JPA
    public ClientChatDB() {
    }

    public ClientChatDB(Long clientID, String clientMessage) {
        this.clientID = clientID;
        this.clientMessage = clientMessage;
        this.clientMessageTime = LocalDateTime.now();
    }

    // Automatically set message time before inserting into database if not provided
    @PrePersist
    protected void onCreate() {
        if (this.clientMessageTime == null) {
            this.clientMessageTime = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getClientMessageID() {
        return clientMessageID;
    }

    public void setClientMessageID(Long clientMessageID) {
        this.clientMessageID = clientMessageID;
    }

    public Long getClientID() {
        return clientID;
    }

    public void setClientID(Long clientID) {
        this.clientID = clientID;
    }

    public String getClientMessage() {
        return clientMessage;
    }

    public void setClientMessage(String clientMessage) {
        this.clientMessage = clientMessage;
    }

    public LocalDateTime getClientMessageTime() {
        return clientMessageTime;
    }

    public void setClientMessageTime(LocalDateTime clientMessageTime) {
        this.clientMessageTime = clientMessageTime;
    }
}
