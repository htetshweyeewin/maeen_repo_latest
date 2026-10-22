package com.jubosongho.membership.dto;

import com.jubosongho.membership.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record RejectRequest(@Size(max = 500, message = "কারণ অনেক বড়") String reason) {
    }

    public record StatsResponse(long total, long pending, long approved, long rejected) {
    }

    public record GeoOption(Long id, String name) {
    }

    public record UpazilaRef(Long id, String name, String districtName) {
    }

    public record OrgZilaRequest(
            @NotBlank(message = "যেলার নাম দিন") @Size(max = 120) String name,
            @NotNull(message = "উপযেলা নির্বাচন করুন") Set<Long> upazilaIds) {
    }

    public record OrgZilaResponse(Long id, String name, List<UpazilaRef> upazilas, long memberCount) {
    }

    public record AdminUserRequest(
            @NotBlank(message = "ইউজারনেম দিন")
            @Pattern(regexp = "[a-zA-Z0-9._-]{3,60}", message = "ইউজারনেমে শুধু ইংরেজি অক্ষর, সংখ্যা, . _ - (৩-৬০ অক্ষর)")
            String username,
            @NotBlank(message = "নাম দিন") @Size(max = 120) String fullName,
            /* নতুন ইউজারে আবশ্যক; সম্পাদনায় খালি রাখলে পরিবর্তন হবে না */
            String password,
            @NotNull(message = "ভূমিকা নির্বাচন করুন") Role role,
            Long orgZilaId,
            boolean active) {
    }

    public record AdminUserResponse(Long id, String username, String fullName, Role role, String roleLabel,
                                    Long orgZilaId, String orgZilaName, boolean active) {
    }
}
