package com.adagency.addanad.modules.communication;

import com.adagency.addanad.modules.communication.observer.CommunicationObserver;
import com.adagency.addanad.modules.communication.observer.CommunicationSubjectImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepo notificationRepo;

    @Autowired
    private CommunicationSubjectImpl communicationSubject;

    @GetMapping("/client/{clientId}")
    public List<NotificationDB> getClientNotifications(@PathVariable Long clientId) {
        return notificationRepo.findForClient(clientId);
    }

    @GetMapping("/client/{clientId}/unread-count")
    public Map<String, Object> getClientUnreadCount(@PathVariable Long clientId) {
        long count = notificationRepo.countUnreadForClient(clientId);
        Map<String, Object> res = new HashMap<>();
        res.put("clientId", clientId);
        res.put("unreadCount", count);
        return res;
    }

    @PutMapping("/client/{clientId}/mark-all-read")
    public ResponseEntity<?> markAllClientNotificationsRead(@PathVariable Long clientId) {
        List<NotificationDB> list = notificationRepo.findForClient(clientId);
        for (NotificationDB n : list) {
            if (!Boolean.TRUE.equals(n.getIsRead())) {
                n.setIsRead(true);
            }
        }
        notificationRepo.saveAll(list);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read for client " + clientId, "count", list.size()));
    }

    @GetMapping({"/admin/all", "/admin"})
    public List<NotificationDB> getAllAdminNotifications() {
        return notificationRepo.findByRecipientRoleOrderByCreatedAtDesc(NotificationDB.ROLE_ADMIN);
    }

    @GetMapping("/admin/{adminId}")
    public List<NotificationDB> getAdminNotifications(@PathVariable Long adminId) {
        return notificationRepo.findForAdmin(adminId);
    }

    @GetMapping({"/admin/unread-count", "/admin/{adminId}/unread-count"})
    public Map<String, Object> getAdminUnreadCount(@PathVariable(required = false) Long adminId) {
        long count = notificationRepo.countUnreadForAdmin(adminId);
        Map<String, Object> res = new HashMap<>();
        res.put("adminId", adminId);
        res.put("unreadCount", count);
        return res;
    }

    @PutMapping("/admin/mark-all-read")
    public ResponseEntity<?> markAllAdminNotificationsRead() {
        List<NotificationDB> list = notificationRepo.findByRecipientRoleOrderByCreatedAtDesc(NotificationDB.ROLE_ADMIN);
        for (NotificationDB n : list) {
            if (!Boolean.TRUE.equals(n.getIsRead())) {
                n.setIsRead(true);
            }
        }
        notificationRepo.saveAll(list);
        return ResponseEntity.ok(Map.of("message", "All administrator notifications marked as read", "count", list.size()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        Optional<NotificationDB> opt = notificationRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Notification with ID " + id + " not found."));
        }
        NotificationDB n = opt.get();
        n.setIsRead(true);
        NotificationDB saved = notificationRepo.save(n);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        Optional<NotificationDB> opt = notificationRepo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Notification with ID " + id + " not found."));
        }
        notificationRepo.delete(opt.get());
        return ResponseEntity.ok(Map.of("message", "Notification with ID " + id + " deleted successfully."));
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationDB notification) {
        if (notification.getTitle() == null || notification.getTitle().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Notification title is required."));
        }
        if (notification.getMessage() == null || notification.getMessage().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Notification message is required."));
        }
        if (notification.getRecipientRole() == null || notification.getRecipientRole().isBlank()) {
            notification.setRecipientRole(NotificationDB.ROLE_ALL);
        }

        communicationSubject.notifyObservers(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @GetMapping("/observers")
    public List<Map<String, Object>> getRegisteredObservers() {
        List<CommunicationObserver> observers = communicationSubject.getRegisteredObservers();
        List<Map<String, Object>> result = new ArrayList<>();
        for (CommunicationObserver obs : observers) {
            Map<String, Object> info = new HashMap<>();
            info.put("name", obs.getObserverName());
            info.put("class", obs.getClass().getSimpleName());
            info.put("handlesClient", obs.supports(NotificationDB.ROLE_CLIENT));
            info.put("handlesAdmin", obs.supports(NotificationDB.ROLE_ADMIN));
            info.put("handlesAll", obs.supports(NotificationDB.ROLE_ALL));
            result.add(info);
        }
        return result;
    }
}
