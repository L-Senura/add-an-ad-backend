package com.adagency.addanad.modules.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// @RestController: Marks this class as a Spring REST Controller where methods return JSON responses automatically.
@RestController
// @RequestMapping: Base URL path for all client chat endpoints.
@RequestMapping("/api/client_chat")
public class ClientChatController {

    // @Autowired: Injects the ClientChatRepo interface to perform database CRUD operations.
    @Autowired
    private ClientChatRepo clientChatRepo;

    //Client sends msg with unique ID
    @PostMapping({"/{clientId}/send"})
    public ClientChatDB sendClientMessage(@PathVariable Long clientId, @RequestBody ClientChatDB chat) {
        chat.setClientID(clientId);
        return clientChatRepo.save(chat);
    }

    //View all client specified messages with clientID
    @GetMapping({ "/{clientId}/messages"})
    public List<ClientChatDB> getClientMessages(@PathVariable Long clientId) {
        return clientChatRepo.findByClientID(clientId);
    }

    //Client's sent message update
    @PutMapping(value = {"/{clientId}/update/{id}"})
    public ResponseEntity<?> updateClientMessage(
            @PathVariable Long clientId,
            @PathVariable Long id,
            @RequestBody ClientChatDB updatedData) {
        Optional<ClientChatDB> chatOptional = clientChatRepo.findById(id);

        if (chatOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Message with ID " + id + " not found.");
        }

        ClientChatDB chat = chatOptional.get();

        // Ensure client can only update messages they sent
        if (chat.getClientID() == null || !chat.getClientID().equals(clientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only update your own sent messages.");
        }

        if (updatedData.getClientMessage() != null) {
            chat.setClientMessage(updatedData.getClientMessage());
        }

        ClientChatDB saved = clientChatRepo.save(chat);
        return ResponseEntity.ok(saved);
    }

    //Client's sent message delete
    @DeleteMapping({"/{clientId}/delete/{id}"})
    public ResponseEntity<String> deleteClientMessage(
            @PathVariable Long clientId,
            @PathVariable Long id) {
        Optional<ClientChatDB> chatOptional = clientChatRepo.findById(id);

        if (chatOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Message with ID " + id + " not found.");
        }

        ClientChatDB chat = chatOptional.get();

        // Ensure client can only delete messages they sent
        if (chat.getClientID() == null || !chat.getClientID().equals(clientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only delete your own sent messages.");
        }

        clientChatRepo.delete(chat);
        return ResponseEntity.ok("Message with ID " + id + " deleted successfully.");
    }
}