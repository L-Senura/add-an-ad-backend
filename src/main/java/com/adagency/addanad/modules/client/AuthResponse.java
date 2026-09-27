package com.adagency.addanad.modules.client.dto;

public class AuthResponse {
    private boolean success;
    private String message;
    private String userType; // "CLIENT" or "ADMIN"
    private Long id;         // clientID or adminID
    private String email;
    private String firstName;
    private String lastName;
    private String statusOrType; // client status ("ACCEPTED") or adminType ("Marketing_Analyst", etc.)

    public AuthResponse() {
    }

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, String userType, Long id, String email, String firstName, String lastName, String statusOrType) {
        this.success = success;
        this.message = message;
        this.userType = userType;
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.statusOrType = statusOrType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getStatusOrType() {
        return statusOrType;
    }

    public void setStatusOrType(String statusOrType) {
        this.statusOrType = statusOrType;
    }
}
