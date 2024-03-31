package kz.demo.bankApplication.service.impl;

import kz.demo.bankApplication.dto.BankResponseDto;
import kz.demo.bankApplication.dto.UserDto;

public interface UserService {
    BankResponseDto createAccount(UserDto userDto);
}
