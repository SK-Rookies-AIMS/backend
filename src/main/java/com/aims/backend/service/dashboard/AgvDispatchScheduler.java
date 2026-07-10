package com.aims.backend.service.dashboard;

import com.aims.backend.dto.dashboard.DispatchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.agv.dispatch-scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AgvDispatchScheduler {

    private final AgvDispatchQueueService dispatchQueueService;
    private final AgvSimulationService agvSimulationService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${app.agv.dispatch-scheduler.fixed-delay-ms:1000}")
    public void dispatchQueuedEvents() {
        if (!running.compareAndSet(false, true)) {
            log.debug("이전 AGV Dispatch Scheduler 작업이 진행 중이므로 이번 실행을 건너뜁니다.");
            return;
        }

        try {
            for (String routeCode : dispatchQueueService.routeCodes()) {
                dispatchOne(routeCode);
            }
        } finally {
            running.set(false);
        }
    }

    private void dispatchOne(String routeCode) {
        dispatchQueueService.poll(routeCode)
                .ifPresent(request -> dispatch(routeCode, request));
    }

    private void dispatch(
            String routeCode,
            DispatchRequest request
    ) {
        try {
            boolean dispatched = agvSimulationService.dispatchAgv(
                    request.eventId(),
                    request.carMasterId(),
                    request.processCode()
            );

            if (!dispatched) {
                dispatchQueueService.requeueFirst(routeCode, request);

                log.debug(
                        "[AGV DISPATCH SCHEDULER][{}] 사용 가능한 AGV 없음. eventId={} 재대기",
                        routeCode,
                        request.eventId()
                );
            }
        } catch (Exception e) {
            dispatchQueueService.requeueFirst(routeCode, request);

            log.error(
                    "[AGV DISPATCH SCHEDULER][{}] 배정 실패. eventId={} 재대기",
                    routeCode,
                    request.eventId(),
                    e
            );
        }
    }
}
