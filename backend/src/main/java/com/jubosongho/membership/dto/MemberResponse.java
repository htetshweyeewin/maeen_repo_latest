package com.jubosongho.membership.dto;

import com.jubosongho.membership.domain.*;

import java.time.Instant;
import java.time.LocalDate;

public record MemberResponse(
        Long id,
        String name,
        String fatherName,
        String motherName,
        LocalDate dateOfBirth,
        BloodGroup bloodGroup,
        String bloodGroupLabel,
        String education,
        String email,
        String mobile1,
        boolean mobile1Call,
        boolean mobile1WhatsApp,
        String mobile2,
        boolean mobile2Call,
        boolean mobile2WhatsApp,
        AddressDto permanentAddress,
        boolean presentSameAsPermanent,
        AddressDto presentAddress,
        OrgLevel responsibilityLevel,
        String responsibilityLevelLabel,
        Designation designation,
        String designationLabel,
        OrgRank orgRank,
        String orgRankLabel,
        LocalDate oathDate,
        MemberStatus status,
        String statusLabel,
        String rejectReason,
        Long orgZilaId,
        String orgZilaName,
        boolean orgZilaManual,
        String reviewedBy,
        Instant reviewedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static MemberResponse from(Member m) {
        return new MemberResponse(
                m.getId(), m.getName(), m.getFatherName(), m.getMotherName(), m.getDateOfBirth(),
                m.getBloodGroup(), label(m.getBloodGroup()), m.getEducation(), m.getEmail(),
                m.getMobile1(), m.isMobile1Call(), m.isMobile1WhatsApp(),
                m.getMobile2(), m.isMobile2Call(), m.isMobile2WhatsApp(),
                AddressDto.from(m.getPermanentAddress()), m.isPresentSameAsPermanent(),
                AddressDto.from(m.getPresentAddress()),
                m.getResponsibilityLevel(), label(m.getResponsibilityLevel()),
                m.getDesignation(), label(m.getDesignation()),
                m.getOrgRank(), label(m.getOrgRank()), m.getOathDate(),
                m.getStatus(), label(m.getStatus()), m.getRejectReason(),
                m.getOrgZila() == null ? null : m.getOrgZila().getId(),
                m.getOrgZila() == null ? null : m.getOrgZila().getName(),
                m.isOrgZilaManual(), m.getReviewedBy(), m.getReviewedAt(),
                m.getCreatedAt(), m.getUpdatedAt());
    }

    private static String label(Labeled l) {
        return l == null ? null : l.getLabel();
    }
}
