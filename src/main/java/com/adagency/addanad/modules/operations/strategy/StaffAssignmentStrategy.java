package com.adagency.addanad.modules.operations.strategy;

import com.adagency.addanad.modules.operations.ProductionStaffDB;

import java.util.List;
import java.util.Optional;

/**
 * STRATEGY PATTERN - Strategy interface.
 *
 * Defines the common contract for every rule the Task Manager can use to choose
 * which Production Staff member should receive a client task. Each rule is a
 * separate concrete class, so rules can be added or swapped without changing
 * the code that uses them.
 */
public interface StaffAssignmentStrategy {

    /**
     * Picks one staff member from the given eligible candidates.
     *
     * @param candidates staff who are already known to be eligible (not on leave, below max workload)
     * @return the chosen staff member, or empty if there are no candidates
     */
    Optional<ProductionStaffDB> selectStaff(List<ProductionStaffDB> candidates);

    /**
     * Picks one staff member, taking the task's category into account.
     * Most strategies ignore the category, so by default this simply calls the
     * method above. Strategies that care about the category (such as Role Match)
     * override it.
     */
    default Optional<ProductionStaffDB> selectStaff(List<ProductionStaffDB> candidates, String taskCategory) {
        return selectStaff(candidates);
    }

    /** True if this strategy needs a task category to work properly. */
    default boolean requiresTaskCategory() {
        return false;
    }

    /** Human-readable name of the rule, shown in API responses. */
    String getStrategyName();
}
