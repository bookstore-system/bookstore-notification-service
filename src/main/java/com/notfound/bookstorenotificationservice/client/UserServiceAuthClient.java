package com.notfound.bookstorenotificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service-auth", url = "${clients.user-service.url:}")
public interface UserServiceAuthClient {

    @PostMapping("/api/v1/auth/login")
    UserServiceApiResponse<UserServiceAuthResponse> login(@RequestBody UserServiceLoginRequest request);
}
