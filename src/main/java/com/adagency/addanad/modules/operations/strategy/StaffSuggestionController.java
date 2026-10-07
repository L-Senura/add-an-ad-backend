package com.adagency.addanad.modules.operations.strategy;

import com.adagency.addanad.modules.operations.ProductionStaffDB;
import com.adagency.addanad.modules.operations.ProductionStaffRepo;
import com.adagency.addanad.modules.operations.TaskDB;
import com.adagency.addanad.modules.operations.TaskRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * STRATEGY PATTERN - Client of the pattern.
 *
 * Exposes read-only endpoints that suggest which Production Staff member should
 * receive a task. It reads staff through the existing ProductionStaffRepo and
 * never writes to the database.
 *
 * Spring injects every StaffAssignmentStrategy bean into the map, keyed by the
 * name given in @Component (e.g. "least-workload").
 */
@RestController
@RequestMapping("/api/staff-suggestion")
@CrossOrigin(origins = "*")
public class StaffSuggestionController {

    @Autowired
    private ProductionStaffRepo productionStaffRepo;

    @Autowired(required = false)
    private TaskRepo taskRepo;

    @Autowired
    private Map<String, StaffAssignmentStrategy> strategies;

    /**
     * Retrieve metadata for all registered assignment strategies.
     * GET /api/staff-suggestion/strategies
     */
    @GetMapping("/strategies")
    public List<Map<String, Object>> getStrategies() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of(
                "key", "role-match",
                "name", "Role Match",
                "requiresCategory", true,
                "description", "Matches task category keywords against employee roles, falling back to least busy staff.",
                "icon", "🎯"
        ));
        list.add(Map.of(
                "key", "least-workload",
                "name", "Least Workload",
                "requiresCategory", false,
                "description", "Selects the eligible employee with the lowest active task count to balance team workload.",
                "icon", "⚖️"
        ));
        list.add(Map.of(
                "key", "most-capacity",
                "name", "Most Remaining Capacity",
                "requiresCategory", false,
                "description", "Selects the employee with the most remaining capacity (maxWorkload - currentWorkload).",
                "icon", "🔋"
        ));
        list.add(Map.of(
                "key", "first-available",
                "name", "First Available",
                "requiresCategory", false,
                "description", "Selects the earliest registered eligible staff member by ID (deterministic order).",
                "icon", "⏱️"
        ));
        return list;
    }

    /**
     * Suggest one staff member using the chosen strategy.
     * Example: GET /api/staff-suggestion?strategy=least-workload
     * Example: GET /api/staff-suggestion?strategy=role-match&category=Graphic Design
     */
    @GetMapping
    public ResponseEntity<?> suggest(@RequestParam(defaultValue = "least-workload") String strategy,
                                     @RequestParam(required = false) String category) {
        StaffAssignmentStrategy chosen = strategies.get(strategy);
        if (chosen == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Unknown strategy '" + strategy + "'. Available: " + strategies.keySet());
        }

        StaffAssigner assigner = new StaffAssigner(chosen); // Context
        return ResponseEntity.ok(buildResult(strategy, chosen, category,
                assigner.suggestStaff(productionStaffRepo.findAll(), category)));
    }

    /**
     * Show what every strategy would pick, side by side.
     * Evaluates all registered strategies (providing category to all).
     * Example: GET /api/staff-suggestion/compare?category=Video Production
     */
    @GetMapping("/compare")
    public List<Map<String, Object>> compare(@RequestParam(required = false) String category) {
        List<ProductionStaffDB> allStaff = productionStaffRepo.findAll();
        StaffAssigner assigner = new StaffAssigner(null);

        List<Map<String, Object>> results = new ArrayList<>();
        for (Map.Entry<String, StaffAssignmentStrategy> entry : strategies.entrySet()) {
            String key = entry.getKey();
            StaffAssignmentStrategy strategy = entry.getValue();
            assigner.setStrategy(strategy); // swap the rule at runtime
            results.add(buildResult(key, strategy, category, assigner.suggestStaff(allStaff, category)));
        }
        return results;
    }

    /**
     * Suggest a staff member for a specific existing task by its ID.
     * Example: GET /api/staff-suggestion/task/1?strategy=role-match
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> suggestForTask(@PathVariable Long taskId,
                                            @RequestParam(defaultValue = "role-match") String strategy) {
        String category = null;
        String taskTitle = "Task #" + taskId;
        String taskPriority = "MEDIUM";

        if (taskRepo != null) {
            Optional<TaskDB> taskOpt = taskRepo.findById(taskId);
            if (taskOpt.isPresent()) {
                TaskDB task = taskOpt.get();
                category = task.getTaskCategory();
                taskTitle = task.getTaskTitle();
                if (task.getPriority() != null) {
                    taskPriority = task.getPriority();
                }
            }
        }

        StaffAssignmentStrategy chosen = strategies.get(strategy);
        if (chosen == null) {
            chosen = strategies.get("role-match");
            if (chosen == null) {
                chosen = strategies.values().iterator().next();
            }
        }

        StaffAssigner assigner = new StaffAssigner(chosen);
        Map<String, Object> result = buildResult(strategy, chosen, category,
                assigner.suggestStaff(productionStaffRepo.findAll(), category));
        result.put("taskId", taskId);
        result.put("taskTitle", taskTitle);
        result.put("taskCategory", category != null ? category : "General");
        result.put("priority", taskPriority);
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> buildResult(String strategyKey, StaffAssignmentStrategy strategy, String category, Optional<ProductionStaffDB> staff) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("strategyKey", strategyKey);
        result.put("strategy", strategy.getStrategyName());
        result.put("queriedCategory", category);
        result.put("eligible", staff.isPresent());
        if (staff.isPresent()) {
            ProductionStaffDB s = staff.get();
            result.put("suggestedStaffId", s.getId());
            result.put("suggestedStaffName", s.getName());
            result.put("role", s.getRole());
            result.put("email", s.getEmail());
            result.put("contactNumber", s.getContactNumber());
            result.put("department", s.getDepartment() != null ? s.getDepartment() : "Production Team");
            result.put("currentWorkload", s.getCurrentWorkload());
            result.put("maxWorkload", s.getMaxWorkload());
            result.put("remainingCapacity", Math.max(0, s.getMaxWorkload() - s.getCurrentWorkload()));
            result.put("status", s.getStatus() != null ? s.getStatus() : "AVAILABLE");
        } else {
            result.put("message", "No eligible staff available.");
        }
        return result;
    }
}
