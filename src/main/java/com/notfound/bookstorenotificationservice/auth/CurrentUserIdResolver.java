package com.notfound.bookstorenotificationservice.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookstorenotificationservice.exception.MissingCurrentUserException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CurrentUserIdResolver {

    private final ObjectMapper objectMapper;

    public CurrentUserIdResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UUID resolve(String xUserId, String authorizationHeader) {
        String rawUserId = StringUtils.hasText(xUserId) ? xUserId : userIdFromJwt(authorizationHeader);
        if (!StringUtils.hasText(rawUserId)) {
            throw new MissingCurrentUserException("Missing X-User-Id header or user id claim");
        }
        try {
            return UUID.fromString(rawUserId.trim());
        } catch (IllegalArgumentException e) {
            throw new MissingCurrentUserException("Current user id is not a valid UUID");
        }
    }

    private String userIdFromJwt(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        try {
            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode payload = objectMapper.readTree(new String(decodedPayload, StandardCharsets.UTF_8));
            for (String claim : new String[] {"userId", "id", "sub"}) {
                JsonNode node = payload.get(claim);
                if (node != null && !node.isNull() && StringUtils.hasText(node.asText())) {
                    return node.asText();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

