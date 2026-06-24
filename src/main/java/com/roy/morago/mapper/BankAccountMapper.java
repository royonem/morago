package com.roy.morago.mapper;

import com.roy.morago.dto.finance.BankAccountRequest;
import com.roy.morago.dto.finance.BankAccountResponse;
import com.roy.morago.entity.finance.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BankAccountMapper {
    BankAccountResponse createResponseFromEntity(BankAccount bankAccount);
    BankAccount createEntityFromRequest(BankAccountRequest request);
}
