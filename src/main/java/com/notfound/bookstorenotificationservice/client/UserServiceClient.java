package com.notfound.bookstorenotificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client for calling User Service.
 * Configure base URL via `USER_SERVICE_URL`.
 */
@FeignClient(
        name = "user-service",
        url = "${USER_SERVICE_URL:}")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}/contact-info")
    ApiResponse<UserContactInfoResponse> getUserContactInfo(@PathVariable("userId") UUID userId);
}

