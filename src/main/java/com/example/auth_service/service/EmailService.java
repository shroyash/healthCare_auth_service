package com.example.auth_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")        
    private String frontendUrl;


    @Async("emailTaskExecutor")
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }


    @Async("emailTaskExecutor")
    public void sendDoctorApprovalEmail(String doctorEmail, String doctorName) {
        try {
            sendHtmlEmail(
                    doctorEmail,
                    "Doctor Account Approved",
                    buildApprovalEmailTemplate(doctorName)
            );
        } catch (Exception e) {
            log.error("Approval email failed for: {}", doctorEmail, e);
        }
    }

    @Async("emailTaskExecutor")
    public void sendDoctorRejectionEmail(String doctorEmail, String doctorName) {
        try {
            sendHtmlEmail(
                    doctorEmail,
                    "Doctor Account Application Update",
                    buildRejectionEmailTemplate(doctorName)
            );
        } catch (Exception e) {
            log.error("Rejection email failed for: {}", doctorEmail, e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("HTML email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("HTML email sending failed", e);
        }
    }

    private String buildApprovalEmailTemplate(String doctorName) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: #10b981; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; border-radius: 0 0 8px 8px; }" +
                ".button { display: inline-block; background: #10b981; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin-top: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h1>Congratulations!</h1></div>" +
                "<div class='content'>" +
                "<h2>Hello Dr. " + doctorName + ",</h2>" +
                "<p>Your doctor account has been <strong>approved</strong>!</p>" +
                "<p>You can now log in and start using all features.</p>" +
                "<a href='" + frontendUrl + "/login' class='button'>Login to Your Account</a>" + // ✅ not hardcoded
                "<p style='margin-top: 30px;'>Best regards,<br>Medical Team</p>" +
                "</div></div></body></html>";
    }

    private String buildRejectionEmailTemplate(String doctorName) {
        return "<!DOCTYPE html>" +
                "<html><head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: #ef4444; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; border-radius: 0 0 8px 8px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h1>Application Update</h1></div>" +
                "<div class='content'>" +
                "<h2>Hello Dr. " + doctorName + ",</h2>" +
                "<p>After careful review, we are unable to approve your account at this time.</p>" +
                "<p>If you believe this was a mistake, please contact our support team.</p>" +
                "<p style='margin-top: 30px;'>Best regards,<br>Medical Team</p>" +
                "</div></div></body></html>";
    }
}