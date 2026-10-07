package com.adagency.addanad.modules.operations.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object for a client submitting/giving a task to the agency.
 */
public class ClientTaskRequest {

    private Long clientId;
    private String clientName;
    private Long campaignId;
    private String taskTitle;
    private String taskDetails;
    private String taskCategory;
    private String priority;
    private LocalDate clientDeadline;

    public ClientTaskRequest() {
    }

    public ClientTaskRequest(Long clientId, String clientName, Long campaignId, String taskTitle,
                             String taskDetails, String taskCategory, String priority, LocalDate clientDeadline) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.campaignId = campaignId;
        this.taskTitle = taskTitle;
        this.taskDetails = taskDetails;
        this.taskCategory = taskCategory;
        this.priority = priority;
        this.clientDeadline = clientDeadline;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(String taskDetails) {
        this.taskDetails = taskDetails;
    }

    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getClientDeadline() {
        return clientDeadline;
    }

    public void setClientDeadline(LocalDate clientDeadline) {
        this.clientDeadline = clientDeadline;
    }
}
