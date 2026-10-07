package com.adagency.addanad.modules.operations.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object used by the Task Coordinator when coordinating/assigning
 * a task given by a client to an employee (Production Staff).
 */
public class TaskCoordinationRequest {

    // ID of the Task Coordinator (admin)
    private Long coordinatorId;

    // ID of the employee (Production Staff) chosen to perform the task
    private Long employeeId;

    // Optional override/assignment of deadline by the coordinator
    private LocalDate deadline;

    // Priority level assigned or updated by the coordinator ("LOW", "MEDIUM", "HIGH", "URGENT")
    private String priority;

    // Specific coordination instructions/guidance from the coordinator to the employee
    private String coordinatorNotes;

    public TaskCoordinationRequest() {
    }

    public TaskCoordinationRequest(Long coordinatorId, Long employeeId, LocalDate deadline, String priority, String coordinatorNotes) {
        this.coordinatorId = coordinatorId;
        this.employeeId = employeeId;
        this.deadline = deadline;
        this.priority = priority;
        this.coordinatorNotes = coordinatorNotes;
    }

    public Long getCoordinatorId() {
        return coordinatorId;
    }

    public void setCoordinatorId(Long coordinatorId) {
        this.coordinatorId = coordinatorId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCoordinatorNotes() {
        return coordinatorNotes;
    }

    public void setCoordinatorNotes(String coordinatorNotes) {
        this.coordinatorNotes = coordinatorNotes;
    }
}
