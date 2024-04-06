package kz.demo.bankApplication.service.impl;

import kz.demo.bankApplication.dto.AccountInfoDto;
import kz.demo.bankApplication.dto.BankResponseDto;
import kz.demo.bankApplication.dto.EmailDetailsDto;
import kz.demo.bankApplication.dto.UserDto;
import kz.demo.bankApplication.entity.User;
import kz.demo.bankApplication.repository.UserRepository;
import kz.demo.bankApplication.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;
    @Override
    public BankResponseDto createAccount(UserDto userDto) {
    /**
     * Сервис для добавления нового пользователя в ДБ
     */
        if(userRepository.existsByEmail(userDto.getEmail())) {
            BankResponseDto response = BankResponseDto.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
            return response;
        }

        User newUser = User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .otherName(userDto.getOtherName())
                .gender(userDto.getGender())
                .accountNumber(AccountUtils.generateIban())
                .accountBalance(BigDecimal.ZERO)
                .email(userDto.getEmail())
                .phoneNumber(userDto.getPhoneNumber())
                .alternativePhoneNumber(userDto.getAlternativePhoneNumber())
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(newUser);

        EmailDetailsDto emailDetailsDto = EmailDetailsDto.builder()
                .recipient(savedUser.getEmail())
                .subject("Account creation")
                .messageBody("Congratulations! Your Account has been succesfully created. \n" +
                        "Your Account Details:\n" +
                        "Account Name: "+ savedUser.getFirstName() + " " + savedUser.getLastName() + " " + savedUser.getOtherName() + "\n" +
                        "Account  Number: "+ savedUser.getAccountNumber())

                .build();
        emailService.sendEmailAlert(emailDetailsDto);
        return BankResponseDto.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .accountInfo(AccountInfoDto.builder()
                        .accountName(savedUser.getFirstName()+" "+savedUser.getLastName()+" "+savedUser.getOtherName())
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .build())
                .build();
    }
}
