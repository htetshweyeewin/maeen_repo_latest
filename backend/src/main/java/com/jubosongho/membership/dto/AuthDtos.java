package com.jubosongho.membership.dto;

import com.jubosongho.membership.domain.AdminUser;
import com.jubosongho.membership.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "ইউজারনেম দিন") String username,
            @NotBlank(message = "পাসওয়ার্ড দিন") String password) {
    }

    public record LoginResponse(String token, UserInfo user) {
    }

    public record UserInfo(Long id, String username, String fullName, Role role, String roleLabel,
                           Long orgZilaId, String orgZilaName) {
        public static UserInfo from(AdminUser u) {
            return new UserInfo(u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.getRole().getLabel(),
                    u.getOrgZila() == null ? null : u.getOrgZila().getId(),
                    u.getOrgZila() == null ? null : u.getOrgZila().getName());
        }
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "বর্তমান পাসওয়ার্ড দিন") String currentPassword,
            @NotBlank(message = "নতুন পাসওয়ার্ড দিন")
            @Size(min = 6, message = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে") String newPassword) {
    }
}
