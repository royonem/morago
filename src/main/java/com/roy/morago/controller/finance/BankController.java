package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.BankAccountRequest;
import com.roy.morago.dto.finance.BankAccountResponse;
import com.roy.morago.service.finance.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "06 - Bank", description = "Bank account management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bank")
public class BankController {
    private final BankService bankService;

    @Operation(
            summary = "Link bank account",
            description = "Links a bank account to the authenticated user. **Role: Any authenticated user.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bank account linked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccountResponse linkBankAccount(@Valid @RequestBody BankAccountRequest request, Authentication authentication) {
        return bankService.linkBankAccount(request, authentication);
    }

    @Operation(
            summary = "Get bank account",
            description = "Returns bank account details. **Role: ADMIN or the account owner.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank account found successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isBankAccountOwner(#id, authentication)")
    @GetMapping("/{id}")
    public BankAccountResponse getBankAccount(@PathVariable Long id) {
        return bankService.getBankAccount(id);
    }

    @Operation(
            summary = "Unlink bank account",
            description = "Removes a linked bank account. **Role: ADMIN or the account owner.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bank account unlinked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    @PreAuthorize("hasRole('ADMIN') or @securityService.isBankAccountOwner(#id, authentication)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlinkBankAccount(@PathVariable Long id) {
        bankService.unlinkBankAccount(id);
    }
}