package com.adagency.addanad.modules.communication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository: Marks this interface as a Spring Data repository for ClientChatDB CRUD operations.
@Repository
public interface ClientChatRepo extends JpaRepository<ClientChatDB, Long> {

    /**
     * Query to find all messages sent by a specific client.
     * Spring Data JPA derives: SELECT * FROM ClientChatDB WHERE clientID = ?
     *
     * @param clientID The ID of the client
     * @return List of ClientChatDB records associated with the given client ID
     */
    List<ClientChatDB> findByClientID(Long clientID);
}
