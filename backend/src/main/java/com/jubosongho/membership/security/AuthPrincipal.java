package com.jubosongho.membership.security;

import com.jubosongho.membership.domain.AdminUser;
import com.jubosongho.membership.domain.Role;

/** লগইনকৃত এডমিনের তথ্য - প্রতিটি রিকোয়েস্টে ডাটাবেজ থেকে নতুন করে লোড হয়। */
public record AuthPrincipal(Long id, String username, Role role, Long orgZilaId) {

    public static AuthPrincipal from(AdminUser u) {
        return new AuthPrincipal(u.getId(), u.getUsername(), u.getRole(),
                u.getOrgZila() == null ? null : u.getOrgZila().getId());
    }

    public boolean isSuperAdmin() {
        return role == Role.SUPER_ADMIN;
    }
}
