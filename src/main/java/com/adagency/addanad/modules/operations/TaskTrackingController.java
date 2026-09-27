package com.adagency.addanad.modules.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Employee (Production Staff) facing task tracking and execution operations.
 * Employees view tasks coordinated to them (with client requirements and coordinator instructions)
 * and update their execution progress.
 */
@RestController
@RequestMapping("/api/employee_tasks")
public class TaskTrackingController {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private ProductionStaffRepo productionStaffRepo;

    /**
     * Employee views all tasks currently assigned to them.
     * Includes client requirements and coordinator notes.
     *
     * Example: GET /api/employee_tasks/1
     */
    @GetMapping({"/{employeeId}"})
    public List<TaskDB> getAssignedTasks(@PathVariable Long employeeId) {
        return taskRepo.findByEmployeeId(employeeId);
    }

    /**
     * Employee views only pending or active tasks (not yet completed).
     *
     * Example: GET /api/employee_tasks/1/pending
     */
    @GetMapping({"/{employeeId}/pending"})
    public List<TaskDB> getPendingTasks(@PathVariable Long employeeId) {
        List<TaskDB> tasks = taskRepo.findByEmployeeId(employeeId);
        return tasks.stream()
                .filter(t -> !"Completed".equalsIgnoreCase(t.getStatus()) && !"Cancelled".equalsIgnoreCase(t.getStatus()))
                .toList();
    }

    /**
     * Employee views their completed tasks.
     *
     * Example: GET /api/employee_tasks/1/completed
     */
    @GetMapping({"/{employeeId}/completed"})
    public List<TaskDB> getCompletedTasks(@PathVariable Long employeeId) {
        return taskRepo.findByEmployeeIdAndStatus(employeeId, "Completed");
    }

    /**
     * Employee updates the status of an assigned task (e.g. "To Do" -> "In Progress" -> "Completed").
     * When status becomes "Completed", employee's current workload is automatically decremented
     * and completedAt timestamp is recorded.
     *
     * Example: PUT /api/employee_tasks/1/5/status?status=Completed
     */
    @PutMapping({"/{employeeId}/{taskId}/status"})
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long employeeId,
            @PathVariable Long taskId,
            @RequestParam String status) {

        Optional<TaskDB> taskOpt = taskRepo.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task with ID " + taskId + " not found.");
        }

        TaskDB task = taskOpt.get();

        // Verify task is assigned to this employee
        if (task.getEmployeeId() == null || !task.getEmployeeId().equals(employeeId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Task ID " + taskId + " is not assigned to employee ID " + employeeId + ".");
        }

        String oldStatus = task.getStatus();
        task.setStatus(status);

        // If newly marked as Completed, record completion time and decrement workload
        if ("Completed".equalsIgnoreCase(status) && !"Completed".equalsIgnoreCase(oldStatus)) {
            task.setCompletedAt(LocalDateTime.now());
            productionStaffRepo.findById(employeeId).ifPresent(emp -> {
                emp.decrementWorkload();
                productionStaffRepo.save(emp);
            });
        }

        TaskDB saved = taskRepo.save(task);
        return ResponseEntity.ok(saved);
    }

    /**
     * Employee checks their own profile, assigned role, availability, and active workload.
     *
     * Example: GET /api/employee_tasks/profile/1
     */
    @GetMapping("/profile/{employeeId}")
    public ResponseEntity<?> getEmployeeProfile(@PathVariable Long employeeId) {
        Optional<ProductionStaffDB> empOpt = productionStaffRepo.findById(employeeId);
        if (empOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee with ID " + employeeId + " not found.");
        }
        return ResponseEntity.ok(empOpt.get());
    }

    /**
     * Employee updates their availability status (e.g. "AVAILABLE", "ON_LEAVE").
     *
     * Example: PUT /api/employee_tasks/1/availability?status=ON_LEAVE
     */
    @PutMapping("/{employeeId}/availability")
    public ResponseEntity<?> updateAvailability(
            @PathVariable Long employeeId,
            @RequestParam String status) {
        Optional<ProductionStaffDB> empOpt = productionStaffRepo.findById(employeeId);
        if (empOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee with ID " + employeeId + " not found.");
        }
        ProductionStaffDB emp = empOpt.get();
        emp.setStatus(status.toUpperCase());
        ProductionStaffDB saved = productionStaffRepo.save(emp);
        return ResponseEntity.ok(saved);
    }
}
