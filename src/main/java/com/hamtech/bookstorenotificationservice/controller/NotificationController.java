package com.hamtech.bookstorenotificationservice.controller;

import com.hamtech.bookstorenotificationservice.model.dto.NotificationRequestDto;
import com.hamtech.bookstorenotificationservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
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
