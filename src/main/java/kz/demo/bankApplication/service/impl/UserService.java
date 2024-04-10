package kz.demo.bankApplication.service.impl;

import kz.demo.bankApplication.dto.*;

public interface UserService {
    BankResponseDto createAccount(UserRequestDto userRequestDto);

    BankResponseDto balanceEnquiry(EnquiryRequestDto request);
    String nameEnquiry(EnquiryRequestDto request);
    BankResponseDto creditAccount(CreditDebitRequestDto request);
    BankResponseDto debitAccount(CreditDebitRequestDto request);
    BankResponseDto transferAccount(TransferRequestDto request);
}
