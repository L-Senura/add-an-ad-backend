package com.adagency.addanad.modules.communication;

import com.adagency.addanad.modules.communication.observer.CommunicationSubjectImpl;
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

    @Autowired(required = false)
    private CommunicationSubjectImpl communicationSubject;

    @GetMapping({"/{adminId}/client/{clientId}"})
    public List<ClientChatDB> getClientMessages(@PathVariable Long clientId) {
        return clientChatRepo.findByClientID(clientId);
    }

    @GetMapping({"/to_client/{clientId}", "/client/{clientId}/messages"})
    public List<AdminChatDB> getAdminMessagesToClient(@PathVariable Long clientId) {
        return adminChatRepo.findByClientID(clientId);
    }

    @PostMapping({"/{adminId}/send/{clientId}"})
    public AdminChatDB sendAdminMessageToClient(
            @PathVariable Long adminId,
            @PathVariable Long clientId,
            @RequestBody AdminChatDB chat) {
        chat.setAdminID(adminId);
        chat.setClientID(clientId);
        AdminChatDB saved = adminChatRepo.save(chat);

        if (communicationSubject != null) {
            communicationSubject.dispatchAdminMessageEvent(
                    adminId,
                    clientId,
                    saved.getAdminMessage(),
                    saved.getAdminMessageID(),
                    false
            );
        }

        return saved;
    }

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

        if (communicationSubject != null) {
            communicationSubject.dispatchAdminMessageEvent(
                    adminId,
                    effectiveClientId,
                    saved.getAdminMessage(),
                    saved.getAdminMessageID(),
                    true
            );
        }

        return ResponseEntity.ok(saved);
    }

    @GetMapping({"/admin/{adminId}"})
    public List<AdminChatDB> getMessagesByAdminId(@PathVariable Long adminId) {
        return adminChatRepo.findByAdminID(adminId);
    }

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

        if (chat.getAdminID() == null || !chat.getAdminID().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only delete your own sent messages.");
        }

        adminChatRepo.delete(chat);
        return ResponseEntity.ok("Message with ID " + id + " deleted successfully.");
    }
}
