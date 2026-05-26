package com.malkris.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class EmailService {

    private final String RESEND_URL = "https://api.resend.com/emails";

    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String html) {

        try {

            String json = buildJson(to, subject, html);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Email sent successfully: " + response.body());
            } else {
                System.out.println("Email failed: " + response.body());
            }

        } catch (Exception e) {
            System.out.println("Email exception: " + e.getMessage());
        }
    }

    private String buildJson(String to, String subject, String html) {

        return """
        {
          "from": "%s",
          "to": ["%s"],
          "subject": "%s",
          "html": "%s"
        }
        """
                .formatted(
                        fromEmail,
                        to,
                        escape(subject),
                        escape(html)
                );
    }

    private String escape(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "")
                .replace("\r", "");
    }
}