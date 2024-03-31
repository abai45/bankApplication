package kz.demo.bankApplication.controller;

import kz.demo.bankApplication.dto.BankResponseDto;
import kz.demo.bankApplication.dto.UserDto;
import kz.demo.bankApplication.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping
    public BankResponseDto createAccount(
            @RequestBody UserDto userDto
            ) {
        return userService.createAccount(userDto);
    }
}
