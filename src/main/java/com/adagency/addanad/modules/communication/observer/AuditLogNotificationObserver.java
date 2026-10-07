package com.adagency.addanad.modules.communication.observer;

import com.adagency.addanad.modules.communication.NotificationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AuditLogNotificationObserver: Concrete Observer for central audit logging.
 */
@Component
public class AuditLogNotificationObserver implements CommunicationObserver {

    private static final Logger log = LoggerFactory.getLogger(AuditLogNotificationObserver.class);

    @Override
    public void update(NotificationDB notification) {
        log.info("[Observer: AuditLogNotificationObserver] AUDIT LOG: Event='{}', SenderRole='{}', SenderID='{}', RecipientRole='{}', RecipientID='{}', Title='{}', CreatedAt='{}'",
                notification.getNotificationType(),
                notification.getSenderRole(),
                notification.getSenderID(),
                notification.getRecipientRole(),
                notification.getRecipientID(),
                notification.getTitle(),
                notification.getCreatedAt());
    }

    @Override
    public String getObserverName() {
        return "AuditLogNotificationObserver";
    }

    @Override
    public boolean supports(String recipientRole) {
        return true;
    }
}
