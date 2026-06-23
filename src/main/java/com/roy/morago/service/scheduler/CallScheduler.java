package com.roy.morago.service.scheduler;

import com.roy.morago.entity.call.Call;
import com.roy.morago.enums.CallStatus;
import com.roy.morago.repository.call.CallRepository;
import com.roy.morago.service.call.CallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallScheduler {
    private final CallService callService;
    private final CallRepository callRepository;


    @Scheduled(fixedDelay = 5000)
    public void autoEndCalls() {
        List<Call> activeCalls = callRepository.findActiveCalls(CallStatus.IN_PROGRESS);
        for (Call call : activeCalls) {
            if (call.getDurationSeconds() >= call.getMaxCallTime()) {
                callService.endCall(call.getId());
            }
        }
    }
}
