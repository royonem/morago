package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.BankAccountRequest;
import com.roy.morago.dto.finance.BankAccountResponse;
import com.roy.morago.entity.finance.BankAccount;
import com.roy.morago.mapper.BankAccountMapper;
import com.roy.morago.repository.finance.BankRepository;
import com.roy.morago.service.user.UserHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BankService {
    private final BankRepository bankRepository;
    private final UserHelper userHelper;
    private final BankAccountMapper bankAccountMapper;
    private final FinanceHelper helper;

    @Transactional
    public BankAccountResponse linkBankAccount(BankAccountRequest request, Authentication authentication) {
        BankAccount bankAccount = bankAccountMapper.createEntityFromRequest(request);
        bankAccount.setUser(userHelper.findUserWithAuthentication(authentication));
        bankRepository.save(bankAccount);
        log.info("Bank account linked: id={}", bankAccount.getId());
        return bankAccountMapper.createResponseFromEntity(bankAccount);
    }

    @Transactional
    public void unlinkBankAccount(Long id) {
        BankAccount account = helper.findBankAccountById(id);
        bankRepository.delete(account);
        log.info("Bank account unlinked: id={}", id);
    }

    public BankAccountResponse getBankAccount(Long id) {
        return bankAccountMapper.createResponseFromEntity(helper.findBankAccountById(id));
    }
}
