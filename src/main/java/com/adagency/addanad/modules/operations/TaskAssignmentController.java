package com.adagency.addanad.modules.operations;

import com.adagency.addanad.modules.operations.dto.TaskCoordinationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller for Task Coordinator operations.
 * The Task Coordinator coordinates tasks among employees (Production Staff)
 * which were submitted / given by clients (or created from campaigns).
 */
@RestController
@RequestMapping("/api/coordinator_tasks")
@CrossOrigin(origins = "*")
public class TaskAssignmentController {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private ProductionStaffRepo productionStaffRepo;

    // =========================================================================
    // Employee (Production Staff) Management & Availability
    // =========================================================================

    /**
     * System displays available Production Staff and their current workload before assignment.
     */
    @GetMapping({"/available_employees", "/employees"})
    public List<ProductionStaffDB> getAvailableEmployees() {
        return productionStaffRepo.findAll();
    }

    /**
     * Coordinator adds/registers a new Production Staff employee to the system.
     */
    @PostMapping("/employees")
    public ResponseEntity<?> addEmployee(@RequestBody ProductionStaffDB employee) {
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Employee name is required.");
        }
        ProductionStaffDB saved = productionStaffRepo.save(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // =========================================================================
    // Coordination of Tasks Given by Clients
    // =========================================================================

    /**
     * Retrieve all tasks given/submitted by clients.
     * Optionally filter by status (e.g. ?status=PENDING_COORDINATION, ?status=In Progress).
     */
    @GetMapping("/client-tasks")
    public List<TaskDB> getAllClientTasks(@RequestParam(required = false) String status) {
        if (status != null && !status.trim().isEmpty()) {
            return taskRepo.findByStatus(status.trim());
        }
        return taskRepo.findAll();
    }

    /**
     * Retrieve all tasks given by clients that are awaiting coordination (unassigned tasks).
     */
    @GetMapping("/unassigned")
    public List<TaskDB> getUnassignedClientTasks() {
        return taskRepo.findByEmployeeIdIsNull();
    }

    /**
     * Retrieve all tasks given by a specific client.
     */
    @GetMapping("/client/{clientId}")
    public List<TaskDB> getTasksByClient(@PathVariable Long clientId) {
        return taskRepo.findByClientId(clientId);
    }

    /**
     * Main Coordination Endpoint:
     * Task Coordinator coordinates a client's task among employees.
     * Assigns employee, sets deadline, priority, coordinator instructions,
     * updates status to "ASSIGNED", and increments employee workload.
     *
     * Example: PUT /api/coordinator_tasks/{taskId}/coordinate
     */
    @PutMapping("/{taskId}/coordinate")
    public ResponseEntity<?> coordinateTask(
            @PathVariable Long taskId,
            @RequestBody TaskCoordinationRequest request) {

        Optional<TaskDB> taskOpt = taskRepo.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task with ID " + taskId + " not found.");
        }

        if (request.getEmployeeId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Employee ID is required to coordinate task.");
        }

        Optional<ProductionStaffDB> empOpt = productionStaffRepo.findById(request.getEmployeeId());
        if (empOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee with ID " + request.getEmployeeId() + " not found.");
        }

        TaskDB task = taskOpt.get();
        ProductionStaffDB employee = empOpt.get();

        // If reassigning from another employee, decrement old employee's workload
        if (task.getEmployeeId() != null && !task.getEmployeeId().equals(employee.getId())) {
            productionStaffRepo.findById(task.getEmployeeId()).ifPresent(oldEmp -> {
                oldEmp.decrementWorkload();
                productionStaffRepo.save(oldEmp);
            });
        }

        // Assign to new employee and update workload if newly assigned or reassigned
        if (task.getEmployeeId() == null || !task.getEmployeeId().equals(employee.getId())) {
            employee.incrementWorkload();
            productionStaffRepo.save(employee);
        }

        task.setEmployeeId(employee.getId());
        task.setEmployeeName(employee.getName());
        if (request.getCoordinatorId() != null) {
            task.setTaskManagerId(request.getCoordinatorId());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        if (request.getPriority() != null && !request.getPriority().trim().isEmpty()) {
            task.setPriority(request.getPriority());
        }
        if (request.getCoordinatorNotes() != null) {
            task.setCoordinatorNotes(request.getCoordinatorNotes());
        }
        task.setStatus("ASSIGNED");
        task.setCoordinatedAt(LocalDateTime.now());

        TaskDB saved = taskRepo.save(task);
        return ResponseEntity.ok(saved);
    }

    /**
     * Coordinator reassigns an existing task from one employee to another.
     */
    @PutMapping("/{taskId}/reassign/{newEmployeeId}")
    public ResponseEntity<?> reassignTask(
            @PathVariable Long taskId,
            @PathVariable Long newEmployeeId,
            @RequestParam(required = false) String reason) {

        Optional<TaskDB> taskOpt = taskRepo.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task with ID " + taskId + " not found.");
        }

        Optional<ProductionStaffDB> newEmpOpt = productionStaffRepo.findById(newEmployeeId);
        if (newEmpOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee with ID " + newEmployeeId + " not found.");
        }

        TaskDB task = taskOpt.get();
        ProductionStaffDB newEmp = newEmpOpt.get();

        // Decrement old employee workload
        if (task.getEmployeeId() != null) {
            productionStaffRepo.findById(task.getEmployeeId()).ifPresent(oldEmp -> {
                oldEmp.decrementWorkload();
                productionStaffRepo.save(oldEmp);
            });
        }

        // Increment new employee workload
        newEmp.incrementWorkload();
        productionStaffRepo.save(newEmp);

        task.setEmployeeId(newEmp.getId());
        task.setEmployeeName(newEmp.getName());
        task.setCoordinatedAt(LocalDateTime.now());
        if (reason != null && !reason.trim().isEmpty()) {
            task.setCoordinatorNotes(
                    (task.getCoordinatorNotes() != null ? task.getCoordinatorNotes() + "\n" : "") +
                            "[Reassigned: " + reason + "]"
            );
        }

        TaskDB saved = taskRepo.save(task);
        return ResponseEntity.ok(saved);
    }

    /**
     * Coordinator overview dashboard summary of all client tasks and employee workloads.
     */
    @GetMapping("/coordination-summary")
    public ResponseEntity<?> getCoordinationSummary() {
        List<TaskDB> allTasks = taskRepo.findAll();
        List<ProductionStaffDB> employees = productionStaffRepo.findAll();

        long unassignedCount = allTasks.stream().filter(t -> t.getEmployeeId() == null).count();
        long inProgressCount = allTasks.stream().filter(t -> "In Progress".equalsIgnoreCase(t.getStatus()) || "ASSIGNED".equalsIgnoreCase(t.getStatus())).count();
        long completedCount = allTasks.stream().filter(t -> "Completed".equalsIgnoreCase(t.getStatus())).count();
        long pendingCoordinationCount = allTasks.stream().filter(t -> "PENDING_COORDINATION".equalsIgnoreCase(t.getStatus())).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalClientTasks", allTasks.size());
        summary.put("unassignedTasksAwaitingCoordination", unassignedCount);
        summary.put("pendingCoordinationTasks", pendingCoordinationCount);
        summary.put("inProgressTasks", inProgressCount);
        summary.put("completedTasks", completedCount);
        summary.put("totalEmployees", employees.size());
        summary.put("employees", employees);

        return ResponseEntity.ok(summary);
    }

    // =========================================================================
    // Legacy / Campaign-Specific Coordinator Endpoints (Preserved for compatibility)
    // =========================================================================

    /**
     * Coordinator divides a campaign's requirements into a new task.
     */
    @PostMapping({"/{coordinatorId}/campaign/{campaignId}/create"})
    public TaskDB createTask(
            @PathVariable Long coordinatorId,
            @PathVariable Long campaignId,
            @RequestBody TaskDB task) {
        task.setTaskManagerId(coordinatorId);
        task.setCampaignId(campaignId);
        if (task.getStatus() == null || task.getStatus().trim().isEmpty()) {
            task.setStatus("To Do");
        }
        return taskRepo.save(task);
    }

    /**
     * View all tasks created under a specific campaign.
     */
    @GetMapping({"/campaign/{campaignId}"})
    public List<TaskDB> getTasksByCampaign(@PathVariable Long campaignId) {
        return taskRepo.findByCampaignId(campaignId);
    }

    /**
     * View all tasks created/managed by a specific coordinator.
     */
    @GetMapping({"/{coordinatorId}"})
    public List<TaskDB> getTasksByCoordinator(@PathVariable Long coordinatorId) {
        return taskRepo.findByTaskManagerId(coordinatorId);
    }

    /**
     * Coordinator assigns a task to a Production Staff member.
     */
    @PutMapping({"/{taskId}/assign/{employeeId}"})
    public TaskDB assignTask(
            @PathVariable Long taskId,
            @PathVariable Long employeeId) {
        return taskRepo.findById(taskId).map(task -> {
            // Update workload if employee changed
            if (task.getEmployeeId() == null || !task.getEmployeeId().equals(employeeId)) {
                if (task.getEmployeeId() != null) {
                    productionStaffRepo.findById(task.getEmployeeId()).ifPresent(oldEmp -> {
                        oldEmp.decrementWorkload();
                        productionStaffRepo.save(oldEmp);
                    });
                }
                productionStaffRepo.findById(employeeId).ifPresent(emp -> {
                    emp.incrementWorkload();
                    productionStaffRepo.save(emp);
                    task.setEmployeeName(emp.getName());
                });
            }
            task.setEmployeeId(employeeId);
            task.setCoordinatedAt(LocalDateTime.now());
            if ("PENDING_COORDINATION".equalsIgnoreCase(task.getStatus())) {
                task.setStatus("ASSIGNED");
            }
            return taskRepo.save(task);
        }).orElse(null);
    }

    /**
     * Coordinator sets a deadline for a task.
     */
    @PutMapping({"/{taskId}/deadline"})
    public TaskDB setDeadline(
            @PathVariable Long taskId,
            @RequestParam LocalDate deadline) {
        return taskRepo.findById(taskId).map(task -> {
            task.setDeadline(deadline);
            return taskRepo.save(task);
        }).orElse(null);
    }

    /**
     * Coordinator tracks the current progress/status of a task.
     */
    @GetMapping({"/{taskId}/progress"})
    public TaskDB trackTaskProgress(@PathVariable Long taskId) {
        return taskRepo.findById(taskId).orElse(null);
    }

    /**
     * Coordinator deletes / cancels a task from the database.
     * Permanently deletes the task record from the DB and decrements assigned employee workload if needed.
     *
     * Example: DELETE /api/coordinator_tasks/{taskId}
     */
    @DeleteMapping({"/{taskId}", "/{taskId}/delete", "/{taskId}/cancel"})
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        Optional<TaskDB> taskOpt = taskRepo.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task with ID " + taskId + " not found.");
        }
        TaskDB task = taskOpt.get();
        if (task.getEmployeeId() != null && !"Completed".equalsIgnoreCase(task.getStatus())) {
            productionStaffRepo.findById(task.getEmployeeId()).ifPresent(emp -> {
                emp.decrementWorkload();
                productionStaffRepo.save(emp);
            });
        }
        taskRepo.delete(task);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Task with ID " + taskId + " has been permanently deleted from the database.");
        response.put("taskId", taskId);
        return ResponseEntity.ok(response);
    }
}
