package com.example.demo.service;


import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.fromNumber}")
    private String fromNumber;

    @Value("${twilio.enabled:true}")
    private boolean twilioEnabled;

    public String sendSms(String phoneNumber, String message) {
        if (!twilioEnabled) {
            // fallback mock để dev/test
            System.out.println("================= SMS MOCK (disabled) =================");
            System.out.println("To: " + phoneNumber);
            System.out.println("Message: " + message);
            System.out.println("=======================================================");
            return null;
        }

        // Gọi Twilio thật
        Message twilioMsg = Message.creator(
                new PhoneNumber(phoneNumber),   // Số nhận
                new PhoneNumber(fromNumber),    // Số Twilio của bạn
                message
        ).create();

        System.out.println("SMS sent via Twilio. SID: " + twilioMsg.getSid());
        return twilioMsg.getSid();
    }
}

