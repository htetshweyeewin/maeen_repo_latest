package com.jubosongho.membership.web;

import com.jubosongho.membership.dto.AdminDtos.GeoOption;
import com.jubosongho.membership.dto.MemberRequest;
import com.jubosongho.membership.dto.MetaResponse;
import com.jubosongho.membership.service.GeoService;
import com.jubosongho.membership.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** পাবলিক লিংক - লগইন ছাড়াই সদস্য ফরম পূরণ */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private static final MetaResponse META = MetaResponse.build();

    private final GeoService geo;
    private final MemberService members;

    public PublicController(GeoService geo, MemberService members) {
        this.geo = geo;
        this.members = members;
    }

    @GetMapping("/meta")
    public MetaResponse meta() {
        return META;
    }

    /** parentId না দিলে বিভাগ; বিভাগের id দিলে যেলা; যেলার id দিলে উপযেলা; উপযেলার id দিলে ইউনিয়ন */
    @GetMapping("/geo")
    public List<GeoOption> geo(@RequestParam(required = false) Long parentId) {
        return geo.children(parentId);
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> submit(@Valid @RequestBody MemberRequest req) {
        // পাবলিক ফরম থেকে যেলা নির্ধারণ গ্রহণযোগ্য নয়
        MemberRequest safe = new MemberRequest(req.name(), req.fatherName(), req.motherName(), req.dateOfBirth(),
                req.bloodGroup(), req.education(), req.email(), req.mobile1(), req.mobile1Call(),
                req.mobile1WhatsApp(), req.mobile2(), req.mobile2Call(), req.mobile2WhatsApp(),
                req.permanentAddress(), req.presentSameAsPermanent(), req.presentAddress(),
                req.responsibilityLevel(), req.designation(), req.orgRank(), req.oathDate(), null);
        var saved = members.submit(safe);
        return Map.of("id", saved.id(),
                "message", "আপনার তথ্য সফলভাবে জমা হয়েছে। যেলা দায়িত্বশীল যাচাই করে অনুমোদন করবেন।");
    }
}
