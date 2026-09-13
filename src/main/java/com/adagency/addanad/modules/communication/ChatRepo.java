package com.adagency.addanad.modules.communication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// @Repository: Marks this interface as a Spring Data repository mechanism for encapsulating storage, retrieval, and search behavior.
@Repository
public interface ChatRepo extends JpaRepository<ChatDB, Long> {

    /**
     * Query to find all messages related to a specific client.
     * Spring Data JPA automatically derives: SELECT * FROM ChatDB WHERE clientId = ?
     *
     * @param clientId The ID of the client
     * @return List of ChatDB records associated with the given client ID
     */
    List<ChatDB> findByClientId(Long clientId);

    /**
     * Query to find all messages handled or sent by a specific admin.
     * Spring Data JPA automatically derives: SELECT * FROM ChatDB WHERE adminId = ?
     *
     * @param adminId The ID of the admin
     * @return List of ChatDB records associated with the given admin ID
     */
    List<ChatDB> findByAdminId(Long adminId);
}