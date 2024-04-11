package kz.demo.bankApplication.service.impl;

import kz.demo.bankApplication.config.JwtTokenProvider;
import kz.demo.bankApplication.dto.*;
import kz.demo.bankApplication.entity.RoleEntity;
import kz.demo.bankApplication.entity.UserEntity;
import kz.demo.bankApplication.repository.UserRepository;
import kz.demo.bankApplication.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

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

        UserEntity newUserEntity = UserEntity.builder()
                .firstName(userRequestDto.getFirstName())
                .lastName(userRequestDto.getLastName())
                .otherName(userRequestDto.getOtherName())
                .gender(userRequestDto.getGender())
                .accountNumber(AccountUtils.generateIban())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequestDto.getEmail())
                .password(passwordEncoder.encode(userRequestDto.getPassword()))
                .phoneNumber(userRequestDto.getPhoneNumber())
                .alternativePhoneNumber(userRequestDto.getAlternativePhoneNumber())
                .status("ACTIVE")
                .role(RoleEntity.valueOf("ROLE_USER"))
                .build();

        UserEntity savedUserEntity = userRepository.save(newUserEntity);

        // Отправка сообщения на почту
        EmailDetailsDto emailDetailsDto = EmailDetailsDto.builder()
                .recipient(savedUserEntity.getEmail())
                .subject("Account creation")
                .messageBody("Congratulations! Your Account has been succesfully created. \n" +
                        "Your Account Details:\n" +
                        "Account Name: "+ savedUserEntity.getFirstName() + " " + savedUserEntity.getLastName() + " " + savedUserEntity.getOtherName() + "\n" +
                        "Account  Number: "+ savedUserEntity.getAccountNumber())

                .build();
        emailService.sendEmailAlert(emailDetailsDto);
        return BankResponseDto.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .accountInfo(AccountInfoDto.builder()
                        .accountName(savedUserEntity.getFirstName()+" "+ savedUserEntity.getLastName()+" "+ savedUserEntity.getOtherName())
                        .accountBalance(savedUserEntity.getAccountBalance())
                        .accountNumber(savedUserEntity.getAccountNumber())
                        .build())
                .build();
    }

    public BankResponseDto loginAccount(LoginAccountDto loginAccountDto) {
        Authentication authentication = null;

        authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginAccountDto.getEmail(),
                        loginAccountDto.getPassword()
                )
        );
        EmailDetailsDto loginAlert = EmailDetailsDto.builder()
                .subject("You're logged in")
                .recipient(loginAccountDto.getEmail())
                .messageBody("You logged into your account.")
                .build();

        emailService.sendEmailAlert(loginAlert);
        return BankResponseDto.builder()
                .responseCode("Login Success")
                .responseMessage(jwtTokenProvider.generateToken(authentication))
                .build();
    }

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
        UserEntity userEntity = userRepository.findByAccountNumber(request.getAccountNumber());
        return BankResponseDto.builder()
                .responseCode(AccountUtils.ACCOUNT_NUMBER_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_NUMBER_FOUND_MESSAGE)
                .accountInfo(AccountInfoDto.builder()
                        .accountBalance(userEntity.getAccountBalance())
                        .accountNumber(userEntity.getAccountNumber())
                        .accountName(userEntity.getFirstName() +" "+ userEntity.getLastName()+ " " + userEntity.getOtherName())
                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequestDto request) {
        boolean isAccountExists = userRepository.existsByAccountNumber(request.getAccountNumber());
        if(!isAccountExists) {
            return AccountUtils.ACCOUNT_NUMBER_DOES_NOT_EXISTS_MESSAGE;
        }
        UserEntity userEntity = userRepository.findByAccountNumber(request.getAccountNumber());
        return userEntity.getFirstName() + " " + userEntity.getLastName() + " " + userEntity.getOtherName();
    }

    @Override
    public BankResponseDto creditAccount(CreditDebitRequestDto request) {
        boolean isAccountExists = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExists) {
            return BankResponseDto.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_DOES_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NUMBER_DOES_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        UserEntity userEntityToCredit = userRepository.findByAccountNumber(request.getAccountNumber());
        userEntityToCredit.setAccountBalance(userEntityToCredit.getAccountBalance().add(request.getAmount()));
        userRepository.save(userEntityToCredit);

        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(userEntityToCredit.getAccountNumber())
                .transactionType("CREDIT")
                .amount(request.getAmount())
                .build();

        transactionService.saveTransaction(transactionDto);

        return BankResponseDto.builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfoDto.builder()
                        .accountBalance(userEntityToCredit.getAccountBalance())
                        .accountNumber(request.getAccountNumber())
                        .accountName(userEntityToCredit.getFirstName()+" "+ userEntityToCredit.getLastName()+" "+ userEntityToCredit.getOtherName())
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
        UserEntity userEntityToDebit = userRepository.findByAccountNumber(request.getAccountNumber());
        BigInteger avilableBalance = userEntityToDebit.getAccountBalance().toBigInteger();
        BigInteger debitAmount = request.getAmount().toBigInteger();

        if(avilableBalance.intValue() < debitAmount.intValue()) {
            return BankResponseDto.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        } else {
            userEntityToDebit.setAccountBalance(userEntityToDebit.getAccountBalance().subtract(request.getAmount()));
            userRepository.save(userEntityToDebit);

            TransactionDto transactionDto = TransactionDto.builder()
                    .accountNumber(userEntityToDebit.getAccountNumber())
                    .transactionType("DEBIT")
                    .amount(request.getAmount())
                    .build();

            transactionService.saveTransaction(transactionDto);

            return BankResponseDto.builder()
                    .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_DEBITED_SUCCESS_MESSAGE)
                    .accountInfo(AccountInfoDto.builder()
                            .accountNumber(request.getAccountNumber())
                            .accountName(userEntityToDebit.getFirstName()+" "+ userEntityToDebit.getLastName()+" " + userEntityToDebit.getOtherName())
                            .accountBalance(userEntityToDebit.getAccountBalance())
                            .build())
                    .build();
        }
    }

    @Override
    public BankResponseDto transferAccount(TransferRequestDto request) {
        boolean isDestinationAccountExists = userRepository.existsByAccountNumber(request.getDestinationAccountNumber());

        if(!isDestinationAccountExists) {
            return BankResponseDto.builder()
                    .responseCode(AccountUtils.DESTINATION_ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.DESTINATION_ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        UserEntity sourceAccount = userRepository.findByAccountNumber(request.getSourceAccountNumber());
        UserEntity destinationAccount = userRepository.findByAccountNumber(request.getDestinationAccountNumber());

        if(request.getAmount().compareTo(sourceAccount.getAccountBalance()) > 0) {
            return BankResponseDto.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        sourceAccount.setAccountBalance(sourceAccount.getAccountBalance().subtract(request.getAmount()));
        destinationAccount.setAccountBalance(destinationAccount.getAccountBalance().add(request.getAmount()));
        String sourceUsername = sourceAccount.getFirstName()+ " "+ sourceAccount.getLastName()+" "+ sourceAccount.getOtherName();
        String destinationUsername = destinationAccount.getFirstName()+ " "+ destinationAccount.getLastName()+" "+ destinationAccount.getOtherName();
        EmailDetailsDto debitAlert = EmailDetailsDto.builder()
                .subject("DEBIT ALERT")
                .recipient(sourceAccount.getEmail())
                .messageBody("The sum of "+ request.getAmount() + " has been debited from your account to " + destinationUsername +
                        "\nYour current balance is " + sourceAccount.getAccountBalance())
                .build();
        userRepository.save(sourceAccount);
        EmailDetailsDto creditAlert = EmailDetailsDto.builder()
                .subject("CREDIT ALERT")
                .recipient(destinationAccount.getEmail())
                .messageBody("The sum of "+ request.getAmount() + " has been sent to your account from " + sourceUsername +
                        "\nYour current balance is " + destinationAccount.getAccountBalance())
                .build();
        userRepository.save(destinationAccount);

        emailService.sendEmailAlert(debitAlert);
        emailService.sendEmailAlert(creditAlert);

        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(destinationAccount.getAccountNumber())
                .transactionType("CREDIT")
                .amount(request.getAmount())
                .build();

        transactionService.saveTransaction(transactionDto);

        return BankResponseDto.builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESS_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESS_MESSAGE)
                .accountInfo(null)
                .build();
    }

    public static void main(String[] args) {
        UserServiceImpl userService = new UserServiceImpl();
        System.out.println(userService.passwordEncoder.encode("12345"));
    }
}
