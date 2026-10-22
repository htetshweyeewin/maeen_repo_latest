package com.jubosongho.membership.domain;

import java.util.EnumSet;
import java.util.Set;

import static com.jubosongho.membership.domain.OrgLevel.*;

/**
 * সাংগঠনিক পদবি। কোন স্তরে কোন পদ আছে তা সদস্য ফরমের ১১ নং টেবিল অনুযায়ী।
 * কেন্দ্রে সব পদ বিদ্যমান।
 */
public enum Designation implements Labeled {
    PRESIDENT("সভাপতি", EnumSet.allOf(OrgLevel.class)),
    VICE_PRESIDENT("সহ-সভাপতি", EnumSet.allOf(OrgLevel.class)),
    GENERAL_SECRETARY("সাধারণ সম্পাদক", EnumSet.allOf(OrgLevel.class)),
    ORGANIZING_SECRETARY("সাংগঠনিক সম্পাদক", EnumSet.allOf(OrgLevel.class)),
    FINANCE_SECRETARY("অর্থ সম্পাদক", EnumSet.allOf(OrgLevel.class)),
    PUBLICITY_SECRETARY("প্রচার সম্পাদক", EnumSet.allOf(OrgLevel.class)),
    TRAINING_SECRETARY("প্রশিক্ষণ সম্পাদক", EnumSet.of(KENDRO, ZILA, UPAZILA, ELAKA)),
    STUDENT_AFFAIRS_SECRETARY("ছাত্র বিষয়ক সম্পাদক", EnumSet.of(KENDRO, ZILA, UPAZILA)),
    MEDIA_SECRETARY("মিডিয়া বিষয়ক সম্পাদক", EnumSet.of(KENDRO)),
    INFO_PUBLICATION_SECRETARY("তথ্য ও প্রকাশনা সম্পাদক", EnumSet.of(KENDRO)),
    LITERATURE_CULTURE_SECRETARY("সাহিত্য ও সংস্কৃতি বিষয়ক সম্পাদক", EnumSet.allOf(OrgLevel.class)),
    SOCIAL_WELFARE_SECRETARY("সমাজ কল্যাণ সম্পাদক", EnumSet.allOf(OrgLevel.class)),
    OFFICE_SECRETARY("দফতর সম্পাদক", EnumSet.allOf(OrgLevel.class));

    private final String label;
    private final Set<OrgLevel> levels;

    Designation(String label, Set<OrgLevel> levels) {
        this.label = label;
        this.levels = levels;
    }

    @Override
    public String getLabel() { return label; }

    public Set<OrgLevel> getLevels() { return levels; }

    public boolean isAvailableAt(OrgLevel level) { return levels.contains(level); }
}
