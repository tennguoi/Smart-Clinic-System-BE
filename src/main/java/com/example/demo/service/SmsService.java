package com.example.demo.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.example.demo.config.TwilioConfig;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private final TwilioConfig twilioConfig;

    public SmsService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    public String sendSms(String phoneNumber, String message) {
        if (!twilioConfig.isEnabled()) {
            System.out.println("================= SMS MOCK (disabled) =================");
            System.out.println("To: " + phoneNumber);
            System.out.println("Message: " + message);
            System.out.println("=======================================================");
            return null;
        }

        // Validate phone number format
        String validatedPhone = validatePhoneNumber(phoneNumber);
        if (validatedPhone == null) {
            System.err.println("Invalid phone number format: " + phoneNumber);
            return null;
        }

        try {
            // Send via Twilio
            Message twilioMsg = Message.creator(
                    new PhoneNumber(validatedPhone),   // Recipient
                    new PhoneNumber(twilioConfig.getFromNumber()),  // ← SỬ DỤNG TwilioConfig
                    message
            ).create();

            System.out.println("SMS sent via Twilio. SID: " + twilioMsg.getSid());
            return twilioMsg.getSid();
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validate and format phone number to E.164 format
     * Example: 0824106982 -> +84824106982
     */
    private String validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        // Remove spaces and special characters
        String cleaned = phone.replaceAll("[\\s()-]", "");

        // Convert to E.164 format
        if (cleaned.startsWith("0")) {
            // Vietnamese number: 0824106982 -> +84824106982
            return "+84" + cleaned.substring(1);
        } else if (cleaned.startsWith("+84")) {
            // Already in E.164 format
            return cleaned;
        } else if (cleaned.startsWith("84")) {
            // Missing + sign
            return "+" + cleaned;
        }

        return null; // Invalid format
    }
}