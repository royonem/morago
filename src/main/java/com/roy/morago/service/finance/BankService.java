package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.BankAccountRequest;
import com.roy.morago.dto.finance.BankAccountResponse;
import com.roy.morago.entity.finance.BankAccount;
import com.roy.morago.entity.user.User;
import com.roy.morago.mapper.BankAccountMapper;
import com.roy.morago.repository.finance.BankRepository;
import com.roy.morago.service.user.UserHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = userHelper.findUserWithAuthentication(authentication);
        log.info("Linking bank account: userId={}", user.getId());
        BankAccount bankAccount = bankAccountMapper.toEntity(request);
        bankAccount.setUser(user);
        bankRepository.save(bankAccount);
        log.info("Bank account linked: userId={}, bankId={}", user.getId(), bankAccount.getId());
        return bankAccountMapper.toResponse(bankAccount);
    }

    @Transactional
    public void unlinkBankAccount(Long id) {
        log.info("Unlinking bank account: bankId={}", id);
        BankAccount account = helper.findBankAccountById(id);
        User user = account.getUser();
        user.setBankAccount(null);
        bankRepository.delete(account);
        log.info("Bank account unlinked: bankId={}, userId={}", id, account.getUser().getId());
    }

    public BankAccountResponse getBankAccount(Long id) {
        return bankAccountMapper.toResponse(helper.findBankAccountById(id));
    }
}
