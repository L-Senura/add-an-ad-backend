package com.adagency.addanad.modules.communication;

import com.adagency.addanad.modules.client.ClientDB;
import com.adagency.addanad.modules.client.ClientRepo;
import com.adagency.addanad.modules.communication.observer.CommunicationSubjectImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/client_chat")
public class ClientChatController {

    @Autowired
    private ClientChatRepo clientChatRepo;

    @Autowired
    private AdminChatRepo adminChatRepo;

    @Autowired(required = false)
    private ClientRepo clientRepo;

    @Autowired(required = false)
    private CommunicationSubjectImpl communicationSubject;

    @PostMapping({"/{clientId}/send"})
    public ClientChatDB sendClientMessage(@PathVariable Long clientId, @RequestBody ClientChatDB chat) {
        chat.setClientID(clientId);
        ClientChatDB saved = clientChatRepo.save(chat);

        if (communicationSubject != null) {
            String clientName = null;
            if (clientRepo != null) {
                try {
                    clientName = clientRepo.findById(clientId)
                            .map(ClientDB::getCompanyName)
                            .orElse(null);
                } catch (Exception ignored) {
                }
            }
            communicationSubject.dispatchClientMessageEvent(
                    clientId,
                    clientName,
                    saved.getClientMessage(),
                    saved.getClientMessageID()
            );
        }

        return saved;
    }

    @GetMapping({"/{clientId}/messages"})
    public List<ClientChatDB> getClientMessages(@PathVariable Long clientId) {
        return clientChatRepo.findByClientID(clientId);
    }

    @GetMapping({"/{clientId}/admin_messages", "/{clientId}/admin-messages"})
    public List<AdminChatDB> getAdminMessagesForClient(@PathVariable Long clientId) {
        return adminChatRepo.findByClientID(clientId);
    }

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

        if (chat.getClientID() == null || !chat.getClientID().equals(clientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only delete your own sent messages.");
        }

        clientChatRepo.delete(chat);
        return ResponseEntity.ok("Message with ID " + id + " deleted successfully.");
    }
}
