package com.adagency.addanad.modules.communication.observer;

import com.adagency.addanad.modules.communication.NotificationDB;

/**
 * CommunicationObserver: Observer Interface (GoF Observer Design Pattern).
 */
public interface CommunicationObserver {

    void update(NotificationDB notification);

    String getObserverName();

    boolean supports(String recipientRole);
}
