package com.notfound.bookstorenotificationservice.messaging.consumer;

import com.notfound.bookstorenotificationservice.config.RabbitMQConfig;
import com.notfound.bookstorenotificationservice.messaging.NotificationEventParser;
import com.notfound.bookstorenotificationservice.messaging.NotificationEventParser.ParsedSagaNotification;
import com.notfound.bookstorenotificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SagaEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SagaEventConsumer.class);

    private final NotificationEventParser eventParser;
    private final NotificationService notificationService;

    public SagaEventConsumer(NotificationEventParser eventParser, NotificationService notificationService) {
        this.eventParser = eventParser;
        this.notificationService = notificationService;
    }

    @RabbitListener(
            queues = RabbitMQConfig.SAGA_EVENTS_QUEUE_NAME,
            containerFactory = "sagaRabbitListenerContainerFactory")
    public void handleSagaEvent(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        ParsedSagaNotification parsed = eventParser.parse(message.getBody(), routingKey);
        logger.info(
                "Received saga notification event: type={}, eventId={}, sagaId={}, orderId={}, routingKey={}",
                parsed.eventType(),
                parsed.eventId(),
                parsed.payload().getSagaId(),
                parsed.payload().getOrderId(),
                routingKey);
        notificationService.sendSagaCheckoutNotification(parsed.eventType(), parsed.payload());
    }
}
