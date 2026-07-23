package com.aims.backend.service.dashboard;

import com.aims.backend.dto.dashboard.DispatchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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

    /**
     * 같은 JVM 내부에서 Scheduler 중복 실행 방지
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(
            fixedDelayString =
                    "${app.agv.dispatch-scheduler.fixed-delay-ms:1000}"
    )
    @SchedulerLock(
            name = "agvDispatchScheduler",
            lockAtMostFor = "PT10S",
            lockAtLeastFor = "PT0.5S"
    )
    public void dispatchQueuedEvents() {

        LockAssert.assertLocked();

        if (!running.compareAndSet(false, true)) {
            log.debug(
                    "[AGV DISPATCH SCHEDULER] 이전 작업이 아직 진행 중입니다."
            );
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

        dispatchQueueService
                .poll(routeCode)
                .ifPresent(request -> dispatch(routeCode, request));

    }

    private void dispatch(
            String routeCode,
            DispatchRequest request
    ) {

        try {

            boolean dispatched =
                    agvSimulationService.dispatchAgv(
                            request.eventId(),
                            request.carMasterId(),
                            request.processCode()
                    );

            if (!dispatched) {

                dispatchQueueService.requeueFirst(
                        routeCode,
                        request
                );

                log.debug(
                        "[AGV DISPATCH][{}] AGV 없음. eventId={} 재대기",
                        routeCode,
                        request.eventId()
                );
            }

        } catch (Exception e) {

            dispatchQueueService.requeueFirst(
                    routeCode,
                    request
            );

            log.error(
                    "[AGV DISPATCH][{}] dispatch 실패. eventId={}",
                    routeCode,
                    request.eventId(),
                    e

            );
        }
    }
}