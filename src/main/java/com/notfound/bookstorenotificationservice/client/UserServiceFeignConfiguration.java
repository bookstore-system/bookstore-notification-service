package com.notfound.bookstorenotificationservice.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class UserServiceFeignConfiguration {

    @Bean
    public RequestInterceptor userServiceAuthInterceptor(UserServiceTokenProvider tokenProvider) {
        return template -> {
            String token = tokenProvider.resolveBearerToken();
            if (token != null && !token.isBlank()) {
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}
