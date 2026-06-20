package com.roy.morago.controller.call;

import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.dto.call.CallResponse;
import com.roy.morago.dto.call.CallSearchRequest;
import com.roy.morago.security.UserPrincipal;
import com.roy.morago.service.call.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/call")
public class CallController {
    private final CallService callService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/")
    public CallResponse requestCall(@RequestBody CallRequest callRequest, @AuthenticationPrincipal UserPrincipal principal) {
        return callService.requestCall(callRequest, principal.getUser());
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isCallParticipant(#id, authentication)")
    @GetMapping("/{id}")
    public CallResponse getCall(@PathVariable Long id) {
        return callService.getCall(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<CallResponse> getAllCalls(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return callService.getAllCalls(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/search")
    public Page<CallResponse> searchCalls(@RequestBody CallSearchRequest request) {
        return callService.searchCalls(request);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId, authentication)")
    @GetMapping("/list/{userId}")
    public Page<CallResponse> getUserCalls(@PathVariable Long userId
            ,@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return callService.getCallsByUserId(userId, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/search/{userId}")
    public Page<CallResponse> searchUserCalls(@PathVariable Long userId, @RequestBody CallSearchRequest request) {
        return callService.searchCallsByUserId(userId, request);
    }

    @PreAuthorize("@securityService.isCallParticipant(#id, authentication)")
    @PatchMapping("/{id}/accept")
    public CallResponse acceptCall(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return callService.acceptCall(id, principal.getUser());
    }

    @PreAuthorize("@securityService.isCallParticipant(#id, authentication)")
    @PatchMapping("/{id}/decline")
    public CallResponse declineCall(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return callService.declineCall(id, principal.getUser());
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isCallParticipant(#id, authentication)")
    @PatchMapping("/{id}/cancel")
    public CallResponse cancelCall(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return callService.cancelCall(id, principal.getUser());
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isCallParticipant(#id, authentication)")
    @PatchMapping("/{id}/end")
    public CallResponse endCall(@PathVariable Long id) {
        return callService.endCall(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @securityService.isCallClient(#id, authentication)")
    @PatchMapping("/{id}/rate")
    public CallResponse rateCall(@PathVariable Long id, @RequestParam Integer rating) {
        return callService.rateCall(id, rating);
    }
}
