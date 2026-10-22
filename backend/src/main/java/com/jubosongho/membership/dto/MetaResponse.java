package com.jubosongho.membership.dto;

import com.jubosongho.membership.domain.*;

import java.util.Arrays;
import java.util.List;

/** ফরমের সব ড্রপডাউনের অপশন (বাংলা লেবেলসহ)। */
public record MetaResponse(
        List<Option> bloodGroups,
        List<Option> orgLevels,
        List<DesignationOption> designations,
        List<Option> orgRanks,
        List<Option> statuses
) {
    public record Option(String code, String label) {
    }

    public record DesignationOption(String code, String label, List<String> levels) {
    }

    public static MetaResponse build() {
        return new MetaResponse(
                options(BloodGroup.values()),
                options(OrgLevel.values()),
                Arrays.stream(Designation.values())
                        .map(d -> new DesignationOption(d.name(), d.getLabel(),
                                d.getLevels().stream().map(Enum::name).toList()))
                        .toList(),
                options(OrgRank.values()),
                options(MemberStatus.values()));
    }

    private static List<Option> options(Labeled[] values) {
        return Arrays.stream(values).map(v -> new Option(v.name(), v.getLabel())).toList();
    }
}
