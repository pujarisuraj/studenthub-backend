package com.college.campuscollab.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridEmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email:studenthub.noreply@gmail.com}")
    private String fromEmail;

    @Value("${sendgrid.from.name:StudentHub}")
    private String fromName;

    public void sendEmail(String toEmail, String subject, String body) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                throw new IOException("Failed to send email. Status: " + response.getStatusCode());
            }

            System.out.println("✅ Email sent successfully to: " + toEmail);
        } catch (IOException ex) {
            System.err.println("❌ Failed to send email to: " + toEmail);
            throw ex;
        }
    }
}
