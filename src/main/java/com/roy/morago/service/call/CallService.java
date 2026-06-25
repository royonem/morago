package com.roy.morago.service.call;

import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.dto.call.CallResponse;
import com.roy.morago.dto.call.CallSearchRequest;
import com.roy.morago.dto.socket.CallEndedEvent;
import com.roy.morago.dto.socket.IncomingCallEvent;
import com.roy.morago.entity.call.Call;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CallStatus;
import com.roy.morago.mapper.CallMapper;
import com.roy.morago.repository.call.CallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CallService {
    private final CallRepository repo;
    private final CallMapper mapper;
    private final CallHelper helper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CallResponse requestCall(CallRequest callRequest, User caller) {
        Call call = helper.createCall(callRequest);
        call.setStatus(CallStatus.REQUESTED);
        helper.setCallInitiator(call, caller);
        helper.setMaxDuration(call, callRequest);
        call.setStatus(CallStatus.RINGING);
        repo.save(call);

        IncomingCallEvent event = IncomingCallEvent.from(call, caller);
        eventPublisher.publishEvent(event);
        return mapper.toResponse(call);
    }

    public CallResponse getCall(Long callId) {
        Call call = helper.findCallById(callId);
        return mapper.toResponse(call);
    }

    public Page<CallResponse> getAllCalls(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    public Page<CallResponse> getCallsByUserId(Long userId, Pageable pageable) {
        return repo.findByClientIdOrTranslatorId(userId, pageable).map(mapper::toResponse);
    }

    public Page<CallResponse> searchCalls(CallSearchRequest request) {
        Specification<Call> spec = helper.buildSpecification(request);
        return repo.findAll(spec, request.toPageable())
                .map(mapper::toResponse);
    }

    public Page<CallResponse> searchCallsByUserId(Long userId, CallSearchRequest request) {
        Specification<Call> spec = helper.buildSpecificationForUser(userId, request);
        return repo.findAll(spec, request.toPageable())
                .map(mapper::toResponse);
    }

    @Transactional
    public CallResponse acceptCall(Long callId, User recipient) {
        Call call = helper.findCallById(callId);
        helper.validateRecipient(call, recipient, "Cannot accept own call");
        helper.validateCallIsRinging(call);
        call.setStatus(CallStatus.ACCEPTED);
        call.setAcceptedAt(LocalDateTime.now());
        return mapper.toResponse(call);
    }

    @Transactional
    public CallResponse startCall(Long callId) {
        Call call = helper.findCallById(callId);
        helper.validateCallIsAccepted(call);
        call.setStatus(CallStatus.IN_PROGRESS);
        call.setStartedAt(LocalDateTime.now());
        return mapper.toResponse(call);
    }

    @Transactional
    public CallResponse cancelCall(Long callId, User caller) {
        Call call = helper.findCallById(callId);
        helper.validateCaller(call, caller);
        helper.validateCallIsRinging(call);
        helper.resolveCancelDecline(call, caller);
        call.setCanceledAt(LocalDateTime.now());
        return mapper.toResponse(call);

        CallEndedEvent event = CallEndedEvent.from(call);
        eventPublisher.publishEvent(event);
        return mapper.toResponse(call);
    }

    @Transactional
    public CallResponse declineCall(Long callId, User recipient) {
        Call call = helper.findCallById(callId);
        helper.validateRecipient(call, recipient, "Cannot decline own call");
        helper.validateCallIsRinging(call);
        helper.resolveCancelDecline(call, recipient);
        call.setCanceledAt(LocalDateTime.now());

        CallEndedEvent event = CallEndedEvent.from(call);
        eventPublisher.publishEvent(event);
        return mapper.toResponse(call);
    }

    @Transactional
    public CallResponse endCall(Long callId) {
        Call call = helper.findCallById(callId);
        helper.validateCallIsInProgress(call);
        call.setStatus(CallStatus.ENDED);
        call.setEndedAt(LocalDateTime.now());
        call.setCost(call.getExpectedCost());
        helper.createCallTransactions(call);

        CallEndedEvent event = CallEndedEvent.from(call);
        eventPublisher.publishEvent(event);
        return mapper.toResponse(call);
    }

    @Transactional
    public CallResponse rateCall(Long callId, Integer rating) {
        Call call = helper.findCallById(callId);
        helper.validateCallRating(rating);
        helper.validateCallIsEnded(call);
        call.setRating(rating);
        return mapper.toResponse(call);
    }
}
