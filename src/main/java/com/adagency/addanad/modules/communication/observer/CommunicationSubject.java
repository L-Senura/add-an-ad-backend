package com.adagency.addanad.modules.communication.observer;

import com.adagency.addanad.modules.communication.NotificationDB;
import java.util.List;

/**
 * CommunicationSubject: Subject / Observable Interface (GoF Observer Design Pattern).
 */
public interface CommunicationSubject {

    void registerObserver(CommunicationObserver observer);

    void removeObserver(CommunicationObserver observer);

    void notifyObservers(NotificationDB notification);

    List<CommunicationObserver> getRegisteredObservers();
}
