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
    private AdminChatRepo adminChatRepo;

    @Autowired
    private ClientChatRepo clientChatRepo;

    //Get client's chat details with clientID
    @GetMapping({"/{adminId}/client/{clientId}"})
    public List<ClientChatDB> getClientMessages(@PathVariable Long clientId) {
        return clientChatRepo.findByClientID(clientId);
    }

    //View all admin messages sent to a specific client
    @GetMapping({"/to_client/{clientId}", "/client/{clientId}/messages"})
    public List<AdminChatDB> getAdminMessagesToClient(@PathVariable Long clientId) {
        return adminChatRepo.findByClientID(clientId);
    }

    //Admin sends a msg to client
    @PostMapping({"/{adminId}/send/{clientId}"})
    public AdminChatDB sendAdminMessageToClient(
            @PathVariable Long adminId,
            @PathVariable Long clientId,
            @RequestBody AdminChatDB chat) {
        chat.setAdminID(adminId);
        chat.setClientID(clientId);
        return adminChatRepo.save(chat);
    }

    //Admin replies to an existing client message by adminId + clientID + client msgID
    @PostMapping(value = {"/{adminId}/{clientID}/reply/{clientMessageID}"})
    public ResponseEntity<?> replyChatWithAdminId(
            @PathVariable Long adminId,
            @PathVariable(required = false) Long clientID,
            @PathVariable Long clientMessageID,
            @RequestBody AdminChatDB replyData) {
        Optional<ClientChatDB> clientChatOptional = clientChatRepo.findById(clientMessageID);
        if (clientChatOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Client message with ID " + clientMessageID + " not found.");
        }

        ClientChatDB clientChat = clientChatOptional.get();
        Long effectiveClientId = (clientID != null) ? clientID : clientChat.getClientID();

        replyData.setAdminID(adminId);
        replyData.setClientID(effectiveClientId);
        replyData.setClientMessageID(clientMessageID);
        AdminChatDB saved = adminChatRepo.save(replyData);
        return ResponseEntity.ok(saved);
    }

    //View messages sent by an Admin with adminID
    @GetMapping({"/admin/{adminId}"})
    public List<AdminChatDB> getMessagesByAdminId(@PathVariable Long adminId) {
        return adminChatRepo.findByAdminID(adminId);
    }

    //Admin updates his own sent message by adminId and message ID (adminMessageID)
    @PutMapping({"/{adminId}/update/{id}"})
    public ResponseEntity<?> updateAdminMessage(
            @PathVariable Long adminId,
            @PathVariable Long id,
            @RequestBody AdminChatDB updatedData) {
        Optional<AdminChatDB> chatOptional = adminChatRepo.findById(id);

        if (chatOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin message with ID " + id + " not found.");
        }

        AdminChatDB chat = chatOptional.get();

        // Ensure admin can only update messages they sent
        if (chat.getAdminID() == null || !chat.getAdminID().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only update your own sent messages.");
        }

        if (updatedData.getAdminMessage() != null) {
            chat.setAdminMessage(updatedData.getAdminMessage());
        }

        AdminChatDB saved = adminChatRepo.save(chat);
        return ResponseEntity.ok(saved);
    }

    //Admin deletes his own sent message by adminId and message ID (adminMessageID)
    @DeleteMapping({"/{adminId}/delete/{id}"})
    public ResponseEntity<String> deleteAdminMessage(
            @PathVariable Long adminId,
            @PathVariable Long id) {
        Optional<AdminChatDB> chatOptional = adminChatRepo.findById(id);

        if (chatOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin message with ID " + id + " not found.");
        }

        AdminChatDB chat = chatOptional.get();

        // Ensure admin can only delete messages they sent
        if (chat.getAdminID() == null || !chat.getAdminID().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only delete your own sent messages.");
        }

        adminChatRepo.delete(chat);
        return ResponseEntity.ok("Message with ID " + id + " deleted successfully.");
    }
}
