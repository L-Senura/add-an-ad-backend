package com.adagency.addanad.modules.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ClientChatController {

    @Autowired
    private Repo repo;

    @PostMapping("/add")
    public ChatDB addChat(@RequestBody ChatDB e){
        return repo.save(e);
    }
}