package com.adagency.addanad.modules.communication;

import jakarta.persistence.*;

// @Entity: Marks this class as a JPA entity mapped to a database table.
@Entity
// @Table: Specifies the name of the database table ("ChatDB").
@Table(name = "ChatDB")
public class ChatDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique ID of the client sending or receiving the message
    private Long clientId;

    // Unique ID of the admin sending or replying to the message
    private Long adminId;

    // Stores message sent by the client
    private String clientMessage;

    // Stores message or reply sent by the admin
    private String adminMessage;

    // Default no-args constructor required by JPA
    public ChatDB() {
    }

    // Getters and Setters for data access and JSON serialization/deserialization
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getClientMessage() {
        return clientMessage;
    }

    public void setClientMessage(String clientMessage) {
        this.clientMessage = clientMessage;
    }

    public String getAdminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }
}


