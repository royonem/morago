package com.roy.morago.dto.socket;

import com.roy.morago.entity.finance.Transaction;
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

    public static TransactionProcessedEvent from(Transaction transaction) {
        TransactionProcessedEvent event = new TransactionProcessedEvent();
        event.setTransactionId(transaction.getId());
        event.setUserId(transaction.getWallet().getId());
        event.setType(transaction.getType());
        event.setStatus(transaction.getStatus());
        event.setAmount(transaction.getAmount());
        event.setNewWalletBalance(transaction.getWallet().getBalance());
        event.setSentAt(LocalDateTime.now());
        return event;
    }
}