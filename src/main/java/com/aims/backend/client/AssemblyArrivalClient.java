package com.aims.backend.client;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.dto.dashboard.AgvArrivalRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssemblyArrivalClient {

    private final RestClient restClient;

    @Value("${external.assembly-url}")
    private String assemblyUrl;

    /**
     * AGV 도착 정보를 Assembly Service로 전달
     */
    public void notifyAgvArrived(
            String eventId
    ) {

        AgvArrivalRequest request =
                AgvArrivalRequest.builder()
                        .eventId(eventId)
                        .build();

        String url =
                assemblyUrl + "/api/internal/agv-arrivals";

        log.info("""
                
                ==============================
                Assembly 도착 API 요청
                
                url={}
                {}
                
                ==============================
                
                """,
                url,
                eventId
        );

        restClient.post()
                .uri(url)
                .body(request)
                .retrieve()
                .toBodilessEntity();

        log.info(
                "[ASSEMBLY ARRIVAL SUCCESS] eventId={}",
                eventId
        );
    }
}