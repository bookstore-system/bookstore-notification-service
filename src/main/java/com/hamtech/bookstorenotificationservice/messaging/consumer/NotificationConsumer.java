package com.hamtech.bookstorenotificationservice.messaging.consumer;

import com.hamtech.bookstorenotificationservice.config.RabbitMQConfig;
import com.hamtech.bookstorenotificationservice.model.dto.OrderEventDto;
import com.hamtech.bookstorenotificationservice.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE_NAME)
    public void handleOrderEvent(OrderEventDto orderEvent) {
        notificationService.sendOrderNotification(orderEvent);
    }
}
