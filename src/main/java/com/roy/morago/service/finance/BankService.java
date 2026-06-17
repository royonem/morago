package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.BankAccountDTO;
import com.roy.morago.entity.finance.BankAccount;
import com.roy.morago.mapper.BankAccountMapper;
import com.roy.morago.repository.finance.BankRepository;
import com.roy.morago.service.user.UserHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BankService {
    private final BankRepository bankRepository;
    private final UserHelper userHelper;
    private final BankAccountMapper bankAccountMapper;
    private final FinanceHelper helper;

    @Transactional
    public BankAccountDTO linkBankAccount(BankAccountDTO dto, Authentication authentication) {
        BankAccount bankAccount = bankAccountMapper.createBankAccountFromDTO(dto);
        bankAccount.setUser(userHelper.findUserWithAuthentication(authentication));
        bankRepository.save(bankAccount);
        return bankAccountMapper.createBankAccountDTO(bankAccount);
    }

    @Transactional
    public void unlinkBankAccount(Long id) {
        BankAccount account = helper.findBankAccountById(id);
        bankRepository.delete(account);
    }

    public BankAccountDTO getBankAccount(Long id) {
        return bankAccountMapper.createBankAccountDTO(helper.findBankAccountById(id));
    }
}
