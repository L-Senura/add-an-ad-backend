package com.adagency.addanad.modules.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin_chat")
public class AdminChatController {

    @Autowired
    private ChatRepo chatRepo;

    //Get client's chat details with clientID
    @GetMapping({ "/{adminId}/client/{clientId}"})
    public List<ChatDB> getClientMessages(@PathVariable Long clientId) {
        return chatRepo.findByClientId(clientId);
    }

    //Admin sends a msg to client
    @PostMapping({"/{adminId}/send/{clientId}"})
    public ChatDB sendAdminMessageToClient(
            @PathVariable Long adminId,
            @PathVariable Long clientId,
            @RequestBody ChatDB chat) {
        chat.setAdminId(adminId);
        chat.setClientId(clientId);
        return chatRepo.save(chat);
    }

    //Admin replies to an existing client message by adminId + clientID + client msgID
    @PostMapping(value = "/{adminId}/{clientID}/reply/{id}")
    public ChatDB replyChatWithAdminId(
            @PathVariable Long adminId,
            @PathVariable Long id,
            @RequestBody ChatDB replyData) {
        return chatRepo.findById(id).map(chat -> {
            chat.setAdminId(adminId);
            chat.setAdminMessage(replyData.getAdminMessage());
            return chatRepo.save(chat);
        }).orElse(null);
    }

    //View messages sent by an Admin with adminID
    @GetMapping({"/admin/{adminId}"})
    public List<ChatDB> getMessagesByAdminId(@PathVariable Long adminId) {
        return chatRepo.findByAdminId(adminId);
    }

    //Admin updates he sent message by adminId and message ID
    @PutMapping(value = {"/{adminId}/update/{id}"})
    public ResponseEntity<?> updateAdminMessage(
            @PathVariable Long adminId,
            @PathVariable Long id,
            @RequestBody ChatDB updatedData) {
        Optional<ChatDB> chatOptional = chatRepo.findById(id);

        if (chatOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Message with ID " + id + " not found.");
        }

        ChatDB chat = chatOptional.get();

        // Ensure admin can only update messages they sent/replied to
        if (chat.getAdminId() == null || !chat.getAdminId().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only update your own sent messages.");
        }

        if (updatedData.getAdminMessage() != null) {
            chat.setAdminMessage(updatedData.getAdminMessage());
        }

        ChatDB saved = chatRepo.save(chat);
        return ResponseEntity.ok(saved);
    }
/**
    //Admin updates his own sent message (fallback when adminId is in query param or body)
    @PutMapping(value = {"/update/{id}"})
    public ResponseEntity<?> updateOwnMessageWithParamOrBody(
            @PathVariable Long id,
            @RequestParam(required = false) Long adminId,
            @RequestBody ChatDB updatedData) {
        Long effectiveAdminId = (adminId != null) ? adminId : updatedData.getAdminId();
        if (effectiveAdminId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Admin ID is required to verify message ownership.");
        }
        return updateAdminMessage(effectiveAdminId, id, updatedData);
    }
**/
    //Admin deletes he sent message by adminId and message ID
    @DeleteMapping({"/{adminId}/delete/{id}"})
    public ResponseEntity<String> deleteAdminMessage(
            @PathVariable Long adminId,
            @PathVariable Long id) {
        Optional<ChatDB> chatOptional = chatRepo.findById(id);

        if (chatOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Message with ID " + id + " not found.");
        }

        ChatDB chat = chatOptional.get();

        // Ensure admin can only delete messages they sent/replied to
        if (chat.getAdminId() == null || !chat.getAdminId().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only delete your own sent messages.");
        }

        chatRepo.delete(chat);
        return ResponseEntity.ok("Message with ID " + id + " deleted successfully.");
    }

/**
    //Admin deletes his own sent message (fallback when adminId is passed as query param)
    @DeleteMapping({"/delete/{id}"})
    public ResponseEntity<String> deleteOwnMessageWithParam(
            @PathVariable Long id,
            @RequestParam(required = false) Long adminId) {
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Admin ID is required as a query parameter (e.g. ?adminId=...) to verify ownership.");
        }
        return deleteAdminMessage(adminId, id);
    }
 **/
}