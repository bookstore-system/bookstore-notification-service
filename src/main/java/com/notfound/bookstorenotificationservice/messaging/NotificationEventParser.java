package com.notfound.bookstorenotificationservice.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookstorenotificationservice.model.dto.BookstoreEventEnvelope;
import com.notfound.bookstorenotificationservice.model.dto.CheckoutNotificationPayload;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NotificationEventParser {

    private final ObjectMapper objectMapper;

    public NotificationEventParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ParsedSagaNotification parse(byte[] body, String routingKey) {
        if (body == null || body.length == 0) {
            throw new IllegalArgumentException("Message body is empty");
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.hasNonNull("type") && root.has("payload")) {
                BookstoreEventEnvelope envelope = objectMapper.treeToValue(root, BookstoreEventEnvelope.class);
                String eventType = StringUtils.hasText(envelope.getType()) ? envelope.getType() : routingKey;
                CheckoutNotificationPayload payload = mergeEnvelope(envelope);
                return new ParsedSagaNotification(eventType, payload, envelope.getEventId());
            }
            CheckoutNotificationPayload flat = objectMapper.treeToValue(root, CheckoutNotificationPayload.class);
            String eventType = inferEventType(routingKey, root);
            return new ParsedSagaNotification(eventType, flat, textOrNull(root, "eventId"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse notification event: " + e.getMessage(), e);
        }
    }

    private CheckoutNotificationPayload mergeEnvelope(BookstoreEventEnvelope envelope) {
        CheckoutNotificationPayload payload = new CheckoutNotificationPayload();
        if (envelope.getPayload() != null && !envelope.getPayload().isNull()) {
            payload = this.objectMapper.convertValue(envelope.getPayload(), CheckoutNotificationPayload.class);
        }
        if (envelope.getSagaId() != null) {
            payload.setSagaId(envelope.getSagaId());
        }
        if (envelope.getOrderId() != null) {
            payload.setOrderId(envelope.getOrderId());
        }
        if (payload.getUserId() == null && StringUtils.hasText(envelope.getUserId())) {
            payload.setUserId(parseUuid(envelope.getUserId()));
        }
        return payload;
    }

    private String inferEventType(String routingKey, JsonNode root) {
        if (StringUtils.hasText(routingKey)
                && (routingKey.startsWith("checkout.")
                        || routingKey.startsWith("payment.")
                        || routingKey.startsWith("order."))) {
            return routingKey;
        }
        String type = textOrNull(root, "type");
        if (StringUtils.hasText(type)) {
            return type;
        }
        String status = textOrNull(root, "status");
        if ("CANCELLED".equalsIgnoreCase(status)) {
            return SagaEventTypes.ORDER_CANCELLED;
        }
        return routingKey != null ? routingKey : SagaEventTypes.CHECKOUT_COMPLETED;
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : null;
    }

    private static UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public record ParsedSagaNotification(String eventType, CheckoutNotificationPayload payload, String eventId) {}
}
