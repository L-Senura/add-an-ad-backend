package com.adagency.addanad.modules.operations.strategy;

import com.adagency.addanad.modules.operations.ProductionStaffDB;

import java.util.List;
import java.util.Optional;

/**
 * STRATEGY PATTERN - Context class.
 *
 * Holds a reference to the currently chosen StaffAssignmentStrategy and delegates
 * the selection to it. The context does not know which concrete rule it is using;
 * it only filters out ineligible staff and then asks the strategy to choose.
 */
public class StaffAssigner {

    private StaffAssignmentStrategy strategy;

    public StaffAssigner(StaffAssignmentStrategy strategy) {
        this.strategy = strategy;
    }

    /** Swap the rule at runtime. */
    public void setStrategy(StaffAssignmentStrategy strategy) {
        this.strategy = strategy;
    }

    public StaffAssignmentStrategy getStrategy() {
        return strategy;
    }

    /**
     * Suggests a staff member for a new task using the current strategy.
     * Staff who are on leave or already at max workload are never suggested.
     */
    public Optional<ProductionStaffDB> suggestStaff(List<ProductionStaffDB> allStaff) {
        return suggestStaff(allStaff, null);
    }

    /**
     * Same as above, but also passes the task's category to the strategy
     * (used by strategies such as Role Match).
     */
    public Optional<ProductionStaffDB> suggestStaff(List<ProductionStaffDB> allStaff, String taskCategory) {
        List<ProductionStaffDB> eligible = allStaff.stream()
                .filter(staff -> !"ON_LEAVE".equalsIgnoreCase(staff.getStatus()))
                .filter(staff -> staff.getCurrentWorkload() < staff.getMaxWorkload())
                .toList();
        return strategy.selectStaff(eligible, taskCategory);
    }
}
