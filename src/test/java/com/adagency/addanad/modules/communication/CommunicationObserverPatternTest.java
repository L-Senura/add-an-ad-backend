package com.adagency.addanad.modules.communication;

import com.adagency.addanad.modules.communication.observer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying the GoF Observer Design Pattern implementation
 * in the Communication module.
 */
@ExtendWith(MockitoExtension.class)
class CommunicationObserverPatternTest {

    private CommunicationSubjectImpl subject;

    @Mock
    private NotificationRepo notificationRepo;

    private ClientNotificationObserver clientObserver;
    private AdminNotificationObserver adminObserver;
    private AuditLogNotificationObserver auditObserver;
    private EmailDispatchObserver emailObserver;

    @BeforeEach
    void setUp() {
        subject = new CommunicationSubjectImpl();
        clientObserver = new ClientNotificationObserver();
        adminObserver = new AdminNotificationObserver();
        auditObserver = new AuditLogNotificationObserver();
        emailObserver = new EmailDispatchObserver();

        subject.registerObserver(clientObserver);
        subject.registerObserver(adminObserver);
        subject.registerObserver(auditObserver);
        subject.registerObserver(emailObserver);
    }

    @Test
    void testObserversRegisteredSuccessfully() {
        List<CommunicationObserver> observers = subject.getRegisteredObservers();
        assertEquals(4, observers.size());
        assertTrue(observers.contains(clientObserver));
        assertTrue(observers.contains(adminObserver));
        assertTrue(observers.contains(auditObserver));
        assertTrue(observers.contains(emailObserver));
    }

    @Test
    void testObserverRemoval() {
        subject.removeObserver(emailObserver);
        List<CommunicationObserver> observers = subject.getRegisteredObservers();
        assertEquals(3, observers.size());
        assertFalse(observers.contains(emailObserver));
    }

    @Test
    void testDispatchClientMessageNotifiesObservers() {
        AtomicInteger customObserverCount = new AtomicInteger(0);
        CommunicationObserver mockObserver = new CommunicationObserver() {
            @Override
            public void update(NotificationDB notification) {
                if ("ADMIN".equals(notification.getRecipientRole())) {
                    customObserverCount.incrementAndGet();
                }
            }

            @Override
            public String getObserverName() {
                return "MockAdminObserver";
            }

            @Override
            public boolean supports(String recipientRole) {
                return "ADMIN".equalsIgnoreCase(recipientRole);
            }
        };

        subject.registerObserver(mockObserver);

        // Client sends message -> should target ADMIN
        subject.dispatchClientMessageEvent(101L, "Apex Brand", "Need urgent update on ad", 501L);

        assertEquals(1, customObserverCount.get());
    }

    @Test
    void testDispatchAdminMessageNotifiesClientObservers() {
        AtomicInteger customClientObserverCount = new AtomicInteger(0);
        CommunicationObserver mockObserver = new CommunicationObserver() {
            @Override
            public void update(NotificationDB notification) {
                if ("CLIENT".equals(notification.getRecipientRole())) {
                    customClientObserverCount.incrementAndGet();
                }
            }

            @Override
            public String getObserverName() {
                return "MockClientObserver";
            }

            @Override
            public boolean supports(String recipientRole) {
                return "CLIENT".equalsIgnoreCase(recipientRole);
            }
        };

        subject.registerObserver(mockObserver);

        // Admin sends message -> should target CLIENT
        subject.dispatchAdminMessageEvent(1L, 101L, "Ad campaign has been scheduled.", 601L, false);

        assertEquals(1, customClientObserverCount.get());
    }

    @Test
    void testRoleSupportFiltering() {
        assertTrue(clientObserver.supports(NotificationDB.ROLE_CLIENT));
        assertFalse(clientObserver.supports(NotificationDB.ROLE_ADMIN));
        assertTrue(clientObserver.supports(NotificationDB.ROLE_ALL));

        assertTrue(adminObserver.supports(NotificationDB.ROLE_ADMIN));
        assertFalse(adminObserver.supports(NotificationDB.ROLE_CLIENT));
        assertTrue(adminObserver.supports(NotificationDB.ROLE_ALL));

        assertTrue(auditObserver.supports(NotificationDB.ROLE_CLIENT));
        assertTrue(auditObserver.supports(NotificationDB.ROLE_ADMIN));

        assertTrue(emailObserver.supports(NotificationDB.ROLE_CLIENT));
        assertTrue(emailObserver.supports(NotificationDB.ROLE_ADMIN));
    }
}
