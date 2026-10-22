package com.jubosongho.membership.service;

import com.jubosongho.membership.domain.Address;
import com.jubosongho.membership.domain.GeoArea;
import com.jubosongho.membership.domain.GeoLevel;
import com.jubosongho.membership.dto.AddressDto;
import com.jubosongho.membership.dto.AdminDtos.GeoOption;
import com.jubosongho.membership.repository.GeoAreaRepository;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

import static com.jubosongho.membership.service.BnText.trimToNull;

/** ঠিকানার ড্রপডাউন ডাটা - মেমোরিতে ক্যাশ করা (প্রায় ৫০০০ রেকর্ড)। */
@Service
public class GeoService {

    private static final long ROOT = 0L;

    private final GeoAreaRepository repo;
    private volatile Map<Long, GeoArea> byId = Map.of();
    private volatile Map<Long, List<GeoArea>> childrenByParent = Map.of();

    public GeoService(GeoAreaRepository repo) {
        this.repo = repo;
    }

    public void reload() {
        Collator bn = Collator.getInstance(Locale.forLanguageTag("bn"));
        List<GeoArea> all = repo.findAll();
        byId = all.stream().collect(Collectors.toUnmodifiableMap(GeoArea::getId, g -> g));
        Map<Long, List<GeoArea>> children = new HashMap<>();
        for (GeoArea g : all) {
            children.computeIfAbsent(g.getParentId() == null ? ROOT : g.getParentId(), k -> new ArrayList<>()).add(g);
        }
        children.values().forEach(list -> list.sort(Comparator.comparing(GeoArea::getName, bn)));
        childrenByParent = children;
    }

    /** parentId null হলে বিভাগসমূহ */
    public List<GeoOption> children(Long parentId) {
        return childrenByParent.getOrDefault(parentId == null ? ROOT : parentId, List.of()).stream()
                .map(g -> new GeoOption(g.getId(), g.getName()))
                .toList();
    }

    public Optional<GeoArea> find(Long id) {
        return Optional.ofNullable(id == null ? null : byId.get(id));
    }

    public boolean isUpazila(Long id) {
        return find(id).map(g -> g.getLevel() == GeoLevel.UPAZILA).orElse(false);
    }

    public String nameOf(Long id) {
        return find(id).map(GeoArea::getName).orElse(null);
    }

    public String parentNameOf(Long id) {
        return find(id).map(GeoArea::getParentId).map(this::nameOf).orElse(null);
    }

    /**
     * ড্রপডাউনের id-গুলোর স্তরবিন্যাস যাচাই করে Address তৈরি করে (নামসহ)।
     * ভুল হলে ফিল্ড-ভিত্তিক ApiException।
     */
    public Address toAddress(AddressDto dto, String prefix) {
        GeoArea division = require(dto.divisionId(), GeoLevel.DIVISION, null, prefix + ".divisionId", "বিভাগ");
        GeoArea district = require(dto.districtId(), GeoLevel.DISTRICT, division, prefix + ".districtId", "যেলা");
        GeoArea upazila = require(dto.upazilaId(), GeoLevel.UPAZILA, district, prefix + ".upazilaId", "উপযেলা");

        GeoArea union = null;
        boolean hasUnions = !childrenByParent.getOrDefault(upazila.getId(), List.of()).isEmpty();
        if (hasUnions || dto.unionId() != null) {
            union = require(dto.unionId(), GeoLevel.UNION, upazila, prefix + ".unionId", "ইউনিয়ন");
        }

        Address a = new Address();
        a.setDivisionId(division.getId());
        a.setDivisionName(division.getName());
        a.setDistrictId(district.getId());
        a.setDistrictName(district.getName());
        a.setUpazilaId(upazila.getId());
        a.setUpazilaName(upazila.getName());
        if (union != null) {
            a.setUnionId(union.getId());
            a.setUnionName(union.getName());
        }
        a.setWard(dto.ward());
        a.setPostOffice(trimToNull(dto.postOffice()));
        a.setVillage(trimToNull(dto.village()));
        return a;
    }

    private GeoArea require(Long id, GeoLevel level, GeoArea parent, String field, String label) {
        GeoArea g = find(id).orElse(null);
        if (g == null || g.getLevel() != level || (parent != null && !parent.getId().equals(g.getParentId()))) {
            throw ApiException.field(field, label + " নির্বাচন করুন");
        }
        return g;
    }
}
