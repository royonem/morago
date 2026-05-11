package com.roy.morago.service.finance;

import com.roy.morago.dto.finance.BankAccountDTO;
import com.roy.morago.entity.finance.BankAccount;
import com.roy.morago.entity.user.User;
import com.roy.morago.exception.BankNotFoundException;
import com.roy.morago.repository.finance.BankRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BankService {
    private final BankRepository bankRepository;

    @Transactional
    public BankAccount linkBankAccount(BankAccountDTO dto, User user) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setUser(user);
        bankAccount.setBankName(dto.getBankName());
        bankAccount.setAccountNumber(dto.getAccountNumber());
        return bankRepository.save(bankAccount);
    }

    @Transactional
    public void unlinkBankAccount(Long id) {
        if (bankRepository.existsById(id)) {
            bankRepository.deleteById(id);
        } else {
            throw new BankNotFoundException("Bank account not found");
        }
    }
}
