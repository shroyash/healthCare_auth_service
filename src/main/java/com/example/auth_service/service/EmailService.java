package com.example.auth_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Simple text email
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    // HTML email
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
            System.out.println("HTML email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email: " + e.getMessage());
            throw new RuntimeException("HTML email sending failed", e);
        }
    }

    // Doctor approval email
    public void sendDoctorApprovalEmail(String doctorEmail, String doctorName) {
        String subject = "Doctor Account Approved";
        String htmlContent = buildApprovalEmailTemplate(doctorName);
        sendHtmlEmail(doctorEmail, subject, htmlContent);
    }

    // Doctor rejection email
    public void sendDoctorRejectionEmail(String doctorEmail, String doctorName) {
        String subject = "Doctor Account Application Update";
        String htmlContent = buildRejectionEmailTemplate(doctorName);
        sendHtmlEmail(doctorEmail, subject, htmlContent);
    }

    // Approval email template
    private String buildApprovalEmailTemplate(String doctorName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: #10b981; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; border-radius: 0 0 8px 8px; }" +
                ".button { display: inline-block; background: #10b981; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>🎉 Congratulations!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Hello Dr. " + doctorName + ",</h2>" +
                "<p>We are pleased to inform you that your doctor account has been <strong>approved</strong>!</p>" +
                "<p>You can now log in to your account and start using all the features available to verified doctors.</p>" +
                "<a href='http://localhost:3000/login' class='button'>Login to Your Account</a>" +
                "<p style='margin-top: 30px;'>If you have any questions, feel free to contact our support team.</p>" +
                "<p>Best regards,<br>Medical Team</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    // Rejection email template
    private String buildRejectionEmailTemplate(String doctorName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: #ef4444; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; border-radius: 0 0 8px 8px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Application Update</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Hello Dr. " + doctorName + ",</h2>" +
                "<p>Thank you for your interest in joining our platform.</p>" +
                "<p>After careful review, we regret to inform you that we are unable to approve your doctor account at this time.</p>" +
                "<p>If you believe this was a mistake or would like to reapply, please contact our support team.</p>" +
                "<p style='margin-top: 30px;'>Best regards,<br>Medical Team</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}