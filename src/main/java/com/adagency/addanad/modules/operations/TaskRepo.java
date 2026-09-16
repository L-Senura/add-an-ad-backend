package com.adagency.addanad.modules.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository interface for TaskDB operations.
 * Supports task coordination workflows among clients, task coordinators, and employees.
 */
@Repository
public interface TaskRepo extends JpaRepository<TaskDB, Long> {

    /**
     * Query to find all tasks belonging to a specific campaign.
     * Spring Data JPA automatically derives: SELECT * FROM TaskDB WHERE campaignId = ?
     *
     * @param campaignId The ID of the campaign
     * @return List of TaskDB records under the given campaign
     */
    List<TaskDB> findByCampaignId(Long campaignId);

    /**
     * Query to find all tasks assigned to a specific employee (Production Staff).
     * Spring Data JPA automatically derives: SELECT * FROM TaskDB WHERE employeeId = ?
     *
     * @param employeeId The ID of the employee
     * @return List of TaskDB records assigned to the given employee
     */
    List<TaskDB> findByEmployeeId(Long employeeId);

    /**
     * Query to find all tasks assigned to an employee filtered by status.
     *
     * @param employeeId The ID of the employee
     * @param status     Task status (e.g. "In Progress", "Completed")
     * @return List of matching TaskDB records
     */
    List<TaskDB> findByEmployeeIdAndStatus(Long employeeId, String status);

    /**
     * Query to find all tasks created/managed by a specific Task Manager (Coordinator).
     * Spring Data JPA automatically derives: SELECT * FROM TaskDB WHERE taskManagerId = ?
     *
     * @param taskManagerId The ID of the Task Manager
     * @return List of TaskDB records created by the given Task Manager
     */
    List<TaskDB> findByTaskManagerId(Long taskManagerId);

    /**
     * Query to find all tasks managed by a Task Coordinator with a specific status.
     *
     * @param taskManagerId The ID of the Task Coordinator
     * @param status        Task status
     * @return List of matching TaskDB records
     */
    List<TaskDB> findByTaskManagerIdAndStatus(Long taskManagerId, String status);

    /**
     * Query to find all tasks given/submitted by a specific client.
     *
     * @param clientId The ID of the client
     * @return List of TaskDB records submitted by the client
     */
    List<TaskDB> findByClientId(Long clientId);

    /**
     * Query to find tasks submitted by a specific client filtered by status.
     *
     * @param clientId The ID of the client
     * @param status   Task status
     * @return List of matching TaskDB records
     */
    List<TaskDB> findByClientIdAndStatus(Long clientId, String status);

    /**
     * Query to find all tasks that have not yet been assigned to an employee.
     * Used by the Task Coordinator to see tasks given by clients awaiting assignment.
     *
     * @return List of unassigned TaskDB records
     */
    List<TaskDB> findByEmployeeIdIsNull();

    /**
     * Query to find all tasks with a specific status (e.g. "PENDING_COORDINATION", "In Progress", "Completed").
     *
     * @param status Task status
     * @return List of matching TaskDB records
     */
    List<TaskDB> findByStatus(String status);
}
