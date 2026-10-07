package com.adagency.addanad.modules.operations.strategy;

import com.adagency.addanad.modules.operations.ProductionStaffDB;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * STRATEGY PATTERN - Concrete Strategy 3.
 *
 * Chooses the earliest registered eligible staff member (lowest staff ID).
 * Aim: a simple, predictable rule.
 */
@Component("first-available")
public class FirstAvailableStrategy implements StaffAssignmentStrategy {

    @Override
    public Optional<ProductionStaffDB> selectStaff(List<ProductionStaffDB> candidates) {
        return candidates.stream()
                .min(Comparator.comparing(ProductionStaffDB::getId));
    }

    @Override
    public String getStrategyName() {
        return "First Available";
    }
}
