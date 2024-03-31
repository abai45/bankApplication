package kz.demo.bankApplication.utils;

import java.math.BigInteger;
import java.time.Year;
import java.util.Random;

public class AccountUtils {
    public static  final String ACCOUNT_EXISTS_CODE = "001";
    public static final String ACCOUNT_EXISTS_MESSAGE = String.format("This email is already exists");

    public static final String ACCOUNT_CREATION_SUCCESS_CODE = "002";
    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE = "Account has been successfully created";

    public static String generateIban() {
        final String KZ_IBAN_PREFIX = "KZ";
        final int KZ_IBAN_LENGTH = 20;

        Random random = new Random();
        StringBuilder bankAccountNumber = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            bankAccountNumber.append(random.nextInt(10));
        }

        StringBuilder ibanBuilder = new StringBuilder(KZ_IBAN_PREFIX);
        ibanBuilder.append("00");
        ibanBuilder.append(bankAccountNumber);

        BigInteger ibanNumber = new BigInteger(ibanBuilder.toString().substring(4));
        BigInteger remainder = ibanNumber.mod(new BigInteger("97"));
        int checksum = 98 - remainder.intValue();

        ibanBuilder.insert(2, String.format("%02d", checksum));

        return ibanBuilder.toString();
    }
    String iban = generateIban();
}
