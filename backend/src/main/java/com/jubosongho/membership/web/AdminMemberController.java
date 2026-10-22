package com.jubosongho.membership.web;

import com.jubosongho.membership.domain.MemberStatus;
import com.jubosongho.membership.domain.OrgLevel;
import com.jubosongho.membership.domain.OrgRank;
import com.jubosongho.membership.dto.AdminDtos.RejectRequest;
import com.jubosongho.membership.dto.AdminDtos.StatsResponse;
import com.jubosongho.membership.dto.MemberRequest;
import com.jubosongho.membership.dto.MemberResponse;
import com.jubosongho.membership.dto.PageResponse;
import com.jubosongho.membership.security.AuthPrincipal;
import com.jubosongho.membership.service.MemberService;
import com.jubosongho.membership.service.MemberService.MemberFilter;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/** যেলা এডমিন (নিজ যেলা) ও কেন্দ্রীয় এডমিন (সব) - সদস্য তালিকা ব্যবস্থাপনা */
@RestController
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final MemberService service;

    public AdminMemberController(MemberService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<MemberResponse> list(@AuthenticationPrincipal AuthPrincipal user,
                                             @RequestParam(required = false) String q,
                                             @RequestParam(required = false) MemberStatus status,
                                             @RequestParam(required = false) Long orgZilaId,
                                             @RequestParam(required = false) OrgRank orgRank,
                                             @RequestParam(required = false) OrgLevel level,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "25") int size) {
        return service.list(new MemberFilter(q, status, orgZilaId, orgRank, level), user, page, size);
    }

    @GetMapping("/stats")
    public StatsResponse stats(@AuthenticationPrincipal AuthPrincipal user,
                               @RequestParam(required = false) String q,
                               @RequestParam(required = false) Long orgZilaId,
                               @RequestParam(required = false) OrgRank orgRank,
                               @RequestParam(required = false) OrgLevel level) {
        return service.stats(new MemberFilter(q, null, orgZilaId, orgRank, level), user);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal AuthPrincipal user,
                                         @RequestParam(required = false) String q,
                                         @RequestParam(required = false) MemberStatus status,
                                         @RequestParam(required = false) Long orgZilaId,
                                         @RequestParam(required = false) OrgRank orgRank,
                                         @RequestParam(required = false) OrgLevel level) {
        byte[] csv = service.exportCsv(new MemberFilter(q, status, orgZilaId, orgRank, level), user);
        String filename = "member-list-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    @GetMapping("/{id}")
    public MemberResponse get(@AuthenticationPrincipal AuthPrincipal user, @PathVariable Long id) {
        return service.get(id, user);
    }

    @PutMapping("/{id}")
    public MemberResponse update(@AuthenticationPrincipal AuthPrincipal user, @PathVariable Long id,
                                 @Valid @RequestBody MemberRequest req) {
        return service.update(id, req, user);
    }

    @PostMapping("/{id}/approve")
    public MemberResponse approve(@AuthenticationPrincipal AuthPrincipal user, @PathVariable Long id) {
        return service.approve(id, user);
    }

    @PostMapping("/{id}/reject")
    public MemberResponse reject(@AuthenticationPrincipal AuthPrincipal user, @PathVariable Long id,
                                 @Valid @RequestBody(required = false) RejectRequest req) {
        return service.reject(id, req == null ? null : req.reason(), user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthPrincipal user, @PathVariable Long id) {
        service.delete(id, user);
    }
}
