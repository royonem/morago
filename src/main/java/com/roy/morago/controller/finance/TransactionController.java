package com.roy.morago.controller.finance;

import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.dto.finance.TransactionSearchRequest;
import com.roy.morago.service.finance.TransactionService;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "08 - Transactions", description = "Transaction management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(
            summary = "Create deposit",
            description = "Creates a new deposit transaction. **Role: Any authenticated user.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Deposit created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Pending transaction already exists")
    })
    @PostMapping("/deposits")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createDeposit(@Valid @RequestBody TransactionRequest dto, Authentication authentication) {
        return transactionService.createDepositTransaction(dto, authentication);
    }

    @Operation(
            summary = "Get transaction by ID",
            description = "Returns transaction details. **Role: ADMIN or the transaction owner.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#id, authentication)")
    public TransactionResponse getTransaction(@PathVariable Long id) {
        return transactionService.getTransaction(id);
    }

    @Operation(
            summary = "Cancel transaction",
            description = "Cancels a pending transaction. **Role: ADMIN or the transaction owner.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction canceled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "409", description = "Transaction is not pending")
    })
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#id, authentication)")
    public void cancelTransaction(@PathVariable Long id) {
        transactionService.cancelTransaction(id);
    }

    @Operation(
            summary = "Get all transactions",
            description = "Returns a paginated list of all transactions. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<TransactionResponse> getAllTransactions(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.getAllTransactions(pageable);
    }

    @Operation(
            summary = "Get user transactions",
            description = "Returns paginated transactions for a specific user. **Role: ADMIN or the user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot access this user's transactions"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId, authentication)")
    public Page<TransactionResponse> getUserTransactions(@PathVariable Long userId, @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.getTransactionsByUserId(userId, pageable);
    }

    @Operation(
            summary = "Search transactions",
            description = "Searches transactions by criteria. **Role: ADMIN only.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<TransactionResponse> searchTransactions(@RequestBody TransactionSearchRequest request) {
        return transactionService.searchTransactions(request);
    }

    @Operation(
            summary = "Search user transactions",
            description = "Searches transactions for a specific user by criteria. **Role: ADMIN or the user themselves.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot access this user's transactions"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/search/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId, authentication)")
    public Page<TransactionResponse> searchUserTransactions(@PathVariable Long userId, @RequestBody TransactionSearchRequest request) {
        return transactionService.searchTransactionsByUserId(userId, request);
    }
}