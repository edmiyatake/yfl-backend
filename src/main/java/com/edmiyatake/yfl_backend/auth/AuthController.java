package com.yfl_backend.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/request-code")
    public ResponseEntity<Void> requestCode(@RequestBody RequestCodeDto request) {
        authService.requestCode(request.getEmail());
        return ResponseEntity.ok().build();
    }
}