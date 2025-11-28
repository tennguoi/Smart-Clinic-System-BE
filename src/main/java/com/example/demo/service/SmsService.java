package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public void sendSms(String phoneNumber, String message) {
        // Mock sending SMS
        System.out.println("================= SMS SENT =================");
        System.out.println("To: " + phoneNumber);
        System.out.println("Message: " + message);
        System.out.println("============================================");
    }
}
