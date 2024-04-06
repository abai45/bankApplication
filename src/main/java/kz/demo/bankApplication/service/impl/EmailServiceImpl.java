package kz.demo.bankApplication.service.impl;

import kz.demo.bankApplication.dto.EmailDetailsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendEmailAlert(EmailDetailsDto emailDetailsDto) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(senderEmail);
            mailMessage.setTo(emailDetailsDto.getRecipient());
            mailMessage.setText(emailDetailsDto.getMessageBody());
            mailMessage.setSubject(emailDetailsDto.getSubject());
            javaMailSender.send(mailMessage);
            System.out.println("Mail sent succesfully");
        } catch (MailException e) {
            throw  new RuntimeException();
        }
    }
}
