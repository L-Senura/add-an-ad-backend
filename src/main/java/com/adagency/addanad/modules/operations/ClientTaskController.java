package com.adagency.addanad.modules.operations;

import com.adagency.addanad.modules.client.ClientDB;
import com.adagency.addanad.modules.client.ClientRepo;
import com.adagency.addanad.modules.operations.dto.ClientTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Client-facing Task operations.
 * Allows registered clients to give / submit advertising tasks to the agency,
 * edit tasks before they are assigned to an employee by admin/coordinator,
 * view the progress of their submitted tasks, and track coordination by the Task Coordinator.
 */
@RestController
@RequestMapping("/api/client_tasks")
public class ClientTaskController {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired(required = false)
    private ClientRepo clientRepo;

    @Autowired(required = false)
    private ProductionStaffRepo productionStaffRepo;

    /**
     * Client gives / submits a new task to the agency.
     * The task is registered with status "PENDING_COORDINATION" for the Task Coordinator to coordinate among employees.
     *
     * Example: POST /api/client_tasks/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitTask(@RequestBody ClientTaskRequest request) {
        if (request.getClientId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Client ID is required to submit a task.");
        }
        if (request.getTaskTitle() == null || request.getTaskTitle().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Task title is required.");
        }
        if (request.getTaskDetails() == null || request.getTaskDetails().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Task details/requirements are required.");
        }

        // Fetch client name if not provided
        String clientName = request.getClientName();
        if ((clientName == null || clientName.trim().isEmpty()) && clientRepo != null) {
            Optional<ClientDB> clientOpt = clientRepo.findById(request.getClientId());
            if (clientOpt.isPresent()) {
                clientName = clientOpt.get().getCompanyName();
            }
        }

        TaskDB task = new TaskDB();
        task.setClientId(request.getClientId());
        task.setClientName(clientName);
        task.setCampaignId(request.getCampaignId());
        task.setTaskTitle(request.getTaskTitle());
        task.setTaskDetails(request.getTaskDetails());
        task.setTaskCategory(request.getTaskCategory() != null ? request.getTaskCategory() : "General Marketing");
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setClientDeadline(request.getClientDeadline());
        task.setStatus("PENDING_COORDINATION");
        task.setCreatedAt(LocalDateTime.now());

        TaskDB saved = taskRepo.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Client edits their created task before admin/coordinator assigns it to an employee.
     * If the task is already assigned to an employee or in progress/completed/cancelled, editing is prohibited.
     *
     * Example: PUT /api/client_tasks/{taskId} or PUT /api/client_tasks/{taskId}/update
     */
    @PutMapping({"/{taskId}", "/{taskId}/update", "/update/{taskId}"})
    public ResponseEntity<?> updateTask(
            @PathVariable Long taskId,
            @RequestBody ClientTaskRequest request) {

        Optional<TaskDB> taskOpt = taskRepo.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Task with ID " + taskId + " not found.");
        }

        TaskDB task = taskOpt.get();

        // Prevent edit if already assigned to an employee
        if (task.getEmployeeId() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot edit task: It has already been assigned to an employee (" +
                            (task.getEmployeeName() != null ? task.getEmployeeName() : "ID: " + task.getEmployeeId()) + ").");
        }

        // Prevent edit if status indicates assignment or completion/cancellation
        String status = task.getStatus() != null ? task.getStatus().toUpperCase() : "";
        if ("ASSIGNED".equals(status) || "IN PROGRESS".equals(status) || "TO DO".equals(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot edit task: The task has already entered coordination or has been assigned.");
        }
        if ("COMPLETED".equals(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot edit task: It has already been completed.");
        }
        if ("CANCELLED".equals(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot edit task: It has been cancelled.");
        }

        // Validate client ownership if clientId provided
        if (request.getClientId() != null && task.getClientId() != null &&
                !task.getClientId().equals(request.getClientId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to edit this task.");
        }

        // Validate title and details if provided
        if (request.getTaskTitle() != null) {
            if (request.getTaskTitle().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Task title cannot be empty.");
            }
            task.setTaskTitle(request.getTaskTitle().trim());
        }

        if (request.getTaskDetails() != null) {
            if (request.getTaskDetails().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Task details cannot be empty.");
            }
            task.setTaskDetails(request.getTaskDetails().trim());
        }

        if (request.getTaskCategory() != null && !request.getTaskCategory().trim().isEmpty()) {
            task.setTaskCategory(request.getTaskCategory().trim());
        }

        if (request.getPriority() != null && !request.getPriority().trim().isEmpty()) {
            task.setPriority(request.getPriority().trim());
        }

        if (request.getCampaignId() != null) {
            task.setCampaignId(request.getCampaignId());
        }

        task.setClientDeadline(request.getClientDeadline());

        TaskDB saved = taskRepo.save(task);
        return ResponseEntity.ok(saved);
    }

    /**
     * Retrieve all tasks given/submitted by a specific client.
     *
     * Example: GET /api/client_tasks/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public List<TaskDB> getTasksByClient(@PathVariable Long clientId) {
        return taskRepo.findByClientId(clientId);
    }

    /**
     * Retrieve all tasks submitted by a client filtered by status.
     *
     * Example: GET /api/client_tasks/client/{clientId}/status/{status}
     */
    @GetMapping("/client/{clientId}/status/{status}")
    public List<TaskDB> getTasksByClientAndStatus(@PathVariable Long clientId, @PathVariable String status) {
        return taskRepo.findByClientIdAndStatus(clientId, status);
    }

    /**
     * View details and current progress of a specific task submitted by a client.
     *
     * Example: GET /api/client_tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskDetails(@PathVariable Long taskId) {
        Optional<TaskDB> taskOpt = taskRepo.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Task with ID " + taskId + " not found.");
        }
        return ResponseEntity.ok(taskOpt.get());
    }

    /**
     * Client cancels / deletes a task from the database.
     * Permanently deletes the task record from the DB, decrementing employee workload if assigned.
     *
     * Example: DELETE /api/client_tasks/{taskId} or DELETE /api/client_tasks/{taskId}/cancel or PUT /api/client_tasks/{taskId}/cancel
     */
    @RequestMapping(
            value = {"/{taskId}", "/{taskId}/cancel", "/delete/{taskId}", "/cancel/{taskId}"},
            method = {RequestMethod.DELETE, RequestMethod.PUT}
    )
    public ResponseEntity<?> cancelTask(@PathVariable Long taskId) {
        Optional<TaskDB> taskOpt = taskRepo.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Task with ID " + taskId + " not found.");
        }
        TaskDB task = taskOpt.get();
        if ("Completed".equalsIgnoreCase(task.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot cancel a task that has already been completed.");
        }

        // If the task was assigned to an employee and not completed, decrement employee workload
        if (task.getEmployeeId() != null && productionStaffRepo != null) {
            productionStaffRepo.findById(task.getEmployeeId()).ifPresent(emp -> {
                emp.decrementWorkload();
                productionStaffRepo.save(emp);
            });
        }

        // Permanently delete the task from the database
        taskRepo.delete(task);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Task with ID " + taskId + " has been cancelled and permanently deleted from the database.");
        response.put("taskId", taskId);
        return ResponseEntity.ok(response);
    }
}
