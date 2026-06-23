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

    // did the endpoint respond?
    LoginCode saved = loginCodeRepository.findByEmail("edwin@example.com").orElseThrow();

    // did the user get a code thats 6 digits and does it have an expiration date
    assertEquals(6, saved.getCode().length());
    assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
}