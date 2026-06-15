package com.roy.morago.controller.call;

import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.dto.call.CallResponse;
import com.roy.morago.security.UserPrincipal;
import com.roy.morago.service.call.CallService;
import lombok.RequiredArgsConstructor;
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

    @PreAuthorize("@securityService.isCallParticipant(#id, authentication)")
    @PatchMapping("/{id}/accept")
    public CallResponse acceptCall(@PathVariable Long id) {
        return callService.acceptCall(id);
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
    public void rateCall(@PathVariable Long id, @RequestParam Integer rating) {
        callService.rateCall(id, rating);
    }
}
