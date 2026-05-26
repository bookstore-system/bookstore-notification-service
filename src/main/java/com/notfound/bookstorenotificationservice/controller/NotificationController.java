package com.notfound.bookstorenotificationservice.controller;

import com.notfound.bookstorenotificationservice.model.dto.NotificationRequestDto;
import com.notfound.bookstorenotificationservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendEmailFallback(@RequestBody NotificationRequestDto request) {
        notificationService.sendEmailFallback(request);
        return ResponseEntity.ok("Email sent successfully (simulated fallback)");
    }
}
