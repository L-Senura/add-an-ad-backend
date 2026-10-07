package com.adagency.addanad.modules.communication.observer;

import com.adagency.addanad.modules.communication.NotificationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * EmailDispatchObserver: Concrete Observer simulating external email alerts.
 */
@Component
public class EmailDispatchObserver implements CommunicationObserver {

    private static final Logger log = LoggerFactory.getLogger(EmailDispatchObserver.class);

    @Override
    public void update(NotificationDB notification) {
        String msg = notification.getMessage();
        String preview = msg != null && msg.length() > 60 ? msg.substring(0, 57) + "..." : msg;
        log.info("[Observer: EmailDispatchObserver] Simulated Email Queue -> To: [Role={}, ID={}], Subject: '{}', Body Preview: '{}'",
                notification.getRecipientRole(),
                notification.getRecipientID() != null ? notification.getRecipientID() : "ALL",
                notification.getTitle(),
                preview);
    }

    @Override
    public String getObserverName() {
        return "EmailDispatchObserver";
    }

    @Override
    public boolean supports(String recipientRole) {
        return true;
    }
}
