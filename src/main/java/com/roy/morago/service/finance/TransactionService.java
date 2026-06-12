package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.entity.finance.Transaction;
import com.roy.morago.entity.user.User;
import com.roy.morago.enums.TransactionStatus;
import com.roy.morago.enums.TransactionType;
import com.roy.morago.mapper.TransactionMapper;
import com.roy.morago.repository.finance.TransactionRepository;
import com.roy.morago.service.user.UserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final FinanceHelper helper;
    private final UserHelper userHelper;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest dto, Authentication authentication) {
        User user = userHelper.findUserWithAuthentication(authentication);

        Transaction transaction = helper.createTransactionEntity(user, dto);

        if (!dto.type().equals(TransactionType.WITHDRAWAL)) {
            helper.processTransaction(transaction);
        }
        transactionRepository.save(transaction);
        return transactionMapper.createTransactionResponse(transaction);
    }

    public TransactionResponse getTransaction(Long transactionId) {
        return transactionMapper.createTransactionResponse(helper.findTransaction(transactionId));
    }

    public List<TransactionResponse> getAllUserTransactions(Long userId) {
        List<Transaction> transactionList = transactionRepository.getAllByWalletUserId(userId);
        return transactionMapper.createTransactionResponseList(transactionList);
    }

    @Transactional
    public void cancelTransaction(Long id) {
        Transaction transaction = helper.findTransaction(id);
        helper.validateTransactionIsPending(transaction, "Error cancelling non-pending transaction.");
        transaction.setStatus(TransactionStatus.CANCELED);
    }
}
