package com.adagency.addanad.modules.operations;

import jakarta.persistence.*;

/**
 * Entity representing an Employee / Production Staff member in the Operations module.
 * The Task Coordinator inspects employee availability, role/specialization,
 * and current workload when coordinating tasks given by clients.
 */
@Entity
@Table(name = "ProductionStaffDB")
public class ProductionStaffDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Optional reference to a linked Admin_user record if applicable
    @Column(name = "adminId")
    private Long adminId;

    // Staff member / employee's full name
    @Column(name = "name", nullable = false)
    private String name;

    // Employee's email address
    @Column(name = "email")
    private String email;

    // Staff role or specialization (e.g. "Graphic Designer", "Video Editor", "Content Writer", "Web Developer")
    @Column(name = "role")
    private String role;

    // Employee contact number
    @Column(name = "contactNumber")
    private String contactNumber;

    // Current workload count (number of active assigned tasks)
    @Column(name = "currentWorkload")
    private int currentWorkload = 0;

    // Maximum concurrent workload capacity (default 5 tasks)
    @Column(name = "maxWorkload")
    private int maxWorkload = 5;

    // Availability status: "AVAILABLE", "BUSY", "ON_LEAVE"
    @Column(name = "status")
    private String status = "AVAILABLE";

    // Default no-args constructor required by JPA
    public ProductionStaffDB() {
    }

    public ProductionStaffDB(String name, String email, String role, String contactNumber) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.contactNumber = contactNumber;
        this.currentWorkload = 0;
        this.maxWorkload = 5;
        this.status = "AVAILABLE";
    }

    // Workload helper methods
    public void incrementWorkload() {
        this.currentWorkload++;
        if (this.currentWorkload >= this.maxWorkload) {
            this.status = "BUSY";
        }
    }

    public void decrementWorkload() {
        if (this.currentWorkload > 0) {
            this.currentWorkload--;
        }
        if (this.currentWorkload < this.maxWorkload && !"ON_LEAVE".equalsIgnoreCase(this.status)) {
            this.status = "AVAILABLE";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public int getCurrentWorkload() {
        return currentWorkload;
    }

    public void setCurrentWorkload(int currentWorkload) {
        this.currentWorkload = currentWorkload;
        if (this.currentWorkload >= this.maxWorkload) {
            this.status = "BUSY";
        } else if (!"ON_LEAVE".equalsIgnoreCase(this.status)) {
            this.status = "AVAILABLE";
        }
    }

    public int getMaxWorkload() {
        return maxWorkload;
    }

    public void setMaxWorkload(int maxWorkload) {
        this.maxWorkload = maxWorkload;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
