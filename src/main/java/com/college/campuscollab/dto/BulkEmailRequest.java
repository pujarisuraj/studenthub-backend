package com.college.campuscollab.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkEmailRequest {
    private List<Long> studentIds;
    private List<String> additionalEmails; // For manual email addresses
    private List<Recipient> additionalRecipients; // For manual recipients with email and name
    private String subject;
    private String message;

    @Data
    public static class Recipient {
        private String email;
        private String name; // Can be null
    }
}
