package com.edmiyatake.yfl_backend.auth;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long CODE_EXPIRY_MINUTES = 10;

    private final LoginCodeRepository loginCodeRepository;

    public AuthService(LoginCodeRepository loginCodeRepository) {
        this.loginCodeRepository = loginCodeRepository;
    }

    public void requestCode(String email) {
        LoginCode loginCode = new LoginCode();
        loginCode.setEmail(email);
        loginCode.setCode(generateCode());
        loginCode.setExpiresAt(Instant.now().plus(CODE_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        loginCode.setAttempts(0);
        loginCode.setCreatedAt(Instant.now());

        loginCodeRepository.save(loginCode);
    }

    public void verifyCode(String email, String code) {
        LoginCode loginCode = loginCodeRepository
                .findFirstByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code"));

        if (loginCode.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code expired");
        }
    }

    private String generateCode() {
        int code = RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }
}