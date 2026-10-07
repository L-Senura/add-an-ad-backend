package com.adagency.addanad.modules.operations.strategy;

import com.adagency.addanad.modules.operations.ProductionStaffDB;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * STRATEGY PATTERN - Concrete Strategy 1.
 *
 * Chooses the staff member with the fewest active tasks right now.
 * Aim: spread work evenly across the team.
 */
@Component("least-workload")
public class LeastWorkloadStrategy implements StaffAssignmentStrategy {

    @Override
    public Optional<ProductionStaffDB> selectStaff(List<ProductionStaffDB> candidates) {
        return candidates.stream()
                .min(Comparator.comparingInt(ProductionStaffDB::getCurrentWorkload));
    }

    @Override
    public String getStrategyName() {
        return "Least Workload";
    }
}
