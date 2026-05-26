package com.malkris.service;

import com.malkris.dto.ContactRequest;
import com.malkris.entity.ContactMessage;
import com.malkris.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    @Override
    public void submit(ContactRequest request) {

        log.info("New contact request from: {}", request.getEmail());

        /*
         * SAVE TO DATABASE
         */
        ContactMessage contactMessage = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .company(request.getCompany())
                .budget(request.getBudget())
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        contactRepository.save(contactMessage);

        try {

            /*
             * ======================================================
             * ADMIN EMAIL HTML
             * ======================================================
             */
            String adminHtml = """
<html>
<body style="margin:0;padding:30px;background:#f4f7fb;font-family:Arial,sans-serif;">
<div style="max-width:600px;margin:auto;background:#ffffff;border-radius:18px;overflow:hidden;border:1px solid #e5e7eb;">
<div style="background:#00d9ff;padding:22px;text-align:center;">
<h1 style="margin:0;color:#000;font-size:26px;font-weight:800;">MALKRIS</h1>
</div>

<div style="padding:30px;color:#111827;">
<h2>New Consultation Request</h2>

<p><b>Name:</b> %s</p>
<p><b>Email:</b> %s</p>
<p><b>Company:</b> %s</p>
<p><b>Budget:</b> %s</p>

<div style="margin-top:20px;background:#f9fafb;padding:15px;border-radius:10px;">
%s
</div>

</div>
</div>
</body>
</html>
""".formatted(
                    safe(request.getName()),
                    safe(request.getEmail()),
                    safe(request.getCompany()),
                    safe(request.getBudget()),
                    safe(request.getMessage())
            );

            sendEmail(
                    "s1bcavetri10@gmail.com",
                    "New Consultation Request",
                    adminHtml
            );

            log.info("Admin email sent");

            /*
             * ======================================================
             * AUTO REPLY EMAIL
             * ======================================================
             */
            String autoReplyHtml = """
<html>
<body style="margin:0;padding:30px;background:#f4f7fb;font-family:Arial,sans-serif;">
<div style="max-width:560px;margin:auto;background:#ffffff;border-radius:18px;overflow:hidden;border:1px solid #e5e7eb;">
<div style="background:#00d9ff;padding:20px;text-align:center;">
<h1 style="margin:0;color:#000;">MALKRIS</h1>
</div>

<div style="padding:30px;">
<h2>Thank You, %s 👋</h2>

<p>We received your request successfully.</p>
<p>Our team will contact you soon.</p>

<p><b>Email:</b> %s</p>

</div>
</div>
</body>
</html>
""".formatted(
                    safe(request.getName()),
                    safe(request.getEmail())
            );

            sendEmail(
                    request.getEmail(),
                    "Thank You for Contacting Malkris",
                    autoReplyHtml
            );

            log.info("Auto reply email sent");

        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
            throw new RuntimeException("Failed to send email");
        }
    }

    /*
     * ===============================
     * RESEND EMAIL SENDER
     * ===============================
     */
    private void sendEmail(String to, String subject, String html) {

        try {
            HttpClient client = HttpClient.newHttpClient();

            String body = """
            {
              "from": "Malkris <connect@malkris.com>",
              "to": ["%s"],
              "subject": "%s",
              "html": "%s"
            }
            """.formatted(to, subject, html.replace("\"", "\\\""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + System.getenv("RESEND_API_KEY"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Resend response: {}", response.body());

        } catch (Exception e) {
            log.error("Resend error: {}", e.getMessage());
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}