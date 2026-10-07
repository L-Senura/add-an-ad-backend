package com.adagency.addanad.modules.operations.strategy;

import com.adagency.addanad.modules.operations.ProductionStaffDB;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * STRATEGY PATTERN - Concrete Strategy 2.
 *
 * Chooses the staff member with the most free capacity (maxWorkload - currentWorkload).
 * Aim: give work to whoever can take the most additional tasks, which can differ
 * from "least workload" when staff have different maximum workloads.
 */
@Component("most-capacity")
public class MostRemainingCapacityStrategy implements StaffAssignmentStrategy {

    @Override
    public Optional<ProductionStaffDB> selectStaff(List<ProductionStaffDB> candidates) {
        return candidates.stream()
                .max(Comparator.comparingInt(
                        staff -> staff.getMaxWorkload() - staff.getCurrentWorkload()));
    }

    @Override
    public String getStrategyName() {
        return "Most Remaining Capacity";
    }
}
