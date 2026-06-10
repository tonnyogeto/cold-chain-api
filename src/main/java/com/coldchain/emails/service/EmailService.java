package com.coldchain.emails.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Value("${spring.application.senderMail}")
    private String fromEmail;
    @Autowired
    private JavaMailSender emailSender;

    public void sendEmailSync(
            String message,
            String subject,
            String recipient
    ) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(fromEmail);
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(message, true);
        emailSender.send(mimeMessage);
    }
}
