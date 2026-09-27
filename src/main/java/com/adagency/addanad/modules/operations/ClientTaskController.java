package com.adagency.addanad.modules.operations;

import com.adagency.addanad.modules.client.ClientDB;
import com.adagency.addanad.modules.client.ClientRepo;
import com.adagency.addanad.modules.operations.dto.ClientTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Client-facing Task operations.
 * Allows registered clients to give / submit advertising tasks to the agency,
 * view the progress of their submitted tasks, and track coordination by the Task Coordinator.
 */
@RestController
@RequestMapping("/api/client_tasks")
public class ClientTaskController {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired(required = false)
    private ClientRepo clientRepo;

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
     * Retrieve all tasks given/submitted by a specific client.
     *
     * Example: GET /api/client_tasks/client/1
     */
    @GetMapping("/client/{clientId}")
    public List<TaskDB> getTasksByClient(@PathVariable Long clientId) {
        return taskRepo.findByClientId(clientId);
    }

    /**
     * Retrieve all tasks submitted by a client filtered by status.
     *
     * Example: GET /api/client_tasks/client/1/status/Completed
     */
    @GetMapping("/client/{clientId}/status/{status}")
    public List<TaskDB> getTasksByClientAndStatus(@PathVariable Long clientId, @PathVariable String status) {
        return taskRepo.findByClientIdAndStatus(clientId, status);
    }

    /**
     * View details and current progress of a specific task submitted by a client.
     *
     * Example: GET /api/client_tasks/5
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
     * Client cancels a task if it has not yet been completed.
     *
     * Example: PUT /api/client_tasks/5/cancel
     */
    @PutMapping("/{taskId}/cancel")
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
        task.setStatus("Cancelled");
        TaskDB saved = taskRepo.save(task);
        return ResponseEntity.ok(saved);
    }
}
