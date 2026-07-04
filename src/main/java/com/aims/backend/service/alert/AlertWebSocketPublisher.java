package com.aims.backend.service.alert;

import com.aims.backend.dto.alert.AlertRealtimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertWebSocketPublisher {

    public static final String ALERT_DESTINATION = "/topic/alerts";

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(AlertRealtimeMessage message) {
        messagingTemplate.convertAndSend(ALERT_DESTINATION, message);
    }
}
