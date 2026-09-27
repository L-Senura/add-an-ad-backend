package com.adagency.addanad.modules.client;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Superclass representing common User attributes.
 * Inherited by Client and Admin entities.
 */
@MappedSuperclass
public abstract class
User {

    @Column(name = "email", nullable = false)
    protected String email;

    @Column(name = "password", nullable = false)
    protected String password;

    @Column(name = "first_name", nullable = false)
    protected String firstName;

    @Column(name = "last_name", nullable = false)
    protected String lastName;

    @Column(name = "contact_number")
    protected String contactNumber;

    public User() {
    }

    public User(String email, String password, String firstName, String lastName, String contactNumber) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
