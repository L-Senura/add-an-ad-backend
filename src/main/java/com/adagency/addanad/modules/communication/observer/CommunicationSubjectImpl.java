package com.adagency.addanad.modules.communication.observer;

import com.adagency.addanad.modules.communication.NotificationDB;
import com.adagency.addanad.modules.communication.NotificationRepo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CommunicationSubjectImpl: Concrete Subject (GoF Observer Design Pattern).
 */
@Service
public class CommunicationSubjectImpl implements CommunicationSubject {

    private static final Logger log = LoggerFactory.getLogger(CommunicationSubjectImpl.class);

    private final List<CommunicationObserver> observers = new CopyOnWriteArrayList<>();

    @Autowired(required = false)
    private NotificationRepo notificationRepo;

    @Autowired(required = false)
    private List<CommunicationObserver> discoveredObservers;

    @PostConstruct
    public void init() {
        if (discoveredObservers != null && !discoveredObservers.isEmpty()) {
            for (CommunicationObserver obs : discoveredObservers) {
                registerObserver(obs);
            }
            log.info("[Observer Pattern] CommunicationSubject initialized with {} observers.", observers.size());
        }
    }

    @Override
    public void registerObserver(CommunicationObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            log.info("[Observer Pattern] Registered observer: {}", observer.getObserverName());
        }
    }

    @Override
    public void removeObserver(CommunicationObserver observer) {
        if (observer != null) {
            boolean removed = observers.remove(observer);
            if (removed) {
                log.info("[Observer Pattern] Detached observer: {}", observer.getObserverName());
            }
        }
    }

    @Override
    public void notifyObservers(NotificationDB notification) {
        if (notification == null) return;

        NotificationDB savedNotification = notification;
        if (notificationRepo != null) {
            try {
                savedNotification = notificationRepo.save(notification);
            } catch (Exception e) {
                log.error("[Observer Pattern] Error persisting NotificationDB: {}", e.getMessage(), e);
            }
        }

        log.info("[Observer Pattern] Broadcasting notification ID={} ('{}') to {} observers",
                savedNotification.getNotificationID(),
                savedNotification.getTitle(),
                observers.size());

        for (CommunicationObserver observer : observers) {
            try {
                if (observer.supports(savedNotification.getRecipientRole())) {
                    observer.update(savedNotification);
                }
            } catch (Exception e) {
                log.error("[Observer Pattern] Observer '{}' error: {}", observer.getObserverName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public List<CommunicationObserver> getRegisteredObservers() {
        return Collections.unmodifiableList(observers);
    }

    public NotificationDB dispatchClientMessageEvent(Long clientId, String clientName, String messageText, Long clientMessageId) {
        String title = "New message from " + (clientName != null && !clientName.isBlank() ? clientName : "Client #" + clientId);
        String preview = messageText != null && messageText.length() > 120 ? messageText.substring(0, 117) + "..." : messageText;
        NotificationDB notification = new NotificationDB(
                NotificationDB.ROLE_ADMIN,
                null,
                NotificationDB.ROLE_CLIENT,
                clientId,
                clientName != null ? clientName : "Client #" + clientId,
                title,
                preview != null ? preview : "New chat message received.",
                NotificationDB.TYPE_CHAT_MESSAGE,
                clientMessageId
        );
        notifyObservers(notification);
        return notification;
    }

    public NotificationDB dispatchAdminMessageEvent(Long adminId, Long clientId, String adminMessage, Long adminMessageId, boolean isReply) {
        String title = isReply ? "Agency Administrator replied to your message" : "New message from Agency Administrator";
        String preview = adminMessage != null && adminMessage.length() > 120 ? adminMessage.substring(0, 117) + "..." : adminMessage;
        NotificationDB notification = new NotificationDB(
                NotificationDB.ROLE_CLIENT,
                clientId,
                NotificationDB.ROLE_ADMIN,
                adminId,
                "Agency Administrator",
                title,
                preview != null ? preview : "You have a new message from the agency administration.",
                isReply ? NotificationDB.TYPE_CHAT_REPLY : NotificationDB.TYPE_CHAT_MESSAGE,
                adminMessageId
        );
        notifyObservers(notification);
        return notification;
    }

    public NotificationDB dispatchClientToAdminReviewEvent(Long clientId, String clientName, Integer rating, String reviewTitle, Long reviewId) {
        String title = "New Agency Review (" + (rating != null ? rating : 5) + " Stars) from " + (clientName != null ? clientName : "Client #" + clientId);
        String body = reviewTitle != null && !reviewTitle.isBlank() ? reviewTitle : "A client submitted a confidential review regarding completed services.";
        NotificationDB notification = new NotificationDB(
                NotificationDB.ROLE_ADMIN,
                null,
                NotificationDB.ROLE_CLIENT,
                clientId,
                clientName != null ? clientName : "Client #" + clientId,
                title,
                body,
                NotificationDB.TYPE_CLIENT_REVIEW,
                reviewId
        );
        notifyObservers(notification);
        return notification;
    }

    public NotificationDB dispatchPublicReviewEvent(Long clientId, String clientName, String reviewerName, Integer rating, String reviewTitle, Long reviewId) {
        String visitor = reviewerName != null && !reviewerName.isBlank() ? reviewerName : "Public Visitor";
        String title = "New " + (rating != null ? rating : 5) + "-Star Public Review from " + visitor;
        String body = reviewTitle != null && !reviewTitle.isBlank() ? reviewTitle : "A visitor left a review on your public brand profile.";
        NotificationDB notification = new NotificationDB(
                NotificationDB.ROLE_CLIENT,
                clientId,
                NotificationDB.ROLE_VISITOR,
                null,
                visitor,
                title,
                body,
                NotificationDB.TYPE_PUBLIC_REVIEW,
                reviewId
        );
        notifyObservers(notification);
        return notification;
    }
}
