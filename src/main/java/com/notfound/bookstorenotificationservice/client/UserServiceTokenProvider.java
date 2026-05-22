package com.notfound.bookstorenotificationservice.client;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserServiceTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceTokenProvider.class);
    private static final int MIN_JWT_LENGTH = 50;
    private static final String DEFAULT_SERVICE_USERNAME = "admin";
    private static final String DEFAULT_SERVICE_PASSWORD = "admin";

    private final UserServiceAuthClient userServiceAuthClient;
    private final String serviceUsername;
    private final String servicePassword;
    private volatile String cachedLoginToken;

    public UserServiceTokenProvider(
            UserServiceAuthClient userServiceAuthClient,
            @Value("${USER_SERVICE_USERNAME:admin}") String serviceUsername,
            @Value("${USER_SERVICE_PASSWORD:admin}") String servicePassword) {
        this.userServiceAuthClient = userServiceAuthClient;
        this.serviceUsername = StringUtils.hasText(serviceUsername)
                ? serviceUsername.trim()
                : DEFAULT_SERVICE_USERNAME;
        this.servicePassword = StringUtils.hasText(servicePassword)
                ? servicePassword
                : DEFAULT_SERVICE_PASSWORD;
    }

    public String resolveBearerToken() {
        if (cachedLoginToken != null && !cachedLoginToken.isBlank()) {
            return cachedLoginToken;
        }
        return loginAndCache();
    }

    public void invalidateCache() {
        cachedLoginToken = null;
    }

    private String loginAndCache() {
        if (!hasServiceCredentials()) {
            throw new IllegalStateException(
                    "Cannot call user-service: service account username/password is missing");
        }

        try {
            UserServiceApiResponse<UserServiceAuthResponse> response =
                    userServiceAuthClient.login(new UserServiceLoginRequest(serviceUsername, servicePassword));
            if (response == null || !response.isSuccess()) {
                String message = response != null ? response.getMessage() : "empty response";
                throw new IllegalStateException("user-service login failed: " + message);
            }

            String token = response.getResult().getToken();
            if (!isUsableJwt(token)) {
                throw new IllegalStateException("user-service login returned an invalid token");
            }

            cachedLoginToken = token;
            logger.info("Obtained user-service token via service account login (username={})", serviceUsername);
            return cachedLoginToken;
        } catch (FeignException e) {
            throw new IllegalStateException(
                    "user-service login failed with HTTP " + e.status() + ": " + e.getMessage(), e);
        }
    }

    private static boolean isUsableJwt(String token) {
        return StringUtils.hasText(token)
                && token.length() >= MIN_JWT_LENGTH
                && !token.contains("...");
    }

    private boolean hasServiceCredentials() {
        return StringUtils.hasText(serviceUsername) && StringUtils.hasText(servicePassword);
    }
}
