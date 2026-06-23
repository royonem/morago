package com.roy.morago.service.call;

import com.roy.morago.constants.SocketEvents;
import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.dto.call.CallSearchRequest;
import com.roy.morago.dto.socket.CallEndedEvent;
import com.roy.morago.entity.call.Call;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.CallStatus;
import com.roy.morago.exception.call.*;
import com.roy.morago.exception.finance.DeficientFundsException;
import com.roy.morago.mapper.CallMapper;
import com.roy.morago.repository.call.CallRepository;
import com.roy.morago.service.SocketService;
import com.roy.morago.service.finance.TransactionService;
import com.roy.morago.service.topic.TopicHelper;
import com.roy.morago.service.user.UserHelper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CallHelper {
    private final CallMapper callMapper;
    private final CallRepository callRepository;
    private final UserHelper userHelper;
    private final TransactionService transactionService;
    private final TopicHelper topicHelper;
    private final SocketService socketService;

    protected Call createCall(CallRequest callRequest) {
        Call call = callMapper.createEntityFromRequest(callRequest);
        call.setClient(userHelper.findUserById(callRequest.clientId()));
        call.setTranslator(userHelper.findUserById(callRequest.translatorId()));
        call.setTopic(topicHelper.findTopicById(callRequest.topicId()));
        call.setCost(0L);
        return call;
    }

    protected void resolveCallEvent(Call call) {
        CallEndedEvent event = new CallEndedEvent();
        CallStatus status = call.getStatus();
        event.setCallId(call.getId());
        event.setStatus(status.name().toLowerCase());
        event.setSentAt(LocalDateTime.now());
        if (status == CallStatus.ENDED) {
            event.setDuration(call.getDurationSeconds());
        }
        socketService.sendToUser(call.getCaller().getId(), SocketEvents.CALL_ENDED, event);
        socketService.sendToUser(call.getReceiver().getId(), SocketEvents.CALL_ENDED, event);
    }

    protected void createCallTransactions(Call call) {
        transactionService.createCallChargeTransaction(call, call.getClient());
        transactionService.createCallEarningTransaction(call, call.getTranslator());
    }

    public Call findCallById(Long callId) {
        return callRepository.findById(callId)
                .orElseThrow(() -> new CallNotFoundException("Call not found"));
    }

    protected Specification<Call> buildSpecification(CallSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = buildPredicates(request, root, cb);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    protected Specification<Call> buildSpecificationForUser(Long userId, CallSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
                    cb.or(
                            cb.equal(root.join("client").get("id"), userId),
                            cb.equal(root.join("translator").get("id"), userId)
                    )
            );
            predicates.addAll(buildPredicates(request, root, cb));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicates(CallSearchRequest request, Root<Call> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        Optional.ofNullable(request.clientId()).ifPresent(id ->
                predicates.add(cb.equal(root.join("client").get("id"), id)));
        Optional.ofNullable(request.translatorId()).ifPresent(id ->
                predicates.add(cb.equal(root.join("translator").get("id"), id)));
        Optional.ofNullable(request.topicId()).ifPresent(id ->
                predicates.add(cb.equal(root.join("topic").get("id"), id)));

        Optional.ofNullable(request.status()).ifPresent(status ->
                predicates.add(cb.equal(root.get("status"), status)));

        // Rating range
        Optional.ofNullable(request.ratingFrom()).ifPresent(rating ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), rating)));
        Optional.ofNullable(request.ratingTo()).ifPresent(rating ->
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), rating)));

        // Accepted date range
        Optional.ofNullable(request.acceptedFrom()).ifPresent(date ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("acceptedAt"), date)));
        Optional.ofNullable(request.acceptedTo()).ifPresent(date ->
                predicates.add(cb.lessThanOrEqualTo(root.get("acceptedAt"), date)));

        // Started date range
        Optional.ofNullable(request.startedFrom()).ifPresent(date ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("startedAt"), date)));
        Optional.ofNullable(request.startedTo()).ifPresent(date ->
                predicates.add(cb.lessThanOrEqualTo(root.get("startedAt"), date)));

        // Ended date range
        Optional.ofNullable(request.endedFrom()).ifPresent(date ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("endedAt"), date)));
        Optional.ofNullable(request.endedTo()).ifPresent(date ->
                predicates.add(cb.lessThanOrEqualTo(root.get("endedAt"), date)));

        // Canceled date range
        Optional.ofNullable(request.canceledFrom()).ifPresent(date ->
                predicates.add(cb.greaterThanOrEqualTo(root.get("canceledAt"), date)));
        Optional.ofNullable(request.canceledTo()).ifPresent(date ->
                predicates.add(cb.lessThanOrEqualTo(root.get("canceledAt"), date)));
        return predicates;
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
        validateCallIsRequested(call);
        User client = userHelper.findUserById(request.clientId());
        long maxDurationMinutes = client.getWallet().getBalance() / 1000;
        validateCallFundsAreSufficient(maxDurationMinutes);
        call.setMaxCallTime(maxDurationMinutes * 60);
    }

    protected Long calculateCallCost(Call call) {
        long callSeconds = call.getDurationSeconds();
        long minutes = (long) Math.ceil(callSeconds / 60.0);
        return minutes * 1000;
    }

    protected void validateRecipient(Call call, User user, String message) {
        Long initiatorId = call.getIsClientInitiator()
                ? call.getClient().getId()
                : call.getTranslator().getId();

        if (user.getId().equals(initiatorId)) {
            throw new InvalidCallRecipientException(message);
        }
    }

    protected void validateCaller(Call call, User user) {
        Long initiatorId = call.getIsClientInitiator()
                ? call.getClient().getId()
                : call.getTranslator().getId();

        if (!user.getId().equals(initiatorId)) {
            throw new InvalidCallerException("Cannot cancel this call");
        }
    }

    private void validateCallIsRequested(Call call) {
        if (call.getStatus() != CallStatus.REQUESTED) {
            throw new InvalidCallStateException("Cannot set a max duration for this call");
        }
    }

    protected void validateCallIsAccepted(Call call) {
        if (call.getStatus() != CallStatus.ACCEPTED) {
            throw new InvalidCallStateException("Cannot start this call");
        }
    }

    protected void validateCallIsRinging(Call call) {
        if (call.getStatus() != CallStatus.RINGING) {
            throw new InvalidCallStateException("Cannot access this call");
        }
    }

    protected void validateCallIsInProgress(Call call) {
        if (call.getStatus() != CallStatus.IN_PROGRESS) {
            throw new InvalidCallStateException("Cannot end this call");
        }
    }

    protected void validateCallIsEnded(Call call) {
        if (call.getStatus() != CallStatus.ENDED) {
            throw new InvalidCallStateException("Cannot rate this call");
        }
    }

    private void validateCallFundsAreSufficient(Long maxDuration) {
        if (maxDuration <= 0) {
            throw new DeficientFundsException("Client does not have enough funds for a call");
        }
    }

    protected void validateCallRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new InvalidCallRatingException("Rating must be 1-5");
        }
    }
}
