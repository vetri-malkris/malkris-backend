package com.malkris.service;

import com.malkris.dto.ContactRequest;
import com.malkris.entity.ContactMessage;
import com.malkris.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final EmailService emailService;

    @Override
    public void submit(ContactRequest request) {

        log.info("New contact request from: {}", request.getEmail());

        // SAVE TO DB
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

            // =========================
            // ADMIN EMAIL
            // =========================
            String adminHtml = """
                <h2>New Contact Request</h2>
                <p><b>Name:</b> %s</p>
                <p><b>Email:</b> %s</p>
                <p><b>Company:</b> %s</p>
                <p><b>Budget:</b> %s</p>
                <p><b>Message:</b> %s</p>
            """.formatted(
                    safe(request.getName()),
                    safe(request.getEmail()),
                    safe(request.getCompany()),
                    safe(request.getBudget()),
                    safe(request.getMessage())
            );

            emailService.sendEmail(
                    "connect@malkris.com",
                    "New Consultation Request",
                    adminHtml
            );

            // =========================
            // AUTO REPLY
            // =========================
            String autoReplyHtml = """
                <h2>Thank You %s 👋</h2>
                <p>We received your request successfully.</p>
                <p>We will contact you soon.</p>
            """.formatted(safe(request.getName()));

            emailService.sendEmail(
                    request.getEmail(),
                    "Thank You for Contacting Malkris",
                    autoReplyHtml
            );

            log.info("Emails sent successfully");

        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
            throw new RuntimeException("Failed to send email");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}