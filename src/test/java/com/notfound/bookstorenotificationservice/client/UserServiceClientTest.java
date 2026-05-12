package com.notfound.bookstorenotificationservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookstorenotificationservice.BookstoreNotificationServiceApplication;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test cho {@link UserServiceClient} (Feign client).
 *
 * Test này sẽ:
 *  - Khởi tạo MockWebServer giả lập user-service.
 *  - Override property {@code clients.user-service.url} trỏ về MockWebServer.
 *  - Gọi Feign client và kiểm tra: HTTP method, đường dẫn, và body trả về được deserialize đúng.
 *
 * Tắt auto-startup của Rabbit listener để không cần broker khi chạy test
 * (RabbitMQConfig của project cần ConnectionFactory nên không thể exclude toàn bộ).
 */
@SpringBootTest(
        classes = BookstoreNotificationServiceApplication.class,
        properties = {
                "spring.rabbitmq.listener.simple.auto-startup=false",
                "spring.rabbitmq.host=127.0.0.1",
                "spring.rabbitmq.port=5672"
        }
)
class UserServiceClientTest {

    static MockWebServer mockWebServer;

    @Autowired
    UserServiceClient userServiceClient;

    @Autowired
    ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) throws Exception {
        if (mockWebServer == null) {
            mockWebServer = new MockWebServer();
            mockWebServer.start();
        }
        String baseUrl = mockWebServer.url("/").toString();
        registry.add("clients.user-service.url", () -> baseUrl);
    }

    @AfterAll
    static void afterAll() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void getUserContactInfo_callsCorrectPath_andReadsResponse() throws Exception {
        UUID userId = UUID.fromString("987e6543-e21b-12d3-a456-426614174111");

        UserContactInfoResponse body = new UserContactInfoResponse();
        body.setUserId(userId);
        body.setEmail("user1@example.com");
        body.setPhoneNumber("0900000001");
        body.setDeviceToken("device-token-abc");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(body)));

        UserContactInfoResponse res = userServiceClient.getUserContactInfo(userId);

        assertThat(res).isNotNull();
        assertThat(res.getUserId()).isEqualTo(userId);
        assertThat(res.getEmail()).isEqualTo("user1@example.com");
        assertThat(res.getPhoneNumber()).isEqualTo("0900000001");
        assertThat(res.getDeviceToken()).isEqualTo("device-token-abc");

        RecordedRequest req = mockWebServer.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/api/v1/users/" + userId + "/contact-info");
    }
}
