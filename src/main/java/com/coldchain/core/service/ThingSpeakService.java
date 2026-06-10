package com.coldchain.core.service;

import com.coldchain.core.dto.ThingSpeakResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ThingSpeakService {
    @Value("${thingspeak.channel-id}")
    private String channelId;

    @Value("${thingspeak.read-api-key}")
    private String readApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public ThingSpeakResponse fetchLatest(){
        String url = "https://api.thingspeak.com/channels/"
                +channelId
                +"/feeds.json?api_key="
                +readApiKey
                +"&results=1";
        return restTemplate.getForObject(url, ThingSpeakResponse.class);
    }
}
