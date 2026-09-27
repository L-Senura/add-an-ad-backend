package com.adagency.addanad.modules.client;

import com.adagency.addanad.modules.client.dto.AuthResponse;
import com.adagency.addanad.modules.client.dto.LoginRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new client.
     * Status is initialized to "PENDING" and requires Admin review and acceptance.
     * Password is encrypted using BCrypt.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerClient(@RequestBody ClientDB client) {
        // Validation
        if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Email is required."));
        }
        if (client.getPassword() == null || client.getPassword().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Password is required."));
        }
        if (client.getFirstName() == null || client.getFirstName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "First name is required."));
        }
        if (client.getLastName() == null || client.getLastName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Last name is required."));
        }
        if (client.getCompanyName() == null || client.getCompanyName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Company name is required."));
        }

        // Check if email already exists
        if (clientRepo.existsByEmail(client.getEmail()) || userRepo.existsByEmail(client.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(false, "Email '" + client.getEmail() + "' is already registered."));
        }

        // Secure password hashing
        String encodedPassword = passwordEncoder.encode(client.getPassword());
        client.setPassword(encodedPassword);

        // Explicitly set pending status awaiting admin acceptance
        client.setStatus("PENDING");

        // Save in ClientDB
        ClientDB savedClient = clientRepo.save(client);

        // Keep UserDB in sync for centralized user credentials
        UserDB userDB = new UserDB(
                savedClient.getEmail(),
                encodedPassword,
                savedClient.getFirstName(),
                savedClient.getLastName(),
                savedClient.getContactNumber(),
                "CLIENT"
        );
        userRepo.save(userDB);

        // Hide password in response
        savedClient.setPassword(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new AuthResponse(
                        true,
                        "Client registered successfully. Your account is pending admin approval.",
                        "CLIENT",
                        savedClient.getClientID(),
                        savedClient.getEmail(),
                        savedClient.getFirstName(),
                        savedClient.getLastName(),
                        savedClient.getStatus()
                )
        );
    }

    /**
     * Authenticate and log in client.
     * Enforces that the client registration has been accepted by an admin.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginClient(@RequestBody LoginRequest loginRequest, HttpSession session) {
        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Email and password are required."));
        }

        Optional<ClientDB> clientOptional = clientRepo.findByEmail(loginRequest.getEmail());
        if (clientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid email or password."));
        }

        ClientDB client = clientOptional.get();

        // Verify BCrypt password
        if (!passwordEncoder.matches(loginRequest.getPassword(), client.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid email or password."));
        }

        // Check registration approval status
        if ("PENDING".equalsIgnoreCase(client.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(false, "Account registration is pending admin approval. Please wait for an admin to accept your account."));
        }

        if ("REJECTED".equalsIgnoreCase(client.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(false, "Account registration was rejected by the administrator."));
        }

        if (!"ACCEPTED".equalsIgnoreCase(client.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse(false, "Account is not active. Status: " + client.getStatus()));
        }

        // Setup session for logged in client
        session.setAttribute("USER_ID", client.getClientID());
        session.setAttribute("USER_EMAIL", client.getEmail());
        session.setAttribute("USER_TYPE", "CLIENT");

        return ResponseEntity.ok(new AuthResponse(
                true,
                "Login successful.",
                "CLIENT",
                client.getClientID(),
                client.getEmail(),
                client.getFirstName(),
                client.getLastName(),
                client.getStatus()
        ));
    }

    /**
     * Log out client by invalidating the HTTP session.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutClient(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new AuthResponse(true, "Client logged out successfully."));
    }

    /**
     * Get client profile details by clientID.
     */
    @GetMapping("/{clientId}")
    public ResponseEntity<?> getClientById(@PathVariable Long clientId) {
        Optional<ClientDB> clientOptional = clientRepo.findById(clientId);
        if (clientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Client with ID " + clientId + " not found.");
        }
        ClientDB client = clientOptional.get();
        client.setPassword(null); // Never expose password hash
        return ResponseEntity.ok(client);
    }

    /**
     * Get client profile details by email.
     */
    @GetMapping("/by-email/{email}")
    public ResponseEntity<?> getClientByEmail(@PathVariable String email) {
        Optional<ClientDB> clientOptional = clientRepo.findByEmail(email);
        if (clientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Client with email " + email + " not found.");
        }
        ClientDB client = clientOptional.get();
        client.setPassword(null);
        return ResponseEntity.ok(client);
    }

    /**
     * Update client profile.
     */
    @PutMapping("/{clientId}")
    public ResponseEntity<?> updateClient(
            @PathVariable Long clientId,
            @RequestBody ClientDB updatedData) {

        Optional<ClientDB> clientOptional = clientRepo.findById(clientId);
        if (clientOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Client with ID " + clientId + " not found.");
        }

        ClientDB client = clientOptional.get();

        if (updatedData.getFirstName() != null) client.setFirstName(updatedData.getFirstName());
        if (updatedData.getLastName() != null) client.setLastName(updatedData.getLastName());
        if (updatedData.getContactNumber() != null) client.setContactNumber(updatedData.getContactNumber());
        if (updatedData.getCompanyName() != null) client.setCompanyName(updatedData.getCompanyName());
        if (updatedData.getCompanyDetails() != null) client.setCompanyDetails(updatedData.getCompanyDetails());

        // Re-hash password if updated
        if (updatedData.getPassword() != null && !updatedData.getPassword().trim().isEmpty()) {
            String newEncoded = passwordEncoder.encode(updatedData.getPassword());
            client.setPassword(newEncoded);

            userRepo.findByEmail(client.getEmail()).ifPresent(u -> {
                u.setPassword(newEncoded);
                userRepo.save(u);
            });
        }

        ClientDB saved = clientRepo.save(client);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }
}
