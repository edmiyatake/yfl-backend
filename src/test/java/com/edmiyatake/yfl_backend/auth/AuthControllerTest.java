package com.edmiyatake.yfl_backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LoginCodeRepository loginCodeRepository;

    /*
        Test for Step 1 of Authentication
        --> When you provide an email, does the system create a row that has a login code and expiry date
     */
    @Test
    void requestCode_createsCodeForNewEmail() {
        ResponseEntity<?> response = restTemplate.postForEntity(
                "/api/v1/auth/request-code",
                new RequestCodeDto("edwin@example.com"),
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        LoginCode saved = loginCodeRepository
                .findFirstByEmailOrderByCreatedAtDesc("edwin@example.com")
                .orElseThrow();

        assertEquals(6, saved.getCode().length());
        assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
    }
}