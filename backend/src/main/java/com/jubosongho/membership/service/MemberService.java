package com.jubosongho.membership.service;

import com.jubosongho.membership.domain.*;
import com.jubosongho.membership.dto.AddressDto;
import com.jubosongho.membership.dto.AdminDtos.StatsResponse;
import com.jubosongho.membership.dto.MemberRequest;
import com.jubosongho.membership.dto.MemberResponse;
import com.jubosongho.membership.dto.PageResponse;
import com.jubosongho.membership.repository.MemberRepository;
import com.jubosongho.membership.security.AuthPrincipal;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.jubosongho.membership.service.BnText.trimToNull;

@Service
public class MemberService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");
    private static final ZoneId DHAKA = ZoneId.of("Asia/Dhaka");

    private final MemberRepository repo;
    private final GeoService geo;
    private final OrgZilaService zilas;
    private final Validator validator;

    public MemberService(MemberRepository repo, GeoService geo, OrgZilaService zilas, Validator validator) {
        this.repo = repo;
        this.geo = geo;
        this.zilas = zilas;
        this.validator = validator;
    }

    public record MemberFilter(String q, MemberStatus status, Long orgZilaId, OrgRank orgRank, OrgLevel level) {
    }

    // ---------- পাবলিক ফরম ----------

    @Transactional
    public MemberResponse submit(MemberRequest req) {
        Member m = new Member();
        apply(m, req);
        ensureMobileUnique(m.getMobile1(), null);
        m.setStatus(MemberStatus.PENDING);
        m.setOrgZila(zilas.findForUpazila(m.getPresentAddress().getUpazilaId()).orElse(null));
        return MemberResponse.from(repo.save(m));
    }

    // ---------- এডমিন ----------

    @Transactional(readOnly = true)
    public PageResponse<MemberResponse> list(MemberFilter filter, AuthPrincipal user, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), NEWEST_FIRST);
        return PageResponse.of(repo.findAll(spec(filter, user), pageable), MemberResponse::from);
    }

    @Transactional(readOnly = true)
    public StatsResponse stats(MemberFilter filter, AuthPrincipal user) {
        MemberFilter scope = new MemberFilter(filter.q(), null, filter.orgZilaId(), filter.orgRank(), filter.level());
        return new StatsResponse(
                repo.count(spec(scope, user)),
                repo.count(spec(withStatus(scope, MemberStatus.PENDING), user)),
                repo.count(spec(withStatus(scope, MemberStatus.APPROVED), user)),
                repo.count(spec(withStatus(scope, MemberStatus.REJECTED), user)));
    }

    @Transactional(readOnly = true)
    public MemberResponse get(Long id, AuthPrincipal user) {
        return MemberResponse.from(load(id, user));
    }

    @Transactional
    public MemberResponse update(Long id, MemberRequest req, AuthPrincipal user) {
        Member m = load(id, user);
        apply(m, req);
        ensureMobileUnique(m.getMobile1(), m.getId());

        if (user.isSuperAdmin() && req.orgZilaId() != null) {
            m.setOrgZilaManual(true);
            m.setOrgZila(req.orgZilaId() == 0 ? null : zilas.get(req.orgZilaId()));
        } else {
            if (user.isSuperAdmin()) m.setOrgZilaManual(false);
            if (!m.isOrgZilaManual()) {
                m.setOrgZila(zilas.findForUpazila(m.getPresentAddress().getUpazilaId()).orElse(null));
            }
        }
        return MemberResponse.from(repo.save(m));
    }

    @Transactional
    public MemberResponse approve(Long id, AuthPrincipal user) {
        Member m = load(id, user);
        m.setStatus(MemberStatus.APPROVED);
        m.setRejectReason(null);
        markReviewed(m, user);
        return MemberResponse.from(m);
    }

    @Transactional
    public MemberResponse reject(Long id, String reason, AuthPrincipal user) {
        Member m = load(id, user);
        m.setStatus(MemberStatus.REJECTED);
        m.setRejectReason(trimToNull(reason));
        markReviewed(m, user);
        return MemberResponse.from(m);
    }

    @Transactional
    public void delete(Long id, AuthPrincipal user) {
        if (!user.isSuperAdmin()) throw ApiException.forbidden();
        repo.delete(load(id, user));
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv(MemberFilter filter, AuthPrincipal user) {
        DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(DHAKA);
        StringBuilder sb = new StringBuilder("﻿"); // BOM - Excel এ বাংলা সঠিকভাবে দেখাতে
        row(sb, "ক্রমিক", "নাম", "পিতার নাম", "মাতার নাম", "জন্ম তারিখ", "রক্তের গ্রুপ", "শিক্ষাগত যোগ্যতা",
                "ই-মেইল", "মোবাইল ১", "মোবাইল ২", "স্থায়ী ঠিকানা", "বর্তমান ঠিকানা", "সাংগঠনিক দায়িত্ব",
                "সাংগঠনিক মান", "শপথ গ্রহণের তারিখ", "সাংগঠনিক যেলা", "অবস্থা", "নিবন্ধনের সময়");
        int i = 1;
        for (Member m : repo.findAll(spec(filter, user), NEWEST_FIRST)) {
            row(sb, String.valueOf(i++), m.getName(), m.getFatherName(), m.getMotherName(),
                    str(m.getDateOfBirth()), label(m.getBloodGroup()), m.getEducation(), m.getEmail(),
                    mobileText(m.getMobile1(), m.isMobile1Call(), m.isMobile1WhatsApp()),
                    mobileText(m.getMobile2(), m.isMobile2Call(), m.isMobile2WhatsApp()),
                    fullAddress(m.getPermanentAddress()), fullAddress(m.getPresentAddress()),
                    m.getResponsibilityLevel() == null ? "" :
                            m.getResponsibilityLevel().getLabel() + " - " + label(m.getDesignation()),
                    label(m.getOrgRank()), str(m.getOathDate()),
                    m.getOrgZila() == null ? "" : m.getOrgZila().getName(),
                    label(m.getStatus()), dateTime.format(m.getCreatedAt()));
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ---------- সহায়ক ----------

    private Member load(Long id, AuthPrincipal user) {
        Member m = repo.findById(id).orElseThrow(() -> ApiException.notFound("সদস্য"));
        if (!user.isSuperAdmin()) {
            Long zilaId = m.getOrgZila() == null ? null : m.getOrgZila().getId();
            if (zilaId == null || !zilaId.equals(user.orgZilaId())) throw ApiException.forbidden();
        }
        return m;
    }

    private void markReviewed(Member m, AuthPrincipal user) {
        m.setReviewedBy(user.username());
        m.setReviewedAt(Instant.now());
    }

    private void ensureMobileUnique(String mobile, Long selfId) {
        boolean exists = selfId == null
                ? repo.existsByMobile1AndStatusNot(mobile, MemberStatus.REJECTED)
                : repo.existsByMobile1AndStatusNotAndIdNot(mobile, MemberStatus.REJECTED, selfId);
        if (exists) {
            throw new ApiException(HttpStatus.CONFLICT, "এই মোবাইল নম্বর দিয়ে ইতিমধ্যে নিবন্ধন করা হয়েছে",
                    Map.of("mobile1", "এই মোবাইল নম্বর দিয়ে ইতিমধ্যে নিবন্ধন করা হয়েছে"));
        }
    }

    /** রিকোয়েস্টের তথ্য যাচাই করে Member এ বসানো (নতুন ও সম্পাদনা উভয় ক্ষেত্রে) */
    private void apply(Member m, MemberRequest req) {
        m.setName(req.name().trim());
        m.setFatherName(req.fatherName().trim());
        m.setMotherName(req.motherName().trim());
        m.setDateOfBirth(req.dateOfBirth());
        m.setBloodGroup(req.bloodGroup());
        m.setEducation(trimToNull(req.education()));
        String email = trimToNull(req.email());
        m.setEmail(email == null ? null : email.toLowerCase(Locale.ROOT));

        String mobile1 = mobile(req.mobile1(), "mobile1");
        String mobile2 = mobile(req.mobile2(), "mobile2");
        if (mobile2 != null && mobile2.equals(mobile1)) {
            throw ApiException.field("mobile2", "দ্বিতীয় মোবাইল নম্বরটি প্রথমটির থেকে ভিন্ন হতে হবে");
        }
        m.setMobile1(mobile1);
        m.setMobile1Call(req.mobile1Call());
        m.setMobile1WhatsApp(req.mobile1WhatsApp());
        m.setMobile2(mobile2);
        m.setMobile2Call(mobile2 != null && req.mobile2Call());
        m.setMobile2WhatsApp(mobile2 != null && req.mobile2WhatsApp());

        Address permanent = geo.toAddress(req.permanentAddress(), "permanentAddress");
        m.setPermanentAddress(permanent);
        m.setPresentSameAsPermanent(req.presentSameAsPermanent());
        if (req.presentSameAsPermanent()) {
            m.setPresentAddress(permanent.copy());
        } else {
            validatePresentAddress(req.presentAddress());
            m.setPresentAddress(geo.toAddress(req.presentAddress(), "presentAddress"));
        }

        if (req.responsibilityLevel() == null) {
            m.setResponsibilityLevel(null);
            m.setDesignation(null);
        } else {
            if (req.designation() == null) throw ApiException.field("designation", "পদবি নির্বাচন করুন");
            if (!req.designation().isAvailableAt(req.responsibilityLevel())) {
                throw ApiException.field("designation", req.responsibilityLevel().getLabel()
                        + " পর্যায়ে '" + req.designation().getLabel() + "' পদ নেই");
            }
            m.setResponsibilityLevel(req.responsibilityLevel());
            m.setDesignation(req.designation());
        }

        m.setOrgRank(req.orgRank());
        m.setOathDate(req.oathDate());
    }

    private void validatePresentAddress(AddressDto dto) {
        if (dto == null) throw ApiException.field("presentAddress", "বর্তমান ঠিকানা দিন");
        Set<ConstraintViolation<AddressDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new LinkedHashMap<>();
            violations.forEach(v -> errors.putIfAbsent("presentAddress." + v.getPropertyPath(), v.getMessage()));
            throw new ApiException(HttpStatus.BAD_REQUEST, "ফরমে কিছু তথ্য সঠিক নয়", errors);
        }
    }

    private static String mobile(String raw, String field) {
        try {
            return BnText.normalizeMobile(raw);
        } catch (IllegalArgumentException e) {
            throw ApiException.field(field, e.getMessage());
        }
    }

    private static MemberFilter withStatus(MemberFilter f, MemberStatus status) {
        return new MemberFilter(f.q(), status, f.orgZilaId(), f.orgRank(), f.level());
    }

    /** যেলা এডমিন শুধু নিজের যেলার সদস্য দেখবে; কেন্দ্রীয় এডমিন সব (orgZilaId=0 মানে যেলাবিহীন) */
    private static Specification<Member> spec(MemberFilter f, AuthPrincipal user) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (!user.isSuperAdmin()) {
                if (user.orgZilaId() == null) return cb.disjunction();
                p.add(cb.equal(root.get("orgZila").get("id"), user.orgZilaId()));
            } else if (f.orgZilaId() != null) {
                p.add(f.orgZilaId() == 0
                        ? cb.isNull(root.get("orgZila"))
                        : cb.equal(root.get("orgZila").get("id"), f.orgZilaId()));
            }
            if (f.status() != null) p.add(cb.equal(root.get("status"), f.status()));
            if (f.orgRank() != null) p.add(cb.equal(root.get("orgRank"), f.orgRank()));
            if (f.level() != null) p.add(cb.equal(root.get("responsibilityLevel"), f.level()));

            String q = trimToNull(f.q());
            if (q != null) {
                String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
                String digits = BnText.toAsciiDigits(q).replaceAll("[^0-9]", "");
                var pres = root.get("presentAddress");
                var perm = root.get("permanentAddress");
                List<Predicate> any = new ArrayList<>(List.of(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("fatherName")), like),
                        cb.like(cb.lower(root.get("motherName")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(pres.get("upazilaName"), like),
                        cb.like(pres.get("unionName"), like),
                        cb.like(pres.get("village"), like),
                        cb.like(perm.get("upazilaName"), like),
                        cb.like(perm.get("unionName"), like),
                        cb.like(perm.get("village"), like)));
                if (digits.length() >= 3) {
                    any.add(cb.like(root.get("mobile1"), "%" + digits + "%"));
                    any.add(cb.like(root.get("mobile2"), "%" + digits + "%"));
                }
                p.add(cb.or(any.toArray(Predicate[]::new)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    private static String fullAddress(Address a) {
        if (a == null) return "";
        List<String> parts = new ArrayList<>();
        add(parts, a.getVillage());
        if (a.getWard() != null) parts.add(a.getWard() + " নং ওয়ার্ড");
        add(parts, a.getUnionName());
        if (a.getPostOffice() != null) parts.add("পোস্ট: " + a.getPostOffice());
        add(parts, a.getUpazilaName());
        add(parts, a.getDistrictName());
        add(parts, a.getDivisionName());
        return String.join(", ", parts);
    }

    private static void add(List<String> parts, String s) {
        if (s != null && !s.isBlank()) parts.add(s);
    }

    private static String mobileText(String number, boolean call, boolean whatsApp) {
        if (number == null) return "";
        List<String> tags = new ArrayList<>();
        if (call) tags.add("কল");
        if (whatsApp) tags.add("WhatsApp");
        return tags.isEmpty() ? number : number + " (" + String.join(", ", tags) + ")";
    }

    private static String label(Labeled l) {
        return l == null ? "" : l.getLabel();
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private static void row(StringBuilder sb, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(',');
            String c = cells[i] == null ? "" : cells[i];
            if (!c.isEmpty() && "=+-@".indexOf(c.charAt(0)) >= 0) c = "'" + c; // স্প্রেডশিট ফর্মুলা ইনজেকশন রোধ
            sb.append('"').append(c.replace("\"", "\"\"")).append('"');
        }
        sb.append("\r\n");
    }
}
