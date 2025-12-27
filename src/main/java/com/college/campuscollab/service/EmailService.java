package com.college.campuscollab.service;

import com.college.campuscollab.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetUrl = "http://localhost:3000/#/reset-password?token=" + resetToken;

        // Console logging for debugging
        System.out.println("=====================================");
        System.out.println("PASSWORD RESET EMAIL");
        System.out.println("To: " + toEmail);
        System.out.println("Reset Link: " + resetUrl);
        System.out.println("Token: " + resetToken);
        System.out.println("=====================================");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("StudentHub - Password Reset Request");
            helper.setFrom("studenthub.noreplys@gmail.com");

            String htmlContent = createPasswordResetEmailTemplate(resetUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send bulk email to multiple users
     */
    public void sendBulkEmail(List<User> users, String subject, String messageContent) {
        int successCount = 0;
        int failCount = 0;

        System.out.println("\n\n=====================================");
        System.out.println("📧 BULK EMAIL - Starting");
        System.out.println("=====================================");
        System.out.println("Total Recipients: " + users.size());
        System.out.println("Subject: " + subject);
        System.out.println("From: studenthub.noreplys@gmail.com");
        System.out.println("=====================================\n");

        for (User user : users) {
            try {
                System.out.println("📤 Attempting to send to: " + user.getFullName() + " (" + user.getEmail() + ")");
                sendHtmlEmailToUser(user.getEmail(), user.getFullName(), subject, messageContent);
                successCount++;
                System.out.println("✅ SUCCESS - Email sent to: " + user.getFullName() + " (" + user.getEmail() + ")");

                // Small delay to avoid rate limiting
                Thread.sleep(200);
            } catch (Exception e) {
                failCount++;
                System.err.println("\n❌ FAILED to send email to: " + user.getFullName() + " (" + user.getEmail() + ")");
                System.err.println("Error Type: " + e.getClass().getName());
                System.err.println("Error Message: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Root Cause: " + e.getCause().getMessage());
                }
                e.printStackTrace();
                System.err.println();
            }
        }

        System.out.println("\n=====================================");
        System.out.println("📊 BULK EMAIL - Complete");
        System.out.println("=====================================");
        System.out.println("✅ Success: " + successCount);
        System.out.println("❌ Failed: " + failCount);
        System.out.println("📈 Total: " + users.size());
        System.out.println("=====================================\n\n");
    }

    /**
     * Send HTML email to individual user
     */
    public void sendHtmlEmailToUser(String toEmail, String userName, String subject, String messageContent)
            throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom("studenthub.noreplys@gmail.com", "StudentHub Admin");

            String htmlContent = createBulkEmailTemplate(userName, subject, messageContent);
            helper.setText(htmlContent, true);

            System.out.println("📧 Sending HTML email to: " + toEmail);
            mailSender.send(message);
            System.out.println("✅ HTML Email sent successfully");
        } catch (Exception e) {
            System.err.println("❌ HTML email failed, trying plain text: " + e.getMessage());
            // Fallback to plain text email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom("studenthub.noreplys@gmail.com", "StudentHub Admin");

            String plainText = "Dear " + userName + ",\n\n" +
                    messageContent + "\n\n" +
                    "Best regards,\n" +
                    "StudentHub Admin\n\n" +
                    "---\n" +
                    "This is an automated message from StudentHub Academic Portal.";

            helper.setText(plainText, false);

            System.out.println("📧 Sending PLAIN TEXT email to: " + toEmail);
            mailSender.send(message);
            System.out.println("✅ Plain text email sent successfully");
        }
    }

    /**
     * Send HTML email to individual user with optional attachments
     */
    public void sendHtmlEmailWithAttachments(String toEmail, String userName, String subject, String messageContent,
            org.springframework.web.multipart.MultipartFile[] attachments) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom("studenthub.noreplys@gmail.com", "StudentHub Admin");

            String htmlContent = createBulkEmailTemplate(userName, subject, messageContent);
            helper.setText(htmlContent, true);

            // Attach files if present
            if (attachments != null && attachments.length > 0) {
                System.out.println("📎 Attaching " + attachments.length + " file(s)...");
                for (org.springframework.web.multipart.MultipartFile file : attachments) {
                    if (!file.isEmpty()) {
                        helper.addAttachment(file.getOriginalFilename(), file);
                        System.out.println("   ✓ Attached: " + file.getOriginalFilename());
                    }
                }
            }

            System.out.println("📧 Sending HTML email with attachments to: " + toEmail);
            mailSender.send(message);
            System.out.println("✅ Email sent successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Convert newlines to HTML <br>
     * tags for email compatibility
     * This ensures formatting works across all email clients including Outlook
     */
    private String formatMessageContent(String message) {
        if (message == null) {
            return "";
        }
        // Replace newlines with <br> tags
        // Handle both \r\n (Windows) and \n (Unix) line breaks
        return message.replace("\r\n", "<br>").replace("\n", "<br>");
    }

    /**
     * Create beautiful HTML email template for bulk emails
     */
    private String createBulkEmailTemplate(String userName, String subject, String messageContent) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>" + subject + "</title>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif; background-color: #f5f5f5;'>"
                +
                "    <table width='100%' cellpadding='0' cellspacing='0' style='padding: 40px 20px;'>" +
                "        <tr>" +
                "            <td align='center'>" +
                "                <table width='600' cellpadding='0' cellspacing='0' style='background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"
                +
                "                    " +
                "                    <!-- Header -->" +
                "                    <tr>" +
                "                        <td style='background-color: #f0f4f8; padding: 50px 30px; text-align: center;'>"
                +
                "                            <a href='https://pujarisuraj.github.io/studenthub/' style='text-decoration: none; display: inline-block;'>"
                +
                "                                <div style='font-size: 110px; margin-bottom: 15px;'>🎓</div>" +
                "                            </a>" +
                "                            <h1 style='color: #2d3e73; margin: 0 0 8px; font-size: 36px; font-weight: 700; letter-spacing: 0.5px;'>StudentHub</h1>"
                +
                "                            <a href='https://pujarisuraj.github.io/studenthub/' style='text-decoration: none;'>"
                +
                "                                <p style='color: #8b95a5; margin: 0; font-size: 13px; letter-spacing: 3px; font-weight: 500;'>ACADEMIC PORTAL</p>"
                +
                "                            </a>" +
                "                        </td>" +
                "                    </tr>" +
                "                    " +
                "                    <!-- Content -->" +
                "                    <tr>" +
                "                        <td style='padding: 40px 35px; background-color: #ffffff;'>" +
                "                            <h2 style='color: #2d3748; margin: 0 0 8px; font-size: 22px; font-weight: 600;'>"
                + subject + "</h2>" +
                "                            <p style='color: #718096; margin: 0 0 28px; font-size: 13px;'>From StudentHub Admin</p>"
                +
                "                            " +
                "                            <!-- Message Card -->" +
                "                            <div style='background-color: #f7fafc; border-left: 4px solid #667eea; padding: 24px 28px; border-radius: 8px; margin-bottom: 28px;'>"
                +
                "                                <p style='color: #1a202c; margin: 0 0 12px; font-size: 15px; font-weight: 600;'>Dear "
                + userName + ",</p>" +
                "                                <div style='color: #2d3748; margin: 0; font-size: 14px; line-height: 1.8;'>"
                + formatMessageContent(messageContent) + "</div>" +
                "                            </div>" +
                "                            " +
                "                            <!-- Signature -->" +
                "                            <div style='margin-top: 32px; padding-top: 24px; border-top: 1px solid #e2e8f0;'>"
                +
                "                                <p style='color: #4a5568; margin: 0 0 4px; font-size: 13px;'>Best regards,</p>"
                +
                "                                <p style='color: #2d3748; margin: 0; font-size: 15px; font-weight: 600;'>StudentHub Admin </p>"
                +
                "                            </div>" +
                "                        </td>" +
                "                    </tr>" +
                "                    " +
                "                    <!-- Footer -->" +
                "                    <tr>" +
                "                        <td style='background-color: #f7fafc; padding: 24px 35px; text-align: center; border-top: 1px solid #e2e8f0;'>"
                +
                "                            <p style='color: #718096; margin: 0 0 8px; font-size: 13px;'>Contact: <a href='tel:+918382038383' style='color: #667eea; text-decoration: none; font-weight: 600;'>+91 8382038383</a></p>"
                +
                "                            <p style='color: #a0aec0; margin: 0; font-size: 11px;'>© 2025 StudentHub. Learn. Build. Share.</p>"
                +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "            </td>" +
                "        </tr>" +
                "    </table>" +
                "</body>" +
                "</html>";
    }

    /**
     * Create HTML email template for password reset
     */
    private String createPasswordResetEmailTemplate(String resetUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif; background-color: #f5f5f5;'>"
                +
                "    <table width='100%' cellpadding='0' cellspacing='0' style='padding: 40px 20px;'>" +
                "        <tr>" +
                "            <td align='center'>" +
                "                <table width='600' cellpadding='0' cellspacing='0' style='background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"
                +
                "                    <tr>" +
                "                        <td style='background-color: #f0f4f8; padding: 50px 30px; text-align: center;'>"
                +
                "                            <div style='font-size: 70px; margin-bottom: 20px;'>🔐</div>" +
                "                            <h1 style='color: #2d3e73; margin: 0 0 8px; font-size: 36px; font-weight: 700; letter-spacing: 0.5px;'>Password Reset</h1>"
                +
                "                            <p style='color: #8b95a5; margin: 0; font-size: 13px; letter-spacing: 3px; font-weight: 500;'>StudentHub Academic Portal</p>"
                +
                "                        </td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td style='padding: 40px 35px; background-color: #ffffff;'>" +
                "                            <h2 style='color: #2d3748; margin: 0 0 8px; font-size: 22px; font-weight: 600;'>Reset Your Password</h2>"
                +
                "                            <p style='color: #718096; margin: 0 0 28px; font-size: 13px; line-height: 1.6;'>Click the button below to reset your password. This link will expire in 5 minutes for security.</p>"
                +
                "                            <div style='text-align: center; margin: 32px 0;'>" +
                "                                <a href='" + resetUrl
                + "' style='display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #000000; padding: 14px 40px; text-decoration: none; border-radius: 10px; font-weight: 600; font-size: 15px; box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);'>Reset Password</a>"
                +
                "                            </div>" +
                "                            <p style='color: #a0aec0; margin-top: 24px; font-size: 12px; line-height: 1.6;'>If you didn't request this, please ignore this email. Your password will remain unchanged.</p>"
                +
                "                        </td>" +
                "                    </tr>" +
                "                    <tr>" +
                "                        <td style='background-color: #f7fafc; padding: 24px 35px; text-align: center; border-top: 1px solid #e2e8f0;'>"
                +
                "                            <p style='color: #718096; margin: 0 0 8px; font-size: 13px;'>Contact: <a href='tel:+918382038383' style='color: #667eea; text-decoration: none; font-weight: 600;'>+91 8382038383</a></p>"
                +
                "                            <p style='color: #718096; margin: 0 0 8px; font-size: 12px;'>This is an automated security message from StudentHub</p>"
                +
                "                            <p style='color: #a0aec0; margin: 0; font-size: 11px;'>© 2025 StudentHub. Learn. Build. Share.</p>"
                +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "            </td>" +
                "        </tr>" +
                "    </table>" +
                "</body>" +
                "</html>";
    }
}
