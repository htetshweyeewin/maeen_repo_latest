package com.jubosongho.membership.service;

import com.jubosongho.membership.domain.AdminUser;
import com.jubosongho.membership.domain.Role;
import com.jubosongho.membership.dto.AdminDtos.AdminUserRequest;
import com.jubosongho.membership.dto.AdminDtos.AdminUserResponse;
import com.jubosongho.membership.dto.AuthDtos.ChangePasswordRequest;
import com.jubosongho.membership.dto.AuthDtos.LoginRequest;
import com.jubosongho.membership.dto.AuthDtos.LoginResponse;
import com.jubosongho.membership.dto.AuthDtos.UserInfo;
import com.jubosongho.membership.repository.AdminUserRepository;
import com.jubosongho.membership.security.AuthPrincipal;
import com.jubosongho.membership.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    private final AdminUserRepository repo;
    private final OrgZilaService zilas;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AdminUserService(AdminUserRepository repo, OrgZilaService zilas, PasswordEncoder encoder, JwtService jwt) {
        this.repo = repo;
        this.zilas = zilas;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    // ---------- লগইন ----------

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        AdminUser u = repo.findByUsername(req.username().trim())
                .filter(AdminUser::isActive)
                .filter(x -> encoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "ইউজারনেম বা পাসওয়ার্ড সঠিক নয়"));
        return new LoginResponse(jwt.generate(u), UserInfo.from(u));
    }

    @Transactional(readOnly = true)
    public UserInfo me(AuthPrincipal principal) {
        return UserInfo.from(load(principal.id()));
    }

    @Transactional
    public void changePassword(AuthPrincipal principal, ChangePasswordRequest req) {
        AdminUser u = load(principal.id());
        if (!encoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw ApiException.field("currentPassword", "বর্তমান পাসওয়ার্ড সঠিক নয়");
        }
        u.setPasswordHash(encoder.encode(req.newPassword()));
    }

    // ---------- এডমিন ব্যবস্থাপনা (কেন্দ্রীয় এডমিন) ----------

    @Transactional(readOnly = true)
    public List<AdminUserResponse> list() {
        return repo.findAllByOrderByRoleAscUsernameAsc().stream().map(AdminUserService::toResponse).toList();
    }

    @Transactional
    public AdminUserResponse create(AdminUserRequest req) {
        if (repo.existsByUsername(req.username().trim())) {
            throw ApiException.field("username", "এই ইউজারনেম ইতিমধ্যে ব্যবহৃত");
        }
        if (req.password() == null || req.password().length() < 6) {
            throw ApiException.field("password", "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে");
        }
        AdminUser u = new AdminUser();
        u.setUsername(req.username().trim());
        apply(u, req);
        return toResponse(repo.save(u));
    }

    @Transactional
    public AdminUserResponse update(Long id, AdminUserRequest req, AuthPrincipal principal) {
        AdminUser u = load(id);
        if (!u.getUsername().equals(req.username().trim())) {
            if (repo.existsByUsername(req.username().trim())) {
                throw ApiException.field("username", "এই ইউজারনেম ইতিমধ্যে ব্যবহৃত");
            }
            u.setUsername(req.username().trim());
        }
        if (u.getId().equals(principal.id()) && (!req.active() || req.role() != Role.SUPER_ADMIN)) {
            throw ApiException.badRequest("নিজের অ্যাকাউন্ট নিষ্ক্রিয় বা ভূমিকা পরিবর্তন করা যাবে না");
        }
        if (req.password() != null && !req.password().isBlank() && req.password().length() < 6) {
            throw ApiException.field("password", "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে");
        }
        apply(u, req);
        return toResponse(u);
    }

    @Transactional
    public void delete(Long id, AuthPrincipal principal) {
        if (id.equals(principal.id())) throw ApiException.badRequest("নিজের অ্যাকাউন্ট মুছে ফেলা যাবে না");
        repo.delete(load(id));
    }

    private void apply(AdminUser u, AdminUserRequest req) {
        u.setFullName(req.fullName().trim());
        u.setRole(req.role());
        u.setActive(req.active());
        if (req.role() == Role.ZILA_ADMIN) {
            if (req.orgZilaId() == null) throw ApiException.field("orgZilaId", "যেলা নির্বাচন করুন");
            u.setOrgZila(zilas.get(req.orgZilaId()));
        } else {
            u.setOrgZila(null);
        }
        if (req.password() != null && !req.password().isBlank()) {
            u.setPasswordHash(encoder.encode(req.password()));
        }
    }

    private AdminUser load(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("এডমিন"));
    }

    private static AdminUserResponse toResponse(AdminUser u) {
        return new AdminUserResponse(u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.getRole().getLabel(),
                u.getOrgZila() == null ? null : u.getOrgZila().getId(),
                u.getOrgZila() == null ? null : u.getOrgZila().getName(),
                u.isActive());
    }
}
