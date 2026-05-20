package com.notfound.bookstorenotificationservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookstorenotificationservice.model.dto.CheckoutNotificationPayload;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationEventParserTest {

    private NotificationEventParser parser;

    @BeforeEach
    void setUp() {
        parser = new NotificationEventParser(new ObjectMapper());
    }

    @Test
    void parse_envelope_extractsSagaAndOrderIds() throws Exception {
        UUID sagaId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String json =
                """
                {
                  "eventId": "evt-1",
                  "sagaId": "%s",
                  "type": "checkout.completed",
                  "orderId": "%s",
                  "userId": "%s",
                  "payload": {
                    "customerEmail": "user@test.com",
                    "customerName": "Nguyen A",
                    "totalPrice": 150000
                  }
                }
                """
                        .formatted(sagaId, orderId, UUID.randomUUID());

        var parsed = parser.parse(json.getBytes(StandardCharsets.UTF_8), "checkout.completed");

        assertThat(parsed.eventType()).isEqualTo("checkout.completed");
        assertThat(parsed.eventId()).isEqualTo("evt-1");
        CheckoutNotificationPayload payload = parsed.payload();
        assertThat(payload.getSagaId()).isEqualTo(sagaId);
        assertThat(payload.getOrderId()).isEqualTo(orderId);
        assertThat(payload.getCustomerEmail()).isEqualTo("user@test.com");
        assertThat(payload.getTotalPrice()).isEqualByComparingTo(new BigDecimal("150000"));
    }

    @Test
    void parse_flatLegacyBody_usesRoutingKey() {
        UUID orderId = UUID.randomUUID();
        String json =
                """
                {
                  "orderId": "%s",
                  "customerEmail": "legacy@test.com",
                  "status": "CANCELLED"
                }
                """
                        .formatted(orderId);

        var parsed = parser.parse(json.getBytes(StandardCharsets.UTF_8), SagaEventTypes.ORDER_CANCELLED);

        assertThat(parsed.eventType()).isEqualTo(SagaEventTypes.ORDER_CANCELLED);
        assertThat(parsed.payload().getOrderId()).isEqualTo(orderId);
        assertThat(parsed.payload().getCustomerEmail()).isEqualTo("legacy@test.com");
    }
}
