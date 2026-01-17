package com.innowise.payment.util;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RandomNumberClient {

    private final RestTemplate restTemplate;

    public RandomNumberClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int getRandomInt() {
        // Используем random.org (ограничение: 1000 запросов/день)
        String url = "https://www.random.org/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new";
        try {
            String response = restTemplate.getForObject(url, String.class);
            return Integer.parseInt(response.trim());
        } catch (Exception e) {
            // fallback на локальный рандом, если API недоступен
            return new java.util.Random().nextInt(100) + 1;
        }
    }
}