package ru.pricat.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pricat.model.Client;
import ru.pricat.model.dto.request.EmailCheckRequestDto;
import ru.pricat.model.dto.request.ProfileCreateRequestDto;
import ru.pricat.model.dto.response.EmailCheckResponseDto;
import ru.pricat.model.dto.response.ProfileCreateResponseDto;
import ru.pricat.service.ClientService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalRestController {

    private final ClientService clientService;

    @PostMapping("/check-email")
    public ResponseEntity<EmailCheckResponseDto> checkEmailUnique(
            @Valid @RequestBody EmailCheckRequestDto request) {
        String email = request.email();
        log.info("Internal check email uniqueness: {}", email);
        boolean isUnique = clientService.isClientEmailUnique(email);
        log.info("Email uniqueness check result for '{}': {}", email, isUnique);
        return ResponseEntity.ok(new EmailCheckResponseDto(isUnique));
    }

    @PostMapping("/profile")
    public ResponseEntity<ProfileCreateResponseDto> createClientProfile(
            @Valid @RequestBody ProfileCreateRequestDto request) {
        String username = request.username();
        log.info("Internal create profile for user: {}", username);
        Client client = clientService.createClientProfile(request);
        log.info("Successfully created profile for: {}", request.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProfileCreateResponseDto("Profile created",
                client.getUsername()));
    }
}
