package kz.demo.bankApplication.service.impl;

import kz.demo.bankApplication.dto.TransactionDto;

public interface TransactionService {
    void saveTransaction(TransactionDto transaction);
}
