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
        System.out.println("================= SMS SERVICE CALLED =================");
        System.out.println("Target Phone: " + phoneNumber);
        System.out.println("Message: " + message);
        System.out.println("Twilio Enabled: " + twilioConfig.isEnabled());
        
        // Check if Twilio is disabled - MOCK mode
        if (!twilioConfig.isEnabled()) {
            System.out.println("⚠ TWILIO IS DISABLED - RUNNING IN MOCK MODE");
            System.out.println("========== SMS MOCK (disabled) ==========");
            System.out.println("To: " + phoneNumber);
            System.out.println("Message: " + message);
            System.out.println("=========================================");
            return "MOCK_SMS_" + System.currentTimeMillis();
        }

        // Validate phone number format
        String validatedPhone = validatePhoneNumber(phoneNumber);
        if (validatedPhone == null) {
            String errorMsg = "Invalid phone number format: " + phoneNumber + 
                            ". Expected format: 0824106982 or +84824106982";
            System.err.println("✗ VALIDATION FAILED: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        System.out.println("✓ Phone validated: " + phoneNumber + " -> " + validatedPhone);
        System.out.println("Using Twilio From Number: " + twilioConfig.getFromNumber());

        try {
            // Send via Twilio
            Message twilioMsg = Message.creator(
                    new PhoneNumber(validatedPhone),                    // Recipient
                    new PhoneNumber(twilioConfig.getFromNumber()),      // From (Twilio number)
                    message                                              // Message content
            ).create();

            System.out.println("✓✓✓ SMS SENT SUCCESSFULLY VIA TWILIO ✓✓✓");
            System.out.println("Twilio SID: " + twilioMsg.getSid());
            System.out.println("Status: " + twilioMsg.getStatus());
            System.out.println("To: " + twilioMsg.getTo());
            System.out.println("From: " + twilioMsg.getFrom());
            System.out.println("======================================================");
            
            return twilioMsg.getSid();
            
        } catch (com.twilio.exception.ApiException e) {
            // Twilio API specific errors
            System.err.println("✗✗✗ TWILIO API ERROR ✗✗✗");
            System.err.println("Error Code: " + e.getCode());
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("More Info: " + e.getMoreInfo());
            System.err.println("Status Code: " + e.getStatusCode());
            e.printStackTrace();
            
            throw new RuntimeException("Twilio API Error [" + e.getCode() + "]: " + e.getMessage(), e);
            
        } catch (Exception e) {
            // General errors
            System.err.println("✗✗✗ UNEXPECTED ERROR SENDING SMS ✗✗✗");
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("======================================================");
            
            throw new RuntimeException("Failed to send SMS to " + phoneNumber + ": " + e.getMessage(), e);
        }
    }

    /**
     * Validate and format phone number to E.164 format
     * Supports multiple Vietnamese formats:
     * - 0824106982 -> +84824106982
     * - +84824106982 -> +84824106982
     * - 84824106982 -> +84824106982
     */
    private String validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            System.out.println("⚠ Phone is null or empty");
            return null;
        }

        // Remove spaces and special characters (except +)
        String cleaned = phone.replaceAll("[\\s()-]", "");
        System.out.println("Phone after cleaning: " + cleaned);

        // Convert to E.164 format for Vietnam (+84)
        if (cleaned.startsWith("0")) {
            // Vietnamese number: 0824106982 -> +84824106982
            String converted = "+84" + cleaned.substring(1);
            System.out.println("Converted format (0xxx -> +84xxx): " + converted);
            return converted;
            
        } else if (cleaned.startsWith("+84")) {
            // Already in E.164 format
            System.out.println("Already in E.164 format: " + cleaned);
            return cleaned;
            
        } else if (cleaned.startsWith("84")) {
            // Missing + sign: 84824106982 -> +84824106982
            String converted = "+" + cleaned;
            System.out.println("Added + prefix: " + converted);
            return converted;
        }

        // Invalid format
        System.out.println("⚠ Invalid phone format: " + cleaned);
        return null;
    }

    /**
     * Check if phone number is valid Vietnamese format
     */
    public boolean isValidVietnamesePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        String cleaned = phone.replaceAll("[\\s()-]", "");
        
        // Check if it matches Vietnamese phone patterns
        return cleaned.startsWith("0") ||           // 0824106982
               cleaned.startsWith("+84") ||         // +84824106982
               cleaned.startsWith("84");            // 84824106982
    }
}