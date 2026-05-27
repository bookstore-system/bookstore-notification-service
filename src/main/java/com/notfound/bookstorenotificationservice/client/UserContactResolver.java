package com.notfound.bookstorenotificationservice.client;

import com.notfound.bookstorenotificationservice.exception.NotificationDeliveryException;
import feign.FeignException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserContactResolver {

    private static final Logger logger = LoggerFactory.getLogger(UserContactResolver.class);

    private final UserServiceClient userServiceClient;

    public UserContactResolver(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public UserContactInfoResponse resolveContact(UUID userId) {
        try {
            ApiResponse<UserContactInfoResponse> response =
                    userServiceClient.getUserContactInfo(userId);
            if (response == null || (response.getCode() != 200 && response.getCode() != 1000)) {
                String message = response != null ? response.getMessage() : "empty response";
                throw new NotificationDeliveryException(
                        "user-service contact-info failed for userId=" + userId + ": " + message);
            }

            UserContactInfoResponse contactInfo = response.getResult();
            if (contactInfo == null || !StringUtils.hasText(contactInfo.getEmail())) {
                logger.warn("user-service returned no email for userId={}", userId);
            }
            return contactInfo;
        } catch (FeignException e) {
            throw new NotificationDeliveryException(
                    "user-service contact-info failed for userId=" + userId + ": " + e.getMessage(),
                    e);
        } catch (RuntimeException e) {
            throw new NotificationDeliveryException(
                    "user-service contact-info failed for userId=" + userId + ": " + e.getMessage(),
                    e);
        }
    }
}
