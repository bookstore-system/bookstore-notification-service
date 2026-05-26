package com.notfound.bookstorenotificationservice.messaging.consumer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.notfound.bookstorenotificationservice.messaging.NotificationEventParser;
import com.notfound.bookstorenotificationservice.messaging.NotificationEventParser.ParsedSagaNotification;
import com.notfound.bookstorenotificationservice.messaging.SagaEventTypes;
import com.notfound.bookstorenotificationservice.model.dto.CheckoutNotificationPayload;
import com.notfound.bookstorenotificationservice.service.NotificationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class SagaEventConsumerTest {

    @Test
    void handleSagaEvent_delegatesParsedNotification() {
        NotificationEventParser parser = mock(NotificationEventParser.class);
        NotificationService service = mock(NotificationService.class);
        SagaEventConsumer consumer = new SagaEventConsumer(parser, service);

        CheckoutNotificationPayload payload = new CheckoutNotificationPayload();
        payload.setSagaId(UUID.randomUUID());
        payload.setOrderId(UUID.randomUUID());
        ParsedSagaNotification parsed =
                new ParsedSagaNotification(SagaEventTypes.CHECKOUT_COMPLETED, payload, "evt-1");

        byte[] body = "{}".getBytes();
        MessageProperties props = new MessageProperties();
        props.setReceivedRoutingKey(SagaEventTypes.CHECKOUT_COMPLETED);
        Message message = new Message(body, props);

        org.mockito.Mockito.when(parser.parse(body, SagaEventTypes.CHECKOUT_COMPLETED)).thenReturn(parsed);

        consumer.handleSagaEvent(message);

        verify(service).sendSagaCheckoutNotification(eq(SagaEventTypes.CHECKOUT_COMPLETED), eq(payload));
    }
}
