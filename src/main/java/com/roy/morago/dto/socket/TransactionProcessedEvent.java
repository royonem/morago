package com.roy.morago.dto.socket;

import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransactionProcessedEvent {
    private Long transactionId;
    private Long userId;
    private TransactionType type;
    private TransactionStatus status;
    private Long amount;
    private Long newWalletBalance;
    private LocalDateTime sentAt;
}