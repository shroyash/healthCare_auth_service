package com.example.auth_service.service;

import com.example.auth_service.globalExpection.EmailSendException;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        Email from = new Email(fromEmail);
        Email recipient = new Email(to);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                log.error("Failed to send email to {}. Status Code: {}, Body: {}",
                        to, response.getStatusCode(), response.getBody());
                throw new EmailSendException("Failed to send email. Status code: " + response.getStatusCode());
            }

            log.info("Email sent successfully to {}. Status Code: {}", to, response.getStatusCode());

        } catch (IOException ex) {
            log.error("IOException while sending email to {}", to, ex);
            throw new EmailSendException("Error sending email", ex);
        }
    }
}
