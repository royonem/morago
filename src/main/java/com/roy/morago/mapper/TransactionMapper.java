package com.roy.morago.mapper;

import com.roy.morago.dto.finance.TransactionRequest;
import com.roy.morago.dto.finance.TransactionResponse;
import com.roy.morago.entity.finance.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    Transaction createTransactionFromDto(TransactionRequest dto);
    TransactionResponse createTransactionResponse(Transaction transaction);
}
