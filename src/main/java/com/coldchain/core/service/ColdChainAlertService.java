package com.coldchain.core.service;

import com.coldchain.emails.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColdChainAlertService {
    private final RestTemplate restTemplate = new RestTemplate();

    private final EmailService emailService;

    @Value("${thingspeak.channel-id}")
    private String channelId;

    @Value("${thingspeak.read-api-key}")
    private String readApiKey;

    // Fixed: Aligned threshold tracking ceiling to 28.0°C
    private final double TEMP_THRESHOLD_CEILING = 28.0;
    private int lastProcessedEntryId = 0;

    @Scheduled(fixedRate = 30000)
    public void monitorColdChainTelemetry() {
        try {
            String thingSpeakUrl = String.format(
                    "https://api.thingspeak.com/channels/%s/feeds.json?api_key=%s&results=1",
                    channelId, readApiKey
            );

            Map<String, Object> response = restTemplate.getForObject(thingSpeakUrl, Map.class);
            if (response != null && response.containsKey("feeds")) {
                List<Map<String, Object>> feeds = (List<Map<String, Object>>) response.get("feeds");

                if (!feeds.isEmpty()) {
                    Map<String, Object> latestFeed = feeds.get(0);

                    // Fixed: Safe extraction using Number bridge interface
                    int currentEntryId = ((Number) latestFeed.get("entry_id")).intValue();

                    if (currentEntryId > lastProcessedEntryId) {
                        lastProcessedEntryId = currentEntryId;

                        String rawTemp = (String) latestFeed.get("field1");
                        if (rawTemp != null) {
                            double currentTemp = Double.parseDouble(rawTemp);

                            if (currentTemp > TEMP_THRESHOLD_CEILING) {
                                dispatchEmergencyEmail(currentTemp, currentEntryId);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ColdChain Engine telemetry parsing error: " + e.getMessage());
            e.printStackTrace(); // Vital for monitoring security or handshake faults
        }
    }

    private void dispatchEmergencyEmail(double breachedTemp, int entryId) throws MessagingException {
        String emailBody = String.format(
                "Warning! The monitored Cold Chain container has breached safe operating thresholds.\n\n" +
                        "Log Entry ID: %d\n" +
                        "Current Temperature Metric: %.2f°C\n" +
                        "Safety Threshold Ceiling: %.2f°C\n\n" +
                        "Please check the container system diagnostics immediately.",
                entryId, breachedTemp, TEMP_THRESHOLD_CEILING
        );
        String systemSenderEmail = "tonnymaishaogeto@gmail.com";
        emailService.sendEmailSync(
                emailBody,
                "🚨 CRITICAL BREACH: ColdChain Unit Warning",
                systemSenderEmail
        );
        System.out.println("Success: Critical threshold alert email safely sent to " + systemSenderEmail);
    }
}