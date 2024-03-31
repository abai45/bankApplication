package kz.demo.bankApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoDto {
    private String accountName;
    private BigDecimal accountBalance;
    private String accountNumber;
}
