package com.adagency.addanad.modules.client;

import jakarta.persistence.*;

/**
 * Entity representing the Client.
 * Inherits common attributes (email, password, firstName, lastName, contactNumber) from User.
 * Has specific attributes: clientID, companyName, companyDetails, status.
 */
@Entity
@Table(name = "ClientDB")
public class ClientDB extends User {

    // Primary Key for ClientDB
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clientID")
    private Long clientID;

    // Company name for advertising agency client
    @Column(name = "company_name", nullable = false)
    private String companyName;

    // Brief introduction about the company
    @Column(name = "company_details", columnDefinition = "TEXT")
    private String companyDetails;

    // Registration status: "PENDING", "ACCEPTED", "REJECTED"
    // Default is "PENDING" until an Admin reviews and accepts
    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    public ClientDB() {
        super();
    }

    public ClientDB(String email, String password, String firstName, String lastName, String contactNumber,
                    String companyName, String companyDetails) {
        super(email, password, firstName, lastName, contactNumber);
        this.companyName = companyName;
        this.companyDetails = companyDetails;
        this.status = "PENDING";
    }

    public ClientDB(String email, String password, String firstName, String lastName, String contactNumber,
                    String companyName, String companyDetails, String status) {
        super(email, password, firstName, lastName, contactNumber);
        this.companyName = companyName;
        this.companyDetails = companyDetails;
        this.status = status;
    }

    // Getters and Setters
    public Long getClientID() {
        return clientID;
    }

    public void setClientID(Long clientID) {
        this.clientID = clientID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDetails() {
        return companyDetails;
    }

    public void setCompanyDetails(String companyDetails) {
        this.companyDetails = companyDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
