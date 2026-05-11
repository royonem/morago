package com.roy.morago.mapper;

import com.roy.morago.dto.finance.TransactionDTO;
import com.roy.morago.entity.finance.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction createTransactionFromDto(TransactionDTO dto);
}
