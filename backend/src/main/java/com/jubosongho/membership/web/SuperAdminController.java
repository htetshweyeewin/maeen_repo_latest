package com.jubosongho.membership.web;

import com.jubosongho.membership.dto.AdminDtos.AdminUserRequest;
import com.jubosongho.membership.dto.AdminDtos.AdminUserResponse;
import com.jubosongho.membership.dto.AdminDtos.OrgZilaRequest;
import com.jubosongho.membership.dto.AdminDtos.OrgZilaResponse;
import com.jubosongho.membership.security.AuthPrincipal;
import com.jubosongho.membership.service.AdminUserService;
import com.jubosongho.membership.service.OrgZilaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** শুধু কেন্দ্রীয় এডমিন (SecurityConfig এ সীমাবদ্ধ): সাংগঠনিক যেলা ও এডমিন ব্যবস্থাপনা */
@RestController
@RequestMapping("/api/admin")
public class SuperAdminController {

    private final OrgZilaService zilas;
    private final AdminUserService users;

    public SuperAdminController(OrgZilaService zilas, AdminUserService users) {
        this.zilas = zilas;
        this.users = users;
    }

    @GetMapping("/zilas")
    public List<OrgZilaResponse> zilas() {
        return zilas.list();
    }

    @PostMapping("/zilas")
    @ResponseStatus(HttpStatus.CREATED)
    public OrgZilaResponse createZila(@Valid @RequestBody OrgZilaRequest req) {
        return zilas.create(req);
    }

    @PutMapping("/zilas/{id}")
    public OrgZilaResponse updateZila(@PathVariable Long id, @Valid @RequestBody OrgZilaRequest req) {
        return zilas.update(id, req);
    }

    @DeleteMapping("/zilas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteZila(@PathVariable Long id) {
        zilas.delete(id);
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return users.list();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse createUser(@Valid @RequestBody AdminUserRequest req) {
        return users.create(req);
    }

    @PutMapping("/users/{id}")
    public AdminUserResponse updateUser(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id,
                                        @Valid @RequestBody AdminUserRequest req) {
        return users.update(id, req, principal);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        users.delete(id, principal);
    }
}
