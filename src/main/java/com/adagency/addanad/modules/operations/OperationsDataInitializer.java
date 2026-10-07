package com.adagency.addanad.modules.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Initializes default Production Staff and initial sample tasks if database tables are empty.
 * Ensures the Operations module has available staff for task assignments, workload balancing,
 * and the GoF Strategy Pattern suggestion engine upon startup.
 */
@Component
@Order(1)
public class OperationsDataInitializer implements CommandLineRunner {

    @Autowired
    private ProductionStaffRepo productionStaffRepo;

    @Autowired
    private TaskRepo taskRepo;

    @Override
    public void run(String... args) {
        seedProductionStaffIfEmpty();
        seedTasksIfEmpty();
    }

    private void seedProductionStaffIfEmpty() {
        if (productionStaffRepo.count() > 0) {
            return;
        }

        System.out.println("[OperationsDataInitializer] Seeding initial production staff members...");

        productionStaffRepo.save(new ProductionStaffDB(
                "Sarah Jenkins",
                "sarah.j@agency.com",
                "Lead Graphic Designer",
                "Creative and Visual Design",
                "+1 (555) 234-5678",
                2,
                5,
                "AVAILABLE"
        ));

        productionStaffRepo.save(new ProductionStaffDB(
                "Liam Torres",
                "liam.t@agency.com",
                "Senior Video Editor and Motion Artist",
                "Video Production and Animation",
                "+1 (555) 345-6789",
                3,
                5,
                "AVAILABLE"
        ));

        productionStaffRepo.save(new ProductionStaffDB(
                "Elena Rostova",
                "elena.r@agency.com",
                "Creative Copywriter and Strategist",
                "Content and Campaign Copy",
                "+1 (555) 456-7890",
                1,
                5,
                "AVAILABLE"
        ));

        productionStaffRepo.save(new ProductionStaffDB(
                "David Chen",
                "david.c@agency.com",
                "Front-End Web and Landing Page Specialist",
                "Digital and Web Engineering",
                "+1 (555) 567-8901",
                4,
                5,
                "BUSY"
        ));

        productionStaffRepo.save(new ProductionStaffDB(
                "Marcus Bennett",
                "marcus.b@agency.com",
                "Social Media Producer and Reels Creator",
                "Social Media and Influencer Content",
                "+1 (555) 678-9012",
                1,
                5,
                "AVAILABLE"
        ));

        productionStaffRepo.save(new ProductionStaffDB(
                "Aria Montgomery",
                "aria.m@agency.com",
                "3D Visualizer and Brand Animator",
                "3D CGI and Brand Visuals",
                "+1 (555) 789-0123",
                2,
                5,
                "AVAILABLE"
        ));

        System.out.println("[OperationsDataInitializer] 6 production staff seeded successfully.");
    }

    private void seedTasksIfEmpty() {
        if (taskRepo.count() > 0) {
            return;
        }

        System.out.println("[OperationsDataInitializer] Seeding initial operations tasks...");

        TaskDB t1 = new TaskDB();
        t1.setClientId(1L);
        t1.setClientName("Nova Marketing Agency");
        t1.setCampaignId(1L);
        t1.setTaskTitle("Design Instagram Story Ad Carousel & Banners");
        t1.setTaskDetails("Need 5 high-converting vertical 1080x1920 story slides for summer sale discount blitz with bold CTA buttons.");
        t1.setTaskCategory("Graphic Design");
        t1.setPriority("HIGH");
        t1.setTaskManagerId(1L);
        t1.setCoordinatorNotes("Follow brand style guide with soft rose and deep slate blue accents. Ensure mobile readability.");
        t1.setEmployeeId(1L);
        t1.setEmployeeName("Sarah Jenkins");
        t1.setStatus("In Progress");
        t1.setClientDeadline(LocalDate.now().plusDays(4));
        t1.setDeadline(LocalDate.now().plusDays(2));
        t1.setCreatedAt(LocalDateTime.now().minusDays(3));
        t1.setCoordinatedAt(LocalDateTime.now().minusDays(2));
        taskRepo.save(t1);

        TaskDB t2 = new TaskDB();
        t2.setClientId(1L);
        t2.setClientName("Nova Marketing Agency");
        t2.setCampaignId(1L);
        t2.setTaskTitle("15-Second YouTube Bumper Promo Reel");
        t2.setTaskDetails("Produce a punchy 15s bumper ad showcasing client mobile app features with sound design and upbeat music.");
        t2.setTaskCategory("Video Production");
        t2.setPriority("URGENT");
        t2.setTaskManagerId(1L);
        t2.setCoordinatorNotes("First 3 seconds must have instant hook and sound effects. Render in 4K and 1080p.");
        t2.setEmployeeId(2L);
        t2.setEmployeeName("Liam Torres");
        t2.setStatus("ASSIGNED");
        t2.setClientDeadline(LocalDate.now().plusDays(7));
        t2.setDeadline(LocalDate.now().plusDays(5));
        t2.setCreatedAt(LocalDateTime.now().minusDays(2));
        t2.setCoordinatedAt(LocalDateTime.now().minusDays(1));
        taskRepo.save(t2);

        TaskDB t3 = new TaskDB();
        t3.setClientId(1L);
        t3.setClientName("Nova Marketing Agency");
        t3.setCampaignId(2L);
        t3.setTaskTitle("Catchy Campaign Slogans & Ad Copywriting");
        t3.setTaskDetails("Deliver 10 headline variations and short-form ad copies for Google search ads and LinkedIn sponsored feed.");
        t3.setTaskCategory("Copywriting");
        t3.setPriority("MEDIUM");
        t3.setStatus("PENDING_COORDINATION");
        t3.setClientDeadline(LocalDate.now().plusDays(14));
        t3.setCreatedAt(LocalDateTime.now().minusHours(8));
        taskRepo.save(t3);

        TaskDB t4 = new TaskDB();
        t4.setClientId(101L);
        t4.setClientName("OmniVanguard Digital");
        t4.setCampaignId(3L);
        t4.setTaskTitle("Promotional Product Landing Page Optimization");
        t4.setTaskDetails("Optimize conversion funnels, fix responsiveness on tablet layouts, and integrate analytics pixel.");
        t4.setTaskCategory("Web Development");
        t4.setPriority("HIGH");
        t4.setTaskManagerId(1L);
        t4.setCoordinatorNotes("Tested across mobile breakpoints. High fidelity completed.");
        t4.setEmployeeId(4L);
        t4.setEmployeeName("David Chen");
        t4.setStatus("Completed");
        t4.setClientDeadline(LocalDate.now().minusDays(5));
        t4.setDeadline(LocalDate.now().minusDays(6));
        t4.setCreatedAt(LocalDateTime.now().minusDays(15));
        t4.setCoordinatedAt(LocalDateTime.now().minusDays(14));
        t4.setCompletedAt(LocalDateTime.now().minusDays(2));
        taskRepo.save(t4);

        System.out.println("[OperationsDataInitializer] 4 initial demo tasks seeded successfully.");
    }
}
