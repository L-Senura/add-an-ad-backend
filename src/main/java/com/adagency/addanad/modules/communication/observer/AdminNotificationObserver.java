package com.adagency.addanad.modules.communication.observer;

import com.adagency.addanad.modules.communication.NotificationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AdminNotificationObserver: Concrete Observer for Admin-directed events.
 */
@Component
public class AdminNotificationObserver implements CommunicationObserver {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationObserver.class);

    @Override
    public void update(NotificationDB notification) {
        log.info("[Observer: AdminNotificationObserver] Dispatched notification ID={} to Admin ID={} -> Title: '{}'",
                notification.getNotificationID(),
                notification.getRecipientID() != null ? notification.getRecipientID() : "ALL_ADMINS",
                notification.getTitle());
    }

    @Override
    public String getObserverName() {
        return "AdminNotificationObserver";
    }

    @Override
    public boolean supports(String recipientRole) {
        return NotificationDB.ROLE_ADMIN.equalsIgnoreCase(recipientRole)
                || NotificationDB.ROLE_ALL.equalsIgnoreCase(recipientRole);
    }
}
