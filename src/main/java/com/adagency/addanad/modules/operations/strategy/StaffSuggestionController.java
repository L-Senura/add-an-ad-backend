package com.adagency.addanad.modules.operations.strategy;

import com.adagency.addanad.modules.operations.ProductionStaffDB;
import com.adagency.addanad.modules.operations.ProductionStaffRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
public class StaffSuggestionController {

    @Autowired
    private ProductionStaffRepo productionStaffRepo;

    @Autowired
    private Map<String, StaffAssignmentStrategy> strategies;

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
        return ResponseEntity.ok(buildResult(chosen,
                assigner.suggestStaff(productionStaffRepo.findAll(), category)));
    }

    /**
     * Show what every strategy would pick, side by side.
     * Strategies that need a task category (Role Match) are included only when a category is given.
     * Example: GET /api/staff-suggestion/compare?category=Video Production
     */
    @GetMapping("/compare")
    public List<Map<String, Object>> compare(@RequestParam(required = false) String category) {
        List<ProductionStaffDB> allStaff = productionStaffRepo.findAll();
        StaffAssigner assigner = new StaffAssigner(null);

        return strategies.values().stream()
                .filter(strategy -> !strategy.requiresTaskCategory() || category != null)
                .map(strategy -> {
                    assigner.setStrategy(strategy); // swap the rule at runtime
                    return buildResult(strategy, assigner.suggestStaff(allStaff, category));
                })
                .toList();
    }

    private Map<String, Object> buildResult(StaffAssignmentStrategy strategy, Optional<ProductionStaffDB> staff) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("strategy", strategy.getStrategyName());
        if (staff.isPresent()) {
            ProductionStaffDB s = staff.get();
            result.put("suggestedStaffId", s.getId());
            result.put("suggestedStaffName", s.getName());
            result.put("role", s.getRole());
            result.put("currentWorkload", s.getCurrentWorkload());
            result.put("maxWorkload", s.getMaxWorkload());
        } else {
            result.put("message", "No eligible staff available.");
        }
        return result;
    }
}
