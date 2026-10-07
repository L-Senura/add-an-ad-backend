package com.adagency.addanad.modules.operations.strategy;

import com.adagency.addanad.modules.operations.ProductionStaffDB;
import com.adagency.addanad.modules.operations.ProductionStaffRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * STRATEGY PATTERN - Demo (client of the pattern).
 *
 * Starts when the backend starts and prints, in the console, which Production
 * Staff member each strategy would choose. A background thread re-checks the
 * staff table every few seconds and prints the result again whenever a staff
 * member's workload or status changes (for example after a task is assigned in
 * the UI). It only reads staff data through the existing ProductionStaffRepo and
 * never changes the database.
 *
 * Spring injects every StaffAssignmentStrategy bean into the map, keyed by the
 * name given in @Component (e.g. "least-workload").
 */
@Component
public class StrategyDemoRunner implements CommandLineRunner {

    // How often the staff table is re-checked for changes (milliseconds).
    private static final long CHECK_INTERVAL_MS = 3000;

    // Example task categories used to demonstrate the Role Match strategy.
    private static final List<String> DEMO_CATEGORIES = List.of(
            "Graphic Design", "Video Production", "Copywriting", "Web Development", "Social Media");

    private final ProductionStaffRepo productionStaffRepo;
    private final Map<String, StaffAssignmentStrategy> strategies;

    public StrategyDemoRunner(ProductionStaffRepo productionStaffRepo,
                              Map<String, StaffAssignmentStrategy> strategies) {
        this.productionStaffRepo = productionStaffRepo;
        this.strategies = strategies;
    }

    @Override
    public void run(String... args) {
        // Daemon thread: it never stops the application from shutting down.
        Thread watcher = new Thread(this::watchStaffChanges, "strategy-demo-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    /** Prints the demo once, then again every time the staff data changes. */
    private void watchStaffChanges() {
        String lastSnapshot = null;
        while (true) {
            try {
                List<ProductionStaffDB> allStaff = productionStaffRepo.findAll();
                String snapshot = takeSnapshot(allStaff);
                if (!snapshot.equals(lastSnapshot)) {
                    printDemo(allStaff, lastSnapshot == null);
                    lastSnapshot = snapshot;
                }
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                return; // application is shutting down
            } catch (Exception e) {
                // A demo problem must never affect the application; try again next round.
                System.out.println("Strategy demo: could not read staff (" + e.getMessage() + ")");
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    return;
                }
            }
        }
    }

    /** A short text fingerprint of the staff data, used to detect changes. */
    private String takeSnapshot(List<ProductionStaffDB> allStaff) {
        StringBuilder sb = new StringBuilder();
        for (ProductionStaffDB staff : allStaff) {
            sb.append(staff.getId()).append(':')
              .append(staff.getCurrentWorkload()).append('/')
              .append(staff.getMaxWorkload()).append(':')
              .append(staff.getRole()).append(':')
              .append(staff.getStatus()).append(';');
        }
        return sb.toString();
    }

    private void printDemo(List<ProductionStaffDB> allStaff, boolean firstRun) {
        System.out.println();
        System.out.println("==================== STRATEGY PATTERN DEMO ====================");
        System.out.println(firstRun ? "(startup)" : "(staff data changed)");
        System.out.println("Production staff in the database:");
        for (ProductionStaffDB staff : allStaff) {
            System.out.printf("  ID %-3d %-18s %-38s status=%-10s workload=%d/%d%n",
                    staff.getId(), staff.getName(), staff.getRole(), staff.getStatus(),
                    staff.getCurrentWorkload(), staff.getMaxWorkload());
        }

        System.out.println();
        System.out.println("Suggested staff member for a new task, per strategy:");
        StaffAssigner assigner = new StaffAssigner(null); // Context
        for (StaffAssignmentStrategy strategy : strategies.values()) {
            if (strategy.requiresTaskCategory()) {
                continue; // shown separately below, because it needs a task category
            }
            assigner.setStrategy(strategy); // swap the rule at runtime
            System.out.printf("  %-25s -> %s%n", strategy.getStrategyName(),
                    describe(assigner.suggestStaff(allStaff)));
        }

        for (StaffAssignmentStrategy strategy : strategies.values()) {
            if (!strategy.requiresTaskCategory()) {
                continue;
            }
            assigner.setStrategy(strategy);
            System.out.println();
            System.out.println(strategy.getStrategyName() + " (depends on the task category):");
            for (String category : DEMO_CATEGORIES) {
                System.out.printf("  %-25s -> %s%n", category,
                        describe(assigner.suggestStaff(allStaff, category)));
            }
        }
        System.out.println("===============================================================");
        System.out.println();
    }

    private String describe(Optional<ProductionStaffDB> pick) {
        return pick.map(s -> s.getName() + " - " + s.getRole()
                        + " (workload " + s.getCurrentWorkload() + "/" + s.getMaxWorkload() + ")")
                .orElse("No eligible staff available");
    }
}
