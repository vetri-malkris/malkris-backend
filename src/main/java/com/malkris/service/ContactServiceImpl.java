package com.malkris.service;

import com.malkris.dto.ContactRequest;
import com.malkris.entity.ContactMessage;
import com.malkris.repository.ContactRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final JavaMailSender mailSender;

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
             * ADMIN MAIL
             * ======================================================
             */

            MimeMessage adminMail = mailSender.createMimeMessage();

            MimeMessageHelper adminHelper =
                    new MimeMessageHelper(adminMail, true, "UTF-8");

            adminHelper.setTo("s1bcavetri10@gmail.com");

            adminHelper.setSubject("New Consultation Request");

            adminHelper.setReplyTo(request.getEmail());

            String adminHtml = """
<html>
<body style="
    margin:0;
    padding:30px;
    background:#f4f7fb;
    font-family:Arial,sans-serif;
">

<div style="
    max-width:600px;
    margin:auto;
    background:#ffffff;
    border-radius:18px;
    overflow:hidden;
    border:1px solid #e5e7eb;
">

    <!-- HEADER -->

    <div style="
        background:#00d9ff;
        padding:22px;
        text-align:center;
    ">

        <h1 style="
            margin:0;
            color:#000;
            font-size:26px;
            font-weight:800;
            letter-spacing:1px;
        ">
            MALKRIS
        </h1>

    </div>

    <!-- BODY -->

    <div style="
        padding:30px;
        color:#111827;
    ">

        <h2 style="
            margin-top:0;
            font-size:22px;
        ">
            New Consultation Request
        </h2>

        <table style="
            width:100%;
            border-collapse:collapse;
            margin-top:25px;
        ">

            <tr>
                <td style="
                    padding:12px;
                    background:#f9fafb;
                    font-weight:600;
                    width:140px;
                    border-radius:8px;
                ">
                    Name
                </td>

                <td style="padding:12px;">
                    {{name}}
                </td>
            </tr>

            <tr>
                <td style="
                    padding:12px;
                    background:#f9fafb;
                    font-weight:600;
                    border-radius:8px;
                ">
                    Email
                </td>

                <td style="padding:12px;">
                    {{email}}
                </td>
            </tr>

            <tr>
                <td style="
                    padding:12px;
                    background:#f9fafb;
                    font-weight:600;
                    border-radius:8px;
                ">
                    Company
                </td>

                <td style="padding:12px;">
                    {{company}}
                </td>
            </tr>

            <tr>
                <td style="
                    padding:12px;
                    background:#f9fafb;
                    font-weight:600;
                    border-radius:8px;
                ">
                    Budget
                </td>

                <td style="padding:12px;">
                    {{budget}}
                </td>
            </tr>

        </table>

        <!-- MESSAGE -->

        <div style="
            margin-top:30px;
            background:#f9fafb;
            padding:20px;
            border-radius:14px;
            border:1px solid #e5e7eb;
        ">

            <p style="
                margin-top:0;
                font-size:13px;
                font-weight:700;
                color:#00b8d9;
                letter-spacing:1px;
            ">
                PROJECT DETAILS
            </p>

            <p style="
                margin:0;
                line-height:1.7;
                color:#374151;
                font-size:15px;
            ">
                {{message}}
            </p>

        </div>

    </div>

</div>

</body>
</html>
""";

            adminHtml = adminHtml.replace("{{name}}", safe(request.getName()));
            adminHtml = adminHtml.replace("{{email}}", safe(request.getEmail()));
            adminHtml = adminHtml.replace("{{company}}", safe(request.getCompany()));
            adminHtml = adminHtml.replace("{{budget}}", safe(request.getBudget()));
            adminHtml = adminHtml.replace("{{message}}", safe(request.getMessage()));

            adminHelper.setText(adminHtml, true);

            mailSender.send(adminMail);

            log.info("Admin mail sent successfully");

            /*
             * ======================================================
             * AUTO REPLY TO CLIENT
             * ======================================================
             */

            MimeMessage autoReplyMail = mailSender.createMimeMessage();

            MimeMessageHelper autoReplyHelper =
                    new MimeMessageHelper(autoReplyMail, true, "UTF-8");

            autoReplyHelper.setTo(request.getEmail());

            autoReplyHelper.setSubject("Thank You for Contacting Malkris");

            String autoReplyHtml = """
<html>
<body style="
    margin:0;
    padding:30px;
    background:#f4f7fb;
    font-family:Arial,sans-serif;
">

<div style="
    max-width:560px;
    margin:auto;
    background:#ffffff;
    border-radius:18px;
    overflow:hidden;
    border:1px solid #e5e7eb;
">

    <!-- HEADER -->

    <div style="
        background:#00d9ff;
        padding:20px;
        text-align:center;
    ">

        <h1 style="
            margin:0;
            color:#000;
            font-size:24px;
            font-weight:800;
            letter-spacing:1px;
        ">
            MALKRIS
        </h1>

    </div>

    <!-- BODY -->

    <div style="
        padding:30px;
        color:#111827;
    ">

        <h2 style="
            margin-top:0;
            font-size:24px;
        ">
            Thank You, {{name}} 👋
        </h2>

        <p style="
            line-height:1.8;
            color:#4b5563;
            font-size:15px;
        ">
            We received your consultation request successfully.
        </p>

        <p style="
            line-height:1.8;
            color:#4b5563;
            font-size:15px;
        ">
            Our team will review your requirements and contact you soon.
        </p>

        <!-- INFO CARD -->

        <div style="
            margin-top:25px;
            background:#f9fafb;
            padding:18px;
            border-radius:14px;
            border:1px solid #e5e7eb;
        ">

            <p style="
                margin-top:0;
                margin-bottom:15px;
                color:#00b8d9;
                font-size:12px;
                font-weight:700;
                letter-spacing:1px;
            ">
                SUBMITTED DETAILS
            </p>

            <p style="
                margin:8px 0;
                color:#374151;
                font-size:14px;
            ">
                <strong>Name:</strong> {{name}}
            </p>

            <p style="
                margin:8px 0;
                color:#374151;
                font-size:14px;
            ">
                <strong>Email:</strong> {{email}}
            </p>

        </div>

        <!-- FOOTER -->

        <p style="
            margin-top:30px;
            font-size:13px;
            color:#6b7280;
            line-height:1.7;
        ">
            Malkris Technologies<br>
            Global Delivery • Scalable Systems • Long-Term Partnership
        </p>

    </div>

</div>

</body>
</html>
""";

            autoReplyHtml = autoReplyHtml.replace("{{name}}", safe(request.getName()));
            autoReplyHtml = autoReplyHtml.replace("{{email}}", safe(request.getEmail()));

            autoReplyHelper.setText(autoReplyHtml, true);

            mailSender.send(autoReplyMail);

            log.info("Auto reply mail sent successfully");

        } catch (Exception e) {

            log.error("Failed to send email: {}", e.getMessage());

            throw new RuntimeException("Failed to send email");
        }
    }

    /*
     * NULL SAFETY
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }
}