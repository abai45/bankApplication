package kz.demo.bankApplication.service.impl;

import kz.demo.bankApplication.dto.TransactionDto;
import kz.demo.bankApplication.entity.TransactionEntity;
import kz.demo.bankApplication.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService{

    @Autowired
    TransactionRepository transactionRepository;
    @Override
    public void saveTransaction(TransactionDto transaction) {
        TransactionEntity transactionEntity = TransactionEntity.builder()
                .transactionType(transaction.getTransactionType())
                .accountNumber(transaction.getAccountNumber())
                .amount(transaction.getAmount())
                .status("SUCCESS")
                .build();

        transactionRepository.save(transactionEntity);
        System.out.println("Transaction saved successfully");
    }
}
