package kz.demo.bankApplication.service.impl;

import kz.demo.bankApplication.dto.EmailDetailsDto;

public interface EmailService {
    void sendEmailAlert(EmailDetailsDto emailDetailsDto);
}
