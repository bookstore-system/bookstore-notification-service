package com.notfound.bookstorenotificationservice.messaging.consumer;

import com.notfound.bookstorenotificationservice.messaging.PromotionEventParser;
import com.notfound.bookstorenotificationservice.messaging.PromotionEventTypes;
import com.notfound.bookstorenotificationservice.model.dto.PromotionCreatedEvent;
import com.notfound.bookstorenotificationservice.notification.NotificationDispatcher;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromotionCreatedConsumerTest {

    @Test
    void handlePromotionCreated_dispatchesParsedEvent() {
        PromotionEventParser parser = mock(PromotionEventParser.class);
        NotificationDispatcher dispatcher = mock(NotificationDispatcher.class);
        PromotionCreatedConsumer consumer = new PromotionCreatedConsumer(parser, dispatcher);
        PromotionCreatedEvent event = new PromotionCreatedEvent();
        event.setPromotionId(UUID.randomUUID());
        event.setCode("PROMO10");
        event.setStatus("ACTIVE");

        byte[] body = "{}".getBytes();
        MessageProperties props = new MessageProperties();
        props.setReceivedRoutingKey(PromotionEventTypes.PROMOTION_CREATED);
        Message message = new Message(body, props);
        when(parser.parsePromotionCreated(body)).thenReturn(event);

        consumer.handlePromotionCreated(message);

        verify(dispatcher).deliver(PromotionEventTypes.PROMOTION_CREATED, event);
    }
}

