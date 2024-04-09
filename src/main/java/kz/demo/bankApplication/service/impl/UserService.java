package kz.demo.bankApplication.service.impl;

import kz.demo.bankApplication.dto.BankResponseDto;
import kz.demo.bankApplication.dto.CreditDebitRequestDto;
import kz.demo.bankApplication.dto.EnquiryRequestDto;
import kz.demo.bankApplication.dto.UserRequestDto;

public interface UserService {
    BankResponseDto createAccount(UserRequestDto userRequestDto);

    BankResponseDto balanceEnquiry(EnquiryRequestDto request);
    String nameEnquiry(EnquiryRequestDto request);
    BankResponseDto creditAccount(CreditDebitRequestDto request);
    BankResponseDto debitAccount(CreditDebitRequestDto request);
}
