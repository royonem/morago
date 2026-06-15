package com.roy.morago.service.call;

import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.entity.call.Call;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CallStatus;
import com.roy.morago.exception.call.CallNotFoundException;
import com.roy.morago.exception.call.InvalidCallStateException;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.mapper.CallMapper;
import com.roy.morago.repository.call.CallRepository;
import com.roy.morago.service.finance.TransactionService;
import com.roy.morago.service.user.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CallHelper {
    private final CallMapper callMapper;
    private final CallRepository callRepository;
    private final UserHelper userHelper;
    private final TransactionService transactionService;

    protected Call createCall(CallRequest callRequest) {
        return callMapper.createEntityFromRequest(callRequest);
    }

    protected void createCallTransactions(Call call) {
        transactionService.createCallChargeTransaction(call, call.getClient());
        transactionService.createCallEarningTransaction(call, call.getTranslator());
    }

    protected Call findCallById(Long callId) {
        return callRepository.findById(callId)
                .orElseThrow(() -> new CallNotFoundException("Call not found"));
    }

    protected void setCallInitiator(Call call, User caller) {
        call.setIsClientInitiator(caller == call.getClient());
    }

    protected void resolveCancelDecline(Call call, User user) {
        boolean isClientInitiator = call.getIsClientInitiator();
        boolean userIsClient = user.getId().equals(call.getClient().getId());

        if ((isClientInitiator && userIsClient) || (!isClientInitiator && !userIsClient)) {
            call.setStatus(CallStatus.CANCELED);
        } else {
            call.setStatus(CallStatus.DECLINED);
        }
    }

    protected void setMaxDuration(Call call, CallRequest request) {
        validateCallStatusIsRequested(call);
        User client = userHelper.findUserById(request.clientId());
        long maxDuration = client.getWallet().getBalance() / 1000;
        validateCallFundsAreSufficient(call, maxDuration);
        call.setMaxCallTime(maxDuration);
    }

    protected Long calculateCallCost(Call call) {
        long callSeconds = calculateCallSeconds(call);
        long minutes = (long) Math.ceil(callSeconds / 60.0);
        return minutes * 1000;
    }

    private long calculateCallSeconds(Call call) {
        LocalDateTime endedAt = call.getEndedAt();
        LocalDateTime startedAt = call.getStartedAt();
        return Duration.between(startedAt, endedAt).toSeconds();
    }

    private void validateCallStatusIsRequested(Call call) {
        if (call.getStatus() != CallStatus.REQUESTED) {
            throw new InvalidCallStateException("Cannot set a max duration for this call");
        }
    }

    private void validateCallFundsAreSufficient(Call call, Long maxDuration) {
        if (maxDuration <= 0) {
            failCall(call);
            throw new DeficientFundsException("Client does not have enough funds for a call");
        }
    }

    private void failCall(Call call) {
        call.setStatus(CallStatus.FAILED);
        call.setMaxCallTime(0L);
        callRepository.save(call);
    }
}
