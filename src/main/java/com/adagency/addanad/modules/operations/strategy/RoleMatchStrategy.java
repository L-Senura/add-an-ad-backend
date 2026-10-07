package com.adagency.addanad.modules.operations.strategy;

import com.adagency.addanad.modules.operations.ProductionStaffDB;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * STRATEGY PATTERN - Concrete Strategy 4.
 *
 * Chooses the least busy staff member whose role fits the task's category
 * (for example a "Graphic Design" task goes to a Graphic Designer).
 * If nobody's role fits, or no category is given, it falls back to the
 * least busy eligible staff member.
 */
@Component("role-match")
public class RoleMatchStrategy implements StaffAssignmentStrategy {

    // Task category (lower case) -> words that appear in a matching staff role (lower case).
    private static final Map<String, List<String>> CATEGORY_ROLE_KEYWORDS = Map.of(
            "graphic design", List.of("graphic", "design"),
            "video production", List.of("video"),
            "copywriting", List.of("writer", "copy", "content"),
            "web development", List.of("web", "developer"),
            "social media", List.of("social")
    );

    private static final Comparator<ProductionStaffDB> BY_WORKLOAD =
            Comparator.comparingInt(ProductionStaffDB::getCurrentWorkload);

    /** Without a task category there is nothing to match, so choose the least busy person. */
    @Override
    public Optional<ProductionStaffDB> selectStaff(List<ProductionStaffDB> candidates) {
        return candidates.stream().min(BY_WORKLOAD);
    }

    @Override
    public Optional<ProductionStaffDB> selectStaff(List<ProductionStaffDB> candidates, String taskCategory) {
        List<String> keywords = taskCategory == null
                ? List.of()
                : CATEGORY_ROLE_KEYWORDS.getOrDefault(taskCategory.trim().toLowerCase(), List.of());

        Optional<ProductionStaffDB> roleMatch = candidates.stream()
                .filter(staff -> roleFits(staff, keywords))
                .min(BY_WORKLOAD);

        // Nobody fits (or unknown category): fall back to the least busy eligible staff member.
        return roleMatch.isPresent() ? roleMatch : selectStaff(candidates);
    }

    @Override
    public boolean requiresTaskCategory() {
        return true;
    }

    @Override
    public String getStrategyName() {
        return "Role Match";
    }

    private boolean roleFits(ProductionStaffDB staff, List<String> keywords) {
        if (staff.getRole() == null) {
            return false;
        }
        String role = staff.getRole().toLowerCase();
        return keywords.stream().anyMatch(role::contains);
    }
}
