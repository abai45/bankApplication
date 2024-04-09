package kz.demo.bankApplication.controller;

import kz.demo.bankApplication.dto.BankResponseDto;
import kz.demo.bankApplication.dto.CreditDebitRequestDto;
import kz.demo.bankApplication.dto.EnquiryRequestDto;
import kz.demo.bankApplication.dto.UserRequestDto;
import kz.demo.bankApplication.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping
    public BankResponseDto createAccount(@RequestBody UserRequestDto userRequestDto) {
        return userService.createAccount(userRequestDto);
    }

    @GetMapping("balanceEnquiry")
    public BankResponseDto bankResponseDto(@RequestBody EnquiryRequestDto request) {
        return userService.balanceEnquiry(request);
    }
    @GetMapping("nameEnquiry")
    public String nameEnquiry(@RequestBody EnquiryRequestDto request) {
        return userService.nameEnquiry(request);
    }

    @PostMapping("credit")
    public BankResponseDto creditAccount(@RequestBody CreditDebitRequestDto request) {
        return userService.creditAccount(request);
    }
    @PostMapping("debit")
    public BankResponseDto debitAccount(@RequestBody CreditDebitRequestDto request) {
        return userService.debitAccount(request);
    }
}
