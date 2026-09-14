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

    // @Autowired: Injects the ChatRepo interface to perform database CRUD operations.
    @Autowired
    private ChatRepo chatRepo;

    //Client sends msg with unique ID
    @PostMapping({"/{clientId}/send"})
    public ChatDB sendClientMessage(@PathVariable Long clientId, @RequestBody ChatDB chat) {
        chat.setClientId(clientId);
        return chatRepo.save(chat);
    }

    //View all client specified messages with clientID
    @GetMapping({"/{clientId}", "/{clientId}/messages", "/{clientId}/see"})
    public List<ChatDB> getClientMessages(@PathVariable Long clientId) {
        return chatRepo.findByClientId(clientId);
    }

    //Client's sent message update
    @PutMapping(value = {"/{clientId}/update/{id}"})
    public ResponseEntity<?> updateClientMessage(
            @PathVariable Long clientId,
            @PathVariable Long id,
            @RequestBody ChatDB updatedData) {
        Optional<ChatDB> chatOptional = chatRepo.findById(id);

        if (chatOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Message with ID " + id + " not found.");
        }

        ChatDB chat = chatOptional.get();

        // Ensure client can only update messages they sent
        if (chat.getClientId() == null || !chat.getClientId().equals(clientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only update your own sent messages.");
        }

        if (updatedData.getClientMessage() != null) {
            chat.setClientMessage(updatedData.getClientMessage());
        }

        ChatDB saved = chatRepo.save(chat);
        return ResponseEntity.ok(saved);
    }

/**
    //Client updates his own sent message (fallback when clientId is in query param or body)
    @RequestMapping(value = {"/update/{id}"}, method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateOwnMessageWithParamOrBody(
            @PathVariable Long id,
            @RequestParam(required = false) Long clientId,
            @RequestBody ChatDB updatedData) {
        Long effectiveClientId = (clientId != null) ? clientId : updatedData.getClientId();
        if (effectiveClientId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Client ID is required to verify message ownership.");
        }
        return updateClientMessage(effectiveClientId, id, updatedData);
    }
**/

    //Client's sent message delete
    @DeleteMapping({"/{clientId}/delete/{id}"})
    public ResponseEntity<String> deleteClientMessage(
            @PathVariable Long clientId,
            @PathVariable Long id) {
        Optional<ChatDB> chatOptional = chatRepo.findById(id);

        if (chatOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Message with ID " + id + " not found.");
        }

        ChatDB chat = chatOptional.get();

        // Ensure client can only delete messages they sent
        if (chat.getClientId() == null || !chat.getClientId().equals(clientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only delete your own sent messages.");
        }

        chatRepo.delete(chat);
        return ResponseEntity.ok("Message with ID " + id + " deleted successfully.");
    }

/**
    //Client deletes his own sent message (fallback when clientId is passed as query param)
    @DeleteMapping({"/delete/{id}"})
    public ResponseEntity<String> deleteOwnMessageWithParam(
            @PathVariable Long id,
            @RequestParam(required = false) Long clientId) {
        if (clientId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Client ID is required as a query parameter (e.g. ?clientId=...) to verify ownership.");
        }
        return deleteClientMessage(clientId, id);
    }
**/
}