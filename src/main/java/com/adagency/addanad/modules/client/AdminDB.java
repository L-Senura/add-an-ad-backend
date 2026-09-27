package com.adagency.addanad.modules.client;

import jakarta.persistence.*;

/**
 * Entity representing the Admin.
 * Inherits common attributes (email, password, firstName, lastName, contactNumber) from User.
 * Has adminID as primary key.
 * Divided into admin types: "Marketing_Analyst", "Task_Manager", "Finance_Officer", "Communication_Executive".
 */
@Entity
@Table(name = "AdminDB")
public class AdminDB extends User {

    // Primary Key for AdminDB
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adminID")
    private Long adminID;

    // Admin type/role: "Marketing_Analyst", "Task_Manager", "Finance_Officer", "Communication_Executive"
    @Column(name = "admin_type", nullable = false)
    private String adminType;

    public AdminDB() {
        super();
    }

    public AdminDB(String email, String password, String firstName, String lastName, String contactNumber, String adminType) {
        super(email, password, firstName, lastName, contactNumber);
        this.adminType = adminType;
    }

    public Long getAdminID() {
        return adminID;
    }

    public void setAdminID(Long adminID) {
        this.adminID = adminID;
    }

    public String getAdminType() {
        return adminType;
    }

    public void setAdminType(String adminType) {
        this.adminType = adminType;
    }
}
