package com.adagency.addanad.modules.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}