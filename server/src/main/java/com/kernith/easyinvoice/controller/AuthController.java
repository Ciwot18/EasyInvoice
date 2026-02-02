package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.config.AuthPrincipal;
import com.kernith.easyinvoice.data.dto.auth.LoginRequest;
import com.kernith.easyinvoice.data.dto.auth.LoginResponse;
import com.kernith.easyinvoice.data.dto.user.ProfileResponse;
import com.kernith.easyinvoice.data.model.User;
import com.kernith.easyinvoice.helper.CurrentUser;
import com.kernith.easyinvoice.service.AuthService;
import com.kernith.easyinvoice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> me(@CurrentUser AuthPrincipal principal) {
        Optional<User> optionalUser = userService.getCurrentUser(principal);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            return ResponseEntity.ok(ProfileResponse.from(optionalUser.get()));
        }
    }
}