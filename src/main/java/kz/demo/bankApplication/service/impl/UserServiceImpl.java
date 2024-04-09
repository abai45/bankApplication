package kz.demo.bankApplication.service.impl;

import kz.demo.bankApplication.dto.*;
import kz.demo.bankApplication.entity.User;
import kz.demo.bankApplication.repository.UserRepository;
import kz.demo.bankApplication.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;


    @Override
    public BankResponseDto createAccount(UserRequestDto userRequestDto) {
    /**
     * Сервис для добавления нового пользователя в ДБ
     */
        if(userRepository.existsByEmail(userRequestDto.getEmail())) {
            BankResponseDto response = BankResponseDto.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
            return response;
        }

        User newUser = User.builder()
                .firstName(userRequestDto.getFirstName())
                .lastName(userRequestDto.getLastName())
                .otherName(userRequestDto.getOtherName())
                .gender(userRequestDto.getGender())
                .accountNumber(AccountUtils.generateIban())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequestDto.getEmail())
                .phoneNumber(userRequestDto.getPhoneNumber())
                .alternativePhoneNumber(userRequestDto.getAlternativePhoneNumber())
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(newUser);

        // Отправка сообщения на почту
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

    // Для транзакций ищем номер аккаунта пользователя
    @Override
    public BankResponseDto balanceEnquiry(EnquiryRequestDto request) {
        boolean isAccountExists = userRepository.existsByAccountNumber(request.getAccountNumber());
        if(!isAccountExists) {
            return BankResponseDto.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_DOES_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NUMBER_DOES_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        return BankResponseDto.builder()
                .responseCode(AccountUtils.ACCOUNT_NUMBER_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_NUMBER_FOUND_MESSAGE)
                .accountInfo(AccountInfoDto.builder()
                        .accountBalance(user.getAccountBalance())
                        .accountNumber(user.getAccountNumber())
                        .accountName(user.getFirstName() +" "+ user.getLastName()+ " " + user.getOtherName())
                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequestDto request) {
        boolean isAccountExists = userRepository.existsByAccountNumber(request.getAccountNumber());
        if(!isAccountExists) {
            return AccountUtils.ACCOUNT_NUMBER_DOES_NOT_EXISTS_MESSAGE;
        }
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        return user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName();
    }

    @Override
    public BankResponseDto creditAccount(CreditDebitRequestDto request) {
        // Проверка если аккаунт существует
        boolean isAccountExists = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExists) {
            return BankResponseDto.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_DOES_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NUMBER_DOES_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User userToCredit = userRepository.findByAccountNumber(request.getAccountNumber());
        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(request.getAmount()));
        userRepository.save(userToCredit);

        return BankResponseDto.builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfoDto.builder()
                        .accountBalance(userToCredit.getAccountBalance())
                        .accountNumber(request.getAccountNumber())
                        .accountName(userToCredit.getFirstName()+" "+userToCredit.getLastName()+" "+userToCredit.getOtherName())
                        .build())
                .build();

    }

    @Override
    public BankResponseDto debitAccount(CreditDebitRequestDto request) {
        boolean isAccountExists = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExists) {
            return BankResponseDto.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_DOES_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NUMBER_DOES_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User userToDebit = userRepository.findByAccountNumber(request.getAccountNumber());
        BigInteger avilableBalance = userToDebit.getAccountBalance().toBigInteger();
        BigInteger debitAmount = request.getAmount().toBigInteger();

        if(avilableBalance.intValue() < debitAmount.intValue()) {
            return BankResponseDto.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        } else {
            userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(request.getAmount()));
            userRepository.save(userToDebit);
            return BankResponseDto.builder()
                    .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_DEBITED_SUCCESS_MESSAGE)
                    .accountInfo(AccountInfoDto.builder()
                            .accountNumber(request.getAccountNumber())
                            .accountName(userToDebit.getFirstName()+" "+userToDebit.getLastName()+" " +userToDebit.getOtherName())
                            .accountBalance(userToDebit.getAccountBalance())
                            .build())
                    .build();
        }
    }
}
