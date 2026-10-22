package com.jubosongho.membership.service;

import com.jubosongho.membership.domain.OrgZila;
import com.jubosongho.membership.dto.AdminDtos.OrgZilaRequest;
import com.jubosongho.membership.dto.AdminDtos.OrgZilaResponse;
import com.jubosongho.membership.dto.AdminDtos.UpazilaRef;
import com.jubosongho.membership.repository.AdminUserRepository;
import com.jubosongho.membership.repository.MemberRepository;
import com.jubosongho.membership.repository.OrgZilaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OrgZilaService {

    private final OrgZilaRepository repo;
    private final MemberRepository members;
    private final AdminUserRepository users;
    private final GeoService geo;

    public OrgZilaService(OrgZilaRepository repo, MemberRepository members, AdminUserRepository users, GeoService geo) {
        this.repo = repo;
        this.members = members;
        this.users = users;
        this.geo = geo;
    }

    @Transactional(readOnly = true)
    public List<OrgZilaResponse> list() {
        return repo.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    /** উপযেলা দিয়ে সংশ্লিষ্ট সাংগঠনিক যেলা খোঁজা (যেলার সংখ্যা কম, তাই সরাসরি লুপ) */
    @Transactional(readOnly = true)
    public Optional<OrgZila> findForUpazila(Long upazilaId) {
        if (upazilaId == null) return Optional.empty();
        return repo.findAll().stream().filter(z -> z.getUpazilaIds().contains(upazilaId)).findFirst();
    }

    public OrgZila get(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("সাংগঠনিক যেলা"));
    }

    @Transactional
    public OrgZilaResponse create(OrgZilaRequest req) {
        return save(new OrgZila(), req);
    }

    @Transactional
    public OrgZilaResponse update(Long id, OrgZilaRequest req) {
        return save(get(id), req);
    }

    private OrgZilaResponse save(OrgZila zila, OrgZilaRequest req) {
        String name = req.name().trim();
        if (repo.existsByNameAndIdNot(name, zila.getId() == null ? -1L : zila.getId())) {
            throw ApiException.field("name", "এই নামে যেলা ইতিমধ্যে আছে");
        }
        Set<Long> upazilaIds = new LinkedHashSet<>(req.upazilaIds());
        for (Long uid : upazilaIds) {
            if (!geo.isUpazila(uid)) throw ApiException.field("upazilaIds", "অবৈধ উপযেলা নির্বাচন করা হয়েছে");
        }
        for (OrgZila other : repo.findAll()) {
            if (other.getId().equals(zila.getId())) continue;
            for (Long uid : upazilaIds) {
                if (other.getUpazilaIds().contains(uid)) {
                    throw ApiException.field("upazilaIds",
                            geo.nameOf(uid) + " উপযেলা ইতিমধ্যে '" + other.getName() + "' যেলার অধীনে আছে");
                }
            }
        }
        zila.setName(name);
        zila.getUpazilaIds().clear();
        zila.getUpazilaIds().addAll(upazilaIds);
        OrgZila saved = repo.saveAndFlush(zila);

        // উপযেলা পরিবর্তনের পর স্বয়ংক্রিয়ভাবে যুক্ত সদস্যদের পুনঃবিন্যাস
        members.detachAutoAssigned(saved);
        if (!upazilaIds.isEmpty()) members.attachByUpazila(saved, upazilaIds);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        OrgZila zila = get(id);
        if (users.existsByOrgZila(zila)) {
            throw new ApiException(HttpStatus.CONFLICT, "এই যেলার এডমিন আছে - আগে এডমিন মুছুন বা অন্য যেলায় সরান");
        }
        members.detachAll(zila);
        repo.delete(zila);
    }

    private OrgZilaResponse toResponse(OrgZila z) {
        List<UpazilaRef> upazilas = z.getUpazilaIds().stream()
                .map(id -> new UpazilaRef(id, geo.nameOf(id), geo.parentNameOf(id)))
                .toList();
        return new OrgZilaResponse(z.getId(), z.getName(), upazilas, members.countByOrgZila(z));
    }
}
