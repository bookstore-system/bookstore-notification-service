package com.notfound.bookstorenotificationservice.messaging;

import com.notfound.bookstorenotificationservice.config.RabbitMQConfig;
import com.notfound.bookstorenotificationservice.model.dto.OrderEventDto;
import com.notfound.bookstorenotificationservice.service.NotificationService;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * Integration test: publish event -> @RabbitListener consumes -> NotificationService invoked.
 *
 * Requires RabbitMQ running on localhost:5672 (e.g. Plan-And-Document docker-compose.dev.yml).
 */
@EnabledIfEnvironmentVariable(named = "RUN_RABBIT_IT", matches = "true")
@SpringBootTest(properties = {
        "spring.rabbitmq.host=127.0.0.1",
        "spring.rabbitmq.port=5672"
})
class OrderEventRabbitIntegrationTest {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @MockBean
    NotificationService notificationService;

    @Test
    void publishOrderEvent_shouldBeConsumedByNotificationService() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(inv -> {
            latch.countDown();
            return null;
        }).when(notificationService).sendOrderNotification(any(OrderEventDto.class));

        OrderEventDto dto = new OrderEventDto();
        dto.setOrderId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        dto.setUserId(UUID.fromString("987e6543-e21b-12d3-a456-426614174111"));
        dto.setStatus("CREATED");
        dto.setCustomerName("TestUser");

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "order.created", dto);

        boolean consumed = latch.await(10, TimeUnit.SECONDS);
        assertThat(consumed).isTrue();

        ArgumentCaptor<OrderEventDto> captor = ArgumentCaptor.forClass(OrderEventDto.class);
        verify(notificationService).sendOrderNotification(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(dto.getUserId());
        assertThat(captor.getValue().getOrderId()).isEqualTo(dto.getOrderId());
    }
}

