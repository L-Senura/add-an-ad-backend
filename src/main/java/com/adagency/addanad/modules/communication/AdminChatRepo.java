package com.adagency.addanad.modules.communication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository: Marks this interface as a Spring Data repository for AdminChatDB CRUD operations.
@Repository
public interface AdminChatRepo extends JpaRepository<AdminChatDB, Long> {

    /**
     * Query to find all messages sent/replied by a specific admin.
     * Spring Data JPA derives: SELECT * FROM AdminChatDB WHERE adminID = ?
     *
     * @param adminID The ID of the admin
     * @return List of AdminChatDB records associated with the given admin ID
     */
    List<AdminChatDB> findByAdminID(Long adminID);

    /**
     * Query to find all messages/replies sent to a specific client.
     * Spring Data JPA derives: SELECT * FROM AdminChatDB WHERE clientID = ?
     *
     * @param clientID The ID of the client
     * @return List of AdminChatDB records associated with the given client ID
     */
    List<AdminChatDB> findByClientID(Long clientID);

    /**
     * Query to find admin reply linked to a specific client message ID.
     * Spring Data JPA derives: SELECT * FROM AdminChatDB WHERE clientMessageID = ?
     *
     * @param clientMessageID The ID of the client message
     * @return List of AdminChatDB records linked to the client message ID
     */
    List<AdminChatDB> findByClientMessageID(Long clientMessageID);
}
