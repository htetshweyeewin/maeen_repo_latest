package com.jubosongho.membership.web;

import com.jubosongho.membership.dto.AuthDtos.ChangePasswordRequest;
import com.jubosongho.membership.dto.AuthDtos.LoginRequest;
import com.jubosongho.membership.dto.AuthDtos.LoginResponse;
import com.jubosongho.membership.dto.AuthDtos.UserInfo;
import com.jubosongho.membership.security.AuthPrincipal;
import com.jubosongho.membership.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminUserService service;

    public AuthController(AdminUserService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @GetMapping("/me")
    public UserInfo me(@AuthenticationPrincipal AuthPrincipal principal) {
        return service.me(principal);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal AuthPrincipal principal,
                               @Valid @RequestBody ChangePasswordRequest req) {
        service.changePassword(principal, req);
    }
}
