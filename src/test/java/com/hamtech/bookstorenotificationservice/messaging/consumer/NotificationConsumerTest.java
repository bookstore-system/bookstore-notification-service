package com.hamtech.bookstorenotificationservice.messaging.consumer;

import com.hamtech.bookstorenotificationservice.model.dto.OrderEventDto;
import com.hamtech.bookstorenotificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationConsumerTest {

    @Test
    void handleOrderEvent_delegatesToService() {
        NotificationService service = mock(NotificationService.class);
        NotificationConsumer consumer = new NotificationConsumer(service);

        OrderEventDto order = new OrderEventDto();
        order.setOrderId(UUID.randomUUID());
        order.setCustomerEmail("a@b.com");
        order.setCustomerName("A");
        order.setStatus("CREATED");

        consumer.handleOrderEvent(order);

        verify(service).sendOrderNotification(order);
    }
}

