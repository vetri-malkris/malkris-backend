
package com.malkris.controller;

import com.malkris.dto.ApiResponse;
import com.malkris.dto.ContactRequest;
import com.malkris.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> submit(
            @Valid @RequestBody ContactRequest request
    ) {

        contactService.submit(request);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Message sent successfully")
                        .build()
        );
    }
}
