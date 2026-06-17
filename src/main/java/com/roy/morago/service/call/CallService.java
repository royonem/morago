package com.roy.morago.service.call;

import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.dto.call.CallResponse;
import com.roy.morago.entity.call.Call;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CallStatus;
import com.roy.morago.mapper.CallMapper;
import com.roy.morago.repository.call.CallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CallService {
    private final CallRepository repo;
    private final CallMapper mapper;
    private final CallHelper helper;

    @Transactional
    public CallResponse requestCall(CallRequest callRequest, User caller) {
        Call call = helper.createCall(callRequest);
        call.setStatus(CallStatus.REQUESTED);
        helper.setCallInitiator(call, caller);
        helper.setMaxDuration(call, callRequest);
        call.setStatus(CallStatus.RINGING);
        repo.save(call);

        return mapper.createResponseFromEntity(call);
    }

    public CallResponse getCall(Long callId) {
        Call call = helper.findCallById(callId);
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse acceptCall(Long callId, User recipient) {
        Call call = helper.findCallById(callId);
        helper.validateRecipient(call, recipient, "Cannot accept own call");
        helper.validateCallIsRinging(call);
        call.setStatus(CallStatus.ACCEPTED);
        call.setAcceptedAt(LocalDateTime.now());
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse startCall(Long callId) {
        Call call = helper.findCallById(callId);
        helper.validateCallIsAccepted(call);
        call.setStatus(CallStatus.IN_PROGRESS);
        call.setStartedAt(LocalDateTime.now());
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse cancelCall(Long callId, User caller) {
        Call call = helper.findCallById(callId);
        helper.validateCaller(call, caller);
        helper.validateCallIsRinging(call);
        helper.resolveCancelDecline(call, caller);
        call.setCanceledAt(LocalDateTime.now());
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse declineCall(Long callId, User recipient) {
        Call call = helper.findCallById(callId);
        helper.validateRecipient(call, recipient, "Cannot decline own call");
        helper.validateCallIsRinging(call);
        helper.resolveCancelDecline(call, recipient);
        call.setCanceledAt(LocalDateTime.now());
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse autoEndCall(Long callId) {
        Call call = helper.findCallById(callId);
        call.setStatus(CallStatus.ENDED);
        call.setEndedAt(LocalDateTime.now());
        call.setCost(helper.calculateCallCost(call));
        // some scheduling logic
        helper.createCallTransactions(call);
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse endCall(Long callId) {
        Call call = helper.findCallById(callId);
        helper.validateCallIsInProgress(call);
        call.setStatus(CallStatus.ENDED);
        call.setEndedAt(LocalDateTime.now());
        call.setCost(helper.calculateCallCost(call));
        helper.createCallTransactions(call);
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public void rateCall(Long callId, Integer rating) {
        Call call = helper.findCallById(callId);
        helper.validateCallRating(rating);
        helper.validateCallIsEnded(call);
        call.setRating(rating);
    }
}
