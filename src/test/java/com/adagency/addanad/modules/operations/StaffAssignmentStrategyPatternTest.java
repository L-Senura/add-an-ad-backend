package com.adagency.addanad.modules.operations;

import com.adagency.addanad.modules.operations.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying the Gang of Four (GoF) Strategy Design Pattern implementation
 * in the Operations module (StaffAssignmentStrategy, StaffAssigner Context, and Concrete Strategies).
 */
class StaffAssignmentStrategyPatternTest {

    private ProductionStaffDB designer;
    private ProductionStaffDB videoEditor;
    private ProductionStaffDB copywriter;
    private ProductionStaffDB onLeaveStaff;
    private ProductionStaffDB maxCapacityStaff;

    @BeforeEach
    void setUp() {
        // Staff 1: Graphic Designer, current: 3, max: 8 (remaining: 5)
        designer = new ProductionStaffDB("Alice Smith", "alice@agency.com", "Senior Graphic Designer", "0711111111");
        designer.setId(10L);
        designer.setCurrentWorkload(3);
        designer.setMaxWorkload(8);
        designer.setStatus("AVAILABLE");

        // Staff 2: Video Editor, current: 1, max: 4 (remaining: 3)
        videoEditor = new ProductionStaffDB("Bob Jones", "bob@agency.com", "Video Producer & Editor", "0722222222");
        videoEditor.setId(20L);
        videoEditor.setCurrentWorkload(1);
        videoEditor.setMaxWorkload(4);
        videoEditor.setStatus("AVAILABLE");

        // Staff 3: Copywriter, current: 2, max: 5 (remaining: 3)
        copywriter = new ProductionStaffDB("Charlie Rose", "charlie@agency.com", "Content Copywriter", "0733333333");
        copywriter.setId(5L);
        copywriter.setCurrentWorkload(2);
        copywriter.setMaxWorkload(5);
        copywriter.setStatus("AVAILABLE");

        // Staff 4: On leave
        onLeaveStaff = new ProductionStaffDB("David Stone", "david@agency.com", "Graphic Designer", "0744444444");
        onLeaveStaff.setId(1L);
        onLeaveStaff.setCurrentWorkload(0);
        onLeaveStaff.setMaxWorkload(5);
        onLeaveStaff.setStatus("ON_LEAVE");

        // Staff 5: At maximum capacity
        maxCapacityStaff = new ProductionStaffDB("Eve Taylor", "eve@agency.com", "Web Developer", "0755555555");
        maxCapacityStaff.setId(2L);
        maxCapacityStaff.setCurrentWorkload(5);
        maxCapacityStaff.setMaxWorkload(5);
        maxCapacityStaff.setStatus("BUSY");
    }

    @Test
    @DisplayName("LeastWorkloadStrategy selects candidate with minimum current workload")
    void testLeastWorkloadStrategy() {
        StaffAssignmentStrategy strategy = new LeastWorkloadStrategy();
        assertEquals("Least Workload", strategy.getStrategyName());
        assertFalse(strategy.requiresTaskCategory());

        List<ProductionStaffDB> candidates = List.of(designer, videoEditor, copywriter);
        Optional<ProductionStaffDB> selected = strategy.selectStaff(candidates);

        assertTrue(selected.isPresent());
        assertEquals("Bob Jones", selected.get().getName(), "Should select Bob who has workload 1");
    }

    @Test
    @DisplayName("MostRemainingCapacityStrategy selects candidate with highest remaining capacity")
    void testMostRemainingCapacityStrategy() {
        StaffAssignmentStrategy strategy = new MostRemainingCapacityStrategy();
        assertEquals("Most Remaining Capacity", strategy.getStrategyName());
        assertFalse(strategy.requiresTaskCategory());

        // designer has remaining capacity 8 - 3 = 5
        // videoEditor has remaining capacity 4 - 1 = 3
        // copywriter has remaining capacity 5 - 2 = 3
        List<ProductionStaffDB> candidates = List.of(designer, videoEditor, copywriter);
        Optional<ProductionStaffDB> selected = strategy.selectStaff(candidates);

        assertTrue(selected.isPresent());
        assertEquals("Alice Smith", selected.get().getName(), "Should select Alice who has 5 remaining capacity slots");
    }

    @Test
    @DisplayName("FirstAvailableStrategy selects candidate with lowest ID")
    void testFirstAvailableStrategy() {
        StaffAssignmentStrategy strategy = new FirstAvailableStrategy();
        assertEquals("First Available", strategy.getStrategyName());
        assertFalse(strategy.requiresTaskCategory());

        // IDs: copywriter = 5, designer = 10, videoEditor = 20
        List<ProductionStaffDB> candidates = List.of(designer, videoEditor, copywriter);
        Optional<ProductionStaffDB> selected = strategy.selectStaff(candidates);

        assertTrue(selected.isPresent());
        assertEquals("Charlie Rose", selected.get().getName(), "Should select Charlie who has ID 5");
    }

    @Test
    @DisplayName("RoleMatchStrategy selects staff matching task category keywords")
    void testRoleMatchStrategyMatchingCategory() {
        StaffAssignmentStrategy strategy = new RoleMatchStrategy();
        assertEquals("Role Match", strategy.getStrategyName());
        assertTrue(strategy.requiresTaskCategory());

        List<ProductionStaffDB> candidates = List.of(designer, videoEditor, copywriter);

        // Graphic design task
        Optional<ProductionStaffDB> forDesign = strategy.selectStaff(candidates, "Graphic Design");
        assertTrue(forDesign.isPresent());
        assertEquals("Alice Smith", forDesign.get().getName());

        // Video production task
        Optional<ProductionStaffDB> forVideo = strategy.selectStaff(candidates, "Video Production");
        assertTrue(forVideo.isPresent());
        assertEquals("Bob Jones", forVideo.get().getName());

        // Copywriting task
        Optional<ProductionStaffDB> forCopy = strategy.selectStaff(candidates, "Copywriting");
        assertTrue(forCopy.isPresent());
        assertEquals("Charlie Rose", forCopy.get().getName());
    }

    @Test
    @DisplayName("RoleMatchStrategy falls back to least busy staff if no role matches")
    void testRoleMatchStrategyFallback() {
        StaffAssignmentStrategy strategy = new RoleMatchStrategy();
        List<ProductionStaffDB> candidates = List.of(designer, videoEditor, copywriter);

        // Web Development category - no web developer in candidates, should fall back to least busy (Bob Jones, workload 1)
        Optional<ProductionStaffDB> fallback = strategy.selectStaff(candidates, "Web Development");
        assertTrue(fallback.isPresent());
        assertEquals("Bob Jones", fallback.get().getName());
    }

    @Test
    @DisplayName("StaffAssigner context filters out on-leave and max-capacity staff")
    void testStaffAssignerContextEligibilityFilter() {
        StaffAssigner assigner = new StaffAssigner(new LeastWorkloadStrategy());

        List<ProductionStaffDB> allStaff = List.of(designer, videoEditor, copywriter, onLeaveStaff, maxCapacityStaff);
        Optional<ProductionStaffDB> selected = assigner.suggestStaff(allStaff);

        assertTrue(selected.isPresent());
        assertNotEquals("David Stone", selected.get().getName(), "On leave staff must be filtered out");
        assertNotEquals("Eve Taylor", selected.get().getName(), "Max capacity staff must be filtered out");
    }

    @Test
    @DisplayName("StaffAssigner context supports runtime strategy swapping")
    void testStaffAssignerRuntimeStrategySwapping() {
        List<ProductionStaffDB> allStaff = List.of(designer, videoEditor, copywriter);

        // Context initialized with LeastWorkload
        StaffAssigner assigner = new StaffAssigner(new LeastWorkloadStrategy());
        assertEquals("Bob Jones", assigner.suggestStaff(allStaff).get().getName());

        // Swap strategy at runtime to MostRemainingCapacity
        assigner.setStrategy(new MostRemainingCapacityStrategy());
        assertEquals("Alice Smith", assigner.suggestStaff(allStaff).get().getName());

        // Swap strategy at runtime to FirstAvailable
        assigner.setStrategy(new FirstAvailableStrategy());
        assertEquals("Charlie Rose", assigner.suggestStaff(allStaff).get().getName());

        // Swap strategy at runtime to RoleMatch for Graphic Design
        assigner.setStrategy(new RoleMatchStrategy());
        assertEquals("Alice Smith", assigner.suggestStaff(allStaff, "Graphic Design").get().getName());
    }

    @Test
    @DisplayName("StaffAssigner returns empty Optional when no staff are eligible")
    void testStaffAssignerNoEligibleCandidates() {
        StaffAssigner assigner = new StaffAssigner(new LeastWorkloadStrategy());
        List<ProductionStaffDB> ineligibleStaff = List.of(onLeaveStaff, maxCapacityStaff);

        Optional<ProductionStaffDB> selected = assigner.suggestStaff(ineligibleStaff);
        assertTrue(selected.isEmpty(), "Should return empty Optional when all staff are ineligible");
    }
}
