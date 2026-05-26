package com.notfound.bookstorenotificationservice.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

/**
 * Envelope chuẩn cho message trên {@code bookstore.events} (saga / orchestrator).
 */
public class BookstoreEventEnvelope {

    private String eventId;
    private UUID sagaId;
    private String correlationId;
    private String causationId;
    private String type;
    private Instant occurredAt;
    private UUID orderId;
    private String userId;
    private JsonNode payload;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public UUID getSagaId() {
        return sagaId;
    }

    public void setSagaId(UUID sagaId) {
        this.sagaId = sagaId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCausationId() {
        return causationId;
    }

    public void setCausationId(String causationId) {
        this.causationId = causationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public JsonNode payload() {
        return payload;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }
}
