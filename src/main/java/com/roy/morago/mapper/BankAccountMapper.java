package com.roy.morago.mapper;

import com.roy.morago.dto.finance.BankAccountDTO;
import com.roy.morago.entity.finance.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BankAccountMapper {
    BankAccountDTO createBankAccountDTO(BankAccount bankAccount);
    BankAccount createBankAccountFromDTO(BankAccountDTO dto);
}
