package com.roy.morago.controller.call;

import com.roy.morago.dto.call.CallRequest;
import com.roy.morago.dto.call.CallResponse;
import com.roy.morago.dto.call.CallSearchRequest;
import com.roy.morago.entity.user.User;
import com.roy.morago.security.UserPrincipal;
import com.roy.morago.service.call.CallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "05 - Calls", description = "Call management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/call")
public class CallController {
    private final CallService callService;

    @Operation(
            summary = "Request a call",
            description = "Initiates a call request. **Role: Any authenticated user (CLIENT or TRANSLATOR).**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call requested successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Translator not found")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/")
    public CallResponse requestCall(@Valid @RequestBody CallRequest callRequest, @AuthenticationPrincipal User user) {
        return callService.requestCall(callRequest, user);
    }

    @Operation(
            summary = "Get call by ID",
            description = "Returns call details. **Role: ADMIN or call participant (caller or receiver).**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call found successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a participant"),
            @ApiResponse(responseCode = "404", description = "Call not found")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCallParticipant(#id, authentication)")
    @GetMapping("/{id}")
    public CallResponse getCall(@PathVariable Long id) {
        return callService.getCall(id);
    }

    @Operation(
            summary = "Get all calls",
            description = "Returns a paginated list of all calls. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calls retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<CallResponse> getAllCalls(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return callService.getAllCalls(pageable);
    }

    @Operation(
            summary = "Get user calls",
            description = "Returns paginated calls for a specific user. **Role: ADMIN or the user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User calls retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot access this user's calls"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId, authentication)")
    @GetMapping("/list/{userId}")
    public Page<CallResponse> getUserCalls(@PathVariable Long userId, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return callService.getCallsByUserId(userId, pageable);
    }

    @Operation(
            summary = "Search calls",
            description = "Searches calls by criteria. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/search")
    public Page<CallResponse> searchCalls(@RequestBody CallSearchRequest request) {
        return callService.searchCalls(request);
    }

    @Operation(
            summary = "Search user calls",
            description = "Searches calls for a specific user by criteria. **Role: ADMIN or the user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot access this user's calls"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId, authentication)")
    @PostMapping("/search/{userId}")
    public Page<CallResponse> searchUserCalls(@PathVariable Long userId, @RequestBody CallSearchRequest request) {
        return callService.searchCallsByUserId(userId, request);
    }

    @Operation(
            summary = "Accept a call",
            description = "Accepts an incoming call. **Role: Call participant (receiver).**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call accepted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a participant"),
            @ApiResponse(responseCode = "404", description = "Call not found"),
            @ApiResponse(responseCode = "409", description = "Call not in ringing state")
    })
    @PreAuthorize("@securityService.isCallParticipant(#id, authentication)")
    @PatchMapping("/{id}/accept")
    public CallResponse acceptCall(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return callService.acceptCall(id, user);
    }

    @Operation(
            summary = "Start a call",
            description = "Starts an accepted call. **Role: Call participant.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call started successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a participant"),
            @ApiResponse(responseCode = "404", description = "Call not found"),
            @ApiResponse(responseCode = "409", description = "Call not in accepted state")
    })
    @PreAuthorize("@securityService.isCallParticipant(#id, authentication)")
    @PostMapping("/{id}/start")
    public CallResponse startCall(@PathVariable Long id) {
        return callService.startCall(id);
    }

    @Operation(
            summary = "Decline a call",
            description = "Declines an incoming call. **Role: Call participant (receiver).**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call declined successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a participant"),
            @ApiResponse(responseCode = "404", description = "Call not found"),
            @ApiResponse(responseCode = "409", description = "Call not in ringing state")
    })
    @PreAuthorize("@securityService.isCallParticipant(#id, authentication)")
    @PatchMapping("/{id}/decline")
    public CallResponse declineCall(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return callService.declineCall(id, user);
    }

    @Operation(
            summary = "Cancel a call",
            description = "Cancels a ringing call. **Role: ADMIN or call participant (caller).**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call canceled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a participant"),
            @ApiResponse(responseCode = "404", description = "Call not found"),
            @ApiResponse(responseCode = "409", description = "Call not in ringing state")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCallParticipant(#id, authentication)")
    @PatchMapping("/{id}/cancel")
    public CallResponse cancelCall(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return callService.cancelCall(id, user);
    }

    @Operation(
            summary = "End a call",
            description = "Ends an in-progress call. **Role: ADMIN or call participant.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call ended successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a participant"),
            @ApiResponse(responseCode = "404", description = "Call not found"),
            @ApiResponse(responseCode = "409", description = "Call not in progress")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCallParticipant(#id, authentication)")
    @PatchMapping("/{id}/end")
    public CallResponse endCall(@PathVariable Long id) {
        return callService.endCall(id);
    }

    @Operation(
            summary = "Rate a call",
            description = "Rate a completed call (1-5). **Role: ADMIN or the client in the call.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call rated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rating (must be 1-5)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the client"),
            @ApiResponse(responseCode = "404", description = "Call not found"),
            @ApiResponse(responseCode = "409", description = "Call not ended")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCallClient(#id, authentication)")
    @PatchMapping("/{id}/rate")
    public CallResponse rateCall(@PathVariable Long id, @RequestParam Integer rating) {
        return callService.rateCall(id, rating);
    }
}