package com.adagency.addanad.modules.operations;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a Task in the Operations module.
 * Tasks are given/requested by clients (or derived from campaigns),
 * coordinated by the Task Coordinator (Task_Manager admin),
 * and executed by employees (Production Staff).
 */
@Entity
@Table(name = "TaskDB")
public class TaskDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID of the client who gave/submitted this task
    @Column(name = "clientId")
    private Long clientId;

    // Name or company name of the client who submitted the task
    @Column(name = "clientName")
    private String clientName;

    // Optional ID of the campaign this task is associated with
    @Column(name = "campaignId")
    private Long campaignId;

    // Title / summary of the task given by the client
    @Column(name = "taskTitle")
    private String taskTitle;

    // Detailed description / requirements given by the client
    @Column(name = "taskDetails", columnDefinition = "TEXT")
    private String taskDetails;

    // Category of the task (e.g. "Graphic Design", "Video Production", "Copywriting", "Social Media")
    @Column(name = "taskCategory")
    private String taskCategory;

    // Task priority: "LOW", "MEDIUM", "HIGH", "URGENT"
    @Column(name = "priority")
    private String priority = "MEDIUM";

    // ID of the Operations & Task Coordinator (admin) who coordinated/assigned this task
    @Column(name = "taskManagerId")
    private Long taskManagerId;

    // Notes and instructions provided by the Task Coordinator to the assigned employee
    @Column(name = "coordinatorNotes", columnDefinition = "TEXT")
    private String coordinatorNotes;

    // ID of the Production Staff member (employee) this task is assigned to
    @Column(name = "employeeId")
    private Long employeeId;

    // Name of the assigned employee
    @Column(name = "employeeName")
    private String employeeName;

    // Task status: "PENDING_COORDINATION", "To Do", "In Progress", "Completed", "Cancelled"
    @Column(name = "status")
    private String status = "PENDING_COORDINATION";

    // Deadline requested by the client
    @Column(name = "clientDeadline")
    private LocalDate clientDeadline;

    // Deadline assigned/set by the Task Coordinator
    @Column(name = "deadline")
    private LocalDate deadline;

    // Timestamp when the task was submitted by the client
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    // Timestamp when the Task Coordinator assigned the task to an employee
    @Column(name = "coordinatedAt")
    private LocalDateTime coordinatedAt;

    // Timestamp when the employee completed the task
    @Column(name = "completedAt")
    private LocalDateTime completedAt;

    // Default no-args constructor required by JPA
    public TaskDB() {
    }

    public TaskDB(Long clientId, String clientName, String taskTitle, String taskDetails, String taskCategory, LocalDate clientDeadline) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.taskTitle = taskTitle;
        this.taskDetails = taskDetails;
        this.taskCategory = taskCategory;
        this.clientDeadline = clientDeadline;
        this.status = "PENDING_COORDINATION";
        this.priority = "MEDIUM";
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null || this.status.trim().isEmpty()) {
            this.status = "PENDING_COORDINATION";
        }
        if (this.priority == null || this.priority.trim().isEmpty()) {
            this.priority = "MEDIUM";
        }
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

    public Long getTaskManagerId() {
        return taskManagerId;
    }

    public void setTaskManagerId(Long taskManagerId) {
        this.taskManagerId = taskManagerId;
    }

    public String getCoordinatorNotes() {
        return coordinatorNotes;
    }

    public void setCoordinatorNotes(String coordinatorNotes) {
        this.coordinatorNotes = coordinatorNotes;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getClientDeadline() {
        return clientDeadline;
    }

    public void setClientDeadline(LocalDate clientDeadline) {
        this.clientDeadline = clientDeadline;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCoordinatedAt() {
        return coordinatedAt;
    }

    public void setCoordinatedAt(LocalDateTime coordinatedAt) {
        this.coordinatedAt = coordinatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
