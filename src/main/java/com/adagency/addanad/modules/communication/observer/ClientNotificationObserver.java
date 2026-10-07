package com.adagency.addanad.modules.communication.observer;

import com.adagency.addanad.modules.communication.NotificationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ClientNotificationObserver: Concrete Observer for Client-directed events.
 */
@Component
public class ClientNotificationObserver implements CommunicationObserver {

    private static final Logger log = LoggerFactory.getLogger(ClientNotificationObserver.class);

    @Override
    public void update(NotificationDB notification) {
        log.info("[Observer: ClientNotificationObserver] Dispatched notification ID={} to Client ID={} -> Title: '{}'",
                notification.getNotificationID(),
                notification.getRecipientID() != null ? notification.getRecipientID() : "ALL_CLIENTS",
                notification.getTitle());
    }

    @Override
    public String getObserverName() {
        return "ClientNotificationObserver";
    }

    @Override
    public boolean supports(String recipientRole) {
        return NotificationDB.ROLE_CLIENT.equalsIgnoreCase(recipientRole)
                || NotificationDB.ROLE_ALL.equalsIgnoreCase(recipientRole);
    }
}
