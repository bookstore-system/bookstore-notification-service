package com.notfound.bookstorenotificationservice.controller;

import com.notfound.bookstorenotificationservice.model.dto.NotificationRequestDto;
import com.notfound.bookstorenotificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    NotificationService notificationService;

    @Test
    void sendEmailFallback_returns200() throws Exception {
        doNothing().when(notificationService).sendEmailFallback(ArgumentMatchers.any(NotificationRequestDto.class));

        NotificationRequestDto dto = new NotificationRequestDto();
        dto.setTo("test@example.com");
        dto.setSubject("Hello");
        dto.setMessage("World");

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sent successfully (simulated fallback)"));
    }
}

