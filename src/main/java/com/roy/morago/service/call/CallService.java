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
    public CallResponse acceptCall(Long callId) {
        Call call = helper.findCallById(callId);
        call.setStatus(CallStatus.ACCEPTED);
        call.setAcceptedAt(LocalDateTime.now());
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse startCall(Long callId) {
        Call call = helper.findCallById(callId);
        call.setStatus(CallStatus.IN_PROGRESS);
        call.setStartedAt(LocalDateTime.now());
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse cancelCall(Long callId, User caller) {
        Call call = helper.findCallById(callId);
        helper.resolveCancelDecline(call, caller);
        call.setCanceledAt(LocalDateTime.now());
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse declineCall(Long callId, User recipient) {
        Call call = helper.findCallById(callId);
        helper.resolveCancelDecline(call, recipient);
        call.setCanceledAt(LocalDateTime.now());
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse autoEndCall(Long callId, User caller) {
        Call call = helper.findCallById(callId);
        call.setStatus(CallStatus.ENDED);
        call.setEndedAt(LocalDateTime.now());
        Long cost = helper.calculateCallCost(call);
        call.setCost(cost);
        // some scheduling logic
        // some finance logic maybe
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public CallResponse endCall(Long callId) {
        Call call = helper.findCallById(callId);
        call.setStatus(CallStatus.ENDED);
        call.setEndedAt(LocalDateTime.now());
        call.setCost(helper.calculateCallCost(call));
        helper.createCallTransactions(call);
        return mapper.createResponseFromEntity(call);
    }

    @Transactional
    public void rateCall(Long id, Integer rating) {
        Call call = helper.findCallById(id);
        call.setRating(rating);
    }
}
