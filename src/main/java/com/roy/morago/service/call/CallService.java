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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class CallService {
    private final CallRepository repo;
    private final CallMapper mapper;
    private final CallHelper helper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CallResponse requestCall(CallRequest callRequest, User caller) {
        log.info("Requesting call: callerId={}", caller.getId());
        Call call = helper.createCall(callRequest);
        call.setStatus(CallStatus.REQUESTED);
        helper.setCallInitiator(call, caller);
        helper.setMaxDuration(call, callRequest);
        call.setStatus(CallStatus.RINGING);
        repo.save(call);
        log.info("Call requested: callId={}, callerId={}, receiverId={}", call.getId(), call.getCaller().getId(), call.getReceiver().getId());

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
    public CallResponse acceptCall(Long callId, User receiver) {
        log.info("Accepting call: callId={}, receiverId={}", callId, receiver.getId());
        Call call = helper.findCallById(callId);
        helper.validateReceiver(call, receiver, "Cannot accept own call");
        helper.validateCallIsRinging(call);
        call.setStatus(CallStatus.ACCEPTED);
        call.setAcceptedAt(LocalDateTime.now());
        log.info("Call accepted: callId={}, callerId={}, receiverId={}",
                callId, call.getCaller().getId(), call.getReceiver().getId());
        return mapper.toResponse(call);
    }

    @Transactional
    public CallResponse startCall(Long callId) {
        log.info("Starting call: callId={}", callId);
        Call call = helper.findCallById(callId);
        helper.validateCallIsAccepted(call);
        call.setStatus(CallStatus.IN_PROGRESS);
        call.setStartedAt(LocalDateTime.now());
        log.info("Call started: callId={}, callerId={}, receiverId={}",
                callId, call.getCaller().getId(), call.getReceiver().getId());
        return mapper.toResponse(call);
    }

    @Transactional
    public CallResponse cancelCall(Long callId, User caller) {
        log.info("Canceling call: callId={}, callerId={}", callId, caller.getId());
        Call call = helper.findCallById(callId);
        helper.validateCaller(call, caller);
        helper.validateCallIsRinging(call);
        helper.resolveCancelDecline(call, caller);
        call.setCanceledAt(LocalDateTime.now());

        CallEndedEvent event = CallEndedEvent.from(call);
        eventPublisher.publishEvent(event);
        log.info("Call cancelled: callId={}, caller={}", callId, caller.getId());
        return mapper.toResponse(call);
    }

    @Transactional
    public CallResponse declineCall(Long callId, User receiver) {
        log.info("Declining call: callId={}, receiverId={}", callId, receiver.getId());
        Call call = helper.findCallById(callId);
        helper.validateReceiver(call, receiver, "Cannot decline own call");
        helper.validateCallIsRinging(call);
        helper.resolveCancelDecline(call, receiver);
        call.setCanceledAt(LocalDateTime.now());

        CallEndedEvent event = CallEndedEvent.from(call);
        eventPublisher.publishEvent(event);
        log.info("Call declined: callId={}, callerId={}, receiverId={}",
                callId, call.getCaller().getId(), call.getReceiver().getId());
        return mapper.toResponse(call);
    }

    @Transactional
    public CallResponse endCall(Long callId) {
        log.info("Ending call: callId={}", callId);
        Call call = helper.findCallById(callId);
        helper.validateCallIsInProgress(call);
        call.setStatus(CallStatus.ENDED);
        call.setEndedAt(LocalDateTime.now());
        call.setCost(call.getExpectedCost());
        helper.createCallTransactions(call);
        log.info("Call ended: callId={}, cost={}, duration={}s", callId, call.getCost(), call.getFullDurationSeconds());

        CallEndedEvent event = CallEndedEvent.from(call);
        eventPublisher.publishEvent(event);
        return mapper.toResponse(call);
    }

    @Transactional
    public CallResponse rateCall(Long callId, Integer rating) {
        log.info("Rating call: callId={}, rating={}", callId, rating);
        Call call = helper.findCallById(callId);
        helper.validateCallRating(rating);
        helper.validateCallIsEnded(call);
        call.setRating(rating);
        log.info("Call rated: callId={}, clientId={}, rating={}", callId, call.getClient().getId(), rating);
        return mapper.toResponse(call);
    }
}
