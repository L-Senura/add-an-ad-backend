package com.adagency.addanad.modules.communication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * NotificationRepo: Spring Data JPA repository for NotificationDB.
 */
@Repository
public interface NotificationRepo extends JpaRepository<NotificationDB, Long> {

    @Query("SELECT n FROM NotificationDB n WHERE n.recipientRole = 'CLIENT' AND (n.recipientID = :clientId OR n.recipientID IS NULL) ORDER BY n.createdAt DESC")
    List<NotificationDB> findForClient(@Param("clientId") Long clientId);

    @Query("SELECT COUNT(n) FROM NotificationDB n WHERE n.recipientRole = 'CLIENT' AND (n.recipientID = :clientId OR n.recipientID IS NULL) AND n.isRead = false")
    long countUnreadForClient(@Param("clientId") Long clientId);

    @Query("SELECT n FROM NotificationDB n WHERE n.recipientRole = 'ADMIN' AND (:adminId IS NULL OR n.recipientID = :adminId OR n.recipientID IS NULL) ORDER BY n.createdAt DESC")
    List<NotificationDB> findForAdmin(@Param("adminId") Long adminId);

    List<NotificationDB> findByRecipientRoleOrderByCreatedAtDesc(String recipientRole);

    @Query("SELECT COUNT(n) FROM NotificationDB n WHERE n.recipientRole = 'ADMIN' AND (:adminId IS NULL OR n.recipientID = :adminId OR n.recipientID IS NULL) AND n.isRead = false")
    long countUnreadForAdmin(@Param("adminId") Long adminId);

    List<NotificationDB> findAllByOrderByCreatedAtDesc();
}
