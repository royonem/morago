package com.roy.morago.mapper;

import com.roy.morago.dto.finance.WithdrawalRequest;
import com.roy.morago.dto.finance.WithdrawalResponse;
import com.roy.morago.entity.finance.Withdrawal;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WithdrawalMapper {
    Withdrawal toEntity(WithdrawalRequest request);
    WithdrawalResponse toResponse(Withdrawal withdrawal);
}
