package com.adagency.addanad.modules.client;

import com.adagency.addanad.modules.client.dto.AuthResponse;
import com.adagency.addanad.modules.client.dto.LoginRequest;
import com.adagency.addanad.modules.communication.AdminChatDB;
import com.adagency.addanad.modules.communication.AdminChatRepo;
import com.adagency.addanad.modules.communication.ClientChatDB;
import com.adagency.addanad.modules.communication.ClientChatRepo;
import com.adagency.addanad.modules.operations.TaskDB;
import com.adagency.addanad.modules.operations.TaskRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminRepo adminRepo;

    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private AdminChatRepo adminChatRepo;

    @Autowired
    private ClientChatRepo clientChatRepo;

    @Autowired
    private com.adagency.addanad.modules.marketing.CampaignAnalysisRepo campaignAnalysisRepo;

    @Autowired
    private com.adagency.addanad.modules.finance.InvoiceRepo invoiceRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final List<String> VALID_ADMIN_TYPES = Arrays.asList(
            "Marketing_Analyst",
            "Task_Manager",
            "Finance_Officer",
            "Communication_Executive"
    );

    /**
     * Register a new admin with a specific adminType:
     * ("Marketing_Analyst", "Task_Manager", "Finance_Officer", "Communication_Executive")
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminDB admin) {
        if (admin.getEmail() == null || admin.getEmail().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Email is required."));
        }
        if (admin.getPassword() == null || admin.getPassword().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Password is required."));
        }
        if (admin.getFirstName() == null || admin.getFirstName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "First name is required."));
        }
        if (admin.getLastName() == null || admin.getLastName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Last name is required."));
        }
        if (admin.getAdminType() == null || admin.getAdminType().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Admin type is required. Valid types: " + VALID_ADMIN_TYPES));
        }

        // Validate adminType format
        boolean validType = VALID_ADMIN_TYPES.stream()
                .anyMatch(t -> t.equalsIgnoreCase(admin.getAdminType().trim()));
        if (!validType) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Invalid admin type '" + admin.getAdminType() + "'. Allowed: " + VALID_ADMIN_TYPES));
        }

        // Check if email already registered
        if (adminRepo.existsByEmail(admin.getEmail()) || userRepo.existsByEmail(admin.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(false, "Email '" + admin.getEmail() + "' is already registered."));
        }

        // Hash password
        String encodedPassword = passwordEncoder.encode(admin.getPassword());
        admin.setPassword(encodedPassword);

        AdminDB savedAdmin = adminRepo.save(admin);

        // Keep UserDB in sync
        UserDB userDB = new UserDB(
                savedAdmin.getEmail(),
                encodedPassword,
                savedAdmin.getFirstName(),
                savedAdmin.getLastName(),
                savedAdmin.getContactNumber(),
                "ADMIN"
        );
        userRepo.save(userDB);

        savedAdmin.setPassword(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new AuthResponse(
                        true,
                        "Admin registered successfully.",
                        "ADMIN",
                        savedAdmin.getAdminID(),
                        savedAdmin.getEmail(),
                        savedAdmin.getFirstName(),
                        savedAdmin.getLastName(),
                        savedAdmin.getAdminType()
                )
        );
    }

    /**
     * Admin login with email and password.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest loginRequest, HttpSession session) {
        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Email and password are required."));
        }

        Optional<AdminDB> adminOptional = adminRepo.findByEmail(loginRequest.getEmail());
        if (adminOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid admin credentials."));
        }

        AdminDB admin = adminOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid admin credentials."));
        }

        // Setup session
        session.setAttribute("USER_ID", admin.getAdminID());
        session.setAttribute("USER_EMAIL", admin.getEmail());
        session.setAttribute("USER_TYPE", "ADMIN");
        session.setAttribute("ADMIN_TYPE", admin.getAdminType());

        return ResponseEntity.ok(new AuthResponse(
                true,
                "Admin login successful.",
                "ADMIN",
                admin.getAdminID(),
                admin.getEmail(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getAdminType()
        ));
    }

    /**
     * Admin logout by invalidating HTTP session.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutAdmin(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new AuthResponse(true, "Admin logged out successfully."));
    }

    /**
     * Retrieve all newly registering clients waiting for admin review and acceptance.
     */
    @GetMapping("/clients/pending")
    public List<ClientDB> getPendingClients() {
        List<ClientDB> pending = clientRepo.findByStatus("PENDING");
        pending.forEach(c -> c.setPassword(null));
        return pending;
    }

    /**
     * Admin accepts a newly registered client.
     */
    @PutMapping("/clients/{clientId}/accept")
    public ResponseEntity<?> acceptClient(@PathVariable Long clientId) {
        Optional<ClientDB> clientOptional = clientRepo.findById(clientId);
        if (clientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Client with ID " + clientId + " not found.");
        }

        ClientDB client = clientOptional.get();
        client.setStatus("ACCEPTED");
        ClientDB saved = clientRepo.save(client);
        saved.setPassword(null);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Client '" + saved.getCompanyName() + "' has been accepted successfully.",
                "client", saved
        ));
    }

    /**
     * Admin rejects a registered client.
     */
    @PutMapping("/clients/{clientId}/reject")
    public ResponseEntity<?> rejectClient(@PathVariable Long clientId) {
        Optional<ClientDB> clientOptional = clientRepo.findById(clientId);
        if (clientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Client with ID " + clientId + " not found.");
        }

        ClientDB client = clientOptional.get();
        client.setStatus("REJECTED");
        ClientDB saved = clientRepo.save(client);
        saved.setPassword(null);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Client '" + saved.getCompanyName() + "' registration has been rejected.",
                "client", saved
        ));
    }

    /**
     * View all clients (optional filter by ?status=ACCEPTED/PENDING/REJECTED).
     */
    @GetMapping("/clients")
    public List<ClientDB> getAllClients(@RequestParam(required = false) String status) {
        List<ClientDB> list;
        if (status != null && !status.trim().isEmpty()) {
            list = clientRepo.findByStatus(status.trim().toUpperCase());
        } else {
            list = clientRepo.findAll();
        }
        list.forEach(c -> c.setPassword(null));
        return list;
    }

    /**
     * Role-specific allocated details endpoint:
     * Admins can get into their relevant admin type to see their allocated details:
     * - Marketing_Analyst: marketing & campaign analysis allocations
     * - Task_Manager: task breakdown and tracking allocations from TaskDB
     * - Finance_Officer: financial allocations, invoices and campaign pricing
     * - Communication_Executive: chat and message allocations from AdminChatDB & ClientChatDB
     */
    @GetMapping("/{adminId}/allocated-details")
    public ResponseEntity<?> getAllocatedDetails(@PathVariable Long adminId) {
        Optional<AdminDB> adminOptional = adminRepo.findById(adminId);
        if (adminOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin with ID " + adminId + " not found.");
        }

        AdminDB admin = adminOptional.get();
        String adminType = admin.getAdminType();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("adminId", admin.getAdminID());
        response.put("adminName", admin.getFirstName() + " " + admin.getLastName());
        response.put("adminEmail", admin.getEmail());
        response.put("adminType", adminType);

        if ("Task_Manager".equalsIgnoreCase(adminType)) {
            // Task Coordinator gets tasks managed and client tasks awaiting coordination
            List<TaskDB> tasks = taskRepo.findByTaskManagerId(adminId);
            List<TaskDB> unassignedClientTasks = taskRepo.findByEmployeeIdIsNull();
            response.put("allocatedRole", "Operations & Task Coordination");
            response.put("taskCount", tasks.size());
            response.put("tasks", tasks);
            response.put("unassignedClientTasksCount", unassignedClientTasks.size());
            response.put("unassignedClientTasks", unassignedClientTasks);
            response.put("overview", "Coordinate tasks given by clients among employees (Production Staff), set deadlines, and track execution.");
        } else if ("Communication_Executive".equalsIgnoreCase(adminType)) {
            // Communication Executive gets chat allocations
            List<AdminChatDB> myMessages = adminChatRepo.findByAdminID(adminId);
            List<ClientChatDB> clientInquiries = clientChatRepo.findAll();
            response.put("allocatedRole", "Client Communication & Messaging");
            response.put("sentRepliesCount", myMessages.size());
            response.put("myReplies", myMessages);
            response.put("totalClientInquiries", clientInquiries.size());
            response.put("recentClientInquiries", clientInquiries);
        } else if ("Marketing_Analyst".equalsIgnoreCase(adminType)) {
            // Marketing Analyst gets campaign metrics & analytics overview
            List<com.adagency.addanad.modules.marketing.CampaignAnalysisDB> analyses = campaignAnalysisRepo.findAll();
            response.put("allocatedRole", "Marketing Analytics & Strategy");
            response.put("totalCampaignsTracked", analyses.size());
            response.put("campaignAnalyses", analyses);
            response.put("overview", "Marketing performance, audience views, visible dates, and campaign progress allocated for review.");
        } else if ("Finance_Officer".equalsIgnoreCase(adminType)) {
            // Finance Officer gets pricing structures and financial allocations
            List<com.adagency.addanad.modules.finance.InvoiceDB> invoices = invoiceRepo.findAll();
            response.put("allocatedRole", "Financial Budgeting & Campaign Billing");
            response.put("totalInvoicesCount", invoices.size());
            response.put("invoices", invoices);
            response.put("rateCard", Map.of(
                    "on-site pin", 1000.0,
                    "YouTube", 1000.0,
                    "FaceBook", 500.0
            ));
            response.put("overview", "Financial transactions, campaign package billing, and platform charges allocated for processing.");
        } else {
            response.put("allocatedRole", adminType);
            response.put("details", "General admin dashboard allocations.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get admin by ID.
     */
    @GetMapping("/{adminId}")
    public ResponseEntity<?> getAdminById(@PathVariable Long adminId) {
        Optional<AdminDB> adminOptional = adminRepo.findById(adminId);
        if (adminOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin with ID " + adminId + " not found.");
        }
        AdminDB admin = adminOptional.get();
        admin.setPassword(null);
        return ResponseEntity.ok(admin);
    }

    /**
     * Get all admins by adminType.
     */
    @GetMapping("/type/{adminType}")
    public List<AdminDB> getAdminsByType(@PathVariable String adminType) {
        List<AdminDB> list = adminRepo.findByAdminType(adminType);
        list.forEach(a -> a.setPassword(null));
        return list;
    }
}
