package com.notfound.bookstorenotificationservice.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookstorenotificationservice.model.dto.PromotionCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class PromotionEventParser {

    private final ObjectMapper objectMapper;

    public PromotionEventParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PromotionCreatedEvent parsePromotionCreated(byte[] body) {
        if (body == null || body.length == 0) {
            throw new IllegalArgumentException("Message body is empty");
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode payload = root.has("payload") && root.get("payload") != null ? root.get("payload") : root;
            return objectMapper.treeToValue(payload, PromotionCreatedEvent.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse promotion.created event: " + e.getMessage(), e);
        }
    }
}

