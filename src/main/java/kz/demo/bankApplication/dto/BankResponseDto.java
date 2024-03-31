package kz.demo.bankApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankResponseDto {
    private String responseCode;
    private String responseMessage;
    private AccountInfoDto accountInfo;
}
