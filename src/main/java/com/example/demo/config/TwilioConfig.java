package com.example.demo.config;

import com.twilio.Twilio;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "twilio")
public class TwilioConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(TwilioConfig.class);
    
    private String accountSid;
    private String authToken;
    private String fromNumber;
    private boolean enabled = true;

    @PostConstruct
    public void init() {
        logger.info("=== TWILIO CONFIGURATION ===");
        logger.info("Enabled: {}", enabled);
        logger.info("Account SID: {}", accountSid);
        logger.info("From Number: {}", fromNumber);
        logger.info("Auth Token: {}", authToken != null ? "***" + authToken.substring(Math.max(0, authToken.length() - 4)) : "null");
        
        if (enabled) {
            if (accountSid == null || accountSid.trim().isEmpty()) {
                throw new IllegalStateException("Twilio Account SID is required when twilio.enabled=true");
            }
            if (authToken == null || authToken.trim().isEmpty()) {
                throw new IllegalStateException("Twilio Auth Token is required when twilio.enabled=true");
            }
            
            try {
                Twilio.init(accountSid, authToken);
                logger.info("Twilio initialized successfully!");
            } catch (Exception e) {
                logger.error("Failed to initialize Twilio: {}", e.getMessage());
                throw new RuntimeException("Twilio initialization failed", e);
            }
        } else {
            logger.info("Twilio is disabled");
        }
    }

    // GETTERS AND SETTERS
    public String getAccountSid() { return accountSid; }
    public void setAccountSid(String accountSid) { this.accountSid = accountSid; }
    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
    public String getFromNumber() { return fromNumber; }
    public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}