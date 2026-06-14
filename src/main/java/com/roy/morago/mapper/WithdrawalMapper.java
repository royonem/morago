package com.roy.morago.mapper;

import com.roy.morago.dto.finance.WithdrawalRequestDTO;
import com.roy.morago.dto.finance.WithdrawalRequestResponse;
import com.roy.morago.entity.finance.WithdrawalRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WithdrawalMapper {
    WithdrawalRequest createWithdrawalRequestFromDto(WithdrawalRequestDTO dto);
    WithdrawalRequestResponse createWithdrawalRequestResponse(WithdrawalRequest request);
}
