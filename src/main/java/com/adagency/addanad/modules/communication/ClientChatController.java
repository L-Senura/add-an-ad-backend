package com.adagency.addanad.modules.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}